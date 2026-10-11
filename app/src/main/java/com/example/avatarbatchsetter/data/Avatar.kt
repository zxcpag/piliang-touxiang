package com.example.avatarbatchsetter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avatars")
data class Avatar(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis()
)
