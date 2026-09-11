plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

neoForge {
    runs {
        named("client") {
            programArguments.addAll("--username", "Dev0")
        }
    }
}

val moonlight_version = extra["moonlight_version"] as String
val codecui_version = extra["codecui_version"] as String
val supplementaries_version = extra["supplementaries_version"] as String
val emi_version = extra["emi_version"] as String

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modCompileOnly("dev.emi:emi-neoforge:${emi_version}")
    modImplementation("curse.maven:jei-238222:7420587")
    modCompileOnly("curse.maven:roughly-enough-items-310111:6199140")

    modRuntimeOnly("net.mehvahdjukaar:codecui-neoforge:${codecui_version}")

    modRuntimeOnly("net.mehvahdjukaar:supplementaries-neoforge:${supplementaries_version}") {
        isTransitive = false
    }
}
