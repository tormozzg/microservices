plugins {
  id("org.springframework.boot") version "2.3.6.RELEASE" apply false
  id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
  kotlin("jvm") version "1.3.72" apply false
  kotlin("plugin.spring") version "1.3.72" apply false
}

group = "org.tormozzg"
version = "1.0.0"

subprojects {
  extra["springCloudVersion"] = "Hoxton.SR9"

  repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
  }
}
