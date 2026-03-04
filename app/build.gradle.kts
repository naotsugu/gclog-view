plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

val os   = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()
val arch = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentArchitecture()
val platform = when {
    os.isMacOsX  && arch.isArm64 -> "mac-aarch64"
    os.isMacOsX  && arch.isAmd64 -> "mac"
    os.isLinux   && arch.isArm64 -> "linux-aarch64"
    os.isLinux   && arch.isAmd64 -> "linux"
    os.isWindows && arch.isAmd64 -> "win"
    else -> throw Error("Unsupported OS: $os, ARCH: $arch")
}

dependencies {
    implementation(libs.gctoolkit.parser)
    implementation(libs.atlantafx.base)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "com.mammb.code.gclog.view.Main"
    applicationDefaultJvmArgs = listOf(
        "-Xms16m",
        "-XX:+UseSerialGC", "-XX:MinHeapFreeRatio=5", "-XX:MaxHeapFreeRatio=10", "-XX:-ShrinkHeapInSteps", "-DidleGcDelayMillis=3000",
        "-Xshare:off",
        "-XX:+UseCompactObjectHeaders",
        "--enable-native-access=javafx.graphics", // Restricted methods will be blocked in a future release unless native access is enabled
        "--enable-native-access=ALL-UNNAMED", // java.lang.System::loadLibrary has been called by io.netty.util.internal.NativeLibraryUtil in an unnamed module
    )
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

javafx {
    version = "26-ea+27"
    modules("javafx.controls")
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier = "gclog-view"
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    }) {
        exclude("module-info.class")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }
    manifest {
        attributes("Main-Class" to "com.mammb.code.gclog.view.Main")
    }
}

tasks.register<Exec>("jpackage") {
    dependsOn(tasks.jar)

    // get the path to the jpackage command from the toolchain
    val javaToolchainService = project.extensions.getByType(JavaToolchainService::class.java)
    val jdkPath = javaToolchainService.launcherFor(java.toolchain).get().executablePath
    println("Toolchain JDK Path: $jdkPath")

    val commandPath = File(jdkPath.asFile.parentFile, "jpackage").absolutePath
    val outputDir = project.layout.buildDirectory.dir("jpackage")
    val inputDir = tasks.jar.get().archiveFile.get().asFile.parentFile

    val iconType = if (os.isWindows) "icon.ico" else if (os.isMacOsX) "icon.icns" else "icon.png"
    val iconPath = "${project.rootDir}/docs/icon/${iconType}"

    commandLine(commandPath,
        "--type", "app-image",
        "--name", "gclog-view",
        "--dest", outputDir.get().asFile.absolutePath,
        "--input", inputDir.absolutePath,
        "--main-jar", tasks.jar.get().archiveFileName.get(),
        "--icon", iconPath,

        "--java-options", "-Xms16m",
        "--java-options", "-XX:+UseSerialGC",
        "--java-options", "-XX:MinHeapFreeRatio=5",
        "--java-options", "-XX:MaxHeapFreeRatio=10",
        "--java-options", "-XX:-ShrinkHeapInSteps",
        "--java-options", "-DidleGcDelayMillis=3000",
        "--java-options", "-Xshare:off",
        "--java-options", "-XX:+UseCompactObjectHeaders",
        "--java-options", "--enable-native-access=javafx.graphics",
        "--java-options", "--enable-native-access=ALL-UNNAMED",
    )
    doFirst {
        if (outputDir.get().asFile.exists()) {
            outputDir.get().asFile.deleteRecursively()
        }
    }

}

tasks.register<Zip>("pkg") {
    dependsOn("jpackage")
    isPreserveFileTimestamps = true
    isReproducibleFileOrder = false
    useFileSystemPermissions()
    archiveFileName = "gclog-view-${platform}.zip"
    from(layout.buildDirectory.dir("jpackage"))
}
