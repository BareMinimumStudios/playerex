rootProject.name = "PlayerEx"

pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases")
        maven("https://libraries.minecraft.net")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.msrandom.net/repository/cloche/")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        from(files("libraries.toml"))
    }
}
