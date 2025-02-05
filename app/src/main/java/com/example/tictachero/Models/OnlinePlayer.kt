package com.example.tictachero.Models

import java.io.Serializable

data class OnlinePlayer (var playerId  : String , var ID : String ,
                    var  playerName : String ,
                    var playerImage : Int ,
                    var wonNumber : Int ,
                    var lostNum :  Int ,
                    var daimondNum :  Int ,
                    var notesNum :  Int ,

                    var  playerStatus : PlayerStatus? ,
                     var gameStatus: GameStatus?
) : Serializable {
    constructor() : this ("","","",0,0,0,0,0,null,null)
}



enum class PlayerStatus {
    ONLINE,OFFLINE,INTHEGAME
}