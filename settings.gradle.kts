pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application",
                "com.android.library" -> useModule("com.android.tools.build:gradle:8.13.2")
                "org.jetbrains.kotlin.android" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
                "org.jetbrains.kotlin.plugin.compose" -> useModule("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
                "org.jetbrains.kotlin.plugin.serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:2.0.21")
                "org.jetbrains.kotlin.kapt" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DevPulse"

include(":app")
include(":core:common")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:testing")
include(":feature:search")
include(":feature:developer")
include(":feature:repository")
include(":feature:saved")
