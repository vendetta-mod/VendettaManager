package dev.beefers.vendetta.manager.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    @SerialName("tag_name") val versionCode: Int,
    @SerialName("name") val versionName: String
)