package com.royyan.myandroidwebproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
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
    
    // 0..3 index of the correct answer
    var correctAnswerIndex by remember { mutableStateOf(0) }
    
    var expandedCategory by remember { mutableStateOf(false) }

    // Fetch categories on launch
    LaunchedEffect(Unit) {
        try {
            // "pvpCateogry" collection from user description
            val snapshot = Firebase.firestore.collection("pvpCateogry").get()
            val loaded = snapshot.documents.map { doc ->
                // Document ID is the name (e.g. "Таухид")
                val name = doc.id
                // Field "id" is the numeric ID
                val data = doc.data<CategoryData>() 
                name to data.id
            }
            categories = loaded
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Ошибка при загрузке категорий: ${e.message}"
            isLoading = false
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
                label = { Text("Текст вопроса (Question Answer)") },
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

                            // "pvpQuestions" collection
                            Firebase.firestore.collection("pvpQuestions").add(newQuestion)

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
