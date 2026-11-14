package dev.playbook.securestorage

class KeychainSecureStorageTest : SecureStorageContract() {
    override fun storage(): SecureStorage = KeychainSecureStorage(service = "dev.playbook.test")
}
