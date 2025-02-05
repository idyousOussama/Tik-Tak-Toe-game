package com.example.tictachero.Activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tictachero.Listeners.PlayerEventListeners
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityPlayWithFriendModeBinding
import com.example.tictachero.databinding.LoseAndWinBottomSheetCustomViewBinding
import com.example.tictachero.databinding.MatchDrawCustomViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PlayWithFriendModeActivity : AppCompatActivity() , View.OnClickListener {
    private lateinit var binding : ActivityPlayWithFriendModeBinding
    var player1Scor = 0
    var player2Scor = 0
    var allMatches = 3
    var pastMatch = 0
    var roundIsDone = false
    var winner = ""
     var currentPlayer  = "O"
    var filledPos : MutableList<String> = mutableListOf("","","","","","","","","")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayWithFriendModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun restartRound() {
        pastMatch++
        binding.matchesNumber.text = pastMatch.toString() +" "+"/" + " " + allMatches.toString()
        if(currentPlayer == "X") {
            binding.player2BarLayout.setBackgroundResource( R.drawable.disable_player_bar_background)
            binding.player1BarLayout.setBackgroundResource(R.drawable.enable_player_bar_background)

        } else {
            binding.player2BarLayout.setBackgroundResource( R.drawable.enable_player_bar_background)
            binding.player1BarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)

        }

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
                val clickPos = (v?.tag as String).toInt()
                if(filledPos[clickPos].isEmpty()) {
                     if(currentPlayer == "O") {
                        currentPlayer =  "X"
                         binding.player1BarLayout.setBackgroundResource(R.drawable.enable_player_bar_background)
                         binding.player2BarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)

                     } else  {
                         currentPlayer = "O"
                         binding.player1BarLayout.setBackgroundResource(R.drawable.disable_player_bar_background)
                         binding.player2BarLayout.setBackgroundResource(R.drawable.enable_player_bar_background)

                     }
                    filledPos[clickPos] = currentPlayer
                    setUi()
                    checkForWinner()

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
                    player1Scor++

                }else {
                    player2Scor++
                }

                binding.matchResultText.text = player1Scor.toString() + " : " + player2Scor.toString()
                upDateWinBtn(i)
            }
        }
        if(filledPos.none(){it.isEmpty()}) {
            roundIsDone = true
            binding.startAndRestartGame.visibility = View.VISIBLE
        }
        if(pastMatch >= 3  && player1Scor > player2Scor  && roundIsDone) {
            showBottomSheet("Player1")
        }else if (pastMatch >= 3  && player2Scor > player1Scor && roundIsDone) {
            showBottomSheet("Player2")
        }else if(pastMatch >= 3  && player1Scor == player2Scor && roundIsDone){
            showDicussDialog()
        }
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
            binding.btn8.id
        )
        for ( item in winningPos) {
            findViewById<TextView>(btnsList[item]).setBackgroundResource(R.drawable.roze_background)
        }
    }
    private fun showBottomSheet (player: String ) {
        val bottomSheetView = layoutInflater.inflate(R.layout.lose_and_win_bottom_sheet_custom_view,null)
        val bottomSheetBinding = LoseAndWinBottomSheetCustomViewBinding.bind(bottomSheetView)
        val bottomSheet = BottomSheetDialog(this , R.style.BottomSheetDialog)
        bottomSheet.setContentView(bottomSheetView)
        bottomSheet.setCancelable(false)
        bottomSheetBinding.bottomSheetAnimation.setAnimation(R.raw.win_animation)
        bottomSheetBinding.bottomSheetMessageText.text = player + " " + "is win...! "
        bottomSheetBinding.bottomSheetMessageText.setTextColor(resources.getColor(R.color.red))

        bottomSheetBinding.bottomSheetAnimation.playAnimation()
        bottomSheetBinding.bottomSheetRestartBtn.setOnClickListener {
            restartGame()
            bottomSheet.cancel()
        }
        bottomSheetBinding.bottomSheetBackBtn.setOnClickListener {
            finish()
        }
        bottomSheet.show()
    }
    private fun restartGame() {
        pastMatch = 0
        player1Scor = 0
        player2Scor = 0
        binding.matchResultText.text = player1Scor.toString() + " : " + player2Scor.toString()
        restartRound()
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
            finish()
        }

        dialog.show()
    }
}