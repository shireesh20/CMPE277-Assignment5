package com.example.assignment5.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.assignment5.entities.AuditPrompt
import com.example.assignment5.entities.Responses

@Dao
interface ChatDao {

    // Insert a new audit prompt into the database
    @Insert
    suspend fun insertAuditPrompt(auditPrompt: AuditPrompt)

    // Insert a new response into the database
    @Insert
    suspend fun insertResponse(response: Responses)

    // Example query to fetch all audit prompts (add if needed for your application)
    @Query("SELECT * FROM audit_prompt")
    suspend fun getAllAuditPrompts(): List<AuditPrompt>

    // Example query to fetch all responses (add if needed for your application)
    @Query("SELECT * FROM responses")
    suspend fun getAllResponses(): List<Responses>

    // Add any additional queries you might need for your application
}
