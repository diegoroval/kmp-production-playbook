package dev.playbook.securestorage

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class SecureStorageContract {

    abstract fun storage(): SecureStorage

    private val key = "test_token"

    @AfterTest
    fun cleanUp() = storage().remove(key)

    @Test
    fun returnsNullWhenMissing() = assertNull(storage().get(key))

    @Test
    fun roundTrip() {
        storage().put(key, "token-ñandú-🔐")
        assertEquals("token-ñandú-🔐", storage().get(key))
    }

    @Test
    fun overwrite() {
        storage().put(key, "v1")
        storage().put(key, "v2")
        assertEquals("v2", storage().get(key))
    }

    @Test
    fun remove() {
        storage().put(key, "v1")
        storage().remove(key)
        assertNull(storage().get(key))
    }

    @Test
    fun survivesNewInstance() {
        storage().put(key, "persistente")
        assertEquals("persistente", storage().get(key))
    }
}
