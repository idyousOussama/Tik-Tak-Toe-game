package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Adapters.NotificationAdapter
import com.example.tictachero.Listeners.NotificationItemListener
import com.example.tictachero.Models.Notification
import com.example.tictachero.Models.NotificationTypes
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityShowPlayerNotificationsLayoutBinding
import com.example.tictachero.databinding.NoInternetDialogViewBinding
import com.example.tictachero.databinding.NotificationDetailsBottomSheetCustomViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ShowPlayerNotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowPlayerNotificationsLayoutBinding
    private val notificationAdapter: NotificationAdapter by lazy {
        NotificationAdapter()
    }
    val notificationsList: ArrayList<Notification> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowPlayerNotificationsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getPlayerNotification()
        handleBtns()
        setNotificationItemListener()
    }

    private fun setNotificationItemListener() {
        notificationAdapter.setNotificationItemListener(object : NotificationItemListener {
            override fun onNotificationMoreDetailsBtnClicked(notification: Notification ,  senderImage: Int,senderName: String) {
                showNotificationDetailsBottomSheet(notification,senderImage,senderName)
            }
            override fun onNotificationClicked(notification: Notification, position: Int) {
              if(playerIsConcted()) {
                  navigateToNewActivityWithInfo(ShowPlayerProfile::class.java ,notification.senderPlayerId
                      , "friendRequest")

                  if(notification.notificationType == NotificationTypes.FOLLOW) {
                  if(!notification.isReaded) {
                      readedNotification(notification)
                      notification.isReaded = true

               notificationAdapter.updateItem(position,notification )
                  }
                  }else {
                      if(!notification.isReaded) {
                          readedNotification(notification)
                          notification.isReaded = true

                          notificationAdapter.updateItem(position,notification )
                      }
                  }
              }else {
                  showNoInternetDialog(null)
              }
            }
        })
    }
