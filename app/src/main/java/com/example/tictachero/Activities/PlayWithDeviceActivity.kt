package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.gameViewModel
import com.example.tictachero.Listeners.PlayerEventListeners
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityPlayWithDeviceBinding
import com.example.tictachero.databinding.LoseAndWinBottomSheetCustomViewBinding
import com.example.tictachero.databinding.MatchDrawCustomViewBinding
import com.example.tictachero.databinding.SelectGameModeDialogCustomViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayWithDeviceActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityPlayWithDeviceBinding
    private var pastMatch = 0
    var deviceScore = 0
    var playerScore = 0
    var allMatches = 3
    val mainScope = CoroutineScope(Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    var filledPos : MutableList<String> = mutableListOf("","","","","","","","","")
    var deviceIsPaly  = true
    var winner  =""
    var roundIsDone = true
    var gameMode : String  = "Easy"
    var diamodNum = 1
    var playerPlayListener : PlayerEventListeners ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayWithDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectGameMode()
        playerPlayListener  = object : PlayerEventListeners {
            override fun onPlayerIsPlayed() {
                val runnable = object : Runnable {
                    override fun run() {
                        when(gameMode) {
                            "Easy" -> {
                                easyMode()
                            }
                            "Medium" -> {
                                mediumMode()
                            }
                            "Hard" -> {
                                hardMode()
                            }
                        }
                    }
                }
                handler.postDelayed(runnable,2000)
            }
        }

        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)
        binding.startAndRestartGame.setOnClickListener {
            restartRound()
        }
    }

    private fun mediumMode() {

        if(!roundIsDone) {
            val possiblePos : ArrayList<Int>  = ArrayList()
            val defaultPos = arrayOf(
                intArrayOf(0,1,2),
                intArrayOf(3,4,5),
                intArrayOf(6,7,8),
                intArrayOf(0,3,6),
                intArrayOf(1,4,7),
                intArrayOf(2,5,8),
                intArrayOf(0,4,8),
                intArrayOf(2,4,6),
            )
        for (i  in defaultPos) {
             if(filledPos[i[0]] == filledPos[i[1]] && filledPos[i[2]].isEmpty()&& filledPos[i[0]].isNotEmpty()) {
                 possiblePos.add(i[2])
             }else if (filledPos[i[1]] == filledPos[i[2]] && filledPos[i[0]].isEmpty()&& filledPos[i[2]].isNotEmpty()) {
                 possiblePos.add(i[0])
             }
        }
            if(possiblePos.isNotEmpty()) {
                val randomPos =possiblePos.random()
                    filledPos[randomPos] = "O"
                    setUi()
                    checkForWinner()
                    deviceIsPaly = true
                    binding.deviceBarLayout.setBackgroundResource(R.drawable.enable_player_bar_background)
                    binding.youBarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)
            }else {
                easyMode()
            }
        }
    }
    private fun selectGameMode() {
        binding.selectGameMode.setOnClickListener {
            showSelecteGameModeDialog()
        }
    }

    private fun showSelecteGameModeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.select_game_mode_dialog_custom_view,null)
        val dialogBinding = SelectGameModeDialogCustomViewBinding.bind(dialogView)
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        when(gameMode) {
            "Easy" -> {
                dialogBinding.easyMode.setBackgroundResource(R.drawable.green_stroke_background)
                           }
            "Medium" -> {
                dialogBinding.mediumMode.setBackgroundResource(R.drawable.green_stroke_background)
            }
            "Hard" -> {
                dialogBinding.hardMode.setBackgroundResource(R.drawable.green_stroke_background)
            }
        }
        dialogBinding.easyMode.setOnClickListener {
            setNewGameMode("Easy",dialog)
        }
        dialogBinding.mediumMode.setOnClickListener {
            setNewGameMode("Medium",dialog)
        }
        dialogBinding.hardMode.setOnClickListener {
            setNewGameMode("Hard",dialog)
        }
        dialog.show()
    }

    private fun setNewGameMode(mode: String, dialog: Dialog) {
        when (mode) {
            "Easy" -> {
                diamodNum = 1
                binding.playModeImage.setImageResource(R.drawable.easy_game)
            }
            "Medium" -> {
                diamodNum = 2
                binding.playModeImage.setImageResource(R.drawable.medium_game)
            }
            "Hard" -> {
                diamodNum = 3
                binding.playModeImage.setImageResource(R.drawable.hard_game)
            }
        }
        gameMode = mode
        binding.diamodNumber.text  = "+" + diamodNum.toString()
        binding.gameModeText.text  = gameMode
        dialog.dismiss()
    }

    private fun restartRound() {
        pastMatch++
        binding.matchesNumber.text = pastMatch.toString() +" "+"/" + " " + allMatches.toString()
        binding.deviceBarLayout.setBackgroundResource( R.drawable.enable_player_bar_background)
        binding.youBarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)
        deviceIsPaly = true
        winner = ""
        roundIsDone = false
        upDateGame()
        binding.startAndRestartGame.text = getString(R.string.restart_text)
        binding.startAndRestartGame.visibility = View.GONE

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

    override fun onClick(v: View?) {
        if(!roundIsDone) {
            if (deviceIsPaly) {
                val clickPos = (v?.tag as String).toInt()
                if(filledPos[clickPos].isEmpty()) {
                    filledPos[clickPos] = "X"
                    setUi()
                    checkForWinner()
                    deviceIsPaly = false
                    playerPlayListener?.onPlayerIsPlayed()
                    binding.deviceBarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)
                    binding.youBarLayout.setBackgroundResource(R.drawable.enable_player_bar_background)
                }

            }
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
                if(filledPos[i[0]] == filledPos[i[1]] && filledPos[i[1]] == filledPos[i[2]] && filledPos[i[0]].isNotEmpty()){
                    winner = filledPos[i[0]]
                    roundIsDone = true
                    binding.startAndRestartGame.visibility = View.VISIBLE

                    if (winner  == "X"){
               playerScore++
                    }else {
               deviceScore++
                }
                    binding.matchResultText.text = deviceScore.toString() + " : " + playerScore.toString()
                    upDateWinBtn(i)
                }
            }
            if(filledPos.none(){it.isEmpty()}){
                roundIsDone = true
                binding.startAndRestartGame.visibility = View.VISIBLE            }
        if(pastMatch >= 3  && playerScore > deviceScore  && roundIsDone) {
            showBottomSheet("win", R.raw.win_animation)
            addDiamondToWalet()
        }else if (pastMatch >= 3  && deviceScore > playerScore && roundIsDone) {
            showBottomSheet("lose", R.raw.lose_animation)

        }else if(pastMatch >= 3  && deviceScore == playerScore && roundIsDone){
            showDicussDialog()
        }

    }
    private fun addDiamondToWalet() {
        mainScope.launch {
            gameViewModel?.increasePlayerDiamod(diamodNum,currentPlayer!!.playerId)
        }
    }

    private fun showDicussDialog() {
        val dialogView = layoutInflater.inflate(R.layout.match_draw_custom_view,null)
        val dialogBinding = MatchDrawCustomViewBinding.bind(dialogView)
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialogBinding.dialogAnimation.setAnimation(R.raw.draw_match_animation)
        dialogBinding.dialogAnimation.playAnimation()
        dialogBinding.dialogMessage.text = getString(R.string.winner_unknow_text)
        dialogBinding.positiveBtn.setOnClickListener {
            dialog.dismiss()
            restartRound()
        }
        dialogBinding.negativeBtn.setOnClickListener {
            dialog.dismiss()
            navigateToNewActivity(SelectPlayModeActivity::class.java)
        }
dialog.show()
    }

    private fun navigateToNewActivity(newActivity: Class<*>) {
startActivity(Intent(baseContext,newActivity))
    }

    private fun upDateGame() {
        filledPos.indices.forEach { index ->
            filledPos[index] = ""
        }
        setUi()
    }
     private fun upDateWinBtn(winningPos : IntArray){
         var btnsList  = arrayOf(
                 binding.btn0.id,
                 binding.btn1.id,
                 binding.btn2.id,
                 binding.btn3.id,
                 binding.btn4.id,
                 binding.btn5.id,
                 binding.btn6.id,
                 binding.btn7.id,
                 binding.btn8.id)
      for ( item in winningPos) {
          findViewById<TextView>(btnsList[item]).setBackgroundResource(R.drawable.roze_background)
      }
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
restartGame()
            bottomSheet.cancel()
        }
        bottomSheetBinding.bottomSheetBackBtn.setOnClickListener {
            navigateToNewActivity(SelectPlayModeActivity::class.java)
        }
        bottomSheet.show()
    }

    private fun restartGame() {
        pastMatch = 0
        deviceScore = 0
        playerScore = 0
        binding.matchResultText.text = deviceScore.toString() + " : " + playerScore.toString()
        restartRound()
    }
