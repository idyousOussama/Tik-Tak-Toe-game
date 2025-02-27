package com.example.tictachero.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.tictachero.Adapters.GameViewPagerAdapter
import com.example.tictachero.Fragments.CreateNewAccountFragment
import com.example.tictachero.Fragments.LoginAccountFragment
import com.example.tictachero.Listeners.SginInAndSginUpTextListener
import com.example.tictachero.Models.Player
import com.example.tictachero.R
import com.example.tictachero.databinding.ActivityLoginAndCreateAccountBinding

class LoginAndCreateAccountActivity : AppCompatActivity() , SginInAndSginUpTextListener {
    private lateinit var binding : ActivityLoginAndCreateAccountBinding
companion object {
    var logPlayerAccount : Player? = null
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding  = ActivityLoginAndCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
getRequestCode()
    }

    private fun getRequestCode() {
        val requestCode = intent.getIntExtra("RequestCode" , 0)
        initViewPager(binding.viewPager, requestCode)
    }
    private fun initViewPager(viewPager: ViewPager, requestCode: Int) {
            val viewPagerAdapter = GameViewPagerAdapter(supportFragmentManager) // Or use childFragmentManager if this is a fragment-based dialog.
        viewPagerAdapter.addFragment(LoginAccountFragment(), getString(R.string.login_text))
        viewPagerAdapter.addFragment(CreateNewAccountFragment(), getString(R.string.create_text))
            viewPager.adapter = viewPagerAdapter
        getPlayerAccount()
            when(requestCode)  {
                0 ->
                {
                    binding.viewPager.currentItem = 0
                }
                1 -> {
                    binding.viewPager.currentItem = 1
                }
            }
    }

    private fun getPlayerAccount() {
         logPlayerAccount = intent.getSerializableExtra("ToLogAccount") as? Player

    }

    override fun onSginInTextClicked() {
        binding.viewPager.currentItem = 0
    }

    override fun onSginUpTextClicked() {
        binding.viewPager.currentItem = 1
    }
}