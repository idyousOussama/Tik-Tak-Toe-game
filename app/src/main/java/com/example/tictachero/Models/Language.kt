package com.example.tictachero.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languages")
data class Language (
    @PrimaryKey(autoGenerate = true)
    val languageId : Int,
    var title : String ,
    var flage : Int,
    var languageSymbol : String ,
    var isSelected : Boolean
)