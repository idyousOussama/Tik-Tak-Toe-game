package com.example.tictachero.Models

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.DataBases.RoomDatabases.Repositories.GameRepositry
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
const val NETWORK_STATUS_CHANGED : String = "NETWORK_STATUS_CHANGED"
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
CoroutineScope(Dispatchers.Main).launch {
    val gameRepo = GameRepositry(this@MyApp)
    val playerCurrentAccount = gameRepo.getPlayerLogainedAccount()
    Toast.makeText(this@MyApp, "315465",Toast.LENGTH_SHORT).show()
    if(playerCurrentAccount != null){
        Toast.makeText(this@MyApp, "hkdshksd",Toast.LENGTH_SHORT).show()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleTracker(playerCurrentAccount.playerId, this@MyApp))

    }

}


    }
}