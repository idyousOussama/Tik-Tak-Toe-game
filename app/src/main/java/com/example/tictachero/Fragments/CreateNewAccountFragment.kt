package com.example.tictachero.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.tictachero.Activities.LoginAndCreateAccountActivity
import com.example.tictachero.Activities.SelectPlayModeActivity
import com.example.tictachero.Activities.SelectPlayModeActivity.Companion.currentPlayer
import com.example.tictachero.Activities.SplashActivity.Companion.firebaseDB
import com.example.tictachero.Activities.SplashActivity.Companion.gameViewModel
import com.example.tictachero.Listeners.SginInAndSginUpTextListener
import com.example.tictachero.Models.GameStatus
import com.example.tictachero.Models.MyApp.Companion.firebaseAuth
import com.example.tictachero.Models.OnlinePlayer
import com.example.tictachero.Models.Player
import com.example.tictachero.Models.PlayerStatus
import com.example.tictachero.Models.PlayerType
import com.example.tictachero.R
import com.example.tictachero.databinding.ConfirmationDialogViewBinding
import com.example.tictachero.databinding.FragmentCreateNewAccountBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom


class CreateNewAccountFragment : Fragment() {
private var playerNameIsValide  : Boolean= false
private var playerLastNameIsValide  : Boolean= true
private var playerEmailIsValide  : Boolean= false
private var playerPasswordIsValide  : Boolean= false
private var playerConPasswordIsValide  : Boolean= false

