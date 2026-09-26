import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

abstract class BuildLiquidWaveAndroid : DefaultTask() {
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val nativeSources: ConfigurableFileCollection
    @get:Internal abstract val androidSdkDirectory: DirectoryProperty
    @get:Input var ndkVersion: String = "26.1.10909125"
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun compile() {
        val sdk = androidSdkDirectory.get().asFile
        val ndk = sdk.resolve("ndk/$ndkVersion")
        check(ndk.isDirectory) { "Android NDK $ndkVersion is required to build Liquid Wave" }
        val toolchain = ndk.resolve("toolchains/llvm/prebuilt").listFiles()
            ?.firstOrNull { it.resolve("bin/aarch64-linux-android24-clang").exists() }
            ?: error("Android NDK clang toolchain is unavailable")
        val root = project.rootProject.projectDir
        val source = root.resolve("native/src/liquid_wave.c")
        val springSource = root.resolve("native/src/liquid_spring.c")
        val jniSource = root.resolve("native/src/liquid_wave_jni.c")
        val headers = root.resolve("native/include")
        val targets = mapOf(
            "arm64-v8a" to "aarch64-linux-android24-clang",
            "armeabi-v7a" to "armv7a-linux-androideabi24-clang",
            "x86_64" to "x86_64-linux-android24-clang"
        )
        for ((abi, compilerName) in targets) {
            val directory = outputDirectory.get().asFile.resolve(abi).apply { mkdirs() }
            val output = directory.resolve("libliquidwave.so")
            val command = listOf(
                toolchain.resolve("bin/$compilerName").absolutePath,
                "-std=c11", "-O3", "-Wall", "-Wextra", "-Werror", "-fPIC", "-shared",
                "-I${headers.absolutePath}", source.absolutePath, springSource.absolutePath, jniSource.absolutePath,
                "-Wl,-soname,libliquidwave.so", "-lm", "-o", output.absolutePath
            )
            check(ProcessBuilder(command).inheritIO().start().waitFor() == 0) {
                "Native Liquid Wave build failed for $abi"
            }
            check(ProcessBuilder(
                toolchain.resolve("bin/llvm-strip").absolutePath,
                "--strip-unneeded", output.absolutePath
            ).inheritIO().start().waitFor() == 0) {
                "Native Liquid Wave strip failed for $abi"
            }
        }
    }
}

abstract class BuildLiquidWaveIos : DefaultTask() {
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val nativeSources: ConfigurableFileCollection
    @get:Input var appleSdk: String = "iphoneos"
    @get:Input var targetTriple: String = "arm64-apple-ios13.0"
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun compile() {
        fun xcrun(vararg args: String): String {
            val process = ProcessBuilder(listOf("xcrun", "--sdk", appleSdk) + args)
                .redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText().trim()
            check(process.waitFor() == 0) { "xcrun failed: $output" }
            return output
        }
        val root = project.rootProject.projectDir
        val directory = outputDirectory.get().asFile.apply { mkdirs() }
        val archive = directory.resolve("libliquidwave.a")
        val clang = xcrun("--find", "clang")
        val sysroot = xcrun("--show-sdk-path")
        val objects = listOf("liquid_wave", "liquid_spring").map { name ->
            val objectFile = directory.resolve("$name.o")
            val command = listOf(
                clang, "-target", targetTriple, "-isysroot", sysroot,
                "-std=c11", "-O3", "-Wall", "-Wextra", "-Werror",
                "-I${root.resolve("native/include").absolutePath}",
                "-c", root.resolve("native/src/$name.c").absolutePath,
                "-o", objectFile.absolutePath
            )
            check(ProcessBuilder(command).inheritIO().start().waitFor() == 0) {
                "Native Liquid build failed for $name / $targetTriple"
            }
            objectFile.absolutePath
        }
        xcrun("ar", "rcs", archive.absolutePath, *objects.toTypedArray())
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

val liquidWaveSources = files(
    rootProject.file("native/include/liquid_wave.h"),
    rootProject.file("native/src/liquid_wave.c"),
    rootProject.file("native/include/liquid_spring.h"),
    rootProject.file("native/src/liquid_spring.c"),
    rootProject.file("native/src/liquid_wave_jni.c")
)
val isMacOsHost = System.getProperty("os.name").contains("Mac", ignoreCase = true)
val buildLiquidWaveAndroid = tasks.register<BuildLiquidWaveAndroid>("buildLiquidWaveAndroid") {
    nativeSources.from(liquidWaveSources)
    outputDirectory.set(layout.buildDirectory.dir("generated/liquidWave/android/jniLibs"))
}

extensions.configure<KotlinMultiplatformAndroidComponentsExtension>("androidComponents") {
    buildLiquidWaveAndroid.configure {
        androidSdkDirectory.set(sdkComponents.sdkDirectory)
    }
    onVariants(selector().all()) { variant ->
        variant.sources.jniLibs?.addGeneratedSourceDirectory(
            buildLiquidWaveAndroid,
            BuildLiquidWaveAndroid::outputDirectory
        )
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/anto426-project/liquid-monet")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orNull
                password = providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
        maven {
            name = "Staging"
            url = uri(rootProject.layout.buildDirectory.dir("maven-repository"))
        }
    }
}


kotlin {
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xexpect-actual-classes",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
            )
        }
    }

    android {
        namespace = "com.anto426.liquidmonet.sdk"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        withHostTest {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            moduleName.set("liquidmonet-sdk")
        }

        optimization {
            minify = true
            keepRules.file("proguard-rules.pro")
            consumerKeepRules.publish = true
            consumerKeepRules.file("consumer-rules.pro")
        }
    }

    listOf(
        Triple(iosArm64(), "iphoneos", "arm64-apple-ios13.0"),
        Triple(iosSimulatorArm64(), "iphonesimulator", "arm64-apple-ios13.0-simulator")
    ).forEach { (iosTarget, appleSdk, targetTriple) ->
        val nativeTask = tasks.register<BuildLiquidWaveIos>(
            "buildLiquidWave${iosTarget.name.replaceFirstChar { it.uppercase() }}"
        ) {
            onlyIf { isMacOsHost }
            nativeSources.from(liquidWaveSources)
            this.appleSdk = appleSdk
            this.targetTriple = targetTriple
            outputDirectory.set(layout.buildDirectory.dir("generated/liquidWave/${iosTarget.name}"))
        }
        iosTarget.compilations.getByName("main").cinterops.create("liquidWave") {
            defFile(project.file("src/nativeInterop/cinterop/liquidWave.def"))
            includeDirs(rootProject.file("native/include"))
            extraOpts("-libraryPath", nativeTask.flatMap { it.outputDirectory }.get().asFile.absolutePath)
        }
        tasks.matching { it.name == "cinteropLiquidWave${iosTarget.name.replaceFirstChar { c -> c.uppercase() }}" }
            .configureEach {
                dependsOn(nativeTask)
                onlyIf { isMacOsHost }
            }
        iosTarget.binaries.framework {
            baseName = "LiquidMonet"
            isStatic = true
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(libs.compose.material3.multiplatform)
                implementation(compose.ui)
                implementation(libs.navigationevent.compose)
                api(libs.kotlinx.datetime)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }
    }
}
