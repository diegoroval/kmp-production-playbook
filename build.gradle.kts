plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/detekt.yml"))
        buildUponDefaultConfig = true
        // sin esto detekt no encuentra los sources de KMP (NO-SOURCE)
        source.setFrom(fileTree("src") { include("**/kotlin/**") })
    }
}
