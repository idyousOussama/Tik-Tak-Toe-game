package com.example.tictachero.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tictachero.Listeners.AccountItemListener
import com.example.tictachero.Models.Player
import com.example.tictachero.R
import com.example.tictachero.databinding.AccountItemCustomViewBinding

class AccountsAdapter : RecyclerView.Adapter<AccountsAdapter.AccountsVH>() {
     private var playerAccountsList : ArrayList<Player> = ArrayList()
private var accountItemListener  :AccountItemListener? = null
    fun setPlayerAccount(playerAccountsList : ArrayList<Player>) {
        this.playerAccountsList = playerAccountsList
    }
    fun addAccountToList(accounts : ArrayList<Player>) {
        playerAccountsList.addAll(accounts)
        notifyDataSetChanged()
    }
fun setAccountItemListener(accountItemListener  :AccountItemListener){
    this.accountItemListener = accountItemListener
}


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AccountsVH {
val view = LayoutInflater.from(p0.context).inflate(R.layout.account_item_custom_view , p0 , false)
        return AccountsVH(view)
    }

    override fun getItemCount(): Int {
return playerAccountsList.size    }

    override fun onBindViewHolder(p0: AccountsVH, p1: Int) {
 val account = playerAccountsList.get(p1)
            p0.setAccount(account.playerImage , account.playerName)
 p0.itemView.setOnClickListener {
     accountItemListener?.onAccountItemClicked(account)
 }
    }
    class AccountsVH(itemView: View) : ViewHolder(itemView) {
val accountBinding  = AccountItemCustomViewBinding.bind(itemView)
         fun setAccount(image : Int , name : String)  {
             accountBinding.accountItemImage.setImageResource(image)
             accountBinding.accountItemName.text = name
         }




    }
}