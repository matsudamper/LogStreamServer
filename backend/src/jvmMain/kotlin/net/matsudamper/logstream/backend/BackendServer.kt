package net.matsudamper.logstream.backend

import java.security.KeyStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveStream
import io.ktor.server.response.*
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

public class BackendServer(
    private val alias: String,
    private val privateKeyPass: String,
    private val logServer: ILogServer,
    private val jksFilePass: String,
) {
    private var engine: ApplicationEngine? = null

    public fun start(caKeyStore: KeyStore, iPv4Address: List<String>) {
        val serverKeyStore = KeyUtil.createServerKeyStore(
            alias = alias,
            pass = privateKeyPass,
            caKeyStore = caKeyStore,
            ipV4Address = iPv4Address,
        )
        engine = embeddedServer(
            Netty,
            environment = applicationEngineEnvironment {
                connector {
                    port = 80
                }
                sslConnector(
                    keyStore = serverKeyStore,
                    keyAlias = alias,
                    keyStorePassword = { jksFilePass.toCharArray() },
                    privateKeyPassword = { privateKeyPass.toCharArray() },
                ) {
                    port = 443
                }
                module {
                    myApplicationModule(
                        pem = KeyUtil.convertPem(alias, caKeyStore),
                        logServer = logServer,
                    )
                }
            },
            configure = {

            },
        ).start(wait = false)
    }

    public fun stop() {
        engine?.stop(1000, 1000)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun Application.myApplicationModule(
    pem: String,
    logServer: ILogServer,
) {
    val json = Json
    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
        )
    }

    routing {
        get("healthz") {
            call.respondText(
                contentType = ContentType.Text.Plain,
            ) {
                "OK"
            }
        }
        get("key.pem") {
            call.respondText(
                contentType = ContentType.Application.OctetStream,
                status = HttpStatusCode.OK,
            ) {
                pem
            }
        }
        post("/log") {
            val log = json.decodeFromStream<Log>(call.receiveStream())
            logServer.receiveLog(log)
        }
    }
}
