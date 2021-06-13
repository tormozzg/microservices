package org.tormozzg.microservices.encoding.jobs

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.probe.FFmpegStream
import net.bramp.ffmpeg.progress.Progress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.tormozzg.microservices.encoding.EncodingConfiguration
import org.tormozzg.microservices.encoding.api.Options
import org.tormozzg.microservices.encoding.api.Resolution
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.logTags
import java.io.File
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class EncodeJob(
    private val config: EncodingConfiguration
) : AbstractJob() {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    private val ffmpeg = FFmpeg(config.ffmpeg)
    private val ffprobe = FFprobe(config.ffprobe)
    private val completeFileDirectory: File = File(config.completeDirectory)

    @PostConstruct
    fun init() {
        if (completeFileDirectory.exists()) {
            if (completeFileDirectory.isFile)
                throw IllegalStateException("Invalid path to complete directory. It is a file")

            if (!completeFileDirectory.canWrite())
                throw IllegalStateException("Directory of compete file is not writable")
        } else {
            if (!completeFileDirectory.mkdirs())
                throw IllegalStateException("Could not create directory of complete files")
        }
    }

    override fun doJob(artifacts: Artifacts): File {
        val task = artifacts.task
        return logTags(
            "TaskId" to task.id,
            "Job" to name
        ) {
            val downloadedFile =
                artifacts.resultFile ?: throw IllegalArgumentException("Downloaded file should not be null")
            if (!downloadedFile.exists()) throw IllegalStateException("Downloaded file ${downloadedFile.absolutePath} is not exists")
            log.debug("Loading video info of file {}", downloadedFile.absolutePath)
            val videoInfo = getVideoInfo(downloadedFile)
                ?: throw IllegalStateException("Could not get video info of file ${downloadedFile.absolutePath}")
            val converted = convertFile(
                downloadedFile,
                getFileName(task),
                videoInfo,
                task.resolution,
                task.options!!
            )
            downloadedFile.delete()
            log.debug("Conventing file has been successfully complete.")
            converted
        }
    }

    private fun getFileName(task: Task): String = "${task.id}.mp4"

    private fun getVideoInfo(src: File): VideoInfo? =
        ffprobe.probe(src.absolutePath)
            .streams
            .find { it.codec_type == FFmpegStream.CodecType.VIDEO }
            ?.let {
                val fps = (it.r_frame_rate.numerator / it.avg_frame_rate.denominator)
                VideoInfo(Resolution(it.width, it.height), fps)
            }

    private fun convertFile(
        src: File,
        fileName: String,
        videoInfo: VideoInfo,
        targetResolution: Resolution,
        options: Options
    ): File {
        val ffmpegProbeResult = ffprobe.probe(src.absolutePath)

        var newWidth = targetResolution.width
        if (newWidth % 2 != 0)
            newWidth += 1

        var newHeight = targetResolution.height
        if (newHeight % 2 != 0)
            newHeight += 1

        val originalResolution = videoInfo.resolution

        val calculatedResolution: Resolution = when {
            originalResolution.width == newWidth && originalResolution.height == newHeight -> originalResolution
            else -> {
                val wR = originalResolution.width.toFloat() / newWidth.toFloat()
                val hR = originalResolution.height.toFloat() / newHeight.toFloat()
                if (wR > hR) {
                    Resolution(newWidth, -2)
                } else {
                    Resolution(-2, newHeight)
                }
            }
        }

        val scaleParametr = "scale=w=${calculatedResolution.width}:h=${calculatedResolution.height}"

        val fps = options.fpsLimitation?.let {
            if (videoInfo.fps > it) it.toInt() else videoInfo.fps
        } ?: videoInfo.fps

        val args = mutableListOf(
            "-preset", options.preset.ffmpeg, "-crf", "${options.crf}", "-profile:v", options.profile.ffmpeg
        )

        options.level?.also {
            args.add("-level")
            args.add(it)
        }

        val dst = File(completeFileDirectory, fileName)

        val builder = FFmpegBuilder()
            .setInput(ffmpegProbeResult)     // Filename, or a FFmpegProbeResult
            .overrideOutputFiles(true) // Override the output if it exists
            .addOutput(dst.absolutePath)
            .disableSubtitle()
            .setVideoCodec("libx264")
            .setVideoPixelFormat("yuv420p")
            .addExtraArgs(*args.toTypedArray())
            .setVideoFrameRate(fps, 1)
            .setFormat("mp4")
            .addExtraArgs("-vf", scaleParametr)

        if (options.copyAudio) {
            builder.setAudioCodec("copy")
        } else {
            builder.disableAudio()
        }

        val job = FFmpegExecutor(ffmpeg, ffprobe)
            .createJob(builder.done()) { progress: Progress ->
                val duration = ffmpegProbeResult.getFormat().duration * TimeUnit.SECONDS.toNanos(1)
                val percentage = progress.out_time_ns / duration
                log.trace("Convert progress {}", percentage)
            }
        job.run()
        return dst

    }

    override val name: String = "Encode"
}

data class VideoInfo(val resolution: Resolution, val fps: Int)