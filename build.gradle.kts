plugins {
    id("parent-logic")
}

defaultTasks = arrayListOf("build", "publishToMavenLocal")

allprojects {
    group = "com.rodriaum.mtg"
    version = "1.21.11-0.0.28"
    description = "Modern Terrain Generator: Generate anything!"
}

subprojects {
    apply(plugin = "base-conventions")

    repositories {
        mavenCentral()
    }
}

val universalJar = tasks.register<Jar>("universalJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    archiveFileName.set("ModernTerrainGenerator-Universal-" + project.version + ".jar")
}

tasks.build {
    dependsOn(universalJar)
}

listOf(
    project(":platforms:paper"),
).forEach { proj ->
    proj.afterEvaluate {
        // Show more errors in intellij
        proj.tasks.withType<JavaCompile>() {
            options.compilerArgs.add("-Xmaxerrs")
            options.compilerArgs.add("5000")
        }
        universalJar {
            // The platform's production jar must exist before the universal jar is assembled.
            dependsOn(proj.tasks.named("reobfJar"))
            val tree = zipTree(proj.the<OTGPlatformExtension>().productionJar)
            from(tree)
            val manifestFile = tree.elements.map { files ->
                files.find { it.asFile.path.endsWith("META-INF/MANIFEST.MF") }!!
            }
            manifest.from(manifestFile)
        }
    }
}