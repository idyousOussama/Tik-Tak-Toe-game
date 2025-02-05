package com.example.tictachero.DataBases.RoomDatabases.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tictachero.DataBases.RoomDatabases.Daos.PlayerDao
import com.example.tictachero.Models.Player

@Database(entities = arrayOf(Player::class) , version = 1, exportSchema = false)
abstract  class GameRoomdatabase : RoomDatabase () {
        abstract fun playerDao(): PlayerDao


        companion object {
            @Volatile
            private var INSTANCE: GameRoomdatabase? = null
            fun getDatabase(context: Context): GameRoomdatabase {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Room.databaseBuilder(
                        context.applicationContext,
                        GameRoomdatabase::class.java,
                        "hhkjiojugjoi")
                        .build().also { INSTANCE = it
                        }
                }
            }
        }
    }
