package com.example.tictachero.Activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.DataBases.RoomDatabases.ViewModels.GameViewModel
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.Models.Language
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.Player
import com.example.tictachero.Models.PlayerStatus
import com.example.tictachero.Models.PlayerType
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivitySplashBinding
import com.example.tictachero.databinding.ConfirmationDialogViewBinding
import com.example.tictachero.databinding.NoInternetDialogViewBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Locale

class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
    val mainScope = CoroutineScope(Dispatchers.Main)
    companion object {
        var gameViewModel : GameViewModel? = null
        val firebaseDB  : FirebaseDatabase = FirebaseDatabase.getInstance()
 var currentLanguage : Language?  = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding  = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gameViewModel = ViewModelProvider(this).get(GameViewModel::class.java)
   getAppLanguage()
    }
    private fun getAppLanguage() {
        CoroutineScope(Dispatchers.Main).launch {
            val appLanguageList  = gameViewModel?.getAllLanguages()
        if(appLanguageList!!.isNotEmpty()) {
                currentLanguage = gameViewModel!!.getSelectLanguage()
            Toast.makeText(baseContext , currentLanguage!!.languageSymbol , Toast.LENGTH_SHORT ).show()
                setLanguage(currentLanguage!!.languageSymbol)
        }else {
            saveAppLanguges()
        }
        }
    }

    private fun getPLayerAccount() {
        mainScope.launch {
            currentPlayer = gameViewModel?.getPlayerLogainedAccount()
         if (currentPlayer != null) {
             navigateToNewActivity(SelectPlayModeActivity :: class.java , currentPlayer!!)
         }else{
             val  playerAccounts = gameViewModel!!.getAllPlayerAccounts()
         if(playerAccounts.isNotEmpty()) {
             startActivity(Intent(this@SplashActivity,ShowPlayerAccountsActivity::class.java))
            finish()
         }else {
             showConfirmationDialog()
         }
         }
        }
    }

    private fun saveAppLanguges() {
CoroutineScope(Dispatchers.Main).launch {
    val languageList: List<Language> = listOf(
        Language(0, getString(R.string.english_text), R.drawable.english_language_icon, "en", isSelected = true),
        Language(0, getString(R.string.arabic_text), R.drawable.arabic_language_icon, "ar", isSelected = false)
    )
// Make sure gameViewModel is not null before using it
    gameViewModel?.insertLanguageList(languageList)
    getPLayerAccount()

}    }

    private fun showConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog_view , null)
        val dialogBinding = ConfirmationDialogViewBinding.bind(dialogView)
        val dialog  = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.confirmationDialogTitle.text = getString(R.string.sgin_up_text)
        dialogBinding.confirmationDialogMessage.text = getString(R.string.confirmation_vistor_or_sgin_dialog_message)
        dialogBinding.confirmationDialogPositiveBtn.setBackgroundResource(R.drawable.deep_blue_background)
        dialogBinding.confirmationDialogNegativeBtn.setBackgroundResource(R.drawable.light_blue_background)
        dialogBinding.confirmationDialogPositiveBtn.setText(getString(R.string.sgin_up_text))
        dialogBinding.confirmationDialogNegativeBtn.text = getString(R.string.continue_text)
        dialogBinding.confirmationDialogPositiveBtn.setOnClickListener {
            if(playerIsConcted()) {
             navigateToSignUpActivty()
            }else {
                dialog.dismiss()
                showNoInternetDialog()
            }
        }
         dialogBinding.confirmationDialogNegativeBtn.setOnClickListener {
             mainScope.launch {
                 val ID : String = generateSecureNumericId()
                 val playerImage  = arrayOf(R.drawable.player_1,R.drawable.player_2,R.drawable.player_3,R.drawable.player_4,R.drawable.player_5,R.drawable.player_6,R.drawable.player_7).random()

                 val newPlayer = Player(ID,ID, null , "player " + ID ,   playerImage ,true,0,3,0 ,0,0 , PlayerType.VISITOR )

                 gameViewModel?.insertNewPlayerAccount(newPlayer)

                     navigateToNewActivity(SelectPlayModeActivity :: class.java ,currentPlayer!!)

             }
         }
        dialog.show()
    }
    private fun navigateToSignUpActivty() {
        val signUpAtivityIntent  = Intent(this ,LoginAndCreateAccountActivity::class.java)
        signUpAtivityIntent.putExtra("RequestCode" , 0)
        startActivity(signUpAtivityIntent)
        finish()
    }

    private fun navigateToNewActivity(newActivity: Class<*>, logainedAccount: Player) {
val newActivityIntent = Intent(this , newActivity)
        newActivityIntent.putExtra("logainedAccount" , logainedAccount)
        startActivity(newActivityIntent)
        finish()
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
    fun generateSecureNumericId(length: Int = 12): String {
        val random = SecureRandom()
        return (1..length)
            .map { random.nextInt(10) } // Generates numbers 0-9
            .joinToString("")
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
            finish()
        }

        noInternet.show()
    }
    private  fun setLanguage(languageCode: String ) {
             val locale = Locale(languageCode)
             Locale.setDefault(locale)
             val config = resources.configuration
             config.setLocale(locale)
             resources.updateConfiguration(config, resources.displayMetrics)
        getPLayerAccount()
    }
}