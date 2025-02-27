package com.example.tictachero.DataBases.RoomDatabases.Repositories

import android.app.Application
import androidx.room.Insert
import androidx.room.Query
import com.example.tictachero.DataBases.RoomDatabases.Database.GameRoomdatabase
import com.example.tictachero.Models.Language
import com.example.tictachero.Models.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepositry (application: Application){

    val gameRoomDB = GameRoomdatabase.getDatabase(application)
val playerDao = gameRoomDB.playerDao()
val languageDao = gameRoomDB.languageDao()


   suspend fun insertNewPlayerAccount (player : Player) {
       withContext(Dispatchers.IO){
           playerDao.insertNewPlayer(player)
       }
   }
  suspend fun getAllPlayerAccounts() : List<Player> {
      return withContext(Dispatchers.IO) {
          playerDao.getAllPlayerAccount()
      }
  }
  suspend fun getPlayerLogainedAccount() : Player {
      return withContext(Dispatchers.IO) {
          playerDao.getPlayerLogainedAccount()
      }
  }
    suspend fun increasePlayerDiamod(diamond : Int , id : String) {
        withContext(Dispatchers.IO) {
            playerDao.increasePlayerDiamod(diamond,id)
        }
    }
suspend    fun updatePlayerNameById(playerName : String , id :String) {
    withContext (Dispatchers.IO) {
        playerDao.updatePlayerNameById(playerName,id)
    }

}

suspend    fun updatePlayerImageById(playerImage : Int , id :String) {
    withContext (Dispatchers.IO) {
        playerDao.updatePlayerImageById(playerImage,id)
    }

}
    suspend fun upDateLoginedAccountById (id:String , islogined : Boolean){
    withContext(Dispatchers.IO) {
        playerDao.upDateLoginedAccountById(id , islogined)
    }

}
    suspend fun getAccountById(playerId : String) : Player {
        return withContext(Dispatchers.IO) {
            playerDao.getAccountById(playerId)
        }
    }





 suspend   fun insertNewLanguage(language: Language) {
        withContext(Dispatchers.IO) {
         languageDao.insertNewLanguage(language)
        }
    }
    suspend fun insertLanguageList(languagesList : List<Language>) {
        withContext(Dispatchers.IO) {
            languageDao.insertLanguageList(languagesList  )
        }
    }
  suspend fun getAllLanguages () : List<Language> {
       return withContext(Dispatchers.IO) {
            languageDao.getAllLanguages()
        }
    }
    suspend fun getSelectLanguage() : Language {
        return withContext(Dispatchers.IO){
            languageDao.getSelectLanguage()
        }
    }
    suspend fun upDateSelectedLanguageById(languageId: Int , isSelected : Boolean) {
        withContext(Dispatchers.IO) {
            languageDao.upDateSelectedLanguageById(languageId , isSelected)
        }
    }











}