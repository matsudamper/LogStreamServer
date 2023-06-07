plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlin.serialization.json)
                implementation(libs.logback.classic)

                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.engine.netty)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.server.statusPages)
                implementation(libs.ktor.server.defaultHeaders)
                implementation(libs.ktor.server.fowardedHeader)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.server.contentNegotiation)
                implementation(libs.ktor.server.callLogging)
                implementation(libs.ktor.server.tlsCertificates)
            }
        }
        val jvmTest by getting {

        }
    }
}
