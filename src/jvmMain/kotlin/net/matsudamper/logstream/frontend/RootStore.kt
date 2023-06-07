package net.matsudamper.logstream.frontend

import java.io.File
import java.security.KeyStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.matsudamper.logstream.backend.BackendServer
import net.matsudamper.logstream.backend.ILogServer
import net.matsudamper.logstream.backend.Log
import net.matsudamper.logstream.frontend.storage.Config

public object Constant {
    public val keyFileName: String = "ca.crt"
    public val keyAlias: String = "LogStream"
    public val keyPass: String = "passpass"
    public val filePass: String = "passpass"
}

public class RootStore {
    public val logFlow: MutableStateFlow<List<Log>> = MutableStateFlow(listOf())

    private var backendServer: BackendServer? = null
    private val objectMapper = Json {
        encodeDefaults = true
    }
    private val configFile = File("config.json")

    @OptIn(ExperimentalSerializationApi::class)
    public fun getConfig(): Config {

        return runCatching {
            configFile.parentFile?.mkdirs()
            objectMapper.decodeFromStream<Config>(configFile.inputStream())
        }.fold(
            onSuccess = {
                println("success")
                it
            },
            onFailure = {
                println("fail")
                it.printStackTrace()
                Config().also { config ->
                    objectMapper.encodeToString(config).also { json ->
                        println(config)
                        println(json)
                        this.configFile.writeText(json)
                    }
                }
            },
        )
    }

    public fun setConfig(block: (Config) -> Config) {
        val config = block(getConfig())
        objectMapper.encodeToString(config).also { json ->
            configFile.writeText(json)
        }
    }

    public fun startServer(caKeyStore: KeyStore, host: String, filePass: String) {
        val backendServer = BackendServer(
            alias = Constant.keyAlias,
            privateKeyPass = Constant.keyPass,
            jksFilePass = filePass,
            logServer = object : ILogServer {
                override fun receiveLog(log: Log) {
                    logFlow.update {
                        it.plus(log)
                    }
                }
            },
        )

        this.backendServer = backendServer
        backendServer.start(
            caKeyStore = caKeyStore,
            iPv4Address = listOf("127.0.0.1", "0.0.0.0", host),
        )
    }

    public fun stopServer() {
        backendServer?.stop()
        backendServer = null
    }
}
