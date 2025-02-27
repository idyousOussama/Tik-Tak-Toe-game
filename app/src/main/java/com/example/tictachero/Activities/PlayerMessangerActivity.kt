package com.example.tictachero.Activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Adapters.MessageAdapter
import com.example.tictachero.Models.Message
import com.example.tictachero.Models.MessageType
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityPlayerMessangerBinding

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


class PlayerMessangerActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityPlayerMessangerBinding

    var receiverPlayer :OnlinePlayer? = null
val messageAdapter by lazy {
    MessageAdapter()
}

    val messagesList :ArrayList<Message> = ArrayList()
    var messageText : String = ""
    var playerConversationRoomRef :  DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding  = ActivityPlayerMessangerBinding.inflate(layoutInflater)
         setContentView(binding.root)

       getReceiverPlayer()
        handleGoBackBtn()
       playerNewMessagesListener()
        handleMessageInput()
         handleSendAndLikeBtn()

    }
    private fun handleGoBackBtn() {

binding.playerMessangerGoBack.setOnClickListener {
    finish()
}
    }
    private fun handleSendAndLikeBtn() {
        binding.likeAndSendMessageBtn.setOnClickListener {
            if(messageText.isNotEmpty()) {
                sendMessage(messageText , MessageType.TEXT)
                binding.messageInput.text = null
                messageText = ""
            }else {
                sendMessage(R.drawable.love_icon.toString() , MessageType.LIKE)
            }
        }
    }

    private fun sendMessage(messageText: String, messageType: MessageType) {
        val currentPlayerRoom = firebaseDB.getReference("Message").child( currentPlayer!!.playerId + receiverPlayer!!.playerId)
        val messageId = playerConversationRoomRef!!.push().key.toString()
        val messagePostedDate = System.currentTimeMillis()
        val message = Message(messageId, currentPlayer!!.playerId,messageText,messagePostedDate,messageType)
        currentPlayerRoom.child(message.messageId).setValue(message).addOnCompleteListener {task ->
    if(task.isSuccessful) {
        if(messageAdapter.itemCount == 0) {
            setUpMessagesRecyclerView()
            binding.loadingMessageStatusLayout.visibility = View.GONE
        }
        messageAdapter.addMessagesToList(message)
  binding.playerMessagesRvList.smoothScrollToPosition(messageAdapter.itemCount - 1)
        setReceiverNewMessage(message)
    }
}
    }
    private fun setReceiverNewMessage(message: Message) {
        val receiverMesssageRef = firebaseDB.getReference("Message").child(receiverPlayer!!.playerId + currentPlayer!!.playerId)
        val receiverNewMessageRef =  firebaseDB.getReference("NewMessage").child(receiverPlayer!!.playerId + currentPlayer!!.playerId)
        receiverMesssageRef.child(message.messageId).setValue(message).addOnCompleteListener { task->
            if(task.isSuccessful) {
                receiverNewMessageRef.child(message.messageId).setValue(message)
            }
        }
    }
    private fun handleMessageInput() {
        binding.messageInput.addTextChangedListener {
            messageText = binding.messageInput.text.toString().trim()
            if(messageText.isNotEmpty()) {

                    binding.likeAndSendMessageBtn.setImageResource(R.drawable.send_icon)

            }else{
                binding.likeAndSendMessageBtn.setImageResource(R.drawable.love_icon)
            }
        }
    }
    private fun playerNewMessagesListener() {
        val playerNewMessage =  firebaseDB.getReference("NewMessage").child(currentPlayer!!.playerId + receiverPlayer!!.playerId)
        playerNewMessage.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists() ) {
                    for(item in p0.children) {
                        val newMessage = item.getValue(Message::class.java)

                        if(newMessage != null) {
                            playerNewMessage.child(newMessage.messageId).removeValue()
     messageAdapter.addMessagesToList(newMessage)
     binding.playerMessagesRvList.smoothScrollToPosition(messageAdapter.itemCount - 1)
 }
                    }


                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
    private fun getReceiverPlayer() {
        receiverPlayer = intent.getSerializableExtra("receiverPlayer") as OnlinePlayer
     if(receiverPlayer != null) {

         binding.playerMessangerReceiverImage.setImageResource(receiverPlayer!!.playerImage)
         binding.playerMessangerReceiverName.text = receiverPlayer!!.playerName
         playerConversationRoomRef =  firebaseDB.getReference("Message").child(currentPlayer!!.playerId + receiverPlayer!!.playerId)
         getPlayerAndReciverConversation()
     }
    }
    private fun getPlayerAndReciverConversation() {
        playerConversationRoomRef!!.addListenerForSingleValueEvent(object  : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                      for(item in p0.children) {
                          val message  = item.getValue(Message::class.java)
                       if(message != null) {
                           messagesList.add(message)
                       }
                      }
                 if(messagesList.isNotEmpty()) {
                     messageAdapter.setMessagesList(messagesList)
                     setUpMessagesRecyclerView()
                 }else {
                     upDateViewVisibility(getString(R.string.no_messages_text) , false)
                 }
                }else {
                    upDateViewVisibility(getString(R.string.no_messages_text) , false)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                upDateViewVisibility(getString(R.string.loading_messages_canceled_message), true)
            }
        })
    }
    private fun upDateViewVisibility(statusText: String, isfailed: Boolean) {
        binding.lodingMessageProgress.visibility = View.GONE
        binding.loadingMessageStatusText.text = statusText
        binding.loadingMessageStatusLayout.visibility = View.VISIBLE
        binding.loadingMessageStatusText.visibility = View.VISIBLE
        if(isfailed){
            binding.messagesRefrechBtn.visibility = View.VISIBLE
        }else {
            binding.messagesRefrechBtn.visibility = View.GONE
        }
    }
    private fun setUpMessagesRecyclerView() {
        binding.playerMessagesRvList.apply {
            layoutManager = LinearLayoutManager(this@PlayerMessangerActivity).apply {
          setStackFromEnd(true);
            }
            setHasFixedSize(true)
        adapter = messageAdapter
        }
        binding.lodingMessageProgress.visibility = View.GONE
        binding.playerMessagesRvList.visibility = View.VISIBLE
    }
}