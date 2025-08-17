import com.google.protobuf.gradle.id

plugins {
	id("java")
	// https://plugins.gradle.org/plugin/io.freefair.lombok
	id("io.freefair.lombok") version "8.13.1"
	// https://mvnrepository.com/artifact/com.google.protobuf/protobuf-gradle-plugin
	id("com.google.protobuf") version "0.9.5"
}

repositories {
	mavenCentral()
}

val jarName = "Assignment2Gradle"
val jarVersion = "0.1.0"
val mainClass = "de.tub.grpc.SimpleClient"

val junitVersion = "5.11.4" // If we upgrade to >= 5.12, the build breaks
val protobufVersion = "4.30.2"
val grpcVersion = "1.71.0"
val log4jVersion = "2.24.3"

dependencies {
	// Testing
	// https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter
	testImplementation(platform("org.junit:junit-bom:$junitVersion"))
	testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")

	// https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api
	implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
	implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
	implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
	// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
	implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0-rc2")

	// Protobuf
	// https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
	implementation("com.google.protobuf:protobuf-java:$protobufVersion")
	// https://mvnrepository.com/artifact/com.google.protobuf/protobuf-gradle-plugin
	runtimeOnly("com.google.protobuf:protobuf-gradle-plugin:0.9.5")
	if (JavaVersion.current().isJava9Compatible) {
		// https://mvnrepository.com/artifact/org.apache.tomcat/tomcat-annotations-api
		implementation("org.apache.tomcat:tomcat-annotations-api:11.0.6")
	}
	// https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api
	implementation("javax.annotation:javax.annotation-api:1.3.2")

	// GRPC
	// https://mvnrepository.com/artifact/io.grpc/grpc-protobuf
	implementation("io.grpc:grpc-protobuf:$grpcVersion")
	implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
	implementation("io.grpc:grpc-stub:$grpcVersion")
	// https://mvnrepository.com/artifact/com.google.api.grpc/proto-google-common-protos
	implementation("com.google.api.grpc:proto-google-common-protos:2.54.1")

	// HTTP
	// https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
	implementation("org.apache.httpcomponents.client5:httpclient5:5.5-alpha1")

	// https://mvnrepository.com/artifact/commons-codec/commons-codec
	implementation("commons-codec:commons-codec:1.18.0")

	// JSON
	// https://mvnrepository.com/artifact/org.json/json
	implementation("org.json:json:20250107")

	// Get rid of SLF4J warning when using protobuf
	// Remove after adding log4j
	// https://mvnrepository.com/artifact/org.slf4j/slf4j-nop
	// implementation("org.slf4j:slf4j-nop:2.1.0-alpha1")
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:$protobufVersion"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
		}
	}
	generateProtoTasks {
		all().forEach {
			if (it.name.startsWith("generateTestProto")) {
				it.dependsOn(":jar")
			}

			it.plugins {
				id("grpc")
			}
		}
	}
}

sourceSets {
	main {
		java {
			srcDir("build/generated/source/proto/main/java")
			srcDir("build/generated/source/proto/main/grpc")
		}
	}
}

tasks.jar {

	duplicatesStrategy = DuplicatesStrategy.INCLUDE

	archiveBaseName.set(jarName)
	version = jarVersion

	manifest.attributes["Main-Class"] = mainClass

	val dependencies = configurations
		.runtimeClasspath
		.get()
		.map { zipTree(it) }
	from(dependencies)
}

tasks.test {
	useJUnitPlatform()
}
