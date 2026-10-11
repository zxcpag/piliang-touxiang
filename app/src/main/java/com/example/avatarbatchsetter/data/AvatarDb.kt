package com.example.avatarbatchsetter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Avatar::class], version = 1, exportSchema = false)
abstract class AvatarDb : RoomDatabase() {

    abstract fun avatarDao(): AvatarDao

    companion object {
        @Volatile
        private var INSTANCE: AvatarDb? = null

        fun get(context: Context): AvatarDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AvatarDb::class.java,
                    "avatar_db"
                ).build().also { INSTANCE = it }
            }
    }
}
