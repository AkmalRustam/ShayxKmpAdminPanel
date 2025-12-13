package com.royyan.myandroidwebproject

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform