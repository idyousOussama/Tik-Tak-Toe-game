package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.tictachero.Models.AppLifecycleTracker
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.Models.Player
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivitySelectPlapyModeBinding
import com.example.tictachero.databinding.NoInternetDialogViewBinding

class SelectPlayModeActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySelectPlapyModeBinding
    companion object {
        var currentPlayer  : Player? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectPlapyModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpPlayerAccount()
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
}