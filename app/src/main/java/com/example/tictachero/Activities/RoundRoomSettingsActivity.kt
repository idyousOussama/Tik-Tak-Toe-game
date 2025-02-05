package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityRoundRoomSettingsBinding
import com.example.tictachero.databinding.ConfirmationDialogViewBinding
import com.example.tictachero.databinding.CounterDialogViewBinding
import com.example.tictachero.databinding.NoInternetDialogViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoundRoomSettingsActivity : AppCompatActivity() {
    private lateinit var binding  : ActivityRoundRoomSettingsBinding
    private val firebaseDB  = FirebaseDatabase.getInstance()
    var opponentPlayer : OnlinePlayer? = null
    var playerType : String = ""
    private val handler = Handler(Looper.getMainLooper()) // Declare Handler globally
    private lateinit var runnable: Runnable
    var battleRoomRef: DatabaseReference? = null

    var currentOnlinePlayer : OnlinePlayer?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding = ActivityRoundRoomSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
setUpPlayersCards()
opponentGameStatusListener()
        handleBtns()
        onBackPressonBachPressed()

    }

    private fun opponentGameStatusListener() {
        if(playerType == "sender"){
            battleRoomRef = firebaseDB.getReference("Rooms").child(currentOnlinePlayer!!.playerId + opponentPlayer?.playerId)
        }else if(playerType == "reciver") {
            battleRoomRef = firebaseDB.getReference("Rooms").child(  opponentPlayer?.playerId + currentOnlinePlayer!!.playerId )
        }
            val playerRef = battleRoomRef?.child("players")!!.child(opponentPlayer!!.playerId)
            playerRef.child("gameStatus").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()) {
                        if (p0.exists()) {
                            val opponentGameStatus  = p0.getValue(String::class.java)
                            when(opponentGameStatus){
                                GameStatus.JOINED.name -> {
                                    setUpOpponentPlayerCard(opponentPlayer!!)
                                    Toast.makeText(baseContext , "Opponent is Joined" , Toast.LENGTH_SHORT).show()
                                }
                                GameStatus.READY.name -> {
                                    enableStartBtn()
                                    Toast.makeText(baseContext , "Opponent is ready to start" , Toast.LENGTH_SHORT).show()
                                }
                                GameStatus.START.name -> {
                                    showCounterDialog()
                                }
                                GameStatus.LEAVE.name -> {
                                  disableStartBtn()
                                  val  confiramtionDialogTitle =  getString(R.string.dialog_leave_room_title)
                                  val  confiramtionDialogMessage =  getString(R.string.dialog_opponent_leave_room_message)
                                  val positiveBtnText = getString(R.string.wait_text)
                                  val negativeBtnText = getString(R.string.leave_text)
                                    shawConferamtiontDialog(confiramtionDialogTitle,confiramtionDialogMessage,positiveBtnText,negativeBtnText)
                                    Toast.makeText(baseContext , "Opponent is ready to start" , Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
    private fun shawConferamtiontDialog(  confiramtionDialogTitle: String, confiramtionDialogMessage: String, positiveBtnText: String, negativeBtnText: String) {
val confirmationDialogView  = layoutInflater.inflate(R.layout.confirmation_dialog_view,null)
val dialogBinding = ConfirmationDialogViewBinding.bind(confirmationDialogView)
  dialogBinding.confirmationDialogTitle.text = confiramtionDialogTitle
  dialogBinding.confirmationDialogMessage.text = confiramtionDialogMessage
  dialogBinding.confirmationDialogPositiveBtn.text = positiveBtnText
  dialogBinding.confirmationDialogNegativeBtn.text = negativeBtnText
val confiramtionDialog = Dialog(this)
confiramtionDialog.setContentView(confirmationDialogView)
        confiramtionDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.confirmationDialogNegativeBtn.setOnClickListener {
            when(negativeBtnText) {
                getString(R.string.leave_text) -> {
                    upDatePlayerGameStatus(GameStatus.LEAVE.name)
                }
            }
         Toast.makeText(baseContext , "jkjdshks4" , Toast.LENGTH_SHORT).show()
        }
        dialogBinding.confirmationDialogPositiveBtn.setOnClickListener {
            confiramtionDialog.dismiss()
        }
        confiramtionDialog.show()
    }

    private fun removeBattleRoom(confiramtionDialog: Dialog) {
Toast.makeText(baseContext , "jkjdshks0" , Toast.LENGTH_SHORT).show()
        battleRoomRef!!.removeValue().addOnCompleteListener {task ->
            if (task.isSuccessful){
                confiramtionDialog.dismiss()
                finish()
            }

        }


    }

    private fun setUpPlayersCards() {
        opponentPlayer = intent.getSerializableExtra("opponent") as OnlinePlayer
       playerType = intent.getStringExtra("player_type_key" ).toString()
        currentOnlinePlayer = intent.getSerializableExtra("currentOnlinePlayer") as OnlinePlayer
  if( playerType.isNotEmpty() &&  playerType == "sender"){
      binding.readyAndSatrtBtn.text = getString(R.string.start_text)
      setUpCurrentPlayerCard()
      binding.waitingTojoinOppenentImage.setImageResource(opponentPlayer!!.playerImage)
  }else if (playerType.isNotEmpty() &&  playerType == "reciver") {
      binding.readyAndSatrtBtn.text = getString(R.string.join_text)
      setUpOpponentPlayerCard (opponentPlayer!!)
      setUpCurrentPlayerCard()
  }

    }
    private fun enableStartBtn(){
        binding.readyAndSatrtBtn.apply{
            setBackgroundResource(R.drawable.deep_blue_background)
            text = getString(R.string.start_text)
            setTextColor(resources.getColor(R.color.white))
            isEnabled = true
        }
    }
    private fun disableStartBtn(){
        binding.readyAndSatrtBtn.apply{
            setBackgroundResource(R.drawable.light_blue_background)
            text = getString(R.string.start_text)
            setTextColor(resources.getColor(R.color.light_gray))
            isEnabled = false
        }
    }
    private fun setUpCurrentPlayerCard() {
        binding.waitingRoomCurrentPlayerImage.setImageResource(currentOnlinePlayer!!.playerImage)
        binding.waitingRoomCurrentPlayerName.text  = getString(R.string.You_text)
        binding.waitingRoomCurrentPlayerId.text = "ID : " + currentOnlinePlayer!!.ID
        binding.waitingRoomCurrentPlayerDiamondNum.text = currentOnlinePlayer!!.daimondNum.toString()
        binding.waitingRoomCurrentPlayerNotesNum.text = "Notes " + currentOnlinePlayer!!.notesNum.toString()    }

    private fun setUpOpponentPlayerCard(opponentPlayer: OnlinePlayer) {
        binding.waitingTojoinLayout.visibility = View.GONE
        binding.waitingTojoinOpponentPlayerLayout.visibility = View.VISIBLE
        binding.waitingRoomOppenenetImage.setImageResource(opponentPlayer.playerImage)
        binding.waitingRoomOppenenetName.text  = opponentPlayer.playerName
        binding.waitingRoomOppenenetId.text ="ID : " + opponentPlayer.ID
        binding.waitingRoomOppenenetDiamondNum.text = opponentPlayer.daimondNum.toString()
        binding.waitingRoomOppenenetNotesNum.text = "Notes " + opponentPlayer.notesNum.toString()
    }
    private fun handleBtns() {
        binding.readyAndSatrtBtn.setOnClickListener {
if(playerIsConcted()) {
    binding.readyAndSatrtBtn.visibility = View.GONE
    if(playerType == "sender") {
        showCounterDialog()
        upDatePlayerGameStatus(GameStatus.START.name)

    }else if(playerType == "reciver") {
        upDatePlayerGameStatus(GameStatus.READY.name)
    }
}else {
    showNoInternetDialog()
}
            binding.leaveBtn.setOnClickListener {
                if (playerIsConcted()){
                    val  confiramtionDialogTitle =  getString(R.string.dialog_leave_room_title)
                    val  confiramtionDialogMessage =  getString(R.string.confiramtion_to_leave_waiting_room)
                    val positiveBtnText = getString(R.string.wait_text)
                    val negativeBtnText = getString(R.string.leave_text)
                    shawConferamtiontDialog(confiramtionDialogTitle,confiramtionDialogMessage,positiveBtnText,negativeBtnText)
                }else {
                    showNoInternetDialog()
                }
            }

        }
    }
    private fun upDatePlayerGameStatus(playerGameStatus: String) {
        val playerRef = battleRoomRef?.child("players")!!.child(currentOnlinePlayer!!.playerId)
        val gameStatusUpDate = mapOf<String , String>(
            "gameStatus" to playerGameStatus
        )
        playerRef?.updateChildren(gameStatusUpDate)
    }

    private fun showCounterDialog() {
val counterDilaogView = layoutInflater.inflate(R.layout.counter_dialog_view,null)
val dialogBinding = CounterDialogViewBinding.bind(counterDilaogView)
val counterDialog = Dialog(this)
        counterDialog.setCancelable(false)
 counterDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
counterDialog.setContentView(counterDilaogView)
counter(dialogBinding.counterText)
        counterDialog.show()

    }



    private fun counter(counterText: TextView) {
        var counter = 3 // Starts from 3

        runnable = object : Runnable {
            override fun run() {
                if (counter > 0) {
                    counter--
                    counterText.text = counter.toString()
                    handler.postDelayed(this, 1000) // Repeat after 500ms
                } else {
                    handler.removeCallbacks(this) // Stop Handler BEFORE navigation
                    navigateToNewActivity(PlayOnlineActivity::class.java)
                }
            }
        }

        handler.post(runnable) // Start the countdown
    }
    private fun navigateToNewActivity(newActivity: Class<*>) {
val newActivityIntent = Intent(this,newActivity )
        newActivityIntent.putExtra("player_type_key" , playerType)
        startActivity(newActivityIntent)
        finish()

    }
    private fun onBackPressonBachPressed() {
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(playerIsConcted()){
                    val  confiramtionDialogTitle =  getString(R.string.dialog_leave_room_title)
                    val  confiramtionDialogMessage =  getString(R.string.confiramtion_to_leave_waiting_room)
                    val positiveBtnText = getString(R.string.wait_text)
                    val negativeBtnText = getString(R.string.leave_text)
                    shawConferamtiontDialog(confiramtionDialogTitle,confiramtionDialogMessage,positiveBtnText,negativeBtnText)
                }else {
                    showNoInternetDialog()
                }

            }
        })
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