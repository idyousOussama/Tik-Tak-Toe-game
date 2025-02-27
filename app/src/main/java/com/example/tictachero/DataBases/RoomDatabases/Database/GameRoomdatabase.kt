package com.example.tictachero.DataBases.RoomDatabases.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tictachero.DataBases.RoomDatabases.Daos.LanguageDao
import com.example.tictachero.DataBases.RoomDatabases.Daos.PlayerDao
import com.example.tictachero.Models.Language
import com.example.tictachero.Models.Player
@Database(entities = arrayOf(Player::class , Language::class) , version = 3, exportSchema = false)
abstract  class GameRoomdatabase : RoomDatabase () {
        abstract fun playerDao(): PlayerDao
        abstract fun languageDao():LanguageDao
    companion object {
        @Volatile
            private var INSTANCE: GameRoomdatabase? = null
            fun getDatabase(context: Context): GameRoomdatabase {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Room.databaseBuilder(
                        context.applicationContext,
                        GameRoomdatabase::class.java,
                        "kjlnkfuyjkkngsddrfsdegl")
                        .build().also { INSTANCE = it
                        }
                }
            }
        }
    }
