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

// Tu peux garder la toolchain (pour compiler) ou l'enlever.
// Ici on la garde en Java 21 :
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

    // JDK embarqué : dossier "jdk" à côté de l'exe
    bundledJrePath = "jdk"

    // Optionnel : version minimale
    jreMinVersion = "21"
}

