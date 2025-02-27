package com.example.tictachero.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictachero.Activities.PlayerMessangerActivity
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.ShowPlayerProfile
import com.example.tictachero.Activities.ShowUserFollowersAndFollowingsActivity.Companion.selectedUserFollowersList
import com.example.tictachero.Activities.ShowUserFollowersAndFollowingsActivity.Companion.selectedUserFollowingsList
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Adapters.SearchedUsersAdapter
import com.example.tictachero.Listeners.SearchedItemListener
import com.example.tictachero.Listeners.SginInAndSginUpTextListener
import com.example.tictachero.Models.MyApp.Companion.currentOnlinePlayer
import com.example.tictachero.Models.Notification
import com.example.tictachero.Models.NotificationTypes
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.FragmentShowUserFollowersBinding
import com.example.tictachero.databinding.UserOptionsBottomSheetViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
class ShowUserFollowersFragment : Fragment() {
    private lateinit var binding: FragmentShowUserFollowersBinding
    private var followerList: ArrayList<String> = ArrayList()
var    userItemAdapter  :  SearchedUsersAdapter? = null
    private val playerUsers: ArrayList<OnlinePlayer> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowUserFollowersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (selectedUserFollowersList.isNotEmpty()) {
            followerList = selectedUserFollowersList
userItemAdapter= SearchedUsersAdapter(currentOnlinePlayer!!.playerFollowersIdsList ,currentOnlinePlayer!!.playerFollowingsIdsList  )

            loadFollowers()
            setUserItemListener()
        }else{
            showNoResultSatatus()
        }

        binding.seeMoreFollowersBtnText.setOnClickListener {
            loadMoreFollowers()
        }
    }

    private fun setUserItemListener() {
        userItemAdapter!!.setUserItemListener(object  : SearchedItemListener{
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
    private fun unFollowUser(user: OnlinePlayer) {
        val userRef = firebaseDB.getReference("Players")
        user.playerFollowersIdsList.remove(currentPlayer!!.playerId)
        val userFollowersListUpDates = mapOf(
            "playerFollowersIdsList" to user.playerFollowersIdsList
        )
        userRef.child(user.playerId).updateChildren(userFollowersListUpDates).addOnCompleteListener { task->
            if(task.isSuccessful){
                currentOnlinePlayer!!.playerFollowingsIdsList.remove(user.playerId)
                val currentUserFollowingListUpDates = mapOf(
                    "playerFollowingsIdsList" to currentOnlinePlayer!!.playerFollowingsIdsList
                )
                userRef.child(currentOnlinePlayer!!.playerId).updateChildren(currentUserFollowingListUpDates).addOnCompleteListener {task2->
                    if(task2.isSuccessful){
                        userItemAdapter!!.removeFollowingUserToCurrentUserFollowingList(user)
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
                currentOnlinePlayer!!.playerFollowingsIdsList.add(user!!.playerId)
                val currentUserFollowingListUpdate = mapOf(
                    "playerFollowingsIdsList" to currentOnlinePlayer!!.playerFollowingsIdsList
                )
                userRef.child(currentPlayer!!.playerId).updateChildren(currentUserFollowingListUpdate).addOnCompleteListener { task2->
                    if(task2.isSuccessful){
                        userItemAdapter!!.addFollowingUserToCurrentUserFollowingList(user)
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
                val notification = Notification(notificationId.toString(), currentOnlinePlayer!!.playerId ,
                    NotificationTypes.FOLLOW ,notificationPostedDate , false )
                postNotification(notification , user.playerId)
                currentOnlinePlayer!!.playerFollowingsIdsList.add(user.playerId)
                val currentUserFollowingUpDates = mapOf(
                    "playerFollowingsIdsList" to currentOnlinePlayer!!.playerFollowingsIdsList
                )
                userRef.child(currentOnlinePlayer!!.playerId).updateChildren(currentUserFollowingUpDates).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        userItemAdapter!!.addFollowingUserToCurrentUserFollowingList(user)
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

    private fun showNoResultSatatus() {
        binding.selectedUserListRV.visibility = View.GONE
        binding.loadingDataProgress.visibility = View.GONE
        binding.getDataStatusText.text = requireContext().getString(R.string.no_result_text)
        binding.getDataStatusImage.setImageResource(R.drawable.no_result)
        binding.getDataStatusLayout.visibility = View.VISIBLE
    }

    private fun loadFollowers() {
        val limit = 10
        var usersToLoad: MutableList<String> = mutableListOf()
        if (followerList.size > limit){
            usersToLoad  = followerList.take(limit).toMutableList()
            binding.seeMoreFollowersBtnText.visibility = View.VISIBLE
        } else{
            usersToLoad  = followerList.take(followerList.size).toMutableList()
        binding.seeMoreFollowersBtnText.visibility = View.GONE
        }
        for (userId in usersToLoad) {
            getUserFromFirebase(userId) { follower ->
                follower?.let { playerUsers.add(it) }
                if (playerUsers.size == usersToLoad.size) {
                    userItemAdapter!!.setSearchedUsersList(playerUsers)
                    setUpRecyclerView()
                }
            }
        }
    }
    private fun loadMoreFollowers() {
        var remainingFollowers: MutableList<String> = mutableListOf()
        if (followerList.size > 10) {
            remainingFollowers =  followerList.take(10).toMutableList()  // Returns a List, not an ArrayList, which is fine
            binding.seeMoreFollowersBtnText.visibility = View.VISIBLE
        } else {
            binding.seeMoreFollowersBtnText.visibility = View.GONE
            remainingFollowers =  followerList.take(followerList.size).toMutableList()
        }
        for (userId  in remainingFollowers) {
            getUserFromFirebase(userId) { follower ->
                follower?.let {
                    userItemAdapter!!.addUserList(it)
                }
            }
        }
    }
    private fun setUpRecyclerView() {
        binding.loadingDataProgress.visibility = View.GONE
        binding.getDataStatusLayout.visibility = View.GONE
        binding.selectedUserListRV.apply {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = userItemAdapter
        }
    }
    private fun getUserFromFirebase(userId: String, callback: (OnlinePlayer?) -> Unit) {
        val userRef = firebaseDB.getReference("Players")
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val follower = p0.getValue(OnlinePlayer::class.java)
                    followerList.remove(userId)
                    callback(follower)
                } else {
                    callback(null)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                callback(null)
            }
        })
    }
    private fun showBottoSheet(user : OnlinePlayer) {
        val bottomSheetView = layoutInflater.inflate(R.layout.user_options_bottom_sheet_view,null)
        val bottomSheetBinding = UserOptionsBottomSheetViewBinding.bind(bottomSheetView)
        bottomSheetBinding.bottomSheetPlayerImage.setImageResource(user.playerImage)
        bottomSheetBinding.userName.text = user.playerName

        val bottomSheet =  BottomSheetDialog(requireContext() , R.style.BottomSheetDialog)
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
        val messangerIntent =Intent(requireContext() , PlayerMessangerActivity::class.java)
        messangerIntent . putExtra("receiverPlayer" , user)
       requireContext().startActivity(messangerIntent)
    }
    private fun moveToVisiteProfile(userId: String) {
        val visitActivityIntent = Intent (requireContext(), ShowPlayerProfile::class.java)
        visitActivityIntent.putExtra("friendRequest",userId)
        requireContext().startActivity(visitActivityIntent)
    }
}

