package com.example.tictachero.Models

import javax.crypto.SecretKey

class Message(var messageId : String , var senderId : String ,var  messageText : String ,var postedDate : Long ,var  messageType : MessageType?) {

    constructor() : this("","","",  0,null)

}

enum class MessageType {
    TEXT,IMAGE,LIKE
}