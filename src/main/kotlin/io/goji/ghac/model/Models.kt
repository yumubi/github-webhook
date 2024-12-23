package io.goji.io.goji.ghac.model

import io.goji.ghac.config.Config



//data class Context(
//    val github: GitHub,
//    val repo: Repo,
//    val commit: Commit,
//    val build: Build,
//    val config: Config,
//    val tpl: Map<String, String> = emptyMap()
//)

data class MessageConfig(
    val text: String? = null,
    val messageFile: String? = null,
    val format: String = "Markdown",
    val templateVars: Map<String, String> = emptyMap(),
    val templateVarsFile: String? = null
)

data class MediaConfig(
    val photos: List<String> = emptyList(),
    val documents: List<String> = emptyList(),
    val stickers: List<String> = emptyList(),
    val audio: List<String> = emptyList(),
    val voice: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val location: List<String> = emptyList(),
    val venue: List<String> = emptyList()
)

data class Settings(
    val debug: Boolean = false,
    val matchEmail: Boolean = false,
    val disableWebPagePreview: Boolean = false,
    val disableNotification: Boolean = false,
    val socks5Proxy: String? = null
)

data class CIEnvironment(
    val repository: Repo,
    val commit: Commit,
    val build: Build,
    val github: GitHub? = null
)



data class GitHub(
    val workflow: String? = null,
    val workspace: String? = null,
    val action: String? = null,
    val eventName: String? = null,
    val eventPath: String? = null
)

data class Repo(
    val fullName: String,
    val namespace: String,
    val name: String
)

data class Commit(
    val sha: String,
    val ref: String,
    val branch: String,
    val link: String?,
    val author: String?,
    val avatar: String?,
    val email: String?,
    val message: String?
)

data class Build(
    val tag: String?,
    val event: String,
    val number: Int,
    val status: String,
    val link: String?,
    val started: Long,
    val finished: Long,
    val pr: String?,
    val deployTo: String?
)

data class Location(
    val title: String = "",
    val address: String = "",
    val latitude: Double,
    val longitude: Double
)
