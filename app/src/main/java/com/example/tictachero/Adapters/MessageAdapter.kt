package com.example.tictachero.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Models.Message
import com.example.tictachero.Models.MessageType
import com.example.tictachero.R
import com.example.tictachero.databinding.MessageCusotmViewBinding


class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageCustomVH>() {
private var messagesList : ArrayList<Message> = ArrayList()

    fun setMessagesList (messagesList : ArrayList<Message>) {
        this.messagesList = messagesList
        notifyDataSetChanged()
    }
    fun addMessagesToList (message : Message) {
        this.messagesList.add(message)
        notifyItemInserted(messagesList.size - 1)
    }

    fun upDateMessage ( position : Int , message : Message) {
      messagesList[position] = message
      notifyItemChanged(position)
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MessageCustomVH {
val messageView = LayoutInflater.from(p0.context).inflate(R.layout.message_cusotm_view,p0,false)
    return MessageCustomVH(messageView)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun onBindViewHolder(p0: MessageCustomVH, p1: Int) {
val message = messagesList.get(p1)
        p0.setMessage(message.senderId,message.messageText,message.messageType!!,message.postedDate)


    }

    class MessageCustomVH(itemView: View) : ViewHolder(itemView) {
val messageBinding = MessageCusotmViewBinding.bind(itemView)

        fun setMessage(senderId : String , messageText :  String, messageType: MessageType , messagePostedDate : Long) {
             if(senderId == currentPlayer!!.playerId) {
                  when(messageType.name) {
                      MessageType.TEXT.name -> {
                          messageBinding.messagesImagesLayout.visibility = View.GONE
                          messageBinding.messagesTextLayout.visibility = View.VISIBLE
                          messageBinding.senderMessageText.visibility = View.VISIBLE
                          messageBinding.senderMessageText.text = messageText
                      }
                      MessageType.LIKE.name -> {
                          messageBinding.messagesTextLayout.visibility = View.GONE
                          messageBinding.messagesImagesLayout.visibility = View.VISIBLE
                          messageBinding.senderMessageImages.visibility = View.VISIBLE
                          messageBinding.senderMessageImages.setImageResource(R.drawable.love_icon)
                      }
                  }
             }else {
                 when(messageType.name) {
                     MessageType.TEXT.name -> {
                         messageBinding.messagesImagesLayout.visibility = View.GONE

                         messageBinding.messagesTextLayout.visibility = View.VISIBLE
                         messageBinding.recieverMessageText.visibility = View.VISIBLE
                         messageBinding.recieverMessageText.text = messageText
                     }
                     MessageType.LIKE.name -> {
                         messageBinding.messagesTextLayout.visibility = View.GONE
                         messageBinding.messagesImagesLayout.visibility = View.VISIBLE
                         messageBinding.recieverMessageImages.visibility = View.VISIBLE
                         messageBinding.recieverMessageImages.setImageResource(R.drawable.love_icon)
                     }
                 }
             }
        }

    }











}