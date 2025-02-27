package com.example.tictachero.Models

data class GameData(
    var winner : String ,
    var currentPlayer : String ,
    var filledPos : MutableList<String>? = mutableListOf("","","","","","","","","") ,
    var roundIsDone : Boolean
){
    constructor() : this("","",null,false)
}
