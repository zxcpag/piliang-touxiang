package com.example.avatarbatchsetter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AvatarDao {

    @Insert
    suspend fun insert(avatar: Avatar): Long

    @Query("SELECT * FROM avatars ORDER BY addedAt DESC")
    suspend fun getAll(): List<Avatar>

    @Query("SELECT * FROM avatars ORDER BY RANDOM() LIMIT 1")
    suspend fun random(): Avatar?

    @Query("SELECT COUNT(*) FROM avatars")
    suspend fun count(): Int

    @Delete
    suspend fun delete(avatar: Avatar)
}
