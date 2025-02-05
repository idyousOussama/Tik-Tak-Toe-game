package com.example.tictachero.DataBases.RoomDatabases.Daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tictachero.Models.Player

@Dao
interface PlayerDao {
    @Insert
    fun insertNewPlayer(player : Player)

    @Query("select * from player")
    fun getAllPlayerAccount() : List<Player>

    @Query("Select * from player where isLogainedAccount = 1")
    fun  getPlayerLogainedAccount() : Player

    @Query("update player set playerDiamond = playerDiamond + :diamond where playerId = :id ")
 fun increasePlayerDiamod(diamond : Int , id : String)

    @Query("update player set playerWonNumber = playerWonNumber + 1 where playerId = :id ")
    fun playerIsWon( id : String)

    @Query("update player set userLoseNumber= userLoseNumber + 1 where playerId = :id ")
    fun playerIsLose(id : String)

}