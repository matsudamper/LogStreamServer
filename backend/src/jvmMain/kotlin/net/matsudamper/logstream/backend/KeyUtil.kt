package net.matsudamper.logstream.backend

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Inet4Address
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*
import javax.security.auth.x500.X500Principal
import io.ktor.network.tls.certificates.KeyType
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.extensions.HashAlgorithm
import io.ktor.network.tls.extensions.SignatureAlgorithm


public object KeyUtil {

    public fun restoreCaKeyStore(file: File, pass: String): KeyStore? {
        return if (file.exists()) {
            KeyStore.getInstance(file, pass.toCharArray())
        } else {
            null
        }
    }

    public fun createCaKeyStore(alias: String, pass: String): KeyStore {
        return buildKeyStore {
            certificate(alias) {
                this.hash = HashAlgorithm.valueOf("SHA256")
                this.sign = SignatureAlgorithm.valueOf("RSA")
                this.password = pass
                this.keyType = KeyType.CA
                this.subject = X500Principal("CN=LogStream CA, OU=LogStream, O=matsudamper, C=RU")
                this.domains = listOf("127.0.0.1", "localhost")
            }
        }
    }

    public fun createServerKeyStore(
        alias: String,
        pass: String,
        caKeyStore: KeyStore,
        ipV4Address: List<String>,
    ): KeyStore {
        return buildKeyStore {
            certificate(alias) {
                hash = HashAlgorithm.SHA256
                keyType = KeyType.Server
                password = pass
                subject = X500Principal("CN=LogStreamServer, OU=LogStream, O=matsudamper, C=RU")
                domains = listOf("localhost")
                ipAddresses = ipV4Address.map {
                    Inet4Address.getByName(it)
                }
                signWith(getKeyPair(alias, pass, caKeyStore), getCertificateChain(alias, caKeyStore)[0])
            }
        }
    }

    public fun convertPem(alias: String, x509KeyStore: KeyStore): String {
        return buildString {
            appendLine("-----BEGIN CERTIFICATE-----")
            appendLine(Base64.getMimeEncoder().encodeToString(x509KeyStore.getCertificate(alias).encoded))
            appendLine("-----END CERTIFICATE-----")
        }
    }

    private fun convertP12(alias: String, privateKeyPass: String, filePass: String, x509KeyStore: KeyStore): ByteArray {
        val x509PrivateKey = getKeyPair(alias, privateKeyPass, x509KeyStore).private
        val store = KeyStore.getInstance("PKCS12").also { store ->
            store.load(null, null)

            store.setKeyEntry(
                alias,
                x509PrivateKey,
                privateKeyPass.toCharArray(),
                getCertificateChain(alias, x509KeyStore).toTypedArray(),
            )
        }
        store.store(FileOutputStream("key.p12"), filePass.toCharArray())
        val out = ByteArrayOutputStream()

        store.store(out, filePass.toCharArray())

        return out.toByteArray()
    }

    private fun getCertificateChain(alias: String, x509KeyStore: KeyStore): List<X509Certificate> {
        @Suppress("UNCHECKED_CAST")
        return x509KeyStore.getCertificateChain(alias)
            .toList() as List<X509Certificate>
    }

    private fun getKeyPair(alias: String, privateKeyPass: String, x509KeyStore: KeyStore): KeyPair {
        val certs = getCertificateChain(alias, x509KeyStore).toTypedArray()
        val password = privateKeyPass.toCharArray()
        val pk = x509KeyStore.getKey(alias, password) as PrivateKey
        password.fill('\u0000')
        return KeyPair(
            certs[0].publicKey,
            pk,
        )
    }
}
