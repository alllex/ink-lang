import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    `maven-publish`
}

repositories {
    mavenCentral()
}

group = "me.alllex.inklang"
version = project.layout.projectDirectory.file("version.txt").asFile.readText().trim()

java.toolchain.languageVersion = libs.versions.jvmToolchain.map { JavaLanguageVersion.of(it) }

tasks.withType<JavaCompile>().configureEach {
    options.release = libs.versions.jdkTarget.map { it.toInt() }
}

fun String.toJdkTarget() = if (toInt() <= 8) "1.$this" else this // 8 => 1.8, 11 => 11
fun String.toKotlinMinor() = split(".").take(2).joinToString(".") // 1.7.21 => 1.7, 1.9 => 1.9

val kotlinTarget = libs.versions.kotlinTarget
val kotlinVersionTarget = kotlinTarget.map { KotlinVersion.fromVersion(it.toKotlinMinor()) }
kotlin {
    targets.all {
        compilations.all {
            compilerOptions.configure {
                languageVersion = kotlinVersionTarget
                apiVersion = kotlinVersionTarget
            }
        }
    }
    coreLibrariesVersion = kotlinTarget.get()

    jvm {
        compilations.all {
            compilerOptions.configure {
                jvmTarget = libs.versions.jdkTarget.map { JvmTarget.fromTarget(it.toJdkTarget()) }
                freeCompilerArgs.add(libs.versions.jdkTarget.map { "-Xjdk-release=${it.toJdkTarget()}" })
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

//    js {
//        browser()
//        nodejs()
//    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.parsus)
                api(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit5)
                implementation(libs.junit.jupiter)
            }
        }
    }
}
