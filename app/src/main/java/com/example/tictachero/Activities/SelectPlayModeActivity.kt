package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.tictachero.Activities.SplashActivity.Companion.gameViewModel
import com.example.tictachero.Models.Player
import com.example.tictachero.Models.Notification
import com.example.tictachero.Models.PlayerStatus
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivitySelectPlapyModeBinding
import com.example.tictachero.databinding.ConfirmationDialogViewBinding
import com.example.tictachero.databinding.NoInternetDialogViewBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectPlayModeActivity : AppCompatActivity() ,NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding : ActivitySelectPlapyModeBinding
    val newNotificationList :ArrayList<Notification>  = ArrayList()
    private  var confirmationDialog : Dialog? =  null
    companion object {
        var currentPlayer  : Player? = null
    }
    val firebaseDb : FirebaseDatabase = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySelectPlapyModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpNavigationView()
        setUpPlayerAccount()
        scaningNewJoinNotification()
        binding.playWithFriendBtn.setOnClickListener {
            navigateToNewActivity(PlayWithFriendModeActivity::class.java)
        }
        binding.playWithDeviceBtn.setOnClickListener {
            navigateToNewActivity(PlayWithDeviceActivity::class.java)
        }
        binding.playOnline.setOnClickListener {
            if(playerIsConcted()) {
                navigateToNewActivity(SearchAboutPlayer2Activity::class.java)

            }else {
                showNoInternetDialog()
            }
        }
        binding.gamePlayInvitationLayout.setOnClickListener {
            if(playerIsConcted()) {
                navigateToNewActivity(ShowPlayerNotificationsActivity::class.java)
                removePlayerNewInvitation()
            }else {
                showNoInternetDialog()
            }
        }
        binding.userImage.setOnClickListener {
            val headerView = binding.drawerNavigationView.getHeaderView(0)
            headerView.findViewById<ImageView>(R.id.nav_user_Image).setImageResource(currentPlayer!!.playerImage)
            headerView.findViewById<TextView>(R.id.nav_user_name_text).text = currentPlayer!!.playerName
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
private fun setUpNavigationView() {
    binding.drawerNavigationView.setNavigationItemSelectedListener(this)
val toggle  = ActionBarDrawerToggle(this ,binding.drawerLayout , null , R.string.Open,R.string.Close)
    toggle.drawerArrowDrawable.color = resources.getColor(R.color.deep_blue)
    binding.drawerLayout.addDrawerListener(toggle)
toggle.syncState()

}
    private fun removePlayerNewInvitation() {
        val newNotifiationRef = firebaseDb.getReference("Notifications").child(currentPlayer!!.playerId).child("newNotification")
 if(newNotificationList.isNotEmpty()) {
     for (item in newNotificationList) {
         newNotifiationRef.child(item.notificationId).removeValue().addOnCompleteListener {task ->
             if(task.isSuccessful) {
                 newNotificationList.remove(item)
             }
         }
     }
     setPlayerNewNotifications(0)
 }




    }

    private fun setPlayerNewNotifications(newNotificationNum: Int) {
 if(newNotificationNum > 0) {
     binding.newNotificationsNumText.text = newNotificationNum.toString()
     binding.newNotificationsNumText.visibility = View.VISIBLE
 }else {
     binding.newNotificationsNumText.text = null
     binding.newNotificationsNumText.visibility = View.GONE


 }



    }

    private fun setUpPlayerAccount() {
        val    playerIntent  = intent
        currentPlayer  = playerIntent.getSerializableExtra("logainedAccount") as? Player

        if (currentPlayer != null) {
            binding.userName.text  = currentPlayer!!.playerName
            binding.userPoint.text  = currentPlayer!!.playerPoints.toString() + " " + "points"
            binding.userDiamondNumbers.text  = currentPlayer!!.playerDiamond.toString()
binding.userImage.setImageResource(currentPlayer!!.playerImage)
        }
    }



    private fun navigateToNewActivity(newActivity : Class<*>) {
        startActivity(Intent (this ,newActivity))
    }
    private fun playerIsConcted() : Boolean{
        val connMgr =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isWifiConn = false
        var isMobileConn = false
        for (network in connMgr.allNetworks) {
            val networkInfo = connMgr.getNetworkInfo(network)
            if (networkInfo!!.type == ConnectivityManager.TYPE_WIFI) {
                isWifiConn = isWifiConn or networkInfo!!.isConnected
            }
            if (networkInfo!!.type == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn = isMobileConn or networkInfo!!.isConnected
            }
        }
        if (isWifiConn == false && isMobileConn == false) {
            return false
        }else{
            return true
        }
    }
    private fun showNoInternetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.no_internet_dialog_view,null)
        val dialogBinding = NoInternetDialogViewBinding.bind(dialogView)
        dialogBinding.noInternetDialogDismissBtn.text = getString(R.string.ok_text)
        dialogBinding.noInternetDialogBackBtn.visibility = View.GONE
        val  noInternet = Dialog(this)
        noInternet.setContentView(dialogView)
        noInternet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        noInternet.setCancelable(false)
        dialogBinding.noInternetDialogDismissBtn.setOnClickListener {
            noInternet.dismiss()
        }

        noInternet.show()
    }
    fun scaningNewJoinNotification() {
        if(playerIsConcted()) {
            val newNotifiationRef = firebaseDb.getReference("Notifications").child(currentPlayer!!.playerId).child("newNotification")
            newNotifiationRef.addValueEventListener(object  : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()) {
                        for (item in p0.children) {
                            val notification = item.getValue(Notification::class.java)
                            if(notification != null) {
                                newNotificationList.add(notification)
                            }
                        }
setPlayerNewNotifications(newNotificationList.size)
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    showSnakBar(getString(R.string.failed_to_get_player_data_message))
                }

            })

        }

    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if(p0.itemId == R.id.nav_user_profile) {
         val intent = Intent(this ,ShowPlayerProfile::class.java)
            intent.putExtra(  "friendRequest",currentPlayer!!.playerId)
            startActivity(intent)

        }else if(p0.itemId == R.id.nav_user_add_new_profile) {
binding.drawerLayout.closeDrawer(GravityCompat.START)
            navigateToCreateAccountActivity(1)
        }else if (p0.itemId == R.id.nav_log_out){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            showConfirmationDialog(getString(R.string.log_out_title_text) , getString(R.string.confriramation_logout_message) , getString(R.string.yes_text),true)
        }
        return true

    }

    private fun showConfirmationDialog(title: String, message: String, btnText: String, move: Boolean) {
val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog_view , null)
        val dialogBinding = ConfirmationDialogViewBinding.bind(dialogView)
        confirmationDialog = Dialog(this)
        dialogBinding.confirmationDialogNegativeBtn.visibility = View.GONE
        dialogBinding.confirmationDialogPositiveBtn.text  = btnText
        dialogBinding.confirmationDialogTitle.text = title
        dialogBinding.confirmationDialogMessage.text = message
        dialogBinding.confirmationDialogPositiveBtn.setOnClickListener {
            if(move) {
                dialogBinding.confirmationDialogPositiveBtn.isEnabled = false
                dialogBinding.confirmationDialogPositiveBtn.text = null
                dialogBinding.confirmationDialogPositiveBtnProgress.visibility = View.VISIBLE
                changePlayerStatus()
            }else{
                confirmationDialog!!.dismiss()
            }
        }
        confirmationDialog!!.setContentView(dialogView)
        confirmationDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    confirmationDialog!!.show()

    }
