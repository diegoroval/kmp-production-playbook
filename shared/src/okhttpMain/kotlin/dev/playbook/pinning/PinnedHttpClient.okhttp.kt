package dev.playbook.pinning

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

actual fun pinnedHttpClient(config: PinningConfig): HttpClient = pinnedOkHttpClient(config)

internal fun pinnedOkHttpClient(
    pinning: PinningConfig,
    extra: OkHttpClient.Builder.() -> Unit = {},
): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            val pinner = CertificatePinner.Builder()
            pinning.pins.forEach { (host, hashes) -> hashes.forEach { pinner.add(host, it) } }
            certificatePinner(pinner.build())
            extra()
        }
    }
}
