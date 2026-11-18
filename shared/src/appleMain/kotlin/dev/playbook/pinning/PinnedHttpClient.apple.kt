package dev.playbook.pinning

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner

actual fun pinnedHttpClient(config: PinningConfig): HttpClient = HttpClient(Darwin) {
    engine {
        val pinner = CertificatePinner.Builder()
        config.pins.forEach { (host, hashes) -> hashes.forEach { pinner.add(host, it) } }
        handleChallenge(pinner.build())
    }
}