private fun easyMode () {
    if(!roundIsDone) {
        val randomPos = filledPos.indexOf(filledPos.filter { it.isEmpty()}.random())
        if(randomPos != null) {
            filledPos[randomPos] = "O"
            setUi()
            checkForWinner()
            deviceIsPaly = true
            binding.deviceBarLayout.setBackgroundResource(R.drawable.enable_player_bar_background)
            binding.youBarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)

        }
    }
}
private fun hardMode() {
    val possiblePos : ArrayList<Int>  = ArrayList()
    val possibleWinPos : ArrayList<Int>  = ArrayList()
    if(!roundIsDone) {


        val defaultPos = arrayOf(
            intArrayOf(0,1,2),
            intArrayOf(3,4,5),
            intArrayOf(6,7,8),
            intArrayOf(0,3,6),
            intArrayOf(1,4,7),
            intArrayOf(2,5,8),
            intArrayOf(0,4,8),
            intArrayOf(2,4,6),
        )
        for (i  in defaultPos) {
            if(filledPos[i[0]] == "O" && filledPos[i[1]] == filledPos[i[0]] && filledPos[i[2]].isEmpty()) {
                possibleWinPos.add(i[2])
            }else if(filledPos[i[0]] == "O" && filledPos[i[0]] == filledPos[i[2]] && filledPos[i[1]].isEmpty()) {
                possibleWinPos.add(i[1])
            }else if(filledPos[i[1]] == "O" && filledPos[i[1]] == filledPos[i[2]] && filledPos[i[0]].isEmpty()) {
                possibleWinPos.add(i[0])
            }else if(filledPos[i[0]] == "X" && filledPos[i[1]] == filledPos[i[0]] && filledPos[i[2]].isEmpty()) {
                possiblePos.add(i[2])
            }else if(filledPos[i[0]] == "X" && filledPos[i[0]] == filledPos[i[2]] && filledPos[i[1]].isEmpty()) {
                possiblePos.add(i[1])
            }else if(filledPos[i[1]] == "X" && filledPos[i[1]] == filledPos[i[2]] && filledPos[i[0]].isEmpty()) {
                possiblePos.add(i[0])
            }
        }
        if(possibleWinPos.isNotEmpty()) {
            val randomPos =possibleWinPos.random()
            setDevicePlay(randomPos)

        } else if(possiblePos.isNotEmpty()) {
            val randomPos =possiblePos.random()
            setDevicePlay(randomPos)
         
        }else {
            easyMode()
        }
}



}

    private fun setDevicePlay(randomPos: Int) {
        filledPos[randomPos] = "O"
        setUi()
        checkForWinner()
        deviceIsPaly = true
        binding.deviceBarLayout.setBackgroundResource(R.drawable.enable_player_bar_background)
        binding.youBarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)


    }
}
