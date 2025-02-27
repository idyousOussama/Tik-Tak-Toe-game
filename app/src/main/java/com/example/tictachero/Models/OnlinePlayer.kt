package com.example.tictachero.Models

import java.io.Serializable

data class OnlinePlayer (var playerId  : String , var ID : String ,
                    var  playerName : String ,
                    var  playerEmail : String ,
                    var playerImage : Int ,
                    var wonNumber : Int ,
                    var lostNum :  Int ,
                    var daimondNum :  Int ,
                    var notesNum :  Int ,
                    var  playerStatus : PlayerStatus? ,
                     var gameStatus: GameStatus?,
                        var playerFollowersIdsList: ArrayList<String>,
                        var playerFollowingsIdsList : ArrayList<String>,
                        var playerChampions : Int,
) : Serializable {
    constructor() : this ("","","","" , 0,0,0,0,0,null,null,ArrayList<String>(),ArrayList<String>(),0)
}



enum class PlayerStatus {
    ONLINE,OFFLINE,INTHEGAME
}