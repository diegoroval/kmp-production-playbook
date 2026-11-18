package dev.playbook.pinning

import kotlin.test.Test
import kotlin.test.assertFailsWith

class PinningConfigTest {
    @Test
    fun aceptaPinActualMasRespaldo() {
        PinningConfig(mapOf("api.example.com" to listOf(PIN_A, PIN_B)))
    }

    @Test
    fun rechazaUnSoloPin() {
        assertFailsWith<IllegalArgumentException> {
            PinningConfig(mapOf("api.example.com" to listOf(PIN_A)))
        }
    }

    @Test
    fun rechazaElMismoPinRepetido() {
        assertFailsWith<IllegalArgumentException> {
            PinningConfig(mapOf("api.example.com" to listOf(PIN_A, PIN_A)))
        }
    }

    @Test
    fun rechazaPinesQueNoSonSha256() {
        assertFailsWith<IllegalArgumentException> {
            PinningConfig(mapOf("api.example.com" to listOf("sha1/AAAAAAAAAAAAAAAAAAAAAAAAAAA=", PIN_B)))
        }
    }

    @Test
    fun rechazaConfigVacia() {
        assertFailsWith<IllegalArgumentException> { PinningConfig(emptyMap()) }
    }
}

internal const val PIN_A = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
internal const val PIN_B = "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
