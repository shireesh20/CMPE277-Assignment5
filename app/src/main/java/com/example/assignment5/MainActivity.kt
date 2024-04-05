package com.example.assignment5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.assignment5.ui.theme.Assignment5Theme
import kotlinx.coroutines.launch
import com.example.assignment5.network.OpenAIService
import com.example.assignment5.network.OpenAIRequest
import com.example.assignment5.network.Message
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.assignment5.database.AppDatabase
import com.example.assignment5.entities.AuditPrompt
import com.example.assignment5.entities.Responses


class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "chat-database"
        ).build()

        setContent {
            Assignment5Theme {
                AppContent(db)
            }
        }
    }
}

fun createRetrofitService(): OpenAIService {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(OpenAIService::class.java)
}

@Composable
fun AppContent(db: AppDatabase?) {
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Prompt") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp)) // Provides space between the text field and the buttons
        Row(
            modifier = Modifier.fillMaxWidth(), // This will make the Row fill the max width
            horizontalArrangement = Arrangement.spacedBy(8.dp) // This adds space between the buttons
        ) {
            Button(
                onClick = {
                    scope.launch {
                        response = callOpenAI(prompt)
                    }
                },
                modifier = Modifier.weight(1f) // This makes the button take up half the space
            ) {
                Text("Send")
            }
            Button(
                onClick = {
                    prompt = "" // Clears the prompt
                    response = "" // Also clears the response if needed
                },
                modifier = Modifier.weight(1f) // This makes the button take up half the space
            ) {
                Text("Cancel")
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Provides space between the buttons and the response
        Button(
            onClick = {
                scope.launch {
                    db?.let { database ->
                        savePromptAndResponse(database, prompt, response)
                    }
                }
            }
        ) {
            Text("Save")
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Text(response, modifier = Modifier.padding(8.dp).fillMaxWidth())
        }
    }
}

suspend fun savePromptAndResponse(db: AppDatabase, prompt: String, response: String) {
    val currentTime = System.currentTimeMillis().toString() // Example timestamp

    // Construct the entities
    val auditPrompt = AuditPrompt(sequenceNumber = 0, dateTime = currentTime, prompt = prompt)
    val responses = Responses(sequenceNumber = 0, dateTime = currentTime, response = response)

    // Insert entities into the database
    db.chatDao().insertAuditPrompt(auditPrompt)
    db.chatDao().insertResponse(responses)
}


suspend fun callOpenAI(prompt: String): String {
    val service = createRetrofitService() // Ensure this method exists in your MainActivity
    val requestBody = OpenAIRequest(
        model = "gpt-3.5-turbo",
        messages = listOf(
            Message(role = "user", content = prompt) // 'prompt' contains the user input
        )
    )

    return try {
        val response = service.createCompletion(requestBody)
        if (response.isSuccessful && response.body() != null) {
            val responseBody = response.body()
            if (responseBody != null && responseBody.choices.isNotEmpty()) {
                val assistantMessage = responseBody.choices.first().message.content.trim()
                if (assistantMessage.isNotEmpty()) {
                    assistantMessage
                } else {
                    "The assistant's response was empty."
                }
            } else {
                "Received an empty response. Response body: $responseBody"
            }
        } else {
            "Error: ${response.errorBody()?.string()}"
        }
    } catch (e: Exception) {
        "Failed to connect to the API: ${e.localizedMessage}"
    }
}





@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    Assignment5Theme {
        AppContent(db = null)
    }
}
