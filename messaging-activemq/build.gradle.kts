
plugins {
    kotlin("jvm")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.apache.activemq:activemq-broker:5.15.4")

    testCompile(kotlin("test-junit"))
    testRuntime(kotlin("reflect"))
    testCompile("org.assertj:assertj-core:3.10.0")
    testCompile("net.wuerl.kotlin:assertj-core-kotlin:0.2.1")
    testCompile("org.awaitility:awaitility:3.1.1")

    compile("io.github.microutils:kotlin-logging:1.5.4")

    implementation("ch.qos.logback:logback-classic:1.2.3")

    compile("org.springframework.boot:spring-boot-starter-activemq")

}