package dev.playbook.pinning

import io.ktor.client.request.get
import io.ktor.network.tls.TlsPeerUnverifiedException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

// en Darwin no hay servidor TLS local sencillo: se prueba contra un host real con pines falsos,
// que tiene que fallar siempre aunque el host rote su certificado
class DarwinPinningTest {
    @Test
    fun pinesIncorrectosCortanLaConexion() = runBlocking {
        val cliente = pinnedHttpClient(PinningConfig(mapOf("example.com" to listOf(PIN_A, PIN_B))))
        assertFailsWith<TlsPeerUnverifiedException> {
            cliente.use { it.get("https://example.com") }
        }
        Unit
    }
}
