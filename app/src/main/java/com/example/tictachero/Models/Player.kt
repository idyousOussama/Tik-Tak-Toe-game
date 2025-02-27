package com.example.tictachero.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "player")
 data class Player (
    @PrimaryKey
     var playerId : String,
 var ID : String
 ,
  var playerEmail : String?
 ,
     var playerName : String ,
     var playerImage : Int,
     var isLogainedAccount :Boolean ,
     var playerPoints : Int ,
     var playerDiamond : Int ,
     var playerPlayedNumber : Int ,
     var playerWonNumber : Int ,
     var userLoseNumber : Int ,
var PlayerType : PlayerType
 ) : Serializable


enum class PlayerType {
    VISITOR,HOSTER
}