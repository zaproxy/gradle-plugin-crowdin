import net.ltgt.gradle.errorprone.errorprone

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.diffplug.spotless")
    id("org.zaproxy.common")
    id("net.ltgt.errorprone") version "4.0.0"
}

repositories {
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.crowdin")
        }
    }
    mavenCentral()
}

group = "org.zaproxy.gradle"
version = "0.5.0"

val crowdin by configurations.creating
configurations["compileOnly"].extendsFrom(crowdin)
configurations["testImplementation"].extendsFrom(crowdin)

dependencies {
    crowdin("com.github.crowdin:crowdin-api-client-java:1.8.0") {
        exclude(group = "org.projectlombok")
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.fasterxml.jackson.core")
    }

    val jacksonVersion = "2.15.2"
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("org.projectlombok:lombok:1.18.24")

    testImplementation("org.assertj:assertj-core:3.26.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    "errorprone"("com.google.errorprone:error_prone_core:2.28.0")
}

java {
    val javaVersion = JavaVersion.VERSION_11
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        error(
            "MissingOverride",
            "WildcardImport",
        )
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

spotless {
    kotlinGradle {
        ktlint()
    }
}

gradlePlugin {
    website.set("https://github.com/zaproxy/gradle-plugin-crowdin")
    vcsUrl.set("https://github.com/zaproxy/gradle-plugin-crowdin.git")
    plugins {
        create("crowdin") {
            id = "org.zaproxy.crowdin"
            implementationClass = "org.zaproxy.gradle.crowdin.CrowdinPlugin"
            displayName = "Plugin to integrate with Crowdin"
            description = "A Gradle plugin to integrate with Crowdin."
            tags.set(listOf("crowdin"))
        }
    }
}

tasks.named("jar", Jar::class).configure {
    from({
        crowdin.filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