    var playerNameText:String = ""
    var playerLastName:String = ""
    var  playerEmail:String = ""
    var playerPassword:String = ""
    var confirmPasswordText:String = ""
    private  var sginInAndSginUpTextListener : SginInAndSginUpTextListener? = null
    private lateinit var binding  :FragmentCreateNewAccountBinding
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
        binding = FragmentCreateNewAccountBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
   binding.loginText.setOnClickListener {
       sginInAndSginUpTextListener!!.onSginInTextClicked()
   }
        showAndHidePasword()
        handleCreateAccountInputes()
        SginUpAccount()
    }
    private fun SginUpAccount() {
        binding.signUpAccountBtn.setOnClickListener {
            enableAndDisableSginUpBtnText(false)
            firebaseAuth!!.createUserWithEmailAndPassword(playerEmail,playerPassword).addOnCompleteListener {  task ->
             if(task.isSuccessful) {
                 val playerId = firebaseAuth!!.currentUser?.uid
                 insertPlayer(playerId!!)
             }else{
                 val ex = task.exception
                 if(ex is FirebaseAuthUserCollisionException) {
                     showFialedCreateAccountDialog(getString(R.string.email_already_used_title) , getString(R.string.email_already_used_message) , getString(R.string.dissmes_text) , false)
                     playerEmailIsValide = false
                     playerEmail = ""
                     binding.playerEmail.setText(playerEmail)
                     enableAndDisableSginUpBtnText(true)
                     handleSginUpBtn()
                 }else if(ex is FirebaseNetworkException) {
                     showFialedCreateAccountDialog(
                         getString(R.string.noIntent_exception_title),
                         getString(R.string.noInternet_exception_message),getString(R.string.ok_text) ,
                         true
                     )
                 }
             }
            }
        }
    }
    private fun insertPlayer(playerId: String) {
val playersRef = firebaseDB.getReference("Players")
        val ID : String = generateSecureNumericId()
        val playerImage  = arrayOf(R.drawable.player_1,R.drawable.player_2,R.drawable.player_3,R.drawable.player_4,R.drawable.player_5,R.drawable.player_6,R.drawable.player_7).random()
        val  onlinePlayer = OnlinePlayer(playerId,ID,playerNameText,playerEmail,playerImage,0,0,6,100,PlayerStatus.ONLINE,GameStatus.CREATED,ArrayList<String>(),ArrayList<String>() , 0)
        playersRef.child(playerId).setValue(onlinePlayer).addOnCompleteListener {task ->
            if(task.isSuccessful) {
                CoroutineScope(Dispatchers.Main).launch {
                    val loginedAccount = gameViewModel!!.getPlayerLogainedAccount()
                 if(loginedAccount != null){
                         upDateLoaginedAccountStatus(loginedAccount.playerId  , onlinePlayer)
                 }else {
                     val newPlayer = Player(onlinePlayer.playerId,onlinePlayer.ID , onlinePlayer.playerEmail,onlinePlayer.playerName , onlinePlayer.playerImage , true , onlinePlayer.notesNum,onlinePlayer.daimondNum,0,0,0,PlayerType.HOSTER)
                     gameViewModel!!.insertNewPlayerAccount(newPlayer)
                     navigateToNewActivity(SelectPlayModeActivity::class.java, newPlayer)
                 }
                }
            }else{
                val exception = task.exception
                 if(exception is FirebaseNetworkException){
                     showFialedCreateAccountDialog(
                         getString(R.string.noIntent_exception_title),
                         getString(R.string.noInternet_exception_message),getString(R.string.ok_text) ,
                         true
                     )
                 }else {
                     showFialedCreateAccountDialog(getString(R.string.faild_to_create_account_title),getString(R.string.fialed_to_create_account_message) ,getString(R.string.ok_text),true)
                 }
            }
        }

    }

    private fun upDateLoaginedAccountStatus(playerId: String , onlinePlayer : OnlinePlayer) {
val playerRef = firebaseDB.getReference("Players")
val loginedAccountStatusUpDates     = mapOf(
    "playerStatus" to PlayerStatus.OFFLINE
)
   playerRef.child(playerId).updateChildren(loginedAccountStatusUpDates).addOnCompleteListener {task ->
      if(task.isSuccessful) {
          CoroutineScope(Dispatchers.Main).launch {
              gameViewModel!!.upDateLoginedAccountById(playerId , false)
              val newPlayer = Player(onlinePlayer.playerId,onlinePlayer.ID , onlinePlayer.playerEmail,onlinePlayer.playerName , onlinePlayer.playerImage , true , onlinePlayer.notesNum,onlinePlayer.daimondNum,0,0,0,PlayerType.HOSTER)
              gameViewModel!!.insertNewPlayerAccount(newPlayer)
              navigateToNewActivity(SelectPlayModeActivity::class.java, newPlayer)
          }
      }

   }


    }

    private fun enableAndDisableSginUpBtnText(isEnable: Boolean) {
if(isEnable){
binding.sginUpProgress.visibility = View.GONE
    binding.signUpAccountBtnText.visibility = View.VISIBLE
    binding.signUpAccountBtnText.setTextColor(resources.getColor(R.color.white))
binding.signUpAccountBtn.isEnabled = true
binding.signUpAccountBtn.setBackgroundResource(R.drawable.deep_blue_background)

}else{
    binding.sginUpProgress.visibility = View.VISIBLE
    binding.signUpAccountBtnText.visibility = View.GONE
    binding.signUpAccountBtn.isEnabled = false
}
    }


    private fun handleCreateAccountInputes()  {
       binding.playerName.addTextChangedListener {
           playerNameText  = binding.playerName.text.toString().trim()
        if(playerNameText.isNotEmpty()) {
            Toast.makeText(requireContext() , "isNotEmpty ",Toast.LENGTH_SHORT).show()

            if(isValidName(playerNameText)) {
                Toast.makeText(requireContext() , "sdjq",Toast.LENGTH_SHORT).show()
                upDatePlayerNameInPute(true)
            }else{
                upDatePlayerNameInPute(false)
            }
        }else {
        upDatePlayerNameInPute(false)
        }
       }
       binding.playerLastName.addTextChangedListener {
            playerLastName = binding.playerLastName.text.toString().trim()
           if(!isValidName(playerLastName) && playerLastName.isNotEmpty()) {
               upDatePlayerLastNameInPute(false)
           }else {
               upDatePlayerLastNameInPute(true)
           }
       }
   binding.playerEmail.addTextChangedListener {
        playerEmail  = binding.playerEmail.text.toString().trim()
       if(playerEmail.isNotEmpty()) {
 if(isValidGmail(playerEmail)){
     upDatePlayerEmailInPute(true)

 }else  {
     upDatePlayerEmailInPute(false)
 }
       }else {
           upDatePlayerEmailInPute(false)
       }
   }

       binding.playerPassword.addTextChangedListener {
            playerPassword = binding.playerPassword.text.toString().trim()
       if(playerPassword.isNotEmpty()) {
 if(isValidPassword(playerPassword))  {
     upDatePlayerPasswordInPute(true)
 }else {
     upDatePlayerPasswordInPute(false)
 }
           if(confirmPasswordText.isNotEmpty()) {
               if(isValidConPassword(confirmPasswordText , playerPassword)) {
                   upDatePlayerConPasswordInPute(true)
               }else{
                   upDatePlayerConPasswordInPute(false)
               }
           }
       }else {
               upDatePlayerPasswordInPute(false)
       binding.playerConfirmPassword.setBackgroundResource(R.drawable.disable_player_inputes)
       binding.playerConfirmPassword.isEnabled = false

       }
       }

       binding.playerConfirmPassword.addTextChangedListener {
            confirmPasswordText = binding.playerConfirmPassword.text.toString()
           if(confirmPasswordText.isNotEmpty()) {
if(isValidConPassword(confirmPasswordText , playerPassword)){
    upDatePlayerConPasswordInPute(true)
}else {
    upDatePlayerConPasswordInPute(false)
}

           }else {
               upDatePlayerConPasswordInPute(false)
           }
       }
   }

    private fun upDatePlayerLastNameInPute(isCorrect: Boolean) {
 if(isCorrect) {
     binding.playerLastName.setBackgroundResource(R.drawable.enable_player_bar_background)
     handleSginUpBtn()
 }else{
     binding.playerLastName.setBackgroundResource(R.drawable.error_inpute_background)

 }
        playerLastNameIsValide = isCorrect
        handleSginUpBtn()
    }

    private fun upDatePlayerConPasswordInPute(isCorrect: Boolean) {
if(isCorrect) {
    binding.playerConfirmPassword.setBackgroundResource(R.drawable.enable_player_bar_background)
}else {
    binding.playerConfirmPassword.setBackgroundResource(R.drawable.error_inpute_background)
}
        playerConPasswordIsValide = isCorrect
        handleSginUpBtn()

    }

    private fun upDatePlayerPasswordInPute(isCorrect: Boolean) {
 if(isCorrect) {
     binding.sginUpPasswordLayout.setBackgroundResource(R.drawable.enable_player_bar_background)
     binding.playerConfirmPassword.isEnabled = true
     binding.playerConfirmPassword.setBackgroundResource(R.drawable.enable_player_bar_background)
 }else{
     binding.sginUpPasswordLayout.setBackgroundResource(R.drawable.error_inpute_background)
 }
        playerPasswordIsValide = isCorrect
        handleSginUpBtn()


    }

    private fun handleSginUpBtn() {
        if(playerNameIsValide && playerEmailIsValide && playerConPasswordIsValide && playerPasswordIsValide && playerLastNameIsValide)  {
            binding.signUpAccountBtn.setBackgroundResource(R.drawable.deep_blue_background)
            binding.signUpAccountBtnText.setTextColor(resources.getColor(R.color.white))
            binding.sginUpPasswordLayout.isEnabled = true
        }else{
            binding.signUpAccountBtn.setBackgroundResource(R.drawable.light_blue_background)
            binding.signUpAccountBtnText.setTextColor(resources.getColor(R.color.light_gray))
            binding.sginUpPasswordLayout.isEnabled = false
        }
    }

    private fun upDatePlayerEmailInPute(isCorrect: Boolean) {
    if(isCorrect) {
        binding.playerEmail.setBackgroundResource(R.drawable.enable_player_bar_background)
    }else {
        binding.playerEmail.setBackgroundResource(R.drawable.error_inpute_background)
    }
        playerEmailIsValide = isCorrect
        handleSginUpBtn()

    }
    private fun upDatePlayerNameInPute(isCorrect: Boolean) {
if (isCorrect) {
    binding.playerName.setBackgroundResource(R.drawable.enable_player_bar_background)
}else{
    binding.playerName.setBackgroundResource(R.drawable.error_inpute_background)
}
        playerNameIsValide = isCorrect
        handleSginUpBtn()
    }
    private fun showAndHidePasword() {
        var isPasswordVisible = false
        val currentTypeface = binding.playerPassword.typeface
        binding.showAndHideSginUpPassword.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                binding.playerPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.playerConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showAndHideSginUpPassword.setImageResource(R.drawable.close_eyes)
                isPasswordVisible = false
            } else {
                // Show password
                binding.playerPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.playerConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showAndHideSginUpPassword.setImageResource(R.drawable.open_eyes)
                isPasswordVisible = true
            }
            binding.playerPassword.setTypeface(currentTypeface)
            binding.playerConfirmPassword.setTypeface(currentTypeface)
            binding.playerPassword.setSelection(binding.playerPassword.text.length)
          if(playerPassword.isEmpty()) {
              binding.sginUpPasswordLayout.setBackgroundResource(R.drawable.enable_player_bar_background)
          }
        if(confirmPasswordText.isEmpty() && playerPassword.isEmpty() ){
            binding.playerConfirmPassword.setBackgroundResource(R.drawable.disable_player_inputes)

        }
        }
    }
    fun isValidName(input: String): Boolean {
        val regex = Regex("^[A-Za-z0-9]{8,}$")

        return input.matches(regex)
    }
    fun isValidGmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9]+@gmail\\.com\$")
        return email.matches(regex)
    }
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")
        return password.matches(passwordRegex)
    }
    fun isValidConPassword(conPassword: String , password: String): Boolean {
        return conPassword == password
    }
    fun generateSecureNumericId(length: Int = 12): String {
        val random = SecureRandom()
        return (1..length)
            .map { random.nextInt(10) } // Generates numbers 0-9
            .joinToString("")
    }
    private fun navigateToNewActivity(newActivity: Class<*>, logainedAccount: Player) {
        currentPlayer  = logainedAccount
        val newActivityIntent = Intent(requireContext() , newActivity)
        newActivityIntent.putExtra("logainedAccount" , logainedAccount)
        startActivity(newActivityIntent)
activity?.finish()

    }
     private fun showFialedCreateAccountDialog(title: String, message: String , btnText : String, isFinish: Boolean){
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