package com.edmik.parentapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform