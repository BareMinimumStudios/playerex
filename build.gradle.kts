import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    `maven-publish`
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.cloche)
}

group = providers.gradleProperty("maven_group").get()
version = providers.gradleProperty("mod_version").get()

repositories {
    cloche.librariesMinecraft()
    mavenCentral()

    cloche {
        main()
        mavenFabric()
        mavenNeoforgedMeta()
        mavenNeoforged()
        mavenParchment()
    }

    maven("https://thedarkcolour.github.io/KotlinForForge/") {
        content { includeGroup("thedarkcolour") }
    }
    maven("https://maven.fzzyhmstrs.me/")
    maven("https://redempt.dev")
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
}

cloche {
    metadata {
        modId = "playerex"
        name = "PlayerEx"
        description = "RPG-style player attributes, progression, and item leveling."
        license = "BML-1.0"

        author("bibireden")
        author("naomi")
        author("DataEncoded")
        contributor("pokesmells")
        contributor("OverlordsIII")
        contributor("CleverNucleus")

        url = "https://github.com/BareMinimumStudios/playerex"
        sources = "https://github.com/BareMinimumStudios/playerex"
        issues = "https://github.com/BareMinimumStudios/playerex/issues"
        icon = "assets/playerex/icon.png"
    }

    common {
        client { }
        mixins.from("src/main/resources/playerex.mixins.json")
        sourceSet.resources.exclude("playerex.mixins.json")
        mappings {
            official()
            parchment(libs.versions.parchment)
        }
        dependencies {
            implementation(libs.crunch)
            compileOnly(libs.mixinextras)
        }
    }

    fabric("fabric:1.21.1") {
        minecraftVersion = "1.21.1"
        loaderVersion = libs.versions.fabric.loader
        includedClient()
        sourceSet.java.srcDir("src/fabric/1.21.1/java")
        mixins.from("src/fabric/1.21.1/playerex.fabric.mixins.json")

        runs {
            server()
            client()
        }

        dependencies {
            include(libs.crunch)
            include(libs.remnant.fabric)
            modImplementation(libs.remnant.fabric)
            modImplementation(libs.data.attributes.fabric)
            fabricApi(libs.versions.fabric.api)
            modImplementation(libs.fabric.language.kotlin)
            modImplementation(libs.fzzy.config.fabric)
        }

        metadata {
            dependencies {
                dependency {
                    modId = "fabric-api"
                    version(libs.versions.fabric.api.get())
                }
                dependency {
                    modId = "fabric-language-kotlin"
                    version(libs.versions.fabric.language.kotlin.get())
                }
                dependency {
                    modId = "fzzy_config"
                    version(libs.versions.fzzy.fabric.get())
                }
                dependency {
                    modId = "data_attributes"
                    version(libs.versions.data.attributes.get())
                }
                suggest("critical_strike", "0", "PlayerEx can delegate critical-hit execution to Critical Strike when installed.")
                suggest("spell_power", "0", "PlayerEx integrates Intelligence/Luckiness/Constitution with Spell Power attributes.")
                suggest("spell_engine", "0", "PlayerEx can expose Spell Engine-aware progression without hard linkage.")
                suggest("ranged_weapon_api", "0", "PlayerEx Dexterity integrates with Ranged Weapon API when installed.")
                suggest("more_rpg_classes", "0", "PlayerEx can surface More RPG Library attributes through Data Attributes.")
                dependency {
                    modId = "remnant"
                    version(libs.versions.remnant.get())
                }
            }

            entrypoint("main") {
                adapter.set("kotlin")
                value.set("com.bibireden.playerex.platform.PlayerExFabricEntrypoint")
            }
            entrypoint("client") {
                adapter.set("kotlin")
                value.set("com.bibireden.playerex.platform.PlayerExFabricClientEntrypoint")
            }

        }
    }

    neoforge("neoforge:1.21.1") {
        sourceSet.java.srcDir("src/neoforge/1.21.1/java")
        mixins.from("src/neoforge/1.21.1/playerex.neoforge.mixins.json")
        minecraftVersion = "1.21.1"
        loaderVersion = libs.versions.neoforge.loader

        runs {
            server()
            client()
        }

        dependencies {
            include(libs.crunch)
            include(libs.remnant.neoforge)
            modImplementation(libs.remnant.neoforge)
            modImplementation(libs.data.attributes.neoforge)
            modImplementation(libs.neoforge.language.kotlin)
            modImplementation(libs.fzzy.config.neoforge)
        }

        metadata {
            modLoader = "kotlinforforge"
            loaderVersion { start = libs.versions.neoforge.language.kotlin.get() }
            blurLogo = false

            dependencies {
                dependency {
                    modId = "kotlinforforge"
                    version(libs.versions.neoforge.language.kotlin.get())
                }
                dependency {
                    modId = "fzzy_config"
                    version(libs.versions.fzzy.neoforge.get())
                }
                dependency {
                    modId = "data_attributes"
                    version(libs.versions.data.attributes.get())
                }
                suggest("critical_strike", "0", "PlayerEx can delegate critical-hit execution to Critical Strike when installed.")
                suggest("spell_power", "0", "PlayerEx integrates Intelligence/Luckiness/Constitution with Spell Power attributes.")
                suggest("spell_engine", "0", "PlayerEx can expose Spell Engine-aware progression without hard linkage.")
                suggest("ranged_weapon_api", "0", "PlayerEx Dexterity integrates with Ranged Weapon API when installed.")
                suggest("more_rpg_classes", "0", "PlayerEx can surface More RPG Library attributes through Data Attributes.")
                dependency {
                    modId = "remnant"
                    version(libs.versions.remnant.get())
                }
            }

        }
    }
}

