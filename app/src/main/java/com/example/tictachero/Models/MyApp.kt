package com.example.tictachero.Models

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.DataBases.RoomDatabases.Repositories.GameRepositry
import com.example.tictachero.Services.NotificattionService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val REQUESTTOJOINCHANNEL_ID = "REQUEST_TO_JOIN_CHANNEL_ID"

class MyApp : Application() {
    companion object {
        var currentOnlinePlayer  : OnlinePlayer? = null
var firebaseAuth: FirebaseAuth? = null
    }

    override fun onCreate() {
        super.onCreate()
firebaseAuth =FirebaseAuth.getInstance()
CoroutineScope(Dispatchers.Main).launch {
    val gameRepo = GameRepositry(this@MyApp)
    currentPlayer = gameRepo.getPlayerLogainedAccount()
    if(currentPlayer != null){
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleTracker(currentPlayer!!.playerId, this@MyApp))
        if(currentPlayer!!.PlayerType != PlayerType.VISITOR) {
            getCurrentOnlinePlayer(currentPlayer!!.playerId)
        }
        }
    scaningNewJoinNotification()
}
        
    }
    private fun getCurrentOnlinePlayer(playerId: String) {
        val playersRef = firebaseDB.getReference("Players")
        playersRef.orderByChild(playerId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    for(onlinePlayer in p0.children ) {
                        currentOnlinePlayer = onlinePlayer.getValue(OnlinePlayer::class.java)
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun scaningNewJoinNotification() {
        if(currentPlayer != null ){
            val firebaseDB = FirebaseDatabase.getInstance()

            val newNotifiationRef = firebaseDB.getReference("Notifications").child(currentPlayer!!.playerId).child("newNotification")
            newNotifiationRef.addValueEventListener(object  : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()) {
                        for (item in p0.children) {
                            val notification = item.getValue(Notification::class.java)
                            if(notification != null) {
                                setNotificationAction(NotificationTypes.RQUESTTOPLAY.name , notification)
                            }

                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }


    }

    private fun setNotificationAction(actionName: String, notification: Notification) {
        Intent(this, NotificattionService::class.java).also {
            it.action = actionName
            it.putExtra("requestNotification" , notification)
            startService(it)
        }
    }
}