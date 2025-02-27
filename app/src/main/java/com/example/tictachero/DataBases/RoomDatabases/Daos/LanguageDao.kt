package com.example.tictachero.DataBases.RoomDatabases.Daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tictachero.Models.Language

@Dao
interface LanguageDao {


@Insert
fun insertNewLanguage(language: Language)
@Insert
fun insertLanguageList(languagesList : List<Language>)

@Query("select * from languages")
fun getAllLanguages () : List<Language>

@Query("select * from languages where isSelected = 1  ")
fun getSelectLanguage() : Language

@Query("upDate languages set isSelected = :isSelected where languageId = :languageId")
fun upDateSelectedLanguageById(languageId: Int , isSelected : Boolean)

}