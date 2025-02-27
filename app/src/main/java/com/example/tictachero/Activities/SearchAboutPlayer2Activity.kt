package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Adapters.SearchedPlayerAdapter
import com.example.tictachero.Listeners.SearchedPlayerListener
import com.example.tictachero.Models.NotificationTypes
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.Notification
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivitySearchAboutPlayer2Binding
import com.example.tictachero.databinding.NoInternetDialogViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchAboutPlayer2Activity : AppCompatActivity() {
private lateinit var binding : ActivitySearchAboutPlayer2Binding
    val playerList : ArrayList<OnlinePlayer> = ArrayList()
var isRequested : Boolean = false
    var currentOnlinePlayer : OnlinePlayer?= null
    private var  searchText = ""
    private var noIntentDialog : Dialog? = null
private val searchingAdapter : SearchedPlayerAdapter  by lazy {
    SearchedPlayerAdapter()
}

    private val firebaseDb = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivitySearchAboutPlayer2Binding.inflate(layoutInflater)
        setContentView(binding.root)
getCurrentOnlinePlayer()
        handleSearchingBar()
        handeleRequestBtnListener()
    }

    private fun getCurrentOnlinePlayer() {
        val playersRef = firebaseDB.getReference("Players")
        playersRef.orderByChild(currentPlayer!!.playerId).addListenerForSingleValueEvent(object : ValueEventListener {
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
    private fun handeleRequestBtnListener() {
        searchingAdapter.setPlayerListener(object : SearchedPlayerListener{
            override fun onRequestBtnClicked(
                player: OnlinePlayer,
                requestBtn: TextView,
                processProgress: ProgressBar
            ) {
 if(!isRequested) {
if(playerIsConcted()) {
    requestBtn.visibility = View.GONE
    processProgress.visibility = View.VISIBLE
    sendRequestToOppenent(player, requestBtn,
        processProgress)
    isRequested = true

}else {
    showNoInternetDialog()
}
 }else {
Toast.makeText(baseContext,getString(R.string.already_select_your_opponent) , Toast.LENGTH_SHORT).show()
 }
            }
        })
    }
    private fun sendRequestToOppenent(
        player: OnlinePlayer,
        requestBtn: TextView,
        processProgress: ProgressBar ) {
        val notifiationRef = firebaseDB.getReference("Notifications").child(player.playerId)
      val notificationId = notifiationRef.push().key.toString()
        // timestamp (current time)
        val currentTimestamp = System.currentTimeMillis()
        // Format and display the date

        val requestNotification = Notification(notificationId,currentOnlinePlayer!!.playerId,NotificationTypes.RQUESTTOPLAY,currentTimestamp,false)
        notifiationRef.child("newNotification").child(notificationId).setValue(requestNotification).addOnCompleteListener {task ->
             if(task.isSuccessful) {
                 notifiationRef.child("Notification").child(notificationId).setValue(requestNotification).addOnCompleteListener { task ->
                     if (task.isSuccessful) {
                         processProgress.visibility = View.GONE
                         requestBtn.visibility = View.VISIBLE
                         createBattleRoom(player)
                     }
                 }
             }
        }
    }

    private fun createBattleRoom(player: OnlinePlayer) {
        val gameRoomRef = firebaseDb.getReference("Rooms").child(currentOnlinePlayer!!.playerId + player.playerId)
        gameRoomRef.child("players").child(currentOnlinePlayer!!.playerId).setValue(currentOnlinePlayer).addOnCompleteListener{ task ->
       if(task.isSuccessful) {
           gameRoomRef.child("players").child(player.playerId).setValue(player)
           navigateToNewActivity(RoundRoomSettingsActivity::class.java , player)
       }
        }
    }
    private fun showNoInternetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.no_internet_dialog_view,null)
        val dialogBinding = NoInternetDialogViewBinding.bind(dialogView)
        noIntentDialog = Dialog(this)
        noIntentDialog!!.setContentView(dialogView)
        noIntentDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        noIntentDialog!!.setCancelable(false)
        dialogBinding.noInternetDialogDismissBtn.setOnClickListener {
            noIntentDialog!!.dismiss()
        }
        dialogBinding.noInternetDialogBackBtn.setOnClickListener {
finish()
        }
        noIntentDialog!!.show()
    }

    private fun handleSearchingBar() {
        binding.searchAboutOpponent.addTextChangedListener {
             searchText = binding.searchAboutOpponent.text.toString()
            if(playerIsConcted()) {
                if(searchText.isNotEmpty()){
                    handleSearchingProcess()
                    getPlayerInFireBase(searchText)
                }else{
                    binding.playerRV.visibility = View.GONE
                    binding.noResultLayout.visibility = View.GONE
                    binding.searchingLayout.visibility = View.GONE
                    binding.searchingOpponentLayout.visibility = View.VISIBLE
                }
            }else {
                showNoInternetDialog()
            }
        }
    }
    private fun getPlayerInFireBase(searchText: String) {
        if(playerList.isEmpty()){
            val playersRef = firebaseDb.getReference("Players")
            playersRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        playerList.clear()
                        for(item in p0.children) {
                            val player = item.getValue(OnlinePlayer::class.java)
                            if(player != null) {
                                playerList.add(player)
                            }
                        }
                        setPlayersList(searchText)
                    }else{
                        handleNoResult()
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
Toast.makeText(baseContext , "This operation is canceled . please try again later !" , Toast.LENGTH_SHORT).show()                }

            })

        }else {
            setPlayersList(searchText)
        }
    }
    private fun setPlayersList(searchText: String) {
        if(playerList.isNotEmpty()) {
            val playerFilttedList = playerList.filter { it.playerName.contains(searchText, ignoreCase = true && it.playerId != currentPlayer!!.playerId)   || it.ID.contains(searchText, ignoreCase = true && it.playerId != currentPlayer!!.playerId)
            } as ArrayList<OnlinePlayer>
            if(playerFilttedList.isNotEmpty()){
                setUpRecyclerView(playerFilttedList)
            }else {
                handleNoResult()
            }
        }else{
            handleNoResult()
        }
    }
    private fun setUpRecyclerView(playerFilttedList: ArrayList<OnlinePlayer>) {
searchingAdapter.setPlayerList(playerFilttedList)
binding.playerRV.apply {
    layoutManager = LinearLayoutManager(context)
setHasFixedSize(true)
    adapter = searchingAdapter
}
        handlePlayerRv()

    }

    private fun handlePlayerRv() {
            binding.playerRV.visibility = View.GONE

        if(binding.searchingLayout.isVisible){
            binding.searchingLayout.visibility = View.GONE
            binding.searchingAnimation.pauseAnimation()
        }
        if(binding.searchingOpponentLayout.isVisible){
            binding.searchingOpponentLayout.visibility = View.GONE
        }
        binding.playerRV.visibility = View.VISIBLE
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
    private fun handleSearchingProcess() {
        if(binding.playerRV.isVisible){
            binding.playerRV.visibility = View.GONE
        }
        if(binding.noResultLayout.isVisible){
            binding.noResultLayout.visibility = View.GONE
        }
        if(binding.searchingOpponentLayout.isVisible){
            binding.searchingOpponentLayout.visibility = View.GONE
        }
        binding.searchingLayout.visibility = View.VISIBLE
        binding.searchingAnimation.setAnimation(R.raw.loading)
        binding.searchingAnimation.playAnimation()

    }
    private fun handleNoResult(){
        if(binding.playerRV.isVisible){
            binding.playerRV.visibility = View.GONE
        }
        if(binding.searchingOpponentLayout.isVisible){
            binding.searchingOpponentLayout.visibility = View.GONE
        }
        if(binding.searchingLayout.isVisible){
            binding.searchingLayout.visibility = View.GONE
            binding.searchingAnimation.pauseAnimation()
        }
        binding.noResultMessageText.text = getString(R.string.no_result_text)
        binding.noResultImage.setImageResource(R.drawable.no_result)
        binding.noResultLayout.visibility = View.VISIBLE

    }
    private fun navigateToNewActivity(newActivity : Class<*> , opponent : OnlinePlayer) {
        val newActivityIntent = Intent (baseContext , newActivity)
        newActivityIntent.putExtra("opponent" , opponent )
        newActivityIntent.putExtra("currentOnlinePlayer" , currentOnlinePlayer )
        newActivityIntent.putExtra("player_type_key" , "sender")
        startActivity(newActivityIntent)

    }


}