// Cloche 0.19.13 does not currently wire its generated Fabric mappings
// archive into the access-widen task graph reliably under Gradle 9.8.0.
// Ensure the archive exists before any Fabric access-widen task resolves the
// remapped compile classpath.
tasks.matching { it.name.startsWith("accessWidenFabric1211") }.configureEach {
    dependsOn("generateFabric1211MappingsArtifact")
}

kotlin {
    jvmToolchain(21)

    // This snapshot keeps loader Kotlin sources directly under the version folder.
    sourceSets.named("fabric1211") {
        kotlin.srcDir("src/fabric/1.21.1/kotlin")
    }
    sourceSets.named("neoforge1211") {
        kotlin.srcDir("src/neoforge/1.21.1/kotlin")
    }

    // Gradle 9.8 embeds Kotlin 2.4.10, but Cloche 0.19.13's
    // classpath-api-stubs compiler plugin predates Kotlin 2.3's
    // CompilerPluginRegistrar#getPluginId ABI. Use the Build Tools API
    // to run an isolated Kotlin 2.2.20 compiler while keeping KGP aligned
    // with Gradle's embedded Kotlin version.
    @OptIn(ExperimentalBuildToolsApi::class, ExperimentalKotlinGradlePluginApi::class)
    compilerVersion.set("2.2.20")

    // Source libraries must match the isolated compiler, not the KGP version.
    coreLibrariesVersion = "2.2.20"
}

dependencies {
    testImplementation(kotlin("test", "2.2.20"))
}

// Keep common compile dependencies in a declared output, outside Gradle task scratch space.
tasks.withType<net.msrandom.stubs.GenerateStubApi>().configureEach {
    outputDirectory.set(layout.buildDirectory.dir("generated/$name"))
}


val validateTranslations = tasks.register("validateTranslations") {
    group = "verification"
    description = "Validate vanilla language files before packaging."
    val languageFiles = fileTree("src/main/resources/assets") { include("**/lang/*.json") }
    inputs.files(languageFiles)
    doLast {
        languageFiles.forEach { file ->
            val translations = groovy.json.JsonSlurper().parse(file) as? Map<*, *>
                ?: error("${file.name}: expected a language object")
            translations.forEach { (key, value) ->
                require(key is String && value is String) {
                    "${file.name}: translation $key must be a string; vanilla rejects the entire file otherwise"
                }
            }
        }
    }
}

tasks.named("check") { dependsOn(validateTranslations) }
tasks.withType<Jar>().configureEach { dependsOn(validateTranslations) }
