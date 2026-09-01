import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

val roomDeckSigningFile = rootProject.file("keystore.properties")
val roomDeckSigningProperties = Properties()
val roomDeckSigningEnvironment = mapOf(
    "storeFile" to "ROOMDECK_STORE_FILE",
    "storePassword" to "ROOMDECK_STORE_PASSWORD",
    "keyAlias" to "ROOMDECK_KEY_ALIAS",
    "keyPassword" to "ROOMDECK_KEY_PASSWORD",
)

if (roomDeckSigningFile.isFile) {
    roomDeckSigningFile.inputStream().use { roomDeckSigningProperties.load(it) }
} else {
    val suppliedEnvironmentKeys = roomDeckSigningEnvironment.filterValues {
        !System.getenv(it).isNullOrBlank()
    }
    require(suppliedEnvironmentKeys.isEmpty() || suppliedEnvironmentKeys.size == roomDeckSigningEnvironment.size) {
        "Production signing requires all ROOMDECK signing environment variables or none of them."
    }
    roomDeckSigningEnvironment.forEach { (propertyName, environmentName) ->
        System.getenv(environmentName)?.takeIf { it.isNotBlank() }?.let {
            roomDeckSigningProperties.setProperty(propertyName, it)
        }
    }
}

if (roomDeckSigningProperties.isNotEmpty()) {
    val requiredKeys = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
    val missingKeys = requiredKeys.filter { roomDeckSigningProperties.getProperty(it).isNullOrBlank() }
    require(missingKeys.isEmpty()) {
        "keystore.properties is missing required entries: ${missingKeys.joinToString()}"
    }

    val configuredStore = rootProject.file(roomDeckSigningProperties.getProperty("storeFile"))
    require(configuredStore.isFile) {
        "The production keystore configured by keystore.properties does not exist."
    }
}

extra["roomDeckSigningProperties"] = roomDeckSigningProperties
extra["roomDeckSigningEnabled"] = roomDeckSigningProperties.isNotEmpty()
