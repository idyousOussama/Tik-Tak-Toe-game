package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Activities.SplashActivity.Companion.gameViewModel
import com.example.tictachero.Adapters.ImagesAdapter
import com.example.tictachero.DataBases.RoomDatabases.ViewModels.GameViewModel
import com.example.tictachero.Listeners.SelectProfileImageListener
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.Models.MyApp.Companion.currentOnlinePlayer
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.PlayerStatus
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityShowPlayerProfileBinding
import com.example.tictachero.databinding.EditProfileDialogViewBinding
import com.example.tictachero.databinding.SelectProfileImageDialogViewBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowPlayerProfile : AppCompatActivity() {
    private lateinit var binding : ActivityShowPlayerProfileBinding
private var onlinePlayer : OnlinePlayer? = null
    private var newCurrentPlayerImage : Int = 0
    private var newCurrentPlayerName : String = ""
    private val profileImageAdapter by lazy {
        ImagesAdapter()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowPlayerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
getPlayer()
handleBtn()
    }
    private fun handleBtn() {
        binding.showProfileGobBacBtn.setOnClickListener {
            finish()
        }
        binding.followPlayerBtn.setOnClickListener {
            if(onlinePlayer!!.playerId == currentPlayer!!.playerId) {
                newCurrentPlayerName = currentOnlinePlayer!!.playerName
                newCurrentPlayerImage = currentOnlinePlayer!!.playerImage
                showEditProfileDialiog()
            }else {
                handleFollowAndUnFollowBtn(false)
                if(showIfPlayerContains(currentPlayer!!.playerId)) {
                    onlinePlayer!!.playerFollowersIdsList.remove(currentPlayer?.playerId)
                    currentOnlinePlayer!!.playerFollowingsIdsList.remove(onlinePlayer?.playerId)
                    upDateFollowAndUnfollowChild(onlinePlayer!!.playerFollowersIdsList,currentOnlinePlayer!!.playerFollowingsIdsList)
                }else {
                    onlinePlayer!!.playerFollowersIdsList.add(currentPlayer!!.playerId)
                    currentOnlinePlayer!!.playerFollowingsIdsList.add(onlinePlayer!!.playerId)
                    upDateFollowAndUnfollowChild(onlinePlayer!!.playerFollowersIdsList,currentOnlinePlayer!!.playerFollowingsIdsList)
                }
            }

        }
        binding.messagePlayerBtn.setOnClickListener {
            if(onlinePlayer!!.playerId == currentPlayer!!.playerId) {
                navigateToNewActivity(SearchingAboutFriendsActivity::class.java )
            }else{
                navigateAndSendReceivePlayer(onlinePlayer  , PlayerMessangerActivity::class.java)
            }


        }
        binding.playerFollowingText.setOnClickListener {
            navigateToShowUserFollowersAndFollowings(ShowUserFollowersAndFollowingsActivity::class.java,1 , onlinePlayer)
        }
        binding.playerFollowersText.setOnClickListener {
            navigateToShowUserFollowersAndFollowings(ShowUserFollowersAndFollowingsActivity::class.java,0 , onlinePlayer)
        }
    }

    private fun navigateToShowUserFollowersAndFollowings(newActivity: Class<*>, requestCode: Int, selectedUser: OnlinePlayer?) {
val newActivityIntent = Intent(this , newActivity)
        newActivityIntent.putExtra("SelectedUser",selectedUser)
        newActivityIntent.putExtra("RequestCode",requestCode)
        startActivity(newActivityIntent)
    }

    private fun navigateToNewActivity(newActivity : Class<*>) {
        startActivity(Intent(this,newActivity))
    }

    private fun navigateAndSendReceivePlayer(receiverPlayer: OnlinePlayer?,newActivity : Class<*>) {
val newIntent = Intent(this,newActivity)
        newIntent.putExtra("receiverPlayer",receiverPlayer)
         startActivity(newIntent)
    }

    private fun showEditProfileDialiog() {
        val  dialogView = layoutInflater.inflate(R.layout.edit_profile_dialog_view,null)
        val dialogBinding = EditProfileDialogViewBinding.bind(dialogView)
        dialogBinding.editPlayerProfileImage.setImageResource(newCurrentPlayerImage)
        dialogBinding.editPlayerProfileName.setText(newCurrentPlayerName)
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.editPlayerProfileImage.setOnClickListener {
         showSelectionImageProfilaDialog(dialogBinding.editPlayerProfileImage,dialogBinding.playerProfileSaveChangesText , dialogBinding.playerProfileSaveChangesBtn)
        }
        dialogBinding.playerProfileSaveChangesBtn.setOnClickListener {
            dialogBinding.playerProfileSaveChangesText.visibility = View.GONE
            dialogBinding.savingProfileProgress.visibility = View.VISIBLE
           upDateCurrentPlayerProfile(dialog)


        }
        handleNewNameInputText(dialogBinding.editPlayerProfileName , dialogBinding.playerProfileSaveChangesText,dialogBinding.playerProfileSaveChangesBtn)
        dialog.show()
    }

    private fun upDateCurrentPlayerProfile(dialog: Dialog) {
        val playerRef = firebaseDB.getReference("Players").child(currentOnlinePlayer!!.playerId)
val currentPlayerUpdates = mapOf<String,Any>(
    "playerName" to newCurrentPlayerName ,
    "playerImage" to newCurrentPlayerImage

)
        playerRef.updateChildren(currentPlayerUpdates).addOnCompleteListener { task->

            if(task.isSuccessful) {
                 CoroutineScope(Dispatchers.Main).launch {
gameViewModel!!.updatePlayerNameById(newCurrentPlayerName, currentOnlinePlayer!!.playerId)
gameViewModel!!.updatePlayerImageById(newCurrentPlayerImage, currentOnlinePlayer!!.playerId)

                binding.showPlayerProfilePlayerImage.setImageResource(newCurrentPlayerImage)
                binding.showPlayerProfilePlayerName.text = newCurrentPlayerName
                     showToast(getString(R.string.success_to_upDate_your_profile))
                     dialog.dismiss()
                 }

            }else {
                showToast(getString(R.string.failed_to_upDate_your_profile))
            }


        }

    }

    private fun handleNewNameInputText(
        editPlayerProfileName: EditText,
        saveBtnText: TextView,
        playerProfileSaveChangesBtn: RelativeLayout
    ) {
        editPlayerProfileName.addTextChangedListener {
            newCurrentPlayerName = editPlayerProfileName.text.toString()
        if(newCurrentPlayerName.isNotEmpty()) {
        if(newCurrentPlayerName == currentPlayer!!.playerName && currentOnlinePlayer?.playerImage == newCurrentPlayerImage) {
            playerProfileSaveChangesBtn.isEnabled = false
            playerProfileSaveChangesBtn.setBackgroundResource(R.drawable.light_blue_background)
            saveBtnText.setTextColor(resources.getColor(R.color.light_gray))

        }else {
            playerProfileSaveChangesBtn.isEnabled =true
            playerProfileSaveChangesBtn.setBackgroundResource(R.drawable.deep_blue_background)
            saveBtnText.setTextColor(resources.getColor(R.color.white))
        }
        }else {
            playerProfileSaveChangesBtn.isEnabled = false
            playerProfileSaveChangesBtn.setBackgroundResource(R.drawable.light_blue_background)
            saveBtnText.setTextColor(resources.getColor(R.color.light_gray))
        }
        }
    }

    private fun showSelectionImageProfilaDialog(
        editPlayerProfileImage: CircleImageView,
        saveBtnText: TextView,
        playerProfileSaveChangesBtn: RelativeLayout
    ) {
        val dialogView = layoutInflater.inflate(R.layout.select_profile_image_dialog_view,null)
        val dialogBinding = SelectProfileImageDialogViewBinding.bind(dialogView)
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val playerImagesList : ArrayList<Int> = ArrayList()
        playerImagesList.add(R.drawable.player_1)
        playerImagesList.add(R.drawable.player_2)
        playerImagesList.add(R.drawable.player_3)
        playerImagesList.add(R.drawable.player_4)
        playerImagesList.add(R.drawable.player_5)
        playerImagesList.add(R.drawable.player_6)
        playerImagesList.add(R.drawable.player_7)
        playerImagesList.remove(newCurrentPlayerImage)
        profileImageAdapter.setPlayerImagesList(playerImagesList)
        setUpImagesRecyclerView(dialogBinding.selectProfileImageRV)
        hadelImageListener(editPlayerProfileImage,dialog,saveBtnText,playerProfileSaveChangesBtn)
        dialog.show()
    }

    private fun hadelImageListener(
        editPlayerProfileImage: CircleImageView,
        dialog: Dialog,
        saveBtnText: TextView,
        playerProfileSaveChangesBtn: RelativeLayout
    ) {
        profileImageAdapter.setImageItemListener(object : SelectProfileImageListener {
            override fun onProfileImageSelected(image: Int) {
                newCurrentPlayerImage = image
                if(newCurrentPlayerImage == currentOnlinePlayer!!.playerImage && newCurrentPlayerName == currentOnlinePlayer!!.playerName) {
                   playerProfileSaveChangesBtn.isEnabled = false
                   playerProfileSaveChangesBtn.setBackgroundResource(R.drawable.light_blue_background)
                    saveBtnText.setTextColor(resources.getColor(R.color.light_gray))
                }else{
                    playerProfileSaveChangesBtn.isEnabled = true
                    playerProfileSaveChangesBtn.setBackgroundResource(R.drawable.deep_blue_background)
                    saveBtnText.setTextColor(resources.getColor(R.color.white))
                    editPlayerProfileImage.setImageResource(newCurrentPlayerImage)
                }
            dialog.dismiss()
            }

        })
    }

    private fun setUpImagesRecyclerView(selectProfileImageRV: RecyclerView) {
        selectProfileImageRV.apply{
            layoutManager = GridLayoutManager(this@ShowPlayerProfile,3)
        setHasFixedSize(true)
            adapter = profileImageAdapter
        }
    }


    private fun upDateFollowAndUnfollowChild(
        followersList: ArrayList<String>,
        playerFollowingsIdsList: ArrayList<String>
    ) {
val playerRef = firebaseDB.getReference("Players").child(onlinePlayer!!.playerId)
  val  playFollowerListUpDates = mapOf<String,ArrayList<String>>(
      "playerFollowersIdsList" to followersList
  )
        playerRef.updateChildren(playFollowerListUpDates).addOnCompleteListener {task->
            if(task .isSuccessful) {
                upDateCurrentPlayerFollowingList(playerFollowingsIdsList)
            }else {
                showToast(getString(R.string.following) + onlinePlayer!!.playerName +" " + getString(R.string.isFailed_text))
                handleFollowAndUnFollowBtn(true)

            }
        }


    }

    private fun upDateCurrentPlayerFollowingList(playerFollowingsIdsList: ArrayList<String>) {
        val playerRef = firebaseDB.getReference("Players").child(currentOnlinePlayer!!.playerId)
        val  currentPlayFollowingListUpDates = mapOf<String,ArrayList<String>>(
            "playerFollowingsIdsList" to playerFollowingsIdsList
        )
        playerRef.updateChildren(currentPlayFollowingListUpDates).addOnCompleteListener { task->
            if(task.isSuccessful) {

                when(binding.followPlayerText.text.toString()) {
                    getString(R.string.follow_text) -> {
                        binding.followPlayerText.text = getString(R.string.UnFollow_text)
                        showToast(getString(R.string.you_are_now_follow)  + onlinePlayer!!.playerName)
                    }
                    getString(R.string.UnFollow_text) -> {
                        binding.followPlayerText.text = getString(R.string.follow_text)
                        showToast(getString(R.string.you_are_now_Unfollow)  + onlinePlayer!!.playerName)
                    }
                }
                handleFollowAndUnFollowBtn(true)
               setOnlinePlayerFollowers()
            }else {
                showToast(getString(R.string.following) + onlinePlayer!!.playerName +" " + getString(R.string.isFailed_text))
                handleFollowAndUnFollowBtn(true)
            }
        }
    }

    private fun showToast(toastMessage: String) {
Toast.makeText(this@ShowPlayerProfile , toastMessage , Toast.LENGTH_SHORT).show()
    }

    private fun  handleFollowAndUnFollowBtn (isDone : Boolean) {
        if(isDone) {
            binding.followPlayerBtn.isEnabled = true
            binding.followingUnfollowingProgress.visibility = View.GONE
            binding.followPlayerText.visibility = View.VISIBLE
        }else {
            binding.followPlayerBtn.isEnabled = false
            binding.followPlayerText.visibility = View.GONE
            binding.followingUnfollowingProgress.visibility = View.VISIBLE

        }

    }

    private fun showIfPlayerContains(playerId: String): Boolean {
return onlinePlayer!!.playerFollowersIdsList.contains(playerId)
    }

    private fun getPlayer() {
    val onlinePlayerID = intent.getStringExtra("friendRequest")
    getPlayerInFireBase(onlinePlayerID!!)
    }
    private fun  getPlayerInFireBase(playerId: String) {
        val playerRef = firebaseDB.getReference("Players").child(playerId)
        playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
              if(p0.exists()) {
onlinePlayer = p0.getValue(OnlinePlayer::class.java)!!
              if(onlinePlayer != null) {
                  setOnLinePlayerProfile(onlinePlayer!!)
              }
              }
            }
            override fun onCancelled(p0: DatabaseError) {
showSnakBar(getString(R.string.failed_to_get_this_profile_message))
            }

        })
    }

    private fun showSnakBar(snakMessege: String) {
        Snackbar.make(binding.root , snakMessege , Snackbar.LENGTH_SHORT).show()
    }

    private fun setOnLinePlayerProfile(onlinePlayer: OnlinePlayer) {
        editProfile()
        binding.showPlayerProfilePlayerImage.setImageResource(onlinePlayer.playerImage)
        binding.showPlayerProfilePlayerName.text = onlinePlayer.playerName
        setOnlinePlayerFollowers()
        setOnlinePlayerFollowing()

 if(onlinePlayer.playerId == currentPlayer!!.playerId){
binding.followPlayerText.text = getString(R.string.edit_text)
binding.messagePlayerBtnText.text = getString(R.string.add_friends_text)
     binding.followPlayerText.setTextColor(resources.getColor(R.color.deep_blue))
     binding.followPlayerBtn.setBackgroundResource(R.drawable.enable_player_bar_background)
     binding.messagePlayerBtn.setBackgroundResource(R.drawable.enable_player_bar_background)
     binding.messagePlayerBtnText.setTextColor(resources.getColor(R.color.deep_blue))
 }else {
     binding.followPlayerText.setTextColor(resources.getColor(R.color.white))
     binding.followPlayerBtn.setBackgroundResource(R.drawable.deep_blue_effect_background)
     binding.messagePlayerBtn.setBackgroundResource(R.drawable.deep_blue_effect_background)
     binding.messagePlayerBtnText.setTextColor(resources.getColor(R.color.white))

     if(onlinePlayer.playerFollowersIdsList.contains(currentPlayer!!.playerId)) {
         binding.followPlayerText.text =  getString(R.string.UnFollow_text)
     }else {
         binding.followPlayerText.text =  getString(R.string.follow_text)
     }
     binding.messagePlayerBtnText.text = getString(R.string.message_text)
 }
    }


    private fun setOnlinePlayerFollowing() {
        if(onlinePlayer!!.playerFollowingsIdsList.size <= 1) {
            binding.playerFollowingText.setText( onlinePlayer!!.playerFollowingsIdsList.size.toString() +" "+getString(R.string.following))
        }else {
            binding.playerFollowingText.setText( onlinePlayer!!.playerFollowingsIdsList.size.toString() + " "+getString(R.string.followings))
        }
    }

    private fun setOnlinePlayerFollowers() {
        if(onlinePlayer!!.playerFollowersIdsList.size <= 1) {
            binding.playerFollowersText.text  = onlinePlayer!!.playerFollowersIdsList.size.toString() +" "+getString(R.string.follower)
        }else {
            binding.playerFollowersText.text = onlinePlayer!!.playerFollowersIdsList.size.toString() + " "+getString(R.string.followers)
        }
    }
    private fun editProfile() {
        binding.showPlayerProfilePlayerName.background = null
        binding.playerFollowersText.background = null
        binding.playerFollowingText.background = null
        binding.messagePlayerBtn.background = null
        binding.followPlayerBtn.background = null
        binding.followPlayerBtn.isEnabled = true
        binding.messagePlayerBtn.isEnabled = true
        binding.playerFollowersText.isEnabled = true
        binding.playerFollowingText.isEnabled = true
binding.followPlayerBtn.isEnabled = true
    }

}