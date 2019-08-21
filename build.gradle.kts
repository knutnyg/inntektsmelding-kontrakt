import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import org.gradle.api.tasks.bundling.Jar
import de.marcphilipp.gradle.nexus.NexusPublishExtension
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.TimeZone


group = "no.nav.sykepenger.kontrakter"
version = "0.1-SNAPSHOT"
val dateFormat = SimpleDateFormat("yyyy.MM.dd-hh-mm")
dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("Europe/Oslo"))
val gitHash = System.getenv("CIRCLE_SHA1") ?: "local-build"
group = "no.nav.helse.xml"
version = "${dateFormat.format(Date())}-$gitHash"
val javaTimeAdapterVersion = "1.1.3"

plugins {
    kotlin("jvm") version "1.3.41"
    "maven-publish"
    signing
    id("io.codearte.nexus-staging") version "0.21.0"
    id("de.marcphilipp.nexus-publish") version "0.2.0"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.migesok", "jaxb-java-time-adapters", javaTimeAdapterVersion)
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

tasks {
    create("printVersion") {
        doLast {
            println(project.version)
        }
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
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