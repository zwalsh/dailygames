import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.6.21" // keep in sync with plugin version
val ktorVersion = "1.6.7"
val logbackVersion = "1.2.5"
val jdbiVersion = "3.14.4"

plugins {
    kotlin("jvm") version "1.6.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    application
}

group = "sh.zachwal"

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(platform("org.jdbi:jdbi3-bom:$jdbiVersion"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css-jvm:1.0.0-pre.332-kotlin-1.6.21")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Observability
    implementation(platform("io.sentry:sentry-bom:7.16.0"))
    implementation("io.sentry:sentry")
    implementation("io.sentry:sentry-logback")

    // Reflection (for @Controller annotation inspection)
    implementation("org.reflections:reflections:0.10.2")

    // database
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.postgresql:postgresql:42.1.4")
    implementation("org.jdbi:jdbi3-core")
    implementation("org.jdbi:jdbi3-kotlin")
    implementation("org.jdbi:jdbi3-postgres")
    implementation("org.jdbi:jdbi3-kotlin-sqlobject")
    implementation("org.jdbi:jdbi3-jackson2")

    // passwords
    implementation("org.mindrot:jbcrypt:0.4")

    // DI (6.0.0 supports both javax & jakarta, has Java 17 support)
    implementation("com.google.inject:guice:6.0.0")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")

    // Caching
    implementation("com.google.guava:guava:33.4.0-jre")

    // Use the Kotlin test library.
    testImplementation(kotlin("test"))

    // For testing Ktor & websockets
    testImplementation("io.ktor:ktor-client-core:$ktorVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.ktor:ktor-client-websockets:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")

    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.testcontainers:postgresql:1.20.0")
    testImplementation("org.liquibase:liquibase-core:4.29.1")

    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")
}

application {
    // Define the main class for the application.
    mainClass.set("sh.zachwal.dailygames.AppKt")
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform()
    // MockK 1.12.3 requires these JVM args to work with Java 17's stronger encapsulation.
    // Alternative: upgrade to Kotlin 1.8+ and Ktor 2.x to use MockK 1.13+
    jvmArgs(
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED"
    )
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
