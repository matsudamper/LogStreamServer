package net.matsudamper.logstream.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Log(
    @SerialName("page_id") val pageId: String,
    @SerialName("logs") val logs: List<String>,
)