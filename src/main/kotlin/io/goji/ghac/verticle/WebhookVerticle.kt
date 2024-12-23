package io.goji.ghac.verticle

import io.goji.ghac.config.Config
import io.goji.ghac.plugin.TemplateEngine
import io.goji.io.goji.ghac.service.TelegramService
import io.goji.io.goji.ghac.service.WebhookHandler
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.coroutineRouter
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory


// WebhookVerticle.kt
class WebhookVerticle(private val webhookConfig: Config) : CoroutineVerticle() {
    private val logger = LoggerFactory.getLogger(WebhookVerticle::class.java)

    override suspend fun start() {
        val templateEngine = TemplateEngine()
        val telegramService = TelegramService(webhookConfig,templateEngine)
        val webhookHandler = WebhookHandler(telegramService, webhookConfig)


        val router = Router.router(vertx)

        router.route().handler(BodyHandler.create())

        coroutineRouter {
            router.post("/webhook")
                .coHandler { ctx ->
//                    supervisorScope {
//                        // Launch child coroutines if needed
//                        val result = async { webhookHandler.handle(ctx) }
//                        result.await()
//                    }

                    webhookHandler.handle(ctx)
                }
                .coFailureHandler { ctx ->
                logger.debug("Error processing webhook: {}",ctx.failure().message)
                ctx.response()
                    .setStatusCode(500)
                    .end("Internal server error")
                }
        }

        // use launch
//        router.post("/webhook").handler { ctx ->
//            launch(ctx.vertx().dispatcher()) {
//                try {
//                    webhookHandler.handle(ctx)
//                } catch (error: Exception) {
//                    logger.error("Error processing webhook", error)
//                    ctx.response()
//                        .setStatusCode(500)
//                        .end("Internal server error")
//                }
//            }
//        }

        val server = vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .coAwait()

        logger.info("Server started on port ${server.actualPort()}")
    }

    private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit): Route {
        return handler { ctx ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }
}

