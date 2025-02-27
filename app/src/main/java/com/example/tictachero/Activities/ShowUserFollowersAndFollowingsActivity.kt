package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Adapters.GameViewPagerAdapter
import com.example.tictachero.Adapters.SearchedUsersAdapter
import com.example.tictachero.Fragments.ShowUserFollowersFragment
import com.example.tictachero.Fragments.ShowUserFollowingsFragment
import com.example.tictachero.Listeners.SearchedItemListener
import com.example.tictachero.Models.MyApp.Companion.currentOnlinePlayer
import com.example.tictachero.Models.Notification
import com.example.tictachero.Models.NotificationTypes
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityShowUserFollowersAndFollowingsBinding
import com.example.tictachero.databinding.UserOptionsBottomSheetViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ShowUserFollowersAndFollowingsActivity : AppCompatActivity(){
    private lateinit var binding : ActivityShowUserFollowersAndFollowingsBinding
companion object {
    var selectedUserFollowersList : ArrayList<String> = ArrayList()
    var selectedUserFollowingsList : ArrayList<String> = ArrayList()
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding =  ActivityShowUserFollowersAndFollowingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    getCurrentOlinePlayer()
        handleReloadBtn()
    }

    private fun handleReloadBtn() {
        binding.reloadingCurrentUserArrowBtn.setOnClickListener{
            binding.reloadingCurrentUserArrowLayout.visibility = View.GONE
            binding.loadingCurrentOnlinePlayerProgress.visibility = View.VISIBLE
            getCurrentOlinePlayer()
        }
        binding.showUserFollowersAndFollowingsGoBackbtn.setOnClickListener {
            finish()
        }
    }

    private fun getCurrentOlinePlayer() {
        if(playerIsConcted()) {
            val userRef = firebaseDB.getReference("Players")
            userRef.child(currentPlayer!!.playerId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        currentOnlinePlayer = p0.getValue(OnlinePlayer::class.java)
                        handleIsLoadedViews()
                        getSelectedUser()
                    }
                }
                override fun onCancelled(p0: DatabaseError) {
                    handleFailedLoadigData(getString(R.string.failed_to_load_page_message))
                }
            })
        }else{
            handleFailedLoadigData(getString(R.string.noInternet_exception_message))
        }

    }
private fun handleFailedLoadigData(messageText : String) {
    binding.loadingCurrentOnlinePlayerProgress.visibility = View.GONE
    binding.reloadingCurrentUserArrowTextMessage.text = messageText
        binding.reloadingCurrentUserArrowBtn.setImageResource(R.drawable.reload_data_icon)
    binding.reloadingCurrentUserArrowLayout.visibility = View.VISIBLE
}
    private fun handleIsLoadedViews() {
        binding.loadingCurrentOnlinePlayerProgress.visibility = View.GONE
        binding.showUserFollowersAndFollowingsUserName.visibility = View.VISIBLE
        binding.showUserFollowersAndFollowingsTabLayout.visibility = View.VISIBLE
        binding.viewPager.visibility = View.VISIBLE
    }

    private fun getSelectedUser() {
  val selectedUser = intent.getSerializableExtra("SelectedUser") as? OnlinePlayer
  val requestCode = intent.getIntExtra("RequestCode" , 0)
        if(selectedUser != null){
            binding.showUserFollowersAndFollowingsUserName.text = selectedUser.playerName
            selectedUserFollowersList = selectedUser.playerFollowersIdsList
            selectedUserFollowingsList = selectedUser.playerFollowingsIdsList
        }
        initViewPager( requestCode)
    }

    private fun initViewPager(requestCode: Int) {
        val   viewPagerAdapter = GameViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(ShowUserFollowersFragment(),selectedUserFollowersList.size.toString()+" "+getString(R.string.followers))
        viewPagerAdapter.addFragment(ShowUserFollowingsFragment(),selectedUserFollowingsList.size.toString()+" " +getString(R.string.followings))
        binding.viewPager.adapter = viewPagerAdapter
        binding.showUserFollowersAndFollowingsTabLayout.setupWithViewPager(binding.viewPager)
            binding.viewPager.currentItem = requestCode
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

}