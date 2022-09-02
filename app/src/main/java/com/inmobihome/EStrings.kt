package com.inmobihome

fun String?.toIntOrDefault(default: Int = 0): Int {
    return this?.toIntOrNull() ?: default
}