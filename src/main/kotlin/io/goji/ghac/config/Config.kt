package io.goji.ghac.config

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import io.goji.io.goji.ghac.model.CIEnvironment
import io.goji.io.goji.ghac.model.MediaConfig
import io.goji.io.goji.ghac.model.MessageConfig
import io.goji.io.goji.ghac.model.Settings
import java.io.File

data class Config(
//    val github: Boolean = false,
    val token: String,
    val recipients: List<String>,
    val message: MessageConfig,
    val media: MediaConfig,
    val settings: Settings,
    val ci: CIEnvironment

)


//class CfgLoader {
//    fun load(envFile: String? = null): Config {
//        val sources = mutableListOf<PropertySource>()
//
//        // Load from environment file if specified
//        envFile?.let { file ->
//            if (File(file).exists()) {
//                sources.add(PropertySource.file(File(file), optional = true))
//            }
//        }
//
//        // Add environment variables source
//        sources.add(PropertySource.environment())
//
//
//        return ConfigLoader().loadConfigOrThrow<Config>(sources)
//    }
//}


private fun loadConfig(): Config {
    val configLoader = ConfigLoader()

    // Try loading from environment variables first
    System.getenv("PLUGIN_ENV_FILE")?.let { filename ->
        File(filename).takeIf { it.exists() }?.let { file ->
            return configLoader.loadConfigOrThrow(file.readText())
        }
    }
    // Then try loading from the default file
    return configLoader.loadConfigOrThrow("/config.yaml")
}


