
plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
    id("org.graalvm.buildtools.native") version "0.9.28"
}

group = "dev.aider"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("com.github.ajalt.colorama:colorama:0.2.0")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("dev.aider.MainKt")
}

// Configure GraalVM Native Image
graalvmNative {
    binaries {
        named("main") {
            imageName.set("aider")
            mainClass.set("dev.aider.MainKt")
            debug.set(false)
            verbose.set(true)
            
            buildArgs.add("--no-fallback")
            buildArgs.add("--enable-url-protocols=http,https")
            buildArgs.add("--initialize-at-build-time=kotlinx.coroutines")
            buildArgs.add("--initialize-at-build-time=io.ktor")
            buildArgs.add("--initialize-at-build-time=kotlinx.serialization")
            buildArgs.add("-H:+ReportExceptionStackTraces")
            buildArgs.add("-H:+AddAllCharsets")
            buildArgs.add("-H:IncludeResources=.*")
        }
    }
}

// Task to build Windows executable (requires GraalVM with native-image installed)
tasks.register("buildWindowsExe") {
    group = "build"
    description = "Build Windows executable using GraalVM Native Image"
    dependsOn("nativeCompile")
    
    doLast {
        val nativeBinary = file("build/native/nativeCompile/aider")
        val windowsExe = file("build/native/nativeCompile/aider.exe")
        
        if (nativeBinary.exists() && !windowsExe.exists()) {
            nativeBinary.renameTo(windowsExe)
        }
        
        println("Windows executable built at: ${windowsExe.absolutePath}")
    }
}

// Task to create distribution zip with executable
tasks.register<Zip>("distWindowsExe") {
    group = "distribution"
    description = "Create distribution zip with Windows executable"
    dependsOn("buildWindowsExe")
    
    from("build/native/nativeCompile") {
        include("aider.exe")
    }
    from(".") {
        include("README.md")
        include("LICENSE")
    }
    
    archiveFileName.set("aider-windows-${version}.zip")
    destinationDirectory.set(file("build/distributions"))
}

// Enhanced jar task for fallback
tasks.jar {
    manifest {
        attributes("Main-Class" to "dev.aider.MainKt")
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("aider-${version}.jar")
}

// Task to create Windows batch script for jar execution
tasks.register("createWindowsBatch") {
    group = "build"
    description = "Create Windows batch script for jar execution"
    dependsOn("jar")
    
    doLast {
        val batchFile = file("build/libs/aider.bat")
        batchFile.writeText("""
            @echo off
            java -jar "%~dp0aider-${version}.jar" %*
        """.trimIndent())
        
        println("Windows batch script created at: ${batchFile.absolutePath}")
    }
}

// Task to create complete Windows distribution
tasks.register<Zip>("distWindows") {
    group = "distribution"
    description = "Create complete Windows distribution with both exe and jar options"
    dependsOn("buildWindowsExe", "createWindowsBatch")
    
    from("build/native/nativeCompile") {
        include("aider.exe")
    }
    from("build/libs") {
        include("aider-${version}.jar")
        include("aider.bat")
    }
    from(".") {
        include("README.md")
        include("LICENSE")
    }
    
    archiveFileName.set("aider-windows-complete-${version}.zip")
    destinationDirectory.set(file("build/distributions"))
}

// Clean task enhancement
tasks.clean {
    delete("build/native")
}
