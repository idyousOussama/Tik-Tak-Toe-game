package com.example.tictachero.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tictachero.Listeners.SearchedPlayerListener
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.PlayerStatus
import com.example.tictachero.R
import com.example.tictachero.databinding.SearchedPlayerCustomViewBinding
class SearchedPlayerAdapter : RecyclerView.Adapter<SearchedPlayerAdapter.SearchedPlayerVH>() {
 var playersList :ArrayList<OnlinePlayer> = ArrayList()
    var playerItemListener :SearchedPlayerListener? = null
 fun setPlayerList (playersList :ArrayList<OnlinePlayer>){
     this.playersList = playersList
     notifyDataSetChanged()
 }

    fun setPlayerListener( playerItemListener :SearchedPlayerListener ) {
        this. playerItemListener =  playerItemListener
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SearchedPlayerVH {
val view = LayoutInflater.from(p0.context).inflate(R.layout.searched_player_custom_view,p0,false)
return SearchedPlayerVH(view)
    }
    override fun getItemCount(): Int {
        return playersList.size
    }

    override fun onBindViewHolder(p0: SearchedPlayerVH, p1: Int) {
    val player = playersList.get(p1)
        p0.setPlayer(player.playerName,player.ID, player.playerImage,player.playerStatus!!)
        p0.searchingPlayerBinding.searchedPlayerPlayerSendRequst.setOnClickListener {
            playerItemListener!!.onRequestBtnClicked(player ,p0.searchingPlayerBinding.searchedPlayerPlayerSendRequst ,  p0.searchingPlayerBinding.requestingProgressBar)
        }
    }

    class SearchedPlayerVH(itemView: View) : ViewHolder(itemView) {
val searchingPlayerBinding = SearchedPlayerCustomViewBinding.bind(itemView)
        fun setPlayer (name : String , id : String , image : Int , playerStatus : PlayerStatus) {
            searchingPlayerBinding.searchedPlayerPlayerName.setText(name)
            searchingPlayerBinding.searchedPlayerPlayerId.setText("ID : " + id)
            searchingPlayerBinding.searchedPlayerPlayerImage.setImageResource(image)
       when (playerStatus.name) {
           PlayerStatus.ONLINE.name -> {
               searchingPlayerBinding.playerStatusImage.setImageResource(R.drawable.online_dot_icon)
           }
           PlayerStatus.OFFLINE.name -> {
               searchingPlayerBinding.playerStatusImage.setImageResource(R.drawable.offline_dot_icon)
              handleRequestBtn(itemView.context.getString(R.string.offline_text) , R.drawable.light_blue_background , false)
           }
           PlayerStatus.INTHEGAME.name -> {
               searchingPlayerBinding.playerStatusImage.setImageResource(R.drawable.game_control_icon)
              handleRequestBtn(itemView.context.getString(R.string.in_game_text) , R.drawable.light_blue_background , false)
           }

       }
        }

        private fun handleRequestBtn(text : String , btnBackground : Int , btnIsEnable : Boolean) {
            searchingPlayerBinding.searchedPlayerPlayerSendRequst.text = text
            searchingPlayerBinding.searchedPlayerPlayerSendRequst.setBackgroundResource(btnBackground)
            searchingPlayerBinding.searchedPlayerPlayerSendRequst.isEnabled = btnIsEnable
        }

    }

}