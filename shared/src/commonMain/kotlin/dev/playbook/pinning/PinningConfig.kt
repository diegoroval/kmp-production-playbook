package dev.playbook.pinning

import io.ktor.client.HttpClient

class PinningConfig(val pins: Map<String, List<String>>) {
    init {
        require(pins.isNotEmpty()) { "sin hosts no hay nada que fijar" }
        pins.forEach { (host, hashes) ->
            hashes.forEach { require(PIN.matches(it)) { "pin inválido para $host: $it" } }
            // con un solo pin, la próxima rotación de llave deja a la app sin red
            require(hashes.toSet().size >= 2) {
                "$host necesita al menos 2 pines distintos: el actual y uno de respaldo"
            }
        }
    }

    private companion object {
        val PIN = Regex("^sha256/[A-Za-z0-9+/]{43}=$")
    }
}

expect fun pinnedHttpClient(config: PinningConfig): HttpClient
