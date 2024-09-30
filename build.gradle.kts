plugins {
    id("java")
}

group = "de.c4vxl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.thoughtworks.xstream:xstream:1.4.20") // used for saving the module state in a file

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "de.c4vxl.app.App"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().filter { it.name != "module-info.class" }.map { if (it.isDirectory) it else zipTree(it) })
}