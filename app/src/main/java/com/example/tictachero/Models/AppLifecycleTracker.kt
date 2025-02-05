package com.example.tictachero.Models

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tictachero.Activities.SplashActivity
import com.example.tictachero.Activities.SplashActivity.Companion.gameViewModel
import com.example.tictachero.DataBases.RoomDatabases.ViewModels.GameViewModel
import com.google.firebase.database.FirebaseDatabase
class AppLifecycleTracker(private val userId: String, private val context: Context) : DefaultLifecycleObserver {

 private val database = FirebaseDatabase.getInstance().getReference("Players").child(userId)

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Toast.makeText(context, "User is Online ✅", Toast.LENGTH_SHORT).show()
        // Update online status in Firebase
        database.child("playerStatus").setValue(PlayerStatus.ONLINE)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Toast.makeText(context, "User is Offline ❌", Toast.LENGTH_SHORT).show()

        // Update online status in Firebase
        database.child("playerStatus").setValue(PlayerStatus.OFFLINE)
    }

    override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
        Toast.makeText(context, "User is Offline ❌", Toast.LENGTH_SHORT).show()

        database.child("playerStatus").setValue(PlayerStatus.OFFLINE)
    }
}

