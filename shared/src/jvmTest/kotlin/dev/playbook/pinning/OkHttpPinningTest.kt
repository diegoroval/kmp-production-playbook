package dev.playbook.pinning

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.CertificatePinner
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import java.net.InetAddress
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// servidor TLS local con una CA propia: prueba el pinning real sin depender de internet
class OkHttpPinningTest {
    private val ca = HeldCertificate.Builder().certificateAuthority(0).build()
    private val server = MockWebServer()
    private val loopback = InetAddress.getByName("127.0.0.1")

    @AfterTest
    fun cerrar() = server.close()

    @Test
    fun pinCorrectoConecta() {
        val llave = servidorCon(nuevaLlave())
        assertEquals("ok", llamar(clienteCon(CertificatePinner.pin(llave.certificate), PIN_B)))
    }

    @Test
    fun pinesIncorrectosCortanLaConexion() {
        servidorCon(nuevaLlave())
        assertFailsWith<SSLPeerUnverifiedException> { llamar(clienteCon(PIN_A, PIN_B)) }
    }

    @Test
    fun rotacionConPinDeRespaldoSigueFuncionando() {
        val actual = nuevaLlave()
        val respaldo = nuevaLlave()
        val cliente = clienteCon(CertificatePinner.pin(actual.certificate), CertificatePinner.pin(respaldo.certificate))

        // el servidor ya rotó a la llave de respaldo; la app publicada no se entera
        servidorCon(respaldo)
        assertEquals("ok", llamar(cliente))
    }

    private fun nuevaLlave() = HeldCertificate.Builder().addSubjectAlternativeName("localhost").signedBy(ca).build()

    private fun servidorCon(llave: HeldCertificate): HeldCertificate {
        val certificados = HandshakeCertificates.Builder().heldCertificate(llave, ca.certificate).build()
        server.useHttps(certificados.sslSocketFactory())
        server.enqueue(MockResponse.Builder().body("ok").build())
        server.start(loopback, 0)
        return llave
    }

    private fun clienteCon(vararg pines: String): HttpClient {
        val confianza = HandshakeCertificates.Builder().addTrustedCertificate(ca.certificate).build()
        return pinnedOkHttpClient(PinningConfig(mapOf("localhost" to pines.toList()))) {
            sslSocketFactory(confianza.sslSocketFactory(), confianza.trustManager)
            // localhost resuelve a IPv4 e IPv6; sin esto OkHttp reintenta por la otra y tapa el error de pinning
            dns { listOf(loopback) }
        }
    }

    private fun llamar(cliente: HttpClient) = runBlocking {
        cliente.use { it.get(server.url("/").toString()).bodyAsText() }
    }
}
