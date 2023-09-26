import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.openjfx.javafxplugin")
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val javafxVersion = "21"
javafx {
    version = javafxVersion
//    modules("javafx.base", "javafx.swing", "javafx.graphics", "javafx.controls", "javafx.fxml", "javafx.media", "javafx.web")
    modules("javafx.base", "javafx.graphics", "javafx.swing", "javafx.media", "javafx.controls")
}

kotlin {
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.desktop.currentOs)
                api("org.openjfx:javafx-base:$javafxVersion:win")
                api("org.openjfx:javafx-graphics:$javafxVersion:win")
                api("org.openjfx:javafx-swing:$javafxVersion:win")
                api("org.openjfx:javafx-media:$javafxVersion:win")
                api("org.openjfx:javafx-controls:$javafxVersion:win")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
            }
        }
        val desktopMain by getting{
            dependsOn(commonMain)
        }
    }
}

// TODO it seems that argument isn't applied to the common sourceSet. Figure out why
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.register("prepareKotlinBuildScriptModel"){

}
