plugins {
    application
    id("edu.sc.seis.launch4j") version "3.0.5"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

application {
    mainClass.set("_Benefices_Up.App")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

launch4j {
    mainClassName = "_Benefices_Up.App"
    outfile = "Benefices_Up.exe"
    productName = "Benefices Up"
    // Version minimale de Java requise
    jreMinVersion = "21"
    // NE PAS mettre bundledJrePath/bundledJre64Bit avec cette version du plugin
}
