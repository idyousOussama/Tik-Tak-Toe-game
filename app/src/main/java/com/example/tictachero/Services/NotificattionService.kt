package com.example.tictachero.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.tictachero.Activities.RoundRoomSettingsActivity
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.Models.MyApp.Companion.currentOnlinePlayer
import com.example.tictachero.Models.NotificationTypes
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.REQUESTTOJOINCHANNEL_ID
import com.example.tictachero.Models.Notification
import com.example.tictachero.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificattionService : Service() {
    val REQUET_TO_JOIN_NOTIFICATION_ID = "1"
    var senderPlayer : OnlinePlayer? = null
    private var opponentPlayer : OnlinePlayer? = null
val firebaseDb = FirebaseDatabase.getInstance()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent!!.action) {
    NotificationTypes.RQUESTTOPLAY.name -> createRequestToJoinNotification(intent)
            GameStatus.JOINED.name -> joinToRoom()
            GameStatus.REFUSE.name ->refuseToJoinRoom()



        }



        return START_STICKY
    }

    private fun refuseToJoinRoom() {
        upDatePlayerGameStatus (GameStatus.REFUSE.name)
    }

    private fun joinToRoom() {

    upDatePlayerGameStatus (GameStatus.JOINED.name)


    }

    private fun upDatePlayerGameStatus(playerGameStatus: String) {
        val  battleRoom = firebaseDb.getReference("Rooms").child(opponentPlayer!!.playerId + currentPlayer!!.playerId)
        val playerRef = battleRoom.child(currentPlayer!!.playerId)
        val gameStatusUpDate = mapOf<String , String>(
            "gameStatus" to playerGameStatus
        )
        playerRef.updateChildren(gameStatusUpDate).addOnCompleteListener { task ->

            if(task.isSuccessful) {
                moveToWaitingRoom()
            }
        }
    }

    private fun moveToWaitingRoom() {
         if(currentOnlinePlayer != null){
             val waitingRoomIntent =Intent(this, RoundRoomSettingsActivity::class.java).apply {
                 putExtra("opponent" , opponentPlayer )
                 putExtra("currentOnlinePlayer" ,currentOnlinePlayer )
                 putExtra("player_type_key" , "reciver")
                 flags = Intent.FLAG_ACTIVITY_NEW_TASK // Add this flag
             }
             startActivity(waitingRoomIntent)
             cancelNotification()
         }

    }

    private fun cancelNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        stopForeground(true)
        notificationManager.cancel(1)
    }

    private fun createRequestToJoinNotification(intent: Intent) {
        var notificationRemoteViews : RemoteViews? = null
        val notification = intent.getSerializableExtra("requestNotification") as? Notification
        getSenderPlayer(notification!!.senderPlayerId!!)
        if (notification != null && senderPlayer != null) {
            when(notification.notificationType!!.name){
                NotificationTypes.RQUESTTOPLAY.name -> {
                    opponentPlayer = senderPlayer
                    notificationRemoteViews  =  RemoteViews(packageName, R.layout.request_to_join_waiting_room_notifications_remote_view).apply {
                        setTextViewText(R.id.notificaion_join_btn ,getString(R.string.join_text))
                        setTextViewText(R.id.notification_player_name,senderPlayer!!.playerName)
                        setTextViewText(R.id.notification_message, getString(R.string.request_to_join_to_waiting_room_notification_message_text))
                        setOnClickPendingIntent(R.id.notificaion_join_btn,joinPendingIntent())
                        setOnClickPendingIntent(R.id.refuse_btn , refusePendingIntent())
                    }
                }
            }
            createNotificationChannel()
            val notification = NotificationCompat.Builder(this, REQUESTTOJOINCHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setCustomContentView(notificationRemoteViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Use only one priority setting
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setOngoing(false)  // Keeps it persistent
                .setAutoCancel(true)
                .build()
            // Start the service as foreground
            startForeground(1, notification)
        }
    }
    private fun getSenderPlayer(senderPlayerId: String) {
        val senderRef = firebaseDB.getReference("Players").child(senderPlayerId)
        senderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    senderPlayer = p0.getValue(OnlinePlayer::class.java)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                senderPlayer = null

            }

        })

    }

    private fun refusePendingIntent(): PendingIntent? {
        val intent = Intent(this,NotificattionService::class.java).apply {
            action = GameStatus.REFUSE.name
        }
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REQUESTTOJOINCHANNEL_ID,
                "Game Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for game invitations"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun joinPendingIntent(): PendingIntent? {
        val intent = Intent(this,NotificattionService::class.java).apply {
            action = GameStatus.JOINED.name
        }
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onBind(intent: Intent): IBinder? {
return null   }
}