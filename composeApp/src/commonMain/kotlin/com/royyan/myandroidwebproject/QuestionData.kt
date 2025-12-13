package com.royyan.myandroidwebproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class CategoryData(
    val id: Int = 0
)

@Serializable
data class QuestionData(
    val categoryId: Int,
    val questionAnswer: String,
    val firstAnswer: String,
    val secondAnswer: String,
    val thirdAnswer: String,
    val fourthAnswer: String,
    val correctAnswer: String
)

@Composable
fun AdminPanelScreen() {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // States
    var categories by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Form inputs
    var selectedCategory by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var questionText by remember { mutableStateOf("") }
    var answer1 by remember { mutableStateOf("") }
    var answer2 by remember { mutableStateOf("") }
    var answer3 by remember { mutableStateOf("") }
    var answer4 by remember { mutableStateOf("") }

    var firestore by remember { mutableStateOf<FirebaseFirestore?>(null) }

    // 0..3 index of the correct answer
    var correctAnswerIndex by remember { mutableStateOf(0) }

    var nextQuestionNumber by remember { mutableStateOf(-1) }

    var expandedCategory by remember { mutableStateOf(false) }

    // Initialize Firebase and Fetch categories on launch
    LaunchedEffect(Unit) {
        try {
            try {
                // To'g'ri FirebaseOptions parametrlari
                val options = FirebaseOptions(
                    applicationId = "1:121302335584:web:b79227bd64b2ea28904305", // applicationId birinchi bo'lishi mumkin
                    apiKey = "AIzaSyBNJFDbCMsNn1NDHqmdZlCh5B4wFJtZJko",
                    projectId = "islam-quiz-c31ab",
                    storageBucket = "islam-quiz-c31ab.firebasestorage.app",
                    authDomain = "islam-quiz-c31ab.firebaseapp.com",
                    gcmSenderId = "121302335584"
                    // measurementId ni olib tashlaymiz, chunki FirebaseOptions klassida u yo'q
                )
                Firebase.initialize(context = null, options = options)
            } catch (e: Exception) {
                println("Firebase init warning: ${e.message}")
            }

            // "pvpCategories" (Siz oxirgi marta shunday yozgandingiz)
            firestore = Firebase.firestore
            val snapshot = firestore?.collection("pvpCategories")?.get()
            val loaded = snapshot?.documents?.map { doc ->
                val name = doc.id
                val id = doc.get<Int>("id")
                name to id
            }
            categories = loaded ?: emptyList()
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Ошибка при загрузке категорий: ${e.message}"
            isLoading = false
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Шейх Admin Panel", style = MaterialTheme.typography.headlineMedium)

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            if (successMessage != null) {
                Text(successMessage!!, color = MaterialTheme.colorScheme.primary)
            }

            // Category Dropdown
            Box {
                OutlinedButton(onClick = { expandedCategory = true }) {
                    Text(text = selectedCategory?.let { "${it.first} (ID: ${it.second})" } ?: "Выберите категорию")
                }
                DropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.first} (ID: ${cat.second})") },
                            onClick = {
                                selectedCategory = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // Question Text
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Текст вопроса") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Ответы (Выберите правильный ответ):", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

            // Answers
            val answers = listOf(answer1, answer2, answer3, answer4)
            val setAnswers = listOf(
                { s: String -> answer1 = s },
                { s: String -> answer2 = s },
                { s: String -> answer3 = s },
                { s: String -> answer4 = s }
            )

            answers.forEachIndexed { index, ans ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (correctAnswerIndex == index),
                        onClick = { correctAnswerIndex = index }
                    )
                    OutlinedTextField(
                        value = ans,
                        onValueChange = setAnswers[index],
                        label = { Text("${index + 1}-ответ") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = {
                    if (selectedCategory == null) {
                        errorMessage = "Пожалуйста, выберите категорию"
                        return@Button
                    }
                    if (questionText.isBlank() || answer1.isBlank() || answer2.isBlank() || answer3.isBlank() || answer4.isBlank()) {
                        errorMessage = "Заполните все поля"
                        return@Button
                    }
                    
                    scope.launch {
                        try {
                            errorMessage = null
                            val finalAnswers = listOf(answer1, answer2, answer3, answer4)
                            val correctAnsText = finalAnswers[correctAnswerIndex]

                            val newQuestion = QuestionData(
                                categoryId = selectedCategory!!.second,
                                questionAnswer = questionText,
                                firstAnswer = answer1,
                                secondAnswer = answer2,
                                thirdAnswer = answer3,
                                fourthAnswer = answer4,
                                correctAnswer = correctAnsText
                            )
                            // 1. Oxirgi ID ni aniqlash
                            val questionsSnapshot = firestore?.collection("pvpQuestions")?.get()
                            val documents = questionsSnapshot?.documents ?: emptyList()

                            // Hamma document ID larini Int ga o'tkazib, max ni topamiz.
                            // Agar hech qanday savol bo'lmasa, maxId 0 bo'ladi.
                            val maxId = documents.mapNotNull { doc ->
                                doc.id.toIntOrNull()
                            }.maxOrNull() ?: 0

                            val newId = maxId + 1

                            // "pvpQuestions" collection
                            firestore?.collection("pvpQuestions")?.document(nextQuestionNumber.toString())?.set(newQuestion)

                            successMessage = "Вопрос успешно добавлен!"
                            // Clear form
                            questionText = ""
                            answer1 = ""
                            answer2 = ""
                            answer3 = ""
                            answer4 = ""
                            correctAnswerIndex = 0
                            // selectedCategory qoladi
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                            successMessage = null
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}
