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
    copyright =
        "© 2025 UP"
    // commente si tu n'as pas d'icône
    // icon = "${projectDir}/lib/icon.ico"
}
