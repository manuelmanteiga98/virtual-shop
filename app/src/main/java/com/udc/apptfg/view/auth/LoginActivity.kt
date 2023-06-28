package com.udc.apptfg.view.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import com.udc.apptfg.R
import com.udc.apptfg.view.HomeActivity
import com.udc.apptfg.databinding.ActivityLoginBinding
import com.udc.apptfg.viewmodel.auth.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup() {

        //Shopping cart icon
        binding.loginImage.setBackgroundResource(R.drawable.shoppingcarticon)
        binding.loginImage.layoutParams.height = 200
        binding.loginImage.layoutParams.width = 200

        //Login button
        binding.loginSignIn.setOnClickListener {
            authViewModel.login(
                binding.loginMail.text.toString(),
                binding.loginPassword.text.toString()
            )
        }

        // Sign up text
        binding.loginSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Forgot text
        binding.loginForgot.setOnClickListener {
            startActivity(Intent(this, ResetPassActivity::class.java))
        }

        // ViewModel auth
        authViewModel.authModel.observe(this) {
            authViewModel.isManager(it.email)
        }

        // Now we know if its a manager account
        authViewModel.manager.observe(this){
            showHome(it)
        }

        // ViewModel error
        authViewModel.errorModel.observe(this) { error ->
            when (error.type) {
                "firebase" -> showAlert(error.msg.toString())
                "email_validation" -> {
                    showAlert(getString(R.string.email_validation))
                    binding.loginMail.error = getString(R.string.email_validation)
                }
                "password_blank" -> {
                    showAlert(getString(R.string.password_blank))
                    binding.loginPassword.error = getString(R.string.password_blank)
                }
                "unexpected" -> showAlert(getString(R.string.error_msg))
            }
        }

    }

    // Error messages
    private fun showAlert(msg: String = "") {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.error))
        if (msg.isEmpty()) {
            builder.setMessage(getString(R.string.error_msg))
        } else {
            builder.setMessage(msg)
        }
        builder.setPositiveButton(getString(R.string.accept), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Transition to home page
    private fun showHome(mgr:Boolean) {
        val homeIntent = Intent(this, HomeActivity::class.java)
        homeIntent.putExtra("mgr", mgr)
        homeIntent.putExtra("email", authViewModel.authModel.value!!.email)
        startActivity(homeIntent)
    }

}