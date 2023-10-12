plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("dgroomes.memory_leak.Runner")
}

/**
 * Configure the compiler task, test task, start script creation task, and the run task to enable Java language "Preview
 * Features" so we can access the Foreign Function and Memory Access APIs which are in preview.
 */
tasks {
    withType(JavaCompile::class.java) {
        options.compilerArgs.addAll(arrayOf("--enable-preview"))
    }

    named<CreateStartScripts>("startScripts") {
        defaultJvmOpts = listOf("--enable-preview", "--enable-native-access=ALL-UNNAMED")
    }

    named<JavaExec>("run") {
        jvmArgs = listOf("--enable-preview", "--enable-native-access=ALL-UNNAMED")
    }
}
