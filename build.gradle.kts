import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.jetbrainsCompose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.openjfx.javafxplugin")
}

group = "zicheng"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    jetbrainsCompose()
}
dependencies {
    implementation(project(":library"))
}


kotlin {
    // See https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    // and https://docs.gradle.org/current/userguide/toolchains.html
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("17"))
        // vendor.set(JvmVendorSpec.ORACLE)
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = properties["gradle.version"] as String
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "compose-video-player"
            packageVersion = "1.0.0"
            buildTypes.release.proguard {
                configurationFiles.from("rules.pro")
            }
        }
    }
}
