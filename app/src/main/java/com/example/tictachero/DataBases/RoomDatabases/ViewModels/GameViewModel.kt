package com.example.tictachero.DataBases.RoomDatabases.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tictachero.DataBases.RoomDatabases.Repositories.GameRepositry
import com.example.tictachero.Models.Language
import com.example.tictachero.Models.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameViewModel (application: Application) : AndroidViewModel(application) {
    val gameRep  = GameRepositry(application)

    suspend fun insertNewPlayerAccount (player : Player) {
        gameRep.insertNewPlayerAccount(player)
    }
    suspend fun getAllPlayerAccounts() : List<Player> {
         return   gameRep.getAllPlayerAccounts()
    }
    suspend fun getPlayerLogainedAccount() : Player {
          return  gameRep.getPlayerLogainedAccount()
    }
    suspend fun increasePlayerDiamod(diamond : Int , id : String) {
            gameRep.increasePlayerDiamod(diamond,id)
    }
    suspend    fun updatePlayerNameById(playerName : String , id :String) {
            gameRep.updatePlayerNameById(playerName,id)
    }
    suspend    fun updatePlayerImageById(playerImage : Int , id :String) {
            gameRep.updatePlayerImageById(playerImage,id)
    }
    suspend fun upDateLoginedAccountById(id:String,islogined : Boolean){
     gameRep.upDateLoginedAccountById(id,islogined)
        }


    suspend fun getAccountById(playerId : String) : Player {
        return    gameRep.getAccountById(playerId)
    }


    suspend   fun insertNewLanguage(language: Language) {
        gameRep.insertNewLanguage(language)

    }
    suspend fun insertLanguageList(languagesList : List<Language>) {
        gameRep.insertLanguageList(languagesList)

    }
    suspend fun getAllLanguages () : List<Language> {
        return gameRep.getAllLanguages()

    }
    suspend fun getSelectLanguage() : Language {
        return gameRep.getSelectLanguage()

    }
    suspend fun upDateSelectedLanguageById(languageId: Int , isSelected : Boolean) {
        gameRep.upDateSelectedLanguageById(languageId , isSelected)
    }













}