package com.example.tictachero.DataBases.RoomDatabases.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tictachero.DataBases.RoomDatabases.Repositories.GameRepositry
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

}