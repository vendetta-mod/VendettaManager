package dev.beefers.vendetta.manager.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Commit(
    val sha: String,
    @SerialName("commit") val info: Info,
    @SerialName("html_url") val url: String,
    val author: User
) {

    @Serializable
    data class Info(
        val message: String
    )

}
