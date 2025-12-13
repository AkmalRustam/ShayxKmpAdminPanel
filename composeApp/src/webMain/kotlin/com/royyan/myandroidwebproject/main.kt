package com.royyan.myandroidwebproject

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        MaterialTheme { // UI chiroyli bo'lishi uchun
            AdminPanelScreen()
        }
    }
}