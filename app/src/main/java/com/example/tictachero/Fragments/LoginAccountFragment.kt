package com.example.tictachero.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.tictachero.Activities.LoginAndCreateAccountActivity
import com.example.tictachero.Activities.LoginAndCreateAccountActivity.Companion.logPlayerAccount
import com.example.tictachero.Activities.SelectPlayModeActivity
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Activities.SplashActivity.Companion.gameViewModel
import com.example.tictachero.Listeners.SginInAndSginUpTextListener
import com.example.tictachero.Models.MyApp.Companion.firebaseAuth
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.Player
import com.example.tictachero.Models.PlayerStatus
import com.example.tictachero.Models.PlayerType
import com.example.tictachero.R
import com.example.tictachero.databinding.ConfirmationDialogViewBinding
import com.example.tictachero.databinding.FragmentCreateNewAccountBinding
import com.example.tictachero.databinding.FragmentLoginAccountBinding
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


class LoginAccountFragment : Fragment() {
    private  var sginInAndSginUpTextListener : SginInAndSginUpTextListener? = null
    private lateinit var binding  : FragmentLoginAccountBinding
    private var playerEmail : String = ""
    private var playerPassword : String  = ""
    private var playerEmailIsValide : Boolean = false
    private var playerPasswordIsValide : Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SginInAndSginUpTextListener) {
            sginInAndSginUpTextListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
binding = FragmentLoginAccountBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(logPlayerAccount != null) {
            playerEmail  = logPlayerAccount!!.playerEmail!!
            if(playerEmail != null ){
                binding.playerEmail.setText(playerEmail)
                playerEmailIsValide = true
            }
        }

    binding.sginUpText.setOnClickListener {
        sginInAndSginUpTextListener!!.onSginUpTextClicked()
    }

        showAndHidePasword()
       // handleSignInInputes()
   signInAccount()
    }

    private fun signInAccount() {
        binding.signInBtn.setOnClickListener {
            enableAndDisableSginInBtnText(false)
            playerEmail = binding.playerEmail.text.toString().trim()
playerPassword = binding.playerPassword.text.toString().trim()
            if(playerEmail.isNotEmpty() && playerPassword.isNotEmpty()) {
                signIn()
            }else{
                if(playerEmail.isEmpty() && playerPassword.isNotEmpty()) {
                    showFialedSginInAccountDialog(requireContext().getString(R.string.email_required_error_title),requireContext().getString(R.string.email_required_error_message),requireContext().getString(R.string.ok_text),false)
                }else if(playerPassword.isEmpty() && playerEmail.isNotEmpty()) {
                    showFialedSginInAccountDialog(requireContext().getString(R.string.password_required_error_title),requireContext().getString(R.string.password_required_error_message),requireContext().getString(R.string.ok_text),false)
                }else {
                    showFialedSginInAccountDialog(requireContext().getString(R.string.email_and_password_required_error_title),requireContext().getString(R.string.email_and_password_required_error_message),requireContext().getString(R.string.ok_text),false)
                }
                enableAndDisableSginInBtnText(true)
            }



        }
    }

    private fun signIn() {
        enableAndDisableSginInBtnText(false)
        firebaseAuth!!.signInWithEmailAndPassword(playerEmail, playerPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign-in successful
                    val currentPlayerId = firebaseAuth!!.currentUser!!.uid
                    getPlayerFromFirebase(currentPlayerId)  // Retrieve player details
                } else {
                    // Sign-in failed
                    enableAndDisableSginInBtnText(true)  // Re-enable the sign-in button
                    val signInEx = task.exception // Capture the exception

                    // Handle specific exceptions based on the error type
                    when (signInEx) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Incorrect password error
                            showFialedSginInAccountDialog(
                                getString(R.string.signIn_failed_error_title),
                                getString(R.string.incorrect_password_failed_error_message),
                                getString(R.string.ok_text),
                                false
                            )
                        }
                        is FirebaseAuthInvalidUserException -> {
                            // No account found with this email
                            showFialedSginInAccountDialog(
                                getString(R.string.signIn_failed_error_title),
                                getString(R.string.no_account_found_error_message),
                                getString(R.string.ok_text),
                                false
                            )
                        }
                        else -> {
                            // Other unknown errors
                            showFialedSginInAccountDialog(
                                getString(R.string.signIn_failed_error_title),
                                getString(R.string.unknow_error_titele),
                                getString(R.string.ok_text),
                                false
                            )
                        }
                    }
                }
            }

    }

    private fun getPlayerFromFirebase(currentPlayerId: String) {
val playersRef = firebaseDB.getReference("Players")
        playersRef.child(currentPlayerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())  {
                        val onlinePlayer = p0.getValue(OnlinePlayer::class.java)
                         if(onlinePlayer != null){
                             getPlayerFromRoomDB(onlinePlayer)
                         }
                }else{
                    enableAndDisableSginInBtnText(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                enableAndDisableSginInBtnText(true)
                when (databaseError.code) {
                    DatabaseError.PERMISSION_DENIED -> {
                        showFialedSginInAccountDialog(getString(R.string.error_permissiton_titele), getString(R.string.Permession_error) , getString(R.string.ok_text) , true)
                    }

                    DatabaseError.NETWORK_ERROR -> {
                        showFialedSginInAccountDialog(getString(R.string.network_permissiton_titele), getString(R.string.networck_error_message) , getString(R.string.ok_text) , false)
                    }

                    DatabaseError.DISCONNECTED -> {
                        showFialedSginInAccountDialog(getString(R.string.network_permissiton_titele), getString(R.string.diconnected_error_message) , getString(R.string.ok_text) , false)
                    }

                    DatabaseError.OPERATION_FAILED -> {
                        showFialedSginInAccountDialog(getString(R.string.opperation_error_titele), getString(R.string.operation_error_message) , getString(R.string.ok_text) , true)
                    }

                    else -> {
                        showFialedSginInAccountDialog(getString(R.string.unknow_error_titele), getString(R.string.unknow_error_message) , getString(R.string.ok_text) , true)
                    }


                }}

        })
    }

    private fun getPlayerFromRoomDB(onlinePlayer: OnlinePlayer) {
CoroutineScope(Dispatchers.Main).launch {
    val logainedAccount = gameViewModel!!.getPlayerLogainedAccount()
if(logainedAccount != null) {
    changeLogainedAccountStatus(logainedAccount , onlinePlayer)
}else{
    val  playerRef = firebaseDB.getReference("Players")
    val currentAccountStatusUpDates = mapOf<String  , PlayerStatus>(
        "playerStatus" to PlayerStatus.ONLINE
    )
    playerRef.child(onlinePlayer.playerId).updateChildren(currentAccountStatusUpDates).addOnCompleteListener { task ->
        if(task.isSuccessful) {
            CoroutineScope(Dispatchers.Main).launch {
                val currentAccount = gameViewModel!!.getAccountById(onlinePlayer.playerId)
 if(currentAccount != null ){
     gameViewModel!!.upDateLoginedAccountById(currentAccount.playerId , true)
     navigateToNewActivity(SelectPlayModeActivity::class.java , currentAccount)

 }else{
     val newPlayer = Player(onlinePlayer.playerId,onlinePlayer.ID , onlinePlayer.playerEmail,onlinePlayer.playerName,onlinePlayer.playerImage,true,onlinePlayer.notesNum,onlinePlayer.daimondNum,0,0,0,PlayerType.HOSTER)
     gameViewModel!!.insertNewPlayerAccount(newPlayer)
     navigateToNewActivity(SelectPlayModeActivity::class.java , newPlayer)
 }

            }

        }else{
            enableAndDisableSginInBtnText(true)
        }
    }

}
}

    }

    private fun changeLogainedAccountStatus(previosAccount: Player , onlinePlayer :OnlinePlayer ) {
val playerRef = firebaseDB.getReference("Players")
val previousAccountStatusUpDates = mapOf<String  , PlayerStatus>(
    "playerStatus" to PlayerStatus.OFFLINE
)
val currentAccountStatusUpDates = mapOf<String  , PlayerStatus>(
    "playerStatus" to PlayerStatus.ONLINE
)
  playerRef.child(previosAccount.playerId).updateChildren(previousAccountStatusUpDates).addOnCompleteListener { task ->
  if(task.isSuccessful) {
      playerRef.child(onlinePlayer.playerId).updateChildren(currentAccountStatusUpDates).addOnCompleteListener { task ->

         if (task.isSuccessful) {
             CoroutineScope(Dispatchers.Main).launch {
                 gameViewModel!!.upDateLoginedAccountById(previosAccount.playerId,false)
                 val currentAccount = gameViewModel!!.getAccountById(onlinePlayer.playerId)
if(currentAccount != null ){
    gameViewModel!!.upDateLoginedAccountById(currentAccount.playerId,true)
    navigateToNewActivity(SelectPlayModeActivity::class.java , currentAccount)
}else{
    val newPlayer = Player(onlinePlayer.playerId,onlinePlayer.ID , onlinePlayer.playerEmail,onlinePlayer.playerName,onlinePlayer.playerImage,true,onlinePlayer.notesNum,onlinePlayer.daimondNum,0,0,0,PlayerType.HOSTER)
    gameViewModel!!.insertNewPlayerAccount(newPlayer)
    navigateToNewActivity(SelectPlayModeActivity::class.java , newPlayer)
}


             }
         }else{
             enableAndDisableSginInBtnText(true)
         }
      }
  }else{
      enableAndDisableSginInBtnText(true)
  }

  }

    }

    private fun handleSignInInputes() {
        binding.playerEmail.addTextChangedListener {
            playerEmail  = binding.playerEmail.text.toString().trim()
            if(playerEmail.isNotEmpty()) {
                if(isValidEmail(playerEmail)){
                    upDatePlayerEmailInPute(true)

                }else  {
                    upDatePlayerEmailInPute(false)
                }
            }else {
                upDatePlayerEmailInPute(false)
            }
        }
        binding.playerPassword.addTextChangedListener {
            playerPassword  = binding.playerPassword.text.toString().trim()
            if(playerEmail.isNotEmpty()) {
                if(isValidPassword(playerPassword)){
                    upDatePlayerPasswordInPute(true)
                }else  {
                    upDatePlayerPasswordInPute(false)
                }
            }else {
                upDatePlayerPasswordInPute(false)
            }
        }
    }
    private fun upDatePlayerPasswordInPute(isCorrect: Boolean) {
 playerPasswordIsValide = isCorrect
        handleSginUpBtn()
    }
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")
        return password.matches(passwordRegex)
    }
    private fun showAndHidePasword() {
        var isPasswordVisible = false
        val currentTypeface = binding.playerPassword.typeface  // Save the current font style

        binding.showAndHideSginInPassword.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                binding.playerPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showAndHideSginInPassword.setImageResource(R.drawable.close_eyes)
                isPasswordVisible = false
            } else {
                // Show password
                binding.playerPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showAndHideSginInPassword.setImageResource(R.drawable.open_eyes)
                isPasswordVisible = true
            }
            binding.playerPassword.setTypeface(currentTypeface)  // Restore the original font

            // Move cursor to the end of the text
            binding.playerPassword.setSelection(binding.playerPassword.text.length)
        }
    }
    fun isValidEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9]+@gmail\\.com\$")
        return email.matches(regex)
    }
    private fun upDatePlayerEmailInPute(isCorrect: Boolean) {
        playerEmailIsValide = isCorrect
        handleSginUpBtn()

    }
    private fun handleSginUpBtn() {
        if(playerEmailIsValide  && playerPasswordIsValide )  {
            binding.signInBtn.setBackgroundResource(R.drawable.deep_blue_background)
            binding.signInBtnText.setTextColor(resources.getColor(R.color.white))

        }else{
            binding.signInBtn.setBackgroundResource(R.drawable.light_blue_background)
            binding.signInBtnText.setTextColor(resources.getColor(R.color.light_gray))

        }
    }
    private fun enableAndDisableSginInBtnText(isEnable: Boolean) {
        if(isEnable){
            binding.signInProgress.visibility = View.GONE
            binding.signInBtnText.visibility = View.VISIBLE
            binding.signInBtnText.setTextColor(resources.getColor(R.color.white))
            binding.signInBtn.isEnabled = true
            binding.signInBtn.setBackgroundResource(R.drawable.deep_blue_background)

        }else{
            binding.signInProgress.visibility = View.VISIBLE
            binding.signInBtnText.visibility = View.GONE
            binding.signInBtn.isEnabled = false
        }
    }
    private fun navigateToNewActivity(newActivity: Class<*>, logainedAccount: Player) {
        currentPlayer  = logainedAccount
        val newActivityIntent = Intent(requireContext() , newActivity)
        newActivityIntent.putExtra("logainedAccount" , logainedAccount)
        startActivity(newActivityIntent)
        activity?.finish()
    }
    private fun showFialedSginInAccountDialog(title: String, message: String , btnText : String, isFinish: Boolean){
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog_view , null)
        val dialogBinding = ConfirmationDialogViewBinding.bind(dialogView)
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(dialogView)
        dialogBinding.confirmationDialogTitle.text  = title
        dialogBinding.confirmationDialogMessage.text = message
        dialogBinding.confirmationDialogNegativeBtn.visibility = View.GONE
        dialogBinding.confirmationDialogPositiveBtn.setBackgroundResource(R.drawable.deep_blue_background)
        dialogBinding.confirmationDialogPositiveBtn.text = btnText
        dialogBinding.confirmationDialogPositiveBtn.setOnClickListener {
            if(isFinish) {
                dialog.dismiss()
                LoginAndCreateAccountActivity().finish()
            }else{
                dialog.dismiss()
            }
        }
        dialog.show()
    }




}