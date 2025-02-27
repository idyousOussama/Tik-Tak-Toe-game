package com.example.tictachero.Activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tictachero.Activities.SplashActivity.Companion.currentLanguage
import com.example.tictachero.Activities.SplashActivity.Companion.gameViewModel
import com.example.tictachero.Adapters.AccountsAdapter
import com.example.tictachero.DataBases.RoomDatabases.ViewModels.GameViewModel
import com.example.tictachero.Listeners.AccountItemListener
import com.example.tictachero.Models.Language
import com.example.tictachero.Adapters.LanguagesAdapter
import com.example.tictachero.Listeners.LanguageItemListener
import com.example.tictachero.Models.Player
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityShowPlayerAccountsBinding
import com.example.tictachero.databinding.SelectLanguagesBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class ShowPlayerAccountsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityShowPlayerAccountsBinding
    private val accountAdapter by lazy {
        AccountsAdapter()
    }
    private val languageAdapter by lazy {
        LanguagesAdapter()
    }
    private var bottomSheetDialog : BottomSheetDialog? = null
    private var playerAccountsList : ArrayList<Player>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding  = ActivityShowPlayerAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(gameViewModel != null ) {
            gameViewModel = ViewModelProvider(this@ShowPlayerAccountsActivity).get(GameViewModel::class.java)
        }

        getAppLanguage()
        getPlayerAccounts()
        setAccountItemListener()
        handleSeeMoreAccount()
        changeAppLanguage()
        setLanguageItemLanguage()
        handleSigninAndSignUpBtns()
    }

    private fun handleSigninAndSignUpBtns() {
        binding.playerAccountsSignInbtn.setOnClickListener {
            navigateToNewAcivity(LoginAndCreateAccountActivity::class.java , null,0)

        }
        binding.playerAccountsSignUpbtn.setOnClickListener {
            navigateToNewAcivity(LoginAndCreateAccountActivity::class.java , null,1)

        }

    }

    private fun setLanguageItemLanguage() {
        languageAdapter.setLanguageItemListener(object : LanguageItemListener{
            override fun onLanguageItemSelected(selectedLanguage: Language) {
upDateCurrentLanguage(selectedLanguage)

            }

        })
    }

    private fun upDateCurrentLanguage(selectedLanguage : Language) {
        CoroutineScope(Dispatchers.Main).launch {
            gameViewModel!!.upDateSelectedLanguageById(currentLanguage!!.languageId , isSelected = false)
            CoroutineScope(Dispatchers.Main).launch {
                gameViewModel!!.upDateSelectedLanguageById(selectedLanguage.languageId , isSelected = true)
            }
        setLanguage(selectedLanguage.languageSymbol)
        }
    }

    private fun getAppLanguage() {
        CoroutineScope(Dispatchers.Main).launch {
         currentLanguage = gameViewModel!!.getSelectLanguage()
            binding.selectAppLanguage.text = currentLanguage!!.title
        }
    }

    private fun changeAppLanguage() {
        binding.selectAppLanguage.setOnClickListener {
            showSelectLanguageBottomSheet()
        }
    }

    private fun showSelectLanguageBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.select_languages_bottom_sheet , null)
        val bottomSheetBinding = SelectLanguagesBottomSheetBinding.bind(bottomSheetView)
        bottomSheetDialog = BottomSheetDialog(this , R.style.BottomSheetDialog)
        bottomSheetDialog!!.setContentView(bottomSheetView)
       setLanguageList(bottomSheetBinding.languagesRv)
        bottomSheetDialog!!.show()
    }

    private fun setLanguageList(languageRv: RecyclerView) {
CoroutineScope(Dispatchers.Main).launch {

    val languageList = gameViewModel!!.getAllLanguages()
    languageAdapter.setLanguages(languageList as ArrayList)
    setRecyclerView( languageRv, languageAdapter)
}

    }


    private fun handleSeeMoreAccount() {
        binding.seeMoreAccountText.setOnClickListener {
            val restAccoountNum = playerAccountsList!!.size - 3
            accountAdapter.addAccountToList(playerAccountsList?.take(restAccoountNum) as ArrayList)
            binding.seeMoreAccountText.visibility = View.GONE
        }

    }

    private fun setAccountItemListener() {
        accountAdapter.setAccountItemListener(object:AccountItemListener{
            override fun onAccountItemClicked(account: Player) {
                navigateToNewAcivity(LoginAndCreateAccountActivity::class.java,account , 0)
            }
        })
    }
    private fun navigateToNewAcivity(newActivity:Class<*> , account: Player?, requestCode: Int) {
      val newActivityIntent = Intent(this@ShowPlayerAccountsActivity , newActivity)
 if (account != null) {
     newActivityIntent.putExtra("ToLogAccount",account)
 }
     newActivityIntent.putExtra("RequestCode" , requestCode)
    startActivity(newActivityIntent)

    }

    private fun getPlayerAccounts() {
        CoroutineScope(Dispatchers.Main).launch {
            playerAccountsList  = gameViewModel?.getAllPlayerAccounts() as? ArrayList<Player>
            setPlayerAccount(playerAccountsList!!)
        }
    }

    private fun setPlayerAccount(playerAccounts: ArrayList<Player>) {
        if(playerAccounts.isNotEmpty()) {
            playerAccounts.reverse()
 if(playerAccounts.size > 3) {
     accountAdapter.setPlayerAccount(playerAccounts.take(3) as ArrayList)
 binding.seeMoreAccountText.visibility = View.VISIBLE
 }else {
     accountAdapter.setPlayerAccount(playerAccounts)
 }
            setRecyclerView( binding.playerAccountRv, accountAdapter)

        }

    }

    private fun <T : RecyclerView.Adapter<*>> setRecyclerView(recyclerView: RecyclerView, rvAdapter: T) {
       recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ShowPlayerAccountsActivity)
            setHasFixedSize(true)
            adapter = rvAdapter
        }
    }
    private fun setLanguage(languageCode: String) {
        // Set the new locale based on the selected language
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }


    }


