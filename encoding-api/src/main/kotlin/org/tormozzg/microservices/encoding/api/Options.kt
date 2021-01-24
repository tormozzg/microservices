package org.tormozzg.microservices.encoding.api

class Options {
  var level: String? = "3.0"
    private set

  fun withLevel(level: String?): Options = this.also {
    it.level = level
  }

  var crf: Short = 23
    private set

  fun withCrf(crf: Short): Options = this.also {
    it.crf = crf
  }

  var fpsLimitation: Short? = null
    private set

  fun withFpsLimitation(fpsLimitation: Short?): Options = this.also {
    it.fpsLimitation = fpsLimitation
  }

  var copyAudio: Boolean = true
    private set

  fun withCopyAudio(copyAudio: Boolean): Options = this.also {
    it.copyAudio = copyAudio
  }

  var profile: Profile = Profile.BASELINE
    private set

  fun withProfile(profile: Profile): Options = this.also {
    it.profile = profile
  }

  var preset: Preset = Preset.MEDIUM
    private set

  fun withPreset(preset: Preset): Options = this.also {
    it.preset = preset
  }

}

enum class Profile(val ffmpeg: String) {
  BASELINE("baseline"),
  MAIN("main"),
  HIGH("high"),
  HIGH10("high10");
}

enum class Preset(val ffmpeg: String) {
  ULTRAFAST("ultrafast"),
  SUPERFAST("superfast"),
  VERYFAST("veryfast"),
  FASTER("faster"),
  FAST("fast"),
  MEDIUM("medium"),
  SLOW("slow")
}