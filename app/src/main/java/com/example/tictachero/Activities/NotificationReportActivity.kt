package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Models.MyApp.Companion.currentOnlinePlayer
import com.example.tictachero.Models.Report
import com.example.tictachero.Models.ReportType
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityNotificationReportBinding
import com.example.tictachero.databinding.NoInternetDialogViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.core.Repo

class NotificationReportActivity : AppCompatActivity() {
  private lateinit var binding : ActivityNotificationReportBinding
var reportTextMessage : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
handleBtns()
handleReportInput()

    }
    private fun handleReportInput() {
    binding.explanatinInput.addTextChangedListener {
        reportTextMessage  = binding.explanatinInput.text.toString()
        if(reportTextMessage.isNotEmpty()) {
            binding.sendReportBtn.setBackgroundResource(R.drawable.deep_blue_background)
            binding.sendReportBtnText.setTextColor(resources.getColor(R.color.white))
            binding.sendRepotText.setTextColor(resources.getColor(R.color.black))
        }else {
            binding.sendReportBtn.setBackgroundResource(R.drawable.light_blue_background)
            binding.sendReportBtnText.setTextColor(resources.getColor(R.color.light_gray))
            binding.sendRepotText.setTextColor(resources.getColor(R.color.gray))
        }

        if(reportTextMessage.length <= 300) {
            binding.reportLengthCounterText.text = reportTextMessage.length.toString() + "/" + (300-reportTextMessage.length).toString()
        }

    }
    }

    private fun handleBtns() {
        binding.sendReportBtn.setOnClickListener {
            sendingRepot()
        }
        binding.sendRepotText.setOnClickListener {
            sendingRepot()
        }
        binding.reportGoBackBtn.setOnClickListener {
            finish()
        }

    }

    private fun sendingRepot() {
        if(playerIsConcted()) {
            if(reportTextMessage.isNotEmpty()) {
                binding.sendRepotText.setTextColor(resources.getColor(R.color.deep_gray))
                binding.sendRepotText.isEnabled = false
                binding.sendReportBtn.isEnabled = false
                binding.sendReportBtnText.visibility = View.GONE
                binding.sendingReportProgress.visibility = View.VISIBLE
                sendReport()
            }else {
                showToast(getString(R.string.describe_a_problem_text))
            }
        }else {
            showNoInternetDialog()
        }
    }

    private fun sendReport() {
        val reportRef = firebaseDB.getReference("Support").child("Reports").child(currentPlayer!!.playerId)
    val reportId  = reportRef.push().key.toString()

        val report = Report(reportId,reportTextMessage,currentOnlinePlayer!!,ReportType.NOTIFICATION )


        reportRef.child(reportId).setValue(report).addOnCompleteListener { task ->
            if(task.isSuccessful){
    showToast(getString(R.string.sending_repot_successful_text))
                finish()
            }else {
                binding.sendRepotText.setTextColor(resources.getColor(R.color.black))
                binding.sendRepotText.isEnabled = true
                binding.sendReportBtn.isEnabled = true
                binding.sendingReportProgress.visibility = View.GONE
                binding.sendReportBtnText.visibility = View.VISIBLE
                showToast(getString(R.string.failed_to_sending_report))
            }

        }
    }

    private fun showToast(toastMessage: String) {
        Toast.makeText(baseContext,toastMessage,Toast.LENGTH_SHORT).show()

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
    private fun showNoInternetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.no_internet_dialog_view,null)
        val dialogBinding = NoInternetDialogViewBinding.bind(dialogView)
        dialogBinding.noInternetDialogDismissBtn.text = getString(R.string.ok_text)
        dialogBinding.noInternetDialogBackBtn.visibility = View.GONE
        val  noInternet = Dialog(this)
        noInternet.setContentView(dialogView)
        noInternet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        noInternet.setCancelable(false)
        dialogBinding.noInternetDialogDismissBtn.setOnClickListener {

            noInternet.dismiss()
        }

        noInternet.show()
    }
}