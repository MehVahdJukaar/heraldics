plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version = extra["moonlight_version"] as String
val codecui_version = extra["codecui_version"] as String

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")

    modRuntimeOnly("net.mehvahdjukaar:codecui-fabric:${codecui_version}")
}
