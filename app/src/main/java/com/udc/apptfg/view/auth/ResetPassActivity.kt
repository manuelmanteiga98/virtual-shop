package com.udc.apptfg.view.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityResetPassBinding
import com.udc.apptfg.viewmodel.auth.AuthViewModel

class ResetPassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPassBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.resetPasswordBtn.setOnClickListener {
            val email = binding.resetPasswordEmail.text.toString()
            authViewModel.forgot(email)
        }

        authViewModel.errorModel.observe(this) { error ->
            when (error.type) {
                "email_validation" -> {
                    showAlert(getString(R.string.email_validation))
                    binding.resetPasswordEmail.error = getString(R.string.email_validation)
                }
                "firebase" -> showAlert(error.msg.toString())
            }
        }

        authViewModel.reset.observe(this) {
            showAlert(getString(R.string.success_msg), getString(R.string.success))
        }

    }

    private fun showAlert(msg: String = "", title: String = getString(R.string.error)) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        if (msg.isEmpty()) {
            builder.setMessage(getString(R.string.error_msg))
        } else {
            builder.setMessage(msg)
        }
        builder.setPositiveButton(getString(R.string.accept), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}