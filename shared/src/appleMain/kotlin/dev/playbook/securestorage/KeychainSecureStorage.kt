package dev.playbook.securestorage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KeychainSecureStorage(private val service: String = "dev.playbook.secure_storage") : SecureStorage {

    override fun get(key: String): String? = withQuery(
        key,
        kSecReturnData to kCFBooleanTrue,
        kSecMatchLimit to kSecMatchLimitOne,
    ) { query ->
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            if (status == errSecItemNotFound) return@memScoped null
            check(status == errSecSuccess) { "Keychain read falló: $status" }
            val data = CFBridgingRelease(result.value) as NSData
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        }
    }

    override fun put(key: String, value: String) {
        remove(key)
        val data = CFBridgingRetain(NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding))
        try {
            withQuery(
                key,
                kSecValueData to data,
                kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            ) {
                val status = SecItemAdd(it, null)
                check(status == errSecSuccess) { "Keychain write falló: $status" }
            }
        } finally {
            CFRelease(data)
        }
    }

    override fun remove(key: String) {
        withQuery(key) {
            val status = SecItemDelete(it)
            check(status == errSecSuccess || status == errSecItemNotFound) { "Keychain delete falló: $status" }
        }
    }

    private fun <T> withQuery(
        key: String,
        vararg extra: Pair<CFTypeRef?, CFTypeRef?>,
        block: (CFMutableDictionaryRef?) -> T,
    ): T {
        val cfService = CFBridgingRetain(service)
        val cfAccount = CFBridgingRetain(key)
        val query = CFDictionaryCreateMutable(
            null,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        )
        try {
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, cfService)
            CFDictionaryAddValue(query, kSecAttrAccount, cfAccount)
            extra.forEach { (k, v) -> CFDictionaryAddValue(query, k, v) }
            return block(query)
        } finally {
            CFRelease(query)
            CFRelease(cfAccount)
            CFRelease(cfService)
        }
    }
}
