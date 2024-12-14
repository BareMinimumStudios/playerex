import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("fabric-loom") version "1.9-SNAPSHOT"
	id("maven-publish")
	id("org.jetbrains.kotlin.jvm") version "2.1.0"
	id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
	id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

version = "${properties["mod_version"]}"
group = "${properties["maven_group"]}"

base {
	archivesName = "${properties["archives_base_name"]}"
}

repositories {
	maven("https://maven.wispforest.io/releases") {
		name = "wispForestReleases"
	}
	maven("https://maven.ladysnake.org/releases") {
		name = "Ladysnake Mods"
	}
	maven("https://api.modrinth.com/maven") {
		name = "Modrinth"
	}
	maven("https://maven.nucleoid.xyz/") {
		name = "Nucleoid"
	}
	maven("https://maven.kosmx.dev/") {
	}
	maven("https://maven.parchmentmc.org") {
		name = "ParchmentMC"
	}
	maven("https://maven.quiltmc.org/repository/release/") {
		name = "Quilt"
	}
}

loom {
    splitEnvironmentSourceSets()

	mods {
		register("playerex-directors-cut") {
			sourceSet("main")
			sourceSet("client")
		}

	}
}

dependencies {
	minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

	mappings(loom.layered() {
		mappings("org.quiltmc:quilt-mappings:${properties["minecraft_version"]}+build.${properties["quilt_mappings_version"]}:intermediary-v2")
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${properties["parchment_version"]}@zip")
	})

	modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")

	// in-house
	modImplementation("maven.modrinth:data-attributes-directors-cut:${properties["data_attributes_version"]}")
	modImplementation("maven.modrinth:opc-directors-cut:${properties["opc_version"]}")?.let(::include)

	// Cardinal Components
	modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${properties["cardinal_components_version"]}")?.let(::include)
	modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${properties["cardinal_components_version"]}")?.let(::include)
	modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-chunk:${properties["cardinal_components_version"]}")?.let(::include)
	modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-level:${properties["cardinal_components_version"]}")?.let(::include)

	modImplementation("maven.modrinth:AdditionalEntityAttributes:${properties["additional_entity_attributes_version"]}")?.let(::include)
	modImplementation("maven.modrinth:ranged-weapon-api:${properties["ranged_weapon_api_version"]}")

	modImplementation("io.wispforest:endec:${properties["endec_version"]}")?.let(::include)
	modImplementation("io.wispforest.endec:gson:${properties["endec_gson_version"]}")?.let(::include)
	modImplementation("io.wispforest.endec:netty:${properties["endec_netty_version"]}")?.let(::include)

	// owo
	annotationProcessor("io.wispforest:owo-lib:${properties["owo_version"]}")?.let(::modImplementation)
	include("io.wispforest:owo-sentinel:${properties["owo_version"]}")

	implementation("com.google.devtools.ksp:symbol-processing-api:${properties["ksp_version"]}")
	implementation("com.squareup:kotlinpoet-ksp:${properties["kotlinpoet_version"]}")
	ksp("dev.kosmx.kowoconfig:ksp-owo-config:${properties["ksp_owo_config_version"]}")

	modImplementation("eu.pb4:placeholder-api:${properties["placeholder_api_version"]}")?.let(::include)

	implementation("net.objecthunter:exp4j:${properties["exp4j_version"]}")?.let(::include)

	annotationProcessor("io.github.llamalad7:mixinextras-fabric:${properties["mixinextras_version"]}")?.let {
		implementation(it)
		include(it)
	}
}

tasks {
	processResources {
		inputs.property("version", project.version)

		filesMatching("fabric.mod.json") {
			expand("version" to project.version)
		}
	}

	jar {
		from("LICENSE") {
			rename { "${it}_${project.base.archivesName.get()}"}
		}
	}

	java {
		// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
		// if it is present.
		// If you remove this line, sources will not be generated.
		withSourcesJar()

		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	// configure the maven publication
	publishing {
		publications {
			create<MavenPublication>("mavenJava") {
				artifact(remapJar) {
					builtBy(remapJar)
				}
				artifact(kotlinSourcesJar) {
					builtBy(remapSourcesJar)
				}
			}
		}

		repositories {}
	}

	withType<KotlinCompile>().configureEach {
		compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
		}
	}
}



