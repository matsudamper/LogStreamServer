import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint.gradle.plugin)
}

group = "net.matsudamper"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set(rootProject.libs.versions.ktlint.get())
        debug.set(false)
        verbose.set(false)
        android.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }

        filter {
            exclude { element -> element.file.path.contains("generated") }
        }
    }

    gradle.projectsEvaluated {
        tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions {
                if (System.getenv()["CI"] == "true") {
                    allWarningsAsErrors = true
                }
                freeCompilerArgs = freeCompilerArgs.plus(
                    listOf(
                        "-Xjsr305=strict",
                        "-opt-in=kotlin.RequiresOptIn",
                        "-Xexplicit-api=strict",
                    ),
                )
            }
        }
    }
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":backend"))
                implementation(project(":frontend"))
                implementation(compose.desktop.currentOs)

                implementation(libs.kotlin.serialization.json)
                implementation(libs.logback.classic)
            }
        }
        val jvmTest by getting {

        }
    }
}