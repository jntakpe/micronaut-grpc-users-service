import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.cloud.tools.jib.gradle.JibTask
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import io.micronaut.gradle.MicronautRuntime.NONE
import io.micronaut.gradle.MicronautTestRuntime.JUNIT_5
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.util.prefixIfNot
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

val commonsVersion: String by project
val kotlinVersion: String by project
val micronautVersion: String by project
val kMongoVersion: String by project
val basePackage = "com.github.jntakpe"
val protoDescriptorPath = "$buildDir/generated/proto.pb"
val grpcServices = listOf("users.UsersService")
val grpcHealthProbeDir = "$buildDir/tmp/bin/"
val probeExecName = "grpc_health_probe"

plugins {
    idea
    `maven-publish`
    jacoco
    val kotlinVersion = "1.4.20"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.google.protobuf") version "0.8.13"
    id("io.micronaut.application") version "1.2.0"
    id("com.google.cloud.tools.jib") version "2.7.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

version = "0.1.9-RC2"
group = "com.github.jntakpe"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    mavenGithub("equidis/commons")
}

micronaut {
    runtime(NONE)
    testRuntime(JUNIT_5)
    processing {
        incremental(true)
        module(project.name)
        group(project.group.toString())
        annotations("$basePackage.*")
    }
}

dependencies {
    kapt(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("org.litote.kmongo:kmongo-annotation-processor:$kMongoVersion")
    implementation(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("com.github.jntakpe:commons-cache:$commonsVersion")
    implementation("com.github.jntakpe:commons-grpc:$commonsVersion")
    implementation("com.github.jntakpe:commons-management:$commonsVersion")
    implementation("com.github.jntakpe:commons-micronaut:$commonsVersion")
    implementation("com.github.jntakpe:commons-mongo:$commonsVersion")
    implementation("com.github.jntakpe:commons-tracing:$commonsVersion")
    runtimeOnly("ch.qos.logback:logback-classic")
    kaptTest(enforcedPlatform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")
    testImplementation("com.github.jntakpe:commons-cache-test:$commonsVersion")
    testImplementation("com.github.jntakpe:commons-mongo-test:$commonsVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("$basePackage.users.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    main {
        java {
            val dirs = listOf("grpc", "java", "javapgv", "reactor")
            srcDirs(dirs.map { it.prefixIfNot("build/generated/source/proto/main/") })
        }
    }
}

protobuf {
    val grpcId = "grpc"
    val javaPgvId = "javapgv"
    val reactorId = "reactor"
    val krotoId = "kroto"
    protoc {
        artifact = "com.google.protobuf:protoc:3.14.0"
    }
    plugins {
        id(grpcId) {
            artifact = "io.grpc:protoc-gen-grpc-java:1.33.1"
        }
        id(javaPgvId) {
            artifact = "io.envoyproxy.protoc-gen-validate:protoc-gen-validate:0.4.1"
        }
        id(reactorId) {
            artifact = "com.salesforce.servicelibs:reactor-grpc:1.0.1"
        }
        id(krotoId) {
            artifact = "com.github.marcoferrer.krotoplus:protoc-gen-kroto-plus:0.6.1"
        }
    }
    generateProtoTasks {
        val krotoConfig = file("kroto-config.yaml")
        all().forEach {
            it.generateDescriptorSet = true
            it.descriptorSetOptions.includeImports = true
            it.descriptorSetOptions.path = protoDescriptorPath
            it.inputs.files(krotoConfig)
            it.plugins {
                id(grpcId)
                id(javaPgvId) {
                    option("lang=java")
                }
                id(reactorId)
                id(krotoId) {
                    outputSubDir = "java"
                    option("ConfigPath=${krotoConfig.name}")
                }
            }
        }
    }
}

jib {
    to {
        image = "eu.gcr.io/equidis/micronaut-users:${project.version}"
    }
    extraDirectories {
        paths {
            path {
                setFrom(grpcHealthProbeDir)
                into = "/bin"
            }
        }
        permissions = mapOf("/bin/${probeExecName}" to "755")
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            javaParameters = true
        }
    }
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.isEnabled = true
        }
        classDirectories.setFrom(
            sourceSets.main.get().output.asFileTree.matching {
                exclude("build/generated", "**/model/entity/**")
            }
        )
    }
    check {
        dependsOn(jacocoTestReport)
    }
    val deploymentZip = register<Zip>("deploymentZip") {
        val path = Paths.get("$buildDir/generated/build-metadata.yaml")
        doFirst {
            if (!Files.exists(path)) Files.createFile(path)
            Files.writeString(
                path,
                """
        app:
          name: ${project.name}
          version: ${project.version}
        image:
          name: micronaut-${project.name}
        api:
          services: ${grpcServices.joinToString(prefix = "[", postfix = "]")}
    """.trimIndent(), StandardOpenOption.SYNC
            )
        }
        archiveFileName.set("deployment-metadata.zip")
        destinationDirectory.set(Paths.get(buildDir.toString(), "distributions").toFile())
        from(path.toString(), protoDescriptorPath)
    }
    assemble {
        dependsOn(deploymentZip)
    }
    withType<JibTask> {
        doFirst { downloadHealthProbeBinary() }
    }
}
val protoJar = tasks.register<Jar>("protoJar") {
    dependsOn(tasks.jar)
    from("src/main/proto", protoDescriptorPath)
    archiveClassifier.set("proto")
}
val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    dependsOn(protoJar.name)
    mergeServiceFiles()
}

publishing {
    publications {
        create<MavenPublication>("mavenProto") {
            artifact(tasks.getByName(protoJar.name))
        }
    }
    repositories {
        mavenGithub("equidis/micronaut-grpc-users-service")
    }
}

fun RepositoryHandler.mavenGithub(repository: String) = maven {
    name = "Github_packages"
    setUrl("https://maven.pkg.github.com/$repository")
    credentials {
        val githubActor: String? by project
        val githubToken: String? by project
        username = githubActor
        password = githubToken
    }
}

fun downloadHealthProbeBinary() {
    val probeVersion = "v0.3.5"
    val url = URL("https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${probeVersion}/${probeExecName}-linux-amd64")
    Files.createDirectories(Paths.get(grpcHealthProbeDir))
    Files.copy(url.openStream(), Paths.get(grpcHealthProbeDir, probeExecName), StandardCopyOption.REPLACE_EXISTING)
}
