package com.example.tictachero.Activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tictachero.DataBases.RoomDatabases.ViewModels.GameViewModel
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.Player
import com.example.tictachero.Models.PlayerStatus
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivitySplashBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID


class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
    val mainScope = CoroutineScope(Dispatchers.Main)
    companion object {
        var gameViewModel : GameViewModel? = null
        val firebaseDB  : FirebaseDatabase = FirebaseDatabase.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding  = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gameViewModel = ViewModelProvider(this).get(GameViewModel::class.java)
        getPLayerAccount()
    }
    private fun getPLayerAccount() {
        mainScope.launch {
            val logainedAccount  = gameViewModel?.getPlayerLogainedAccount()
         if (logainedAccount != null) {
             navigateToNewActivity(SelectPlayModeActivity :: class.java , logainedAccount)
         }else {
             if(playerIsConcted()){
                 val playerRef = firebaseDB.getReference("Players")
                 val playerId = playerRef.push().key
                 val ID : String = generateSecureNumericId()
                 val playerImage  = arrayOf(R.drawable.player_1,R.drawable.player_2,R.drawable.player_3,R.drawable.player_4,R.drawable.player_5,R.drawable.player_6,R.drawable.player_7).random()
                 val newPlayerOnline = OnlinePlayer(playerId!!,ID,"player " + ID , playerImage , 0,0,0,0 , PlayerStatus.ONLINE , GameStatus.CREATED)
                 val newPlayer = Player(newPlayerOnline.playerId,ID,newPlayerOnline.playerName , newPlayerOnline.playerImage ,true,0,3,0 ,0,0 )
                 playerRef.child(playerId).setValue(newPlayerOnline).addOnCompleteListener {task ->
                     if(task.isSuccessful) {
                         mainScope.launch {
                             gameViewModel?.insertNewPlayerAccount(newPlayer)
                             gameViewModel?.getPlayerLogainedAccount()?.let {
                                 navigateToNewActivity(SelectPlayModeActivity :: class.java ,
  it                                 )
                             }
                         }
                     }else if(task.isCanceled) {
                         Toast.makeText(baseContext," Sign-in failed. Please try again later" , Toast.LENGTH_SHORT).show()
                     }
                 }
             }else {
                 Toast.makeText(baseContext , "Please check your internet and try again" , Toast.LENGTH_SHORT).show()
             }
         }
        }
    }
    private fun navigateToNewActivity(newActivity: Class<*>, logainedAccount: Player) {
var newActivityIntent = Intent(this , newActivity)
        newActivityIntent.putExtra("logainedAccount" , logainedAccount)
        startActivity(newActivityIntent)
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
    fun generateSecureNumericId(length: Int = 12): String {
        val random = SecureRandom()
        return (1..length)
            .map { random.nextInt(10) } // Generates numbers 0-9
            .joinToString("")
    }
}