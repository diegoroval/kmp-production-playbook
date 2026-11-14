package dev.playbook

actual fun platformName(): String = "JVM ${System.getProperty("java.version")}"
