plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version = extra["moonlight_version"] as String
val codecui_version = extra["codecui_version"] as String
val supplementaries_version = extra["supplementaries_version"] as String
val emi_version = extra["emi_version"] as String

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")

    modCompileOnly("dev.emi:emi-fabric:${emi_version}")
    modCompileOnly("curse.maven:jei-238222:7420583")
    modCompileOnly("curse.maven:roughly-enough-items-310111:6199139")
    modCompileOnly("curse.maven:roughly-enough-items-310111:6199140")

    modRuntimeOnly("net.mehvahdjukaar:codecui-fabric:${codecui_version}")

    modRuntimeOnly("net.mehvahdjukaar:supplementaries-fabric:${supplementaries_version}")
}
