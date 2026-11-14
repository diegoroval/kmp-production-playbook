package dev.playbook.securestorage

import androidx.test.platform.app.InstrumentationRegistry

class KeystoreSecureStorageTest : SecureStorageContract() {
    override fun storage(): SecureStorage =
        KeystoreSecureStorage(InstrumentationRegistry.getInstrumentation().targetContext, name = "test")
}
