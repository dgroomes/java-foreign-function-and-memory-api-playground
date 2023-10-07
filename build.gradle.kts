plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("dgroomes.foreign_memory.App")
}

dependencies {
    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
    implementation(libs.classgraph)
}

/**
 * Configure the compiler task and start script creation task to enable Java language "Preview Features" so we can
 * access the Foreign Function and Memory Access APIs which are in preview.
 */
tasks {
    withType(JavaCompile::class.java) {
        options.compilerArgs.addAll(arrayOf("--enable-preview"))
    }

    named<CreateStartScripts>("startScripts") {
        defaultJvmOpts = listOf("--enable-preview", "--enable-native-access=ALL-UNNAMED")
    }
}

/*
  Warning: The below part only exists to ease the developer experience when opening this project in an IDE. See the
  extensive note in the file `settings.gradle.kts` for more information. This Gradle file is not meant to be used as an
  example.

  This build file will define the subproject `heterogeneous-foreign-memory/` as a Gradle project so that Intellij will
  automatically recognize the source code provide a working "click the green play button to execute the program" experience.
  But, importantly, this subproject still works as a standalone project without Gradle.

  A good test for validating that this Gradle configuration works is to actually execute the main methods. Try it with:
    * `./gradlew heterogeneous-foreign-memory:run`

  A better test is to open this project in Intellij, wait for Intellij to make sense of the project, and then click the
  green play buttons in the editor gutter on the "public static void main" method. The program should run.
 */

project("heterogeneous-foreign-memory") {

    apply(plugin = "java")
    apply(plugin = "application")

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    sourceSets {
        main {
            java {
                setSrcDirs(listOf("src"))
            }
        }
    }

    tasks {
        withType(JavaCompile::class.java) {
            options.compilerArgs.addAll(arrayOf("--enable-preview"))
        }

        withType<JavaExec> {
            jvmArgs = listOf("--enable-preview")
        }
    }

    application {
        mainClass.set("JaggedSteppingWindowDemo")
    }
}
