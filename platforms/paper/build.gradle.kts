import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("platform-conventions")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    implementation(project(":common:common-core"))

    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.18") {
        exclude("org.yaml")
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.18")
}

tasks {
    jar {
        archiveClassifier.set("deobf")
    }

    shadowJar {
        archiveClassifier.set("deobf-all")
    }

    reobfJar {
        inputJar.set(shadowJar.flatMap { it.archiveFile })
    }
}

mtgPlatform {
    productionJar.set(tasks.reobfJar.flatMap { it.outputJar })
}

paper {
    name = "ModernTerrainGenerator"
    version = project.version.toString()
    description = "A Modern Paper Terrain Generator based in Minecraft."
    author = "Rodriaum"
    main = "com.rodriaum.mtg.paper.MTGPlugin"

    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    apiVersion = "1.21"
    foliaSupported = false

    permissions {
        register("mtg.*") {
            default = BukkitPluginDescription.Permission.Default.FALSE
            children = listOf(
                "mtg.cmd.*",
                "mtg.default"
            )
        }

        register("mtg.cmd.*") {
            default = BukkitPluginDescription.Permission.Default.FALSE
            children = listOf(
                "mtg.cmd.biome",
                "mtg.cmd.data",
                "mtg.cmd.edit",
                "mtg.cmd.export",
                "mtg.cmd.flush",
                "mtg.cmd.preset",
                "mtg.cmd.region",
                "mtg.cmd.structure",
                "mtg.cmd.help",
                "mtg.cmd.map",
                "mtg.cmd.spawn",
                "mtg.cmd.tp"
            )
        }

        register("mtg.cmd.biome") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Show chunk biome and block stats"
        }

        register("mtg.cmd.data") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Modifies MTG data"
        }

        register("mtg.cmd.edit") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows you to edit presets"
        }

        register("mtg.cmd.export") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Exports the current WorldEdit selection as a new BO3."
        }

        register("mtg.cmd.flush") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Flushes MTG data"
        }

        register("mtg.cmd.preset") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Shows current preset"
        }

        register("mtg.cmd.region") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Configures region for MTG commands"
        }

        register("mtg.cmd.structure") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Shows information about a structure"
        }

        register("mtg.cmd.help") {
            default = BukkitPluginDescription.Permission.Default.TRUE
            description = "Show help"
        }

        register("mtg.cmd.map") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Generate map image, terrain/biome options are available"
        }

        register("mtg.cmd.spawn") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Spawn BO3 where you look"
        }

        register("mtg.cmd.tp") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Teleport to the given biome (name or ID, max range 1000 chunks)"
        }

        register("mtg.default") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }

    serverDependencies {
        register("WorldEdit") {
            required = false
        }
    }
}