import de.marcphilipp.gradle.nexus.NexusPublishExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*


val dateFormat = SimpleDateFormat("yyyy.MM.dd-hh-mm")
dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("Europe/Oslo"))
val gitHash = System.getenv("CIRCLE_SHA1")?.takeLast(5) ?: "local-build"
val javaTimeAdapterVersion = "1.1.3"

group = "no.nav.sykepenger.kontrakter"
version = "${dateFormat.format(Date())}-$gitHash"

plugins {
    kotlin("jvm") version "1.3.41"
    "maven-publish"
    signing
    id("io.codearte.nexus-staging") version "0.21.0"
    id("de.marcphilipp.nexus-publish") version "0.2.0"
}

val jacksonVersion = "2.9.9"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.migesok", "jaxb-java-time-adapters", javaTimeAdapterVersion)
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("javax.validation:validation-api:2.0.0.Final")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

repositories {
    mavenCentral()
    jcenter()
}

nexusStaging {
    packageGroup = "no.nav"
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
}

configure<NexusPublishExtension> {
    username.set(System.getenv("SONATYPE_USERNAME"))
    password.set(System.getenv("SONATYPE_PASSWORD"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            this.credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.getByName("sourcesJar"))
            artifact(tasks.getByName("javadocJar"))
            pom {
                name.set("Income Report DTO")
                description.set("Data Object for the Income report for Sickness Benefits")
                url.set("https://github.com/navikt/inntektsmelding-kontrakt")

                organization {
                    name.set("NAV (Arbeids- og velferdsdirektoratet) - The Norwegian Labour and Welfare Administration")
                    url.set("https://www.nav.no/")
                }

                developers {
                    developer {
                        organization.set("NAV (Arbeids- og velferdsdirektoratet) - The Norwegian Labour and Welfare Administration")
                        organizationUrl.set("https://www.nav.no/")
                    }
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/inntektsmelding-kontrakt.git")
                    developerConnection.set("scm:git:https://github.com/navikt/inntektsmelding-kontrakt.git")
                    url.set("https://github.com/navikt/inntektsmelding-kontrakt")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    register("printVersion") {
        doLast {
            println(project.version)
        }
    }
    val generatePomPropertiesForJar by registering {
        val outputDir = file("$buildDir/pom-properties")
        outputDir.mkdirs()
        val outputFile = file("$outputDir/pom.properties")
        outputFile.writeText("""
#Generated by Gradle
#${Date()}
groupId=${project.group}
artifactId=${project.name}
version=${project.version}
""".trimIndent())
        outputs.file(outputFile)
    }

    withType<Jar> {
        val generatePomFileForMavenJavaPublication = getByName("generatePomFileForMavenJavaPublication")
        dependsOn(generatePomPropertiesForJar, generatePomFileForMavenJavaPublication)

        into("META-INF/maven/${project.group}/${project.name}") {
            from(generatePomPropertiesForJar)
            from(generatePomFileForMavenJavaPublication) {
                rename(".+", "pom.xml")
            }
        }
    }
    test {
        useJUnitPlatform()
        filter {
            includeTestsMatching("*Test")
        }
    }
}