private fun showSnakBar(snakMessage : String) {
    Snackbar.make(binding.root,snakMessage, Snackbar.LENGTH_LONG).show()

}
    private fun changePlayerStatus() {
val playersRef = firebaseDb.getReference("Players")
        val playerStatusUpdates = mapOf<String , PlayerStatus>(
            "playerStatus" to PlayerStatus.OFFLINE
        )
        playersRef.child(currentPlayer!!.playerId).updateChildren(playerStatusUpdates).addOnCompleteListener { task->
            if(task.isSuccessful) {
                CoroutineScope(Dispatchers.Main).launch {
                    gameViewModel?.upDateLoginedAccountById(currentPlayer!!.playerId,false)
                    confirmationDialog!!.dismiss()
navigateToNewActivity(ShowPlayerAccountsActivity::class.java)
                }
            }else {
                confirmationDialog!!.dismiss()
                showConfirmationDialog(getString(R.string.log_out_failed_title_text) , getString(R.string.confriramation_logout_failed_message) , getString(R.string.ok_text),false)
            }
        }
    }

    private fun navigateToCreateAccountActivity(requestCode: Int) {
val logAndCreateAccountIntent = Intent(this , LoginAndCreateAccountActivity::class.java)
        logAndCreateAccountIntent.putExtra("RequestCode" , requestCode)
    startActivity(logAndCreateAccountIntent)
        finish()
    }


}