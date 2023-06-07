package net.matsudamper.logstream.frontend.storage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.matsudamper.logstream.frontend.Constant

@Serializable
public data class Config(
    @SerialName("caKeyPath") val caKeyPath: String = Constant.keyFileName,
)