package com.example.tictachero.Listeners

import android.widget.ProgressBar
import android.widget.TextView
import com.example.tictachero.Models.OnlinePlayer


interface SearchedPlayerListener {
    fun onRequestBtnClicked(player : OnlinePlayer , requestBtn : TextView  ,processProgress : ProgressBar)
}