plugins {
    id("com.possible-triangle.common")
}

common {
    //pinned so the build doesn't need to hit maven.neoforged.net to list versions
    neoformVersion = "1.21.1-20240808.144430"
    accessWidener()
}

val moonlight_version = extra["moonlight_version"] as String
val emi_version = extra["emi_version"] as String

dependencies {
    //@jar skips moonlight's module metadata: its jar variants are tagged neoforge-only, so in this module gradle
    //would otherwise fall back to the access transformer variant and the whole api would be missing from the classpath
    modCompileOnly("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}@jar")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modCompileOnly("dev.emi:emi-xplat-mojmap:${emi_version}")
    modCompileOnly("curse.maven:jei-238222:7420587")
    modCompileOnly("curse.maven:roughly-enough-items-310111:6199140")
}
