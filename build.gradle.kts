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