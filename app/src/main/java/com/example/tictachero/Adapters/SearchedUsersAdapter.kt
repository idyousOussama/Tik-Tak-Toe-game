package com.example.tictachero.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tictachero.Listeners.SearchedItemListener
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.SearchedUserItemCustomViewBinding

class SearchedUsersAdapter(var currentUserFollowers : ArrayList<String>,var  currentUserFollowings : ArrayList<String>) : RecyclerView.Adapter<SearchedUsersAdapter.SerchedUsersVh>() {
private var usersList :ArrayList<OnlinePlayer> = ArrayList()
private var searchedItemListener : SearchedItemListener? = null
    fun setSearchedUsersList (usersList :ArrayList<OnlinePlayer>) {
        this.usersList = usersList
        notifyDataSetChanged()
    }
    fun setUserItemListener(searchedItemListener : SearchedItemListener){
        this.searchedItemListener = searchedItemListener
    }
fun clearUserList() {
    usersList.clear()
    notifyDataSetChanged()
}
    fun addUserList(users: OnlinePlayer) {
        this.usersList.add(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SerchedUsersVh {
    val view = LayoutInflater.from(p0.context).inflate(R.layout.searched_user_item_custom_view , p0,false)
return SerchedUsersVh(view)

    }
    fun addFollowingUserToCurrentUserFollowingList(user: OnlinePlayer) {
        if (!currentUserFollowings.contains(user.playerId)) {
            currentUserFollowings.add(user.playerId)
        }
        val userPos = usersList.indexOf(user)
        if (userPos != -1) {
            notifyItemChanged(userPos)
        }
    }
    fun removeFollowingUserToCurrentUserFollowingList(user: OnlinePlayer) {
        if (currentUserFollowings.contains(user.playerId)) {
            currentUserFollowings.remove(user.playerId)
        }
        val userPos = usersList.indexOf(user)
        if (userPos != -1) {
            notifyItemChanged(userPos)
        }
    }
    fun removeUserItem(user: OnlinePlayer) {
        val userPos = usersList.indexOf(user)
        if (userPos != -1) {
            // Remove the user from the list
            usersList.remove(user)

            // Notify the adapter that an item has been removed
            notifyItemRemoved(userPos)
            // Optionally, you might also want to notify the adapter of item positions shifting
            // So it can update the subsequent items' positions in the list.
            notifyItemRangeChanged(userPos, usersList.size - userPos)
        }
    }


    override fun getItemCount(): Int {
return usersList.size    }

    override fun onBindViewHolder(p0: SerchedUsersVh, p1: Int) {
val user = usersList.get(p1)
val isFollower = currentUserFollowers.contains(user.playerId)
val isFollowing = currentUserFollowings.contains(user.playerId)
p0.setUser(user.playerImage , user.playerName , isFollower , isFollowing)

p0.searchedUserBinding.searchedUserFollowBtn.setOnClickListener {
    p0.searchedUserBinding.searchedUserFollowBtnText.visibility = View.GONE
    p0.searchedUserBinding.userItemOperationProgress.visibility = View.VISIBLE
    if(isFollowing  && isFollower || isFollowing && !isFollower) {
        searchedItemListener!!.onUnFollow(user)
    }else if (isFollower && !isFollowing) {
        searchedItemListener!!.onAcceptFollow(user)
    }else {
        searchedItemListener!!.onFollowing(user)
    }

}

   p0.searchedUserBinding.searchedUserPlayerCancelBtn.setOnClickListener {
removeUserItem(user)
   }
        p0.itemView.setOnClickListener {
            searchedItemListener!!.onUserItemListener(user)
        }
    }

    class SerchedUsersVh(itemView: View) : ViewHolder(itemView){
val searchedUserBinding = SearchedUserItemCustomViewBinding.bind(itemView)
   fun setUser(image : Int , name: String  , isFollower  : Boolean , isFollowing : Boolean) {
       searchedUserBinding.userItemOperationProgress.visibility = View.GONE
       searchedUserBinding.searchedUserFollowBtnText.visibility = View.VISIBLE
       searchedUserBinding.searchedUserUserImage.setImageResource(image)
       searchedUserBinding.searchedUserUserName.text = name

       if(isFollowing  && isFollower || isFollowing && !isFollower) {
           searchedUserBinding.searchedUserFollowBtnText.text = itemView.context.getString(R.string.UnFollow_text)
       }else if (isFollower && !isFollowing) {
           searchedUserBinding.searchedUserFollowBtnText.text = itemView.context.getString(R.string.accept_text)
       }else {
           searchedUserBinding.searchedUserFollowBtnText.text = itemView.context.getString(R.string.follow_text)
       }
   }

    }
}