package com.example.tictachero.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Adapters.SearchedUsersAdapter
import com.example.tictachero.Listeners.SearchedItemListener
import com.example.tictachero.Models.MyApp.Companion.currentOnlinePlayer
import com.example.tictachero.Models.Notification
import com.example.tictachero.Models.NotificationTypes
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivitySearchingAboutFriendsBinding
import com.example.tictachero.databinding.SearchedPlayerCustomViewBinding
import com.example.tictachero.databinding.UserOptionsBottomSheetViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SearchingAboutFriendsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchingAboutFriendsBinding
 private var currentOnlineUser : OnlinePlayer? = null
    private var searchedUsersAdapter : SearchedUsersAdapter? = null

    private val usersList : ArrayList<OnlinePlayer> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchingAboutFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
            getCurrentOnlineUser()
   handleSearchbarInpute()




    }

    private fun setUserItemListener() {
        searchedUsersAdapter!!.setUserItemListener(object : SearchedItemListener{
            override fun onFollowing(user: OnlinePlayer) {
                followUser(user)
            }
            override fun onAcceptFollow(user: OnlinePlayer) {
             acceptFollow(user)
            }

            override fun onUnFollow(user: OnlinePlayer) {
         unFollowUser(user)
            }

            override fun onUserItemListener(user: OnlinePlayer) {
                showBottoSheet(user)
            }
        })
    }
    private fun showBottoSheet(user : OnlinePlayer) {
        val bottomSheetView = layoutInflater.inflate(R.layout.user_options_bottom_sheet_view,null)
        val bottomSheetBinding = UserOptionsBottomSheetViewBinding.bind(bottomSheetView)
        bottomSheetBinding.bottomSheetPlayerImage.setImageResource(user.playerImage)
        bottomSheetBinding.userName.text = user.playerName

        val bottomSheet =  BottomSheetDialog(this , R.style.BottomSheetDialog)
        bottomSheet.setContentView(bottomSheetView)
        bottomSheet.show()
        bottomSheetBinding.visitUserProfile.setOnClickListener {
            bottomSheet.dismiss()
            moveToVisiteProfile(user.playerId)
        }
        bottomSheetBinding.sendMessageToUser.setOnClickListener {
            bottomSheet.dismiss()
            moveToSendToUserMessage(user)
        }
    }
    private fun moveToSendToUserMessage(user: OnlinePlayer) {
        val messangerIntent = Intent(this ,PlayerMessangerActivity::class.java)
        messangerIntent . putExtra("receiverPlayer" , user)
        startActivity(messangerIntent)
    }

    private fun moveToVisiteProfile(userId: String) {
        val visitActivityIntent = Intent (this , ShowPlayerProfile::class.java)
        visitActivityIntent.putExtra("friendRequest",userId)
        startActivity(visitActivityIntent)
    }

    private fun unFollowUser(user: OnlinePlayer) {
val userRef = firebaseDB.getReference("Players")
   user.playerFollowersIdsList.remove(currentPlayer!!.playerId)
      val userFollowersListUpDates = mapOf(
          "playerFollowersIdsList" to user.playerFollowersIdsList
      )
        userRef.child(user.playerId).updateChildren(userFollowersListUpDates).addOnCompleteListener { task->
           if(task.isSuccessful){
               currentOnlineUser!!.playerFollowingsIdsList.remove(user.playerId)
               val currentUserFollowingListUpDates = mapOf(
                   "playerFollowingsIdsList" to currentOnlineUser!!.playerFollowingsIdsList
               )
userRef.child(currentOnlinePlayer!!.playerId).updateChildren(currentUserFollowingListUpDates).addOnCompleteListener {task2->
    if(task2.isSuccessful){
        searchedUsersAdapter!!.removeFollowingUserToCurrentUserFollowingList(user)
    }
}
           }
       }

    }

    private fun acceptFollow(user: OnlinePlayer) {
        val userRef  = firebaseDB.getReference("Players")
user.playerFollowersIdsList.add(currentPlayer!!.playerId)
        val userFollowersLisUpdate = mapOf(
            "playerFollowersIdsList" to user.playerFollowersIdsList
        )
        userRef.child(user.playerId).updateChildren(userFollowersLisUpdate).addOnCompleteListener { task->
            if(task.isSuccessful){
                val notificationId = userRef.push().key
                val notificationPostedDate= System.currentTimeMillis()
                val notification = Notification(notificationId.toString(), currentPlayer!!.playerId , NotificationTypes.ACCEPTE ,notificationPostedDate,false)
                postNotification(notification , user.playerId)
                currentOnlineUser!!.playerFollowingsIdsList.add(user!!.playerId)
                val currentUserFollowingListUpdate = mapOf(
                    "playerFollowingsIdsList" to currentOnlineUser!!.playerFollowingsIdsList
                )
                userRef.child(currentPlayer!!.playerId).updateChildren(currentUserFollowingListUpdate).addOnCompleteListener { task2->
                    if(task2.isSuccessful){
                        searchedUsersAdapter!!.addFollowingUserToCurrentUserFollowingList(user)
                    }
                }

            }
        }
    }

    private fun followUser(user: OnlinePlayer) {
    val userRef = firebaseDB.getReference("Players")
    user.playerFollowersIdsList.add(currentOnlinePlayer!!.playerId)
    val userFollowerUpDates = mapOf(
        "playerFollowersIdsList" to user.playerFollowersIdsList
    )
        userRef.child(user.playerId).updateChildren(userFollowerUpDates).addOnCompleteListener { task->
            if(task.isSuccessful){
                val notificationId = userRef.push().key
                        val notificationPostedDate= System.currentTimeMillis()
                val notification = Notification(notificationId.toString(), currentOnlinePlayer!!.playerId ,NotificationTypes.FOLLOW ,notificationPostedDate , false )
             postNotification(notification , user.playerId)
                currentOnlinePlayer!!.playerFollowingsIdsList.add(user.playerId)
                val currentUserFollowingUpDates = mapOf(
                    "playerFollowingsIdsList" to currentOnlinePlayer!!.playerFollowingsIdsList
                )
                userRef.child(currentOnlinePlayer!!.playerId).updateChildren(currentUserFollowingUpDates).addOnCompleteListener { task ->
                 if(task.isSuccessful){
                    searchedUsersAdapter!!.addFollowingUserToCurrentUserFollowingList(user)
                 }


                }
            }
        }



    }

    private fun postNotification(notification: Notification, userId: String)  {
val noficationsRef = firebaseDB.getReference("Notifications").child(userId)
val newNoficationsRef = firebaseDB.getReference("newNotification").child(userId)
        noficationsRef.child(notification.notificationId).setValue(notification).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                newNoficationsRef.child(notification.notificationId).setValue(notification)
            }
        }


    }

    private fun handleSearchbarInpute() {
        binding.searchingFriendInpute.addTextChangedListener {
            val userSearchText = binding.searchingFriendInpute.text.toString().trim()
             if(userSearchText.isNotEmpty() ){
                 binding.searchingForFriendMesageAndImageLayout.visibility =View.GONE
                 binding.friendsSearchingProgress.visibility =View.VISIBLE
              getUsersBySearchText(userSearchText)
             }else{
                 handleSearchingInputeEmpty()
             }

        }
    }

    private fun handleSearchingInputeEmpty() {
        binding.friendsSearchingProgress.visibility = View.GONE
        binding.frtiendsListRv.visibility= View.GONE
        binding.searchingForFriendMesageAndImageLayout.visibility = View.VISIBLE
        binding.searchingForFriendImage.visibility = View.GONE
        binding.searchingForFriendTextMessage.text = getString(R.string.searching_about_frient_motivate_text)
    }

    private fun getUsersBySearchText(userSearchText: String) {
      if(usersList.isNotEmpty()){
          filerUserBySearchText(usersList , userSearchText)
      }else{
          getUserFromFireBase(userSearchText)
      }

    }

    private fun getUserFromFireBase(userSearchText: String) {
        val usersRef = firebaseDB.getReference("Players")
        usersRef.addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
if(p0.exists()){
    usersList.clear()
for(userItem in p0.children) {
    val user = userItem.getValue(OnlinePlayer::class.java)
    if(user!= null  && user.playerId != currentPlayer!!.playerId){
        usersList.add(user)
    }
}

filerUserBySearchText(usersList , userSearchText)
}else{
   handleNotUserList()
}


            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun filerUserBySearchText(foundUsersList: ArrayList<OnlinePlayer>, userSearchText: String) {
   val usersFilterdList = foundUsersList.filter { it.playerName.contains(userSearchText) }
     if(usersFilterdList.isNotEmpty()){
         handleUserFounded()
         searchedUsersAdapter!!.clearUserList()
         searchedUsersAdapter!!.setSearchedUsersList(usersFilterdList as ArrayList)
     }else {
         handleNotUserList()
     }

    }

    private fun handleUserFounded() {
        binding.friendsSearchingProgress.visibility = View.GONE
         if(binding.searchingForFriendMesageAndImageLayout.isVisible){
             binding.searchingForFriendMesageAndImageLayout.visibility = View.GONE
         }
    binding.frtiendsListRv.visibility  = View.VISIBLE

    }

    private fun setUpRecyclerView() {
        binding.frtiendsListRv.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
             adapter =searchedUsersAdapter
        }
    }

    private fun handleNotUserList() {
        binding.frtiendsListRv.visibility = View.GONE
binding.friendsSearchingProgress.visibility = View.GONE
        binding.searchingForFriendImage.visibility = View.VISIBLE
        binding.searchingForFriendImage.setImageResource(R.drawable.no_result)
        binding.searchingForFriendTextMessage.text = getString(R.string.no_result_text)
binding.searchingForFriendMesageAndImageLayout.visibility = View.VISIBLE
    }

    private fun getCurrentOnlineUser() {
    val playerRef = firebaseDB.getReference("Players")
        playerRef.child(currentPlayer!!.playerId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
if(p0.exists()) {
    currentOnlinePlayer = p0.getValue(OnlinePlayer::class.java)
currentOnlineUser = currentOnlinePlayer
    searchedUsersAdapter = SearchedUsersAdapter(currentOnlineUser!!.playerFollowersIdsList , currentOnlineUser!!.playerFollowingsIdsList)
    setUserItemListener()
    setUpRecyclerView()

}
}
   override fun onCancelled(p0: DatabaseError) {

            }

        })
    }
}