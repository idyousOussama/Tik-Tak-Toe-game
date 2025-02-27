package com.example.tictachero.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Listeners.NotificationItemListener
import com.example.tictachero.Models.Notification
import com.example.tictachero.Models.NotificationTypes
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.NotificationCustomViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter  : RecyclerView.Adapter<NotificationAdapter.notificationVH>() {
    private var notifiationList : ArrayList<Notification> = ArrayList()
var senderPlayer : OnlinePlayer? = null
    private var notificationItemListener: NotificationItemListener? = null
    fun setNotificationList(notifiationList : ArrayList<Notification>) {
      this.notifiationList = notifiationList
        notifyDataSetChanged()
    }
    fun setNotificationItemListener( notificationItemListener : NotificationItemListener) {
        this.notificationItemListener = notificationItemListener
    }
    fun removeNotificationItem(notification: Notification) {
        notifiationList.remove(notification)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): notificationVH {
val view = LayoutInflater.from(p0.context).inflate(R.layout.notification_custom_view,p0,false)
 return  notificationVH(view)
    }

    override fun getItemCount(): Int {
    return notifiationList.size
    }
    fun updateItem(position: Int, newNotification: Notification) {
        notifiationList[position] = newNotification
        notifyItemChanged(position) // Notify the adapter that the item has changed
    }

    override fun onBindViewHolder(p0: notificationVH, p1: Int) {
val notification = notifiationList.get(p1)
        getSenderPlayer(notification , p0)
    p0.binding.showNoificationSheetBtn.setOnClickListener {
        notificationItemListener?.onNotificationMoreDetailsBtnClicked(notification ,senderPlayer!!.playerImage , senderPlayer!!.playerName)
    }
        p0.itemView.setOnClickListener {
            notificationItemListener?.onNotificationClicked(notification,p1)
        }
    }
    class notificationVH(itemView: View) : ViewHolder(itemView) {
    var binding = NotificationCustomViewBinding.bind(itemView)

        fun setNotification (senderImage : Int , senderName : String,notificationType: NotificationTypes, isRead : Boolean , tampDate : Long) {
            binding.notificationMessage.background = null
            binding.notificationDateText.background = null
            if(isRead) {
                binding.notificationViewLayout.setBackgroundResource(R.color.white)
            }else {
                binding.notificationViewLayout.setBackgroundResource(R.color.light_light_blue)
            }
            val postedDate = formatTimestamp(tampDate)
            binding.notificationDateText.text = itemView.context.getString(R.string.since_text) +" : " + postedDate
            when(notificationType.name) {
                NotificationTypes.RQUESTTOPLAY.name -> {
                    binding.notificationMessage.text = getNotificationtText(senderName)
                }
            }
            binding.playerNotificationImage.setImageResource(senderImage)
     }


        private fun getNotificationtText(playerName: String): SpannableStringBuilder {
            val fullText = playerName + " " + itemView.context.getString(R.string.request_to_join_to_waiting_room_notification_message_text)
            val spannable = SpannableStringBuilder(fullText)
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    playerName.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            return spannable
        }
        fun formatTimestamp(timestamp: Long): String {
            // Use "MMM dd, yyyy hh:mm a" for the desired format
            val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val date = Date(timestamp)
            return sdf.format(date)
        }

    }
    private fun getSenderPlayer(notification: Notification, notificationVH: notificationVH) {
        val senderRef = firebaseDB.getReference("Players").child(notification.senderPlayerId!!)
        senderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    senderPlayer = p0.getValue(OnlinePlayer::class.java)
                    notificationVH.setNotification(senderPlayer!!.playerImage , senderPlayer!!.playerName,notification.notificationType!!, notification.isReaded, notification.postedDate)

                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

}