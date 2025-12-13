@file:OptIn(ExperimentalWasmJsInterop::class)

package com.royyan.myandroidwebproject

import kotlin.js.Promise

// Kerakli Wasm turlarini import qilamiz
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlinx.browser.window

// JavaScript funksiyalarini Wasm/Kotlin ga tanishtirish
external fun saveQuestionToFirestore(
    categoryId: String,
    question: String,
    // Array<String> o'rniga JsArray<JsString> ishlatiladi
    answers: JsArray<JsString>,
    correctIndex: Int
): Promise<JsBoolean> // Boolean o'rniga JsBoolean

// Qaytuvchi qiymat ham JsArray<JsString> bo'lishi kerak
external fun getCategoriesFromFirestore(): Promise<JsArray<JsString>>
