import net.ltgt.gradle.errorprone.errorprone

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.diffplug.spotless") version "6.14.1"
    id("net.ltgt.errorprone") version "3.0.1"
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
version = "0.4.0-SNAPSHOT"

val crowdin by configurations.creating
configurations["compileOnly"].extendsFrom(crowdin)
configurations["testImplementation"].extendsFrom(crowdin)

dependencies {
    crowdin("com.github.crowdin:crowdin-api-client-java:1.5.3") {
        exclude(group = "org.projectlombok")
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.fasterxml.jackson.core")
    }

    val jacksonVersion = "2.12.3"
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("org.apache.httpcomponents:httpclient:4.5.3")
    implementation("org.projectlombok:lombok:1.18.10")

    testImplementation("org.assertj:assertj-core:3.24.2")
    val jupiterVersion = "5.9.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")

    "errorprone"("com.google.errorprone:error_prone_core:2.18.0")
}

java {
    val javaVersion = JavaVersion.VERSION_11
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs = listOf("-Xlint:all", "-Xlint:-path", "-Xlint:-options", "-Werror")
    options.errorprone {
        disableAllChecks.set(true)
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
    java {
        licenseHeaderFile("gradle/spotless/license.java")
        googleJavaFormat("1.7").aosp()
    }

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
