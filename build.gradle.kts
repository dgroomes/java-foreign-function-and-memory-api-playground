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
