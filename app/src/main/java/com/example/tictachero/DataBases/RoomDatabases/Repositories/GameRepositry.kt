package com.example.tictachero.DataBases.RoomDatabases.Repositories

import android.app.Application
import com.example.tictachero.DataBases.RoomDatabases.Database.GameRoomdatabase
import com.example.tictachero.Models.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepositry (application: Application){

    val gameRoomDB = GameRoomdatabase.getDatabase(application)
val playerDao = gameRoomDB.playerDao()


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


}