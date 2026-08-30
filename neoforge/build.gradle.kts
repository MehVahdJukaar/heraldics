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

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modRuntimeOnly("net.mehvahdjukaar:codecui-neoforge:${codecui_version}")
}
