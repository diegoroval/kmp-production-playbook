package dev.playbook.securestorage

interface SecureStorage {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun remove(key: String)
}
