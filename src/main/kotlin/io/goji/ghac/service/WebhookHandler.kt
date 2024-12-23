package io.goji.io.goji.ghac.service

import io.vertx.ext.web.RoutingContext
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.goji.ghac.config.Config
import io.vertx.core.json.JsonObject
import io.vertx.core.parsetools.JsonParser

import org.slf4j.LoggerFactory

class WebhookHandler(
    private val telegramService: TelegramService,
    private val config: Config
) {
    private val logger = LoggerFactory.getLogger(WebhookHandler::class.java)
    private val objectMapper = jacksonObjectMapper()
    private val  parser = JsonParser.newParser()
    suspend fun handle(ctx: RoutingContext) {
        try {
            val payload = ctx.body().asJsonObject()
            val eventType = ctx.request().getHeader("X-GitHub-Event")
            logger.debug("Received GitHub webhook event: {}, payload: {}", eventType, payload)

            //val context = objectMapper.convertValue(payload, Context::class.java)
            parseEvent(payload, eventType)


            ctx.response().setStatusCode(200).end()
        } catch (e: Exception) {
            logger.error("Error processing webhook", e)
            ctx.response()
                .setStatusCode(500)
                .end("Error processing webhook: ${e.message}")
        }
    }


    //todo 分类处理payload, 产生不同的context, 然后发送消息
    private suspend fun parseEvent(payload: JsonObject, eventType: String) {

        return when (eventType) {
            "push" -> handlePushEvent(payload)
            "pull_request" -> handlePullRequestEvent(payload)
            else -> {
                logger.debug("Received unsupported event type: {}", eventType)
            }
        }
    }


    private suspend fun handlePushEvent(payload: JsonObject) {
        val ref = payload.getString("ref")
        val commits = payload.getJsonArray("commits")
        val repository = payload.getJsonObject("repository")
        val pusher = payload.getJsonObject("pusher")
        val sender = payload.getJsonObject("sender")


        logger.debug("Received push event for ref: {} from {} with {} commits", ref, pusher.getString("name"), commits.size())
        //todo
        telegramService.sendMessages(payload)

    }



    private suspend fun handlePullRequestEvent(payload: JsonObject) {
        val action = payload.getString("action")
        val pullRequest = payload.getJsonObject("pull_request")
        val repository = payload.getJsonObject("repository")
        val sender = payload.getJsonObject("sender")
        logger.debug("Received pull request event: {} for PR #{} by {}", action, pullRequest.getInteger("number"), sender.getString("login"))
        telegramService.sendMessages(payload)
        //todo
    }


}
