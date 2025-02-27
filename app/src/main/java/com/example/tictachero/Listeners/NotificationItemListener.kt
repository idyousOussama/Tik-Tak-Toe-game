package com.example.tictachero.Listeners

import com.example.tictachero.Adapters.NotificationAdapter
import com.example.tictachero.Models.Notification

interface NotificationItemListener {
fun onNotificationMoreDetailsBtnClicked(notification :Notification , senderImage : Int , senderName : String )
fun onNotificationClicked(notification :Notification , position :Int)
}