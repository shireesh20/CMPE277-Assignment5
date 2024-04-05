package com.example.assignment5.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.assignment5.dao.ChatDao
import com.example.assignment5.entities.AuditPrompt
import com.example.assignment5.entities.Responses

@Database(entities = [AuditPrompt::class, Responses::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
