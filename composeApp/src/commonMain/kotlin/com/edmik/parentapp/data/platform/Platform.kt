package com.edmik.parentapp.data.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform