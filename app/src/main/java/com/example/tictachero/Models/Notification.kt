package com.example.tictachero.Models

import java.io.Serializable

class Notification (
     var notificationId : String ,var senderPlayerId  : String? , var notificationType : NotificationTypes?  , var postedDate : Long, var isReaded : Boolean) : Serializable {


          constructor() : this ("" , null , null,0,false)
     }