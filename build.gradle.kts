import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import io.micronaut.gradle.MicronautRuntime.NONE
import io.micronaut.gradle.MicronautTestRuntime.JUNIT_5
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.util.prefixIfNot

val kotlinVersion: String by project
val micronautVersion: String by project
val reactorVersion: String by project
val kMongoVersion: String by project
val grpcPgvVersion: String by project
val grpcServicesVersion: String by project
val grpcReactorVersion: String by project
val junitVersion: String by project
val mockkVersion: String by project
val assertJVersion: String by project
val testContainersVersion: String by project
val basePackage = "com.github.jntakpe.users"

plugins {
    idea
    `maven-publish`
    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.google.protobuf") version "0.8.13"
    id("io.micronaut.application") version "1.0.3"
    id("com.google.cloud.tools.jib") version "2.5.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

version = "0.1.0"
group = "com.github.jntakpe"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
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
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("org.litote.kmongo:kmongo-annotation-processor:$kMongoVersion")
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation(platform("io.projectreactor:reactor-bom:$reactorVersion"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("javax.annotation:javax.annotation-api")
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.grpc:micronaut-grpc-runtime")
    implementation("io.micronaut.mongodb:micronaut-mongo-reactive")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.envoyproxy.protoc-gen-validate:pgv-java-grpc:$grpcPgvVersion")
    implementation("io.grpc:grpc-services:$grpcServicesVersion")
    implementation("com.salesforce.servicelibs:reactor-grpc-stub:$grpcReactorVersion")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.litote.kmongo:kmongo-async-serialization:$kMongoVersion")
    runtimeOnly("ch.qos.logback:logback-classic")
    kaptTest(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.projectreactor:reactor-test:")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:mongodb:$testContainersVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("$basePackage.ApplicationKt")
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
        artifact = "com.google.protobuf:protoc:3.13.0"
    }
    plugins {
        id(grpcId) {
            artifact = "io.grpc:protoc-gen-grpc-java:1.32.1"
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
            it.inputs.files(krotoConfig)
            it.plugins {
                id(grpcId)
                id(javaPgvId) {
                    option("lang=java")
                }
                id(reactorId)
                id(krotoId) {
                    outputSubDir = "java"
                    option("ConfigPath=$krotoConfig")
                }
            }
        }
    }
}

jib {
    to {
        image = "gcr.io/demo/jib-image"
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            javaParameters = true
        }
    }
}
val protoJar = tasks.register<Jar>("protoJar") {
    dependsOn(tasks.jar)
    from("src/main/proto")
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
}
