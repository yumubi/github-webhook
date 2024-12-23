package io.goji

import com.sksamuel.hoplite.ConfigLoader
import io.goji.ghac.config.Config
import io.goji.ghac.verticle.WebhookVerticle
import io.vertx.core.Vertx
import java.io.File

fun main() {
    val config = loadConfig()
    val vertx = Vertx.vertx()
    vertx.deployVerticle(WebhookVerticle(config))


}

private fun loadConfig(): Config {
    val configLoader = ConfigLoader()

    // Try loading from environment variables first
    System.getenv("PLUGIN_ENV_FILE")?.let { filename ->
        File(filename).takeIf { it.exists() }?.let { file ->
            return configLoader.loadConfigOrThrow(file.readText())
        }
    }
    // Then try loading from the default file
    return configLoader.loadConfigOrThrow<Config>("/config.yaml", )
}


