package com.example.assignment5.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "responses")
data class Responses(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sequenceNumber: Int,
    val dateTime: String,
    val response: String
)
