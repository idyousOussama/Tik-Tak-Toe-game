package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictachero.Models.GameData
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityPlayOnlineBinding
import com.example.tictachero.databinding.LoseAndWinBottomSheetCustomViewBinding
import com.example.tictachero.databinding.NoInternetDialogViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlayOnlineActivity : AppCompatActivity() , View.OnClickListener {
    private lateinit var binding : ActivityPlayOnlineBinding
    private var onlineCurrentPlayer : OnlinePlayer? = null
    private var opponentplayer : OnlinePlayer? = null
    private var playerType : String  = ""
    private var currentPlayerScore = 0
    private var opponentPlayerScore = 0
    private var gameIsOver : Boolean = false
    private var gameData : GameData? = null
    private var filledPos : MutableList<String> = mutableListOf("","","","","","","","","")
    private var  firebaseDB = FirebaseDatabase.getInstance()
    private var battleRoomRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
 binding = ActivityPlayOnlineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getPlayers()
        handleStartGameBtn()
        handleDataGameListener()
        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

    }

    private fun handleDataGameListener() {
        battleRoomRef!!.child("GameData")
        battleRoomRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    for(item in p0.children) {
                        gameData = p0.getValue(GameData::class.java)
                    }
                if(gameData!!.winner.isNotEmpty()) {
                    gameIsOver = true
                    if(gameData!!.winner == "O") {
                        if(playerType =="sender") {
                         opponentPlayerScore ++
                        }else {
                         currentPlayerScore++
                        }
                    }else {
                        if(playerType =="sender") {
                            currentPlayerScore++
                        }else {
                            opponentPlayerScore ++
                        }
                    setPlayersScore()
                    }
                }




                }


            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setPlayersScore() {
        binding.matchResultText.text = currentPlayerScore.toString() +" : "+ opponentPlayerScore.toString()
    }


    private fun handleStartGameBtn() {
        binding.startAndRestartGame.setOnClickListener {
 if(playerIsConcted()) {
     gameData = GameData("","",filledPos , false)
    battleRoomRef!!.child("GameData").setValue(gameData)

 }else {
     showNoInternetDialog()
 }
        }
    }

    private fun getPlayers() {
        onlineCurrentPlayer = intent.getSerializableExtra("currentOnlinePlayer") as OnlinePlayer
        opponentplayer =  intent.getSerializableExtra("opponent") as OnlinePlayer
        playerType = intent.getStringExtra("player_type_key").toString()
        setCurrentPlayerInfo()
        if(onlineCurrentPlayer != null && opponentplayer != null ) {
            if(playerType == "sender"){
                battleRoomRef = firebaseDB.getReference("Rooms").child(onlineCurrentPlayer!!.playerId + opponentplayer?.playerId)
                binding.currentplayerSymobolImage.setImageResource(R.drawable.x_symbol_image)
                binding.opponentPlayerSymbolImage.setImageResource(R.drawable.o_symbol_image)
                binding.currentplayerName.text = getString(R.string.You_text)
            }else if(playerType == "reciver"){
                battleRoomRef = firebaseDB.getReference("Rooms").child(  opponentplayer?.playerId + onlineCurrentPlayer!!.playerId )
                binding.opponentPlayerSymbolImage.setImageResource(R.drawable.x_symbol_image)
                binding.currentplayerSymobolImage.setImageResource(R.drawable.o_symbol_image)
                binding.startAndRestartGame.visibility = View.GONE
            }

            setPlayersInfo()


        }


    }

    private fun setPlayersInfo() {
        binding.currentPlayerResultBarImage.setImageResource(onlineCurrentPlayer!!.playerImage)
        binding.opponentPlayerResultBarImage.setImageResource(opponentplayer!!.playerImage)
        binding.opponentPlayerImage.setImageResource(opponentplayer!!.playerImage)
        binding.currentplayerImage.setImageResource(onlineCurrentPlayer!!.playerImage)
        binding.opponentPlayerName.text = opponentplayer!!.playerName
        binding.startAndRestartGame.visibility = View.VISIBLE
    }

    private fun setCurrentPlayerInfo() {
        binding.opponentPlayerImage.setImageResource(opponentplayer!!.playerImage)
        binding.opponentPlayerName.text = opponentplayer?.playerName
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

    override fun onClick(v: View?) {
        val clickPo = (v?.tag as String).toInt()
    if(gameData!!.filledPos!![clickPo].isEmpty()){
        if(playerIsConcted()) {
            if(playerType == "sender") {
                if(gameData!!.currentPlayer == "O" || gameData!!.currentPlayer.isEmpty()) {
                    gameData!!.currentPlayer = "X"
                    filledPos[clickPo] = "X"
                    setUi()
                    checkForWinner()
                }
            }else if(playerType == "reciver"){
                if(gameData!!.currentPlayer == "X") {
                    gameData!!.currentPlayer = "O"
                    filledPos[clickPo] = "O"
                    setUi()
                    checkForWinner()
                }
            }




        }else {
            showNoInternetDialog()
        }
    }

    }
    private fun setUi() {
        binding.btn0.apply {
            text  = filledPos[0]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
        binding.btn1.apply {
            text  = filledPos[1]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
        binding.btn2.apply {
            text  = filledPos[2]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
        binding.btn3.apply {
            text  = filledPos[3]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
        binding.btn4.apply {
            text  = filledPos[4]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
        binding.btn5.apply {
            text  = filledPos[5]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
        binding.btn6.apply {
            text  = filledPos[6]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
        binding.btn7.apply {
            text  = filledPos[7]
            setBackgroundResource(R.drawable.enable_btn_background)

        }
        binding.btn8.apply {
            text  = filledPos[8]
            setBackgroundResource(R.drawable.enable_btn_background)
        }
    }
    private fun checkForWinner() {
        val winningPos = arrayOf(
            intArrayOf(0,1,2),
            intArrayOf(3,4,5),
            intArrayOf(6,7,8),
            intArrayOf(0,3,6),
            intArrayOf(1,4,7),
            intArrayOf(2,5,8),
            intArrayOf(0,4,8),
            intArrayOf(2,4,6),
        )

        for (i in winningPos) {
            if(gameData!!.filledPos!![i[0]] == gameData!!.filledPos!![i[1]] && gameData!!.filledPos!![i[1]] == gameData!!.filledPos!![i[2]] && gameData!!.filledPos!![i[0]].isNotEmpty()){
            gameData!!.winner = gameData!!.filledPos!![i[0]]

            }
        }
        if(filledPos.none(){it.isEmpty()}){
            gameData!!.winner = "draw"
            gameIsOver  = true
        }

     setCurrentGameData()


    }

    private fun setCurrentGameData() {
        battleRoomRef!!.child("GameData").setValue(gameData)

    }
    private fun showBottomSheet (stuation : String , animation : Int) {
        val bottomSheetView = layoutInflater.inflate(R.layout.lose_and_win_bottom_sheet_custom_view,null)
        val bottomSheetBinding = LoseAndWinBottomSheetCustomViewBinding.bind(bottomSheetView)
        val bottomSheet = BottomSheetDialog(this , R.style.BottomSheetDialog)
        bottomSheet.setContentView(bottomSheetView)
        bottomSheet.setCancelable(false)
        bottomSheetBinding.bottomSheetAnimation.setAnimation(animation)
        bottomSheetBinding.bottomSheetAnimation.playAnimation()
        when(stuation) {
            "lose" -> {
                bottomSheetBinding.bottomSheetMessageText.text = getString(R.string.you_lose_text)
                bottomSheetBinding.bottomSheetMessageText.setTextColor(resources.getColor(R.color.red))
            }
            "win" -> {
                bottomSheetBinding.bottomSheetMessageText.text = getString(R.string.you_win_text)
                bottomSheetBinding.bottomSheetMessageText.setTextColor(resources.getColor(R.color.green))
            }
        }
        bottomSheetBinding.bottomSheetRestartBtn.setOnClickListener {
            bottomSheet.cancel()
        }
        bottomSheetBinding.bottomSheetBackBtn.setOnClickListener {
        }
        bottomSheet.show()
    }


}