private fun navigateToNewActivityWithInfo(
    newActivity: Class<*>,
    senderPlayer: String?,
    key: String
) {
    val newActivityIntent = Intent(baseContext , newActivity)
    newActivityIntent.putExtra(key ,senderPlayer )
   startActivity(newActivityIntent)
}

    private fun showNotificationDetailsBottomSheet(
        notification: Notification,
        senderImage: Int,
        senderName: String
    ) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.notification_details_bottom_sheet_custom_view, null)
        val bottomSheetBinding =
            NotificationDetailsBottomSheetCustomViewBinding.bind(bottomSheetView)
        val bottomSheetDialog =
            BottomSheetDialog(this@ShowPlayerNotificationsActivity, R.style.BottomSheetDialog)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.setCancelable(true)
        bottomSheetBinding.bottomSheetPlayerImage.setImageResource(senderImage)
        when (notification.notificationType!!.name) {
            NotificationTypes.RQUESTTOPLAY.name -> {
                bottomSheetBinding.bottomSheetPlayerMessageText.text = getNotificationtText(
                    senderName,
                    getString(R.string.requested_you_to_join_to_their_room )
                )
            }
        }
        bottomSheetBinding.deleteNotificationLayoutBtn.setOnClickListener {
             if (playerIsConcted()) {
                 bottomSheetBinding.deletingNotificationPogress.visibility = View.VISIBLE
                 removePlayerNewInvitation(notification ,bottomSheetDialog , bottomSheetBinding)
             }else {
                 showNoInternetDialog(bottomSheetDialog)
             }

        }
        bottomSheetBinding.reportNotificationBtnLayout.setOnClickListener {
            navigateToNewActivity(NotificationReportActivity::class.java)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()

    }

    private fun navigateToNewActivity(newActivity: Class<NotificationReportActivity>) {
val newActivityIntent = Intent (baseContext,newActivity)
        startActivity(newActivityIntent)
    }

    private fun handleBtns() {
        binding.notificationGoBackBtn.setOnClickListener {
            finish()
        }
        binding.refrechBtn.setOnClickListener {
            binding.notificationRVList.visibility = View.GONE
            binding.failedLoadingNoificationLayout.visibility = View.GONE
            binding.loadingNoificationLayout.visibility = View.GONE
            getPlayerNotification()
        }
        binding.readAllNotificationsBtn.setOnClickListener {
            if (notificationsList.isNotEmpty()) {
                val unReadNotifications = notificationsList.filter { !it.isReaded }
                if (unReadNotifications.isNotEmpty()) {
                    binding.readAllNotificationsBtn.isEnabled = false
                    binding.readingNotificationImage.visibility = View.GONE
                    binding.readingNotificationProgress.visibility = View.VISIBLE
                    for (item in unReadNotifications) {
                        readedNotification(item)

                    }
                    notificationAdapter.setNotificationList(notificationsList)
                    binding.readAllNotificationsBtn.isEnabled = true
                    binding.readingNotificationProgress.visibility = View.GONE
                    binding.readingNotificationImage.visibility = View.VISIBLE
                                    } else {
                    showToast(getString(R.string.all_notifications_was_readed_message))
                }

            } else {
                showToast(getString(R.string.no_notification_found_text))

            }


        }

    }

    private fun readedNotification(notificationItem: Notification) {

            val playerNotifiationRef = SplashActivity.firebaseDB.getReference("Notifications")
                .child(currentPlayer!!.playerId).child("Notification")

            val notificationUpDate = mapOf<String, Boolean>(
                "readed" to true
            )
            playerNotifiationRef.child(notificationItem.notificationId).updateChildren(notificationUpDate)
            val itemIndex = notificationsList.indexOf(notificationItem)
            notificationsList.get(itemIndex).isReaded = true




    }

    private fun showToast(toastMessage: String) {
        Toast.makeText(baseContext, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun getPlayerNotification() {
        val playerNotifiationRef = SplashActivity.firebaseDB.getReference("Notifications").child(currentPlayer!!.playerId)
                .child("Notification")
        playerNotifiationRef.orderByChild("postedDate")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        for (item in p0.children) {

                            val notification = item.getValue(Notification::class.java)
                            if (notification != null) {
                                notificationsList.add(notification)
                            }
                        }
                        if (notificationsList.isNotEmpty()) {
                            notificationsList.reverse()
                            Toast.makeText(
                                baseContext,
                                notificationsList.size.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.loadingNoificationLayout.visibility = View.GONE
                            binding.notificationRVList.visibility = View.VISIBLE
                            setUpReclerView(notificationsList)
                        } else {
                            setUpNoNotificationFound()
                        }
                    } else {
                        setUpNoNotificationFound()

                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    binding.loadingNoificationLayout.visibility = View.GONE
                    binding.notificationRVList.visibility = View.GONE
                    binding.failedLoadingNoificationLayout.visibility = View.VISIBLE
                }

            })


    }

    private fun setUpNoNotificationFound() {
        binding.loadingNoificationLayout.visibility = View.GONE
        if (binding.notificationRVList.isVisible) {
            binding.notificationRVList.visibility = View.GONE
        }
        binding.failedLoadingNoificationImage.setImageResource(R.drawable.no_notificatiion_found_icon)
        binding.failedLoadingNoificationMessage.text =
            getString(R.string.no_notification_found_text)
        binding.failedLoadingNoificationMessage.setTextColor(resources.getColor(R.color.black))
        binding.failedLoadingNoificationMessage.typeface = Typeface.DEFAULT_BOLD
        binding.refrechBtn.visibility = View.GONE
        binding.failedLoadingNoificationLayout.visibility = View.VISIBLE
    }

    private fun setUpReclerView(notificationsList: ArrayList<Notification>) {
        notificationAdapter.setNotificationList(notificationsList)
        binding.notificationRVList.apply {
            layoutManager = LinearLayoutManager(this@ShowPlayerNotificationsActivity)
            setHasFixedSize(true)
            adapter = notificationAdapter
        }
    }

    private fun getNotificationtText(playerName: String, part2: String): SpannableStringBuilder {
        val fullText = playerName + " " + part2
        val spannable = SpannableStringBuilder(fullText)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            playerName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun removePlayerNewInvitation(
        notification: Notification,
        bottomSheetDialog: BottomSheetDialog,
        bottomSheetBinding: NotificationDetailsBottomSheetCustomViewBinding
    ) {
        val playerNotifiationRef =
            SplashActivity.firebaseDB.getReference("Notifications").child(currentPlayer!!.playerId)
                .child("Notification")
        playerNotifiationRef.child(notification.notificationId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                notificationsList.remove(notification)
                bottomSheetDialog.dismiss()
                notificationAdapter.removeNotificationItem(notification)
                showToast(getString(R.string.notification_removed_successfully))
                 if(notificationsList.isEmpty()) {
                     setUpNoNotificationFound()
                 }
            }else  {
                bottomSheetBinding.deletingNotificationPogress.visibility = View.GONE
                showToast(getString(R.string.notification_removed_failed))
            }
        }



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
    private fun showNoInternetDialog(bottomSheet: BottomSheetDialog?) {
        val dialogView = layoutInflater.inflate(R.layout.no_internet_dialog_view,null)
        val dialogBinding = NoInternetDialogViewBinding.bind(dialogView)
        dialogBinding.noInternetDialogDismissBtn.text = getString(R.string.ok_text)
        dialogBinding.noInternetDialogBackBtn.visibility = View.GONE
        val  noInternet = Dialog(this)
        noInternet.setContentView(dialogView)
        noInternet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        noInternet.setCancelable(false)
        dialogBinding.noInternetDialogDismissBtn.setOnClickListener {
            if(bottomSheet != null) {
                bottomSheet.dismiss()
            }
            noInternet.dismiss()
        }

        noInternet.show()
    }
}