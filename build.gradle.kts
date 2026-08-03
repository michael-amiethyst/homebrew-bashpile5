import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//////////////
// Main Config
//////////////

val antlrVersion = properties["version.antlr"] as String
val cliktVersion = properties["version.clikt"] as String
val junitVersion = properties["version.junit"] as String

group = "org.bashpile.core"
version = properties["version.bashpile"] as String

plugins {
    antlr
    // kotlin version in plugins must be literal
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("org.graalvm.buildtools.native") version "1.1.5"
    id("org.gradlex.jvm-dependency-conflict-detection") version "2.2"
    id("com.adarshr.test-logger") version "4.0.0"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // clikt
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("com.github.ajalt.clikt:clikt-markdown:$cliktVersion")

    // antlr
    antlr("org.antlr:antlr4:$antlrVersion")
    antlr("org.antlr:antlr4-runtime:$antlrVersion")
    implementation("com.yuvalshavit:antlr-denter:1.1")

    // logging
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.24.3")
    implementation("ch.qos.logback:logback-classic:1.5.19")
    runtimeOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.0")

    // other dependencies
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("commons-io:commons-io:2.14.0")
    implementation("com.google.guava:guava:32.0.1-jre")

    // tests
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.matching("GraalVM"))
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveVersion = ""
    manifest {
        attributes["Main-Class"] = "org.bashpile.core.MainKt"
    }
}

// for use in the bin/tokenize script
val saveClasspath = tasks.register("saveClasspath") {
    doFirst {
        File("build/classpath.txt").writeText(sourceSets["main"].runtimeClasspath.asPath)
    }
}
tasks.build { dependsOn(saveClasspath) }

////////////////////
// antlr integration
////////////////////

val generatedOutputFilename = "${layout.buildDirectory.get()}/generated/sources/main/java/antlr/org/bashpile/core"

tasks.generateGrammarSource {
    // set the output directory to some arbitrary location in the `/build` directory.
    // by convention `/build/generated/sources/main/java/<generator name>` is often used
    outputDirectory = file(generatedOutputFilename)

    // pass '-package' to make the generator put code in not default space
    arguments = listOf("-package", "org.bashpile.core", "-no-listener", "-visitor")
}

// workaround for an antlr bug, it should be fixed after 4.13.2
tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.withType<AntlrTask>())
}

sourceSets {
    main {
        java {
            srcDir(generatedOutputFilename)
        }
    }
}

//////////////////////////////////
// Adjust PATH on Windows machines
//////////////////////////////////

val linuxHome = System.getProperty("user.home")

val homebrewPaths = System.getenv("HOMEBREW_PREFIX")
    ?.let { listOf("$it/bin", "$it/sbin") }
    .orEmpty()

val linuxPath = (
        listOf(
            "$linuxHome/.sdkman/candidates/java/current/bin",
            "/home/linuxbrew/.linuxbrew/bin",
            "/home/linuxbrew/.linuxbrew/sbin",
            "$linuxHome/.linuxbrew/bin",
            "$linuxHome/.linuxbrew/sbin",
        ) + homebrewPaths + listOf(
            "/usr/local/sbin",
            "/usr/local/bin",
            "/usr/sbin",
            "/usr/bin",
            "/sbin",
            "/bin",
        )
        ).joinToString(":")

val missingLinuxToolchainPath = "/usr/bin" !in System.getenv("PATH").orEmpty().split(':')

tasks.withType<Exec>().configureEach {
    if (missingLinuxToolchainPath) {
        environment("PATH", linuxPath)
    }
}

tasks.withType<JavaExec>().configureEach {
    if (missingLinuxToolchainPath) {
        environment("PATH", linuxPath)
    }
}

tasks.withType<Test>().configureEach {
    if (missingLinuxToolchainPath) {
        environment("PATH", linuxPath)
    }
}

/////////////////
// GraalVM Native
/////////////////

// Windows-hosted IntelliJ can start WSL Gradle without the normal Linux PATH.
// Native Image requires /usr/bin for gcc, as, and linker tools.
val nativeToolchainOnPath = System.getenv("PATH")
    .orEmpty()
    .split(':')
    .contains("/usr/bin")

tasks.named("nativeCompile") {
    onlyIf {
        if (!nativeToolchainOnPath) {
            logger.warn(
                "Skipping nativeCompile on Windows. " +
                        "Run nativeCompile from a WSL terminal or " +
                        "run your IDE with a WSL backend (like Intellij Ultimate can do) " +
                        "to build the native binary."
            )
        }
        nativeToolchainOnPath
    }
}

graalvmNative {
    binaries {
        all {
            resources.autodetect()
        }
        named("main") {
            // more options at https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html#configure-native-image
            imageName.set("bashpile")
            mainClass.set("org.bashpile.core.MainKt")
            sharedLibrary.set(false)

            // additional buildArgs at https://www.graalvm.org/21.3/reference-manual/native-image/Options/

            // From https://stackoverflow.com/questions/72770461/graalvm-native-image-can-not-compile-logback-dependencies
            buildArgs.add("-H:+UnlockExperimentalVMOptions")
            buildArgs.add("-H:ReflectionConfigurationFiles=../../../src/main/resources/reflection-config.json")
            buildArgs.add("-H:CCompilerPath=/usr/bin/gcc")
        }
    }
}

////////////////////////////////////////////////////////
// system tests - integration tests for the whole system
////////////////////////////////////////////////////////

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        compileClasspath += sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.test.get().output
    }
}

val intTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val intTestRuntimeOnly: Configuration by configurations.getting

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    intTestImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    intTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    dependsOn("nativeCompile")

    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

//////////////////////////////////////
// Create the executable from the .jar
//////////////////////////////////////

val createBashpileExe = tasks.register<Exec>("create-bashpile-exe") {
    dependsOn("shadowJar")
    // CWD is project root
    commandLine("bin/create-bashpile-exe")
}

tasks.build { finalizedBy(createBashpileExe)}