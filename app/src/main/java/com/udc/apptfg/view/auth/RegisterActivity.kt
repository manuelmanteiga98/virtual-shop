package com.udc.apptfg.view.auth

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import com.udc.apptfg.R
import com.udc.apptfg.view.HomeActivity
import com.udc.apptfg.databinding.ActivityRegisterBinding
import com.udc.apptfg.viewmodel.auth.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private var hasImage = false
    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    @SuppressLint("UseCompatLoadingForDrawables")
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // Image selected
                hasImage = true
                binding.registerPhoto.background = null
                binding.registerPhoto.setImageURI(uri)
            } else {
                // Image not selected
                hasImage = false
                binding.registerPhoto.background = getDrawable(R.drawable.image_border)
                binding.registerPhoto.setImageResource(R.drawable.ic_camera)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Job Type settings
        val spinner: Spinner = binding.registerJobType
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            this,
            R.array.job_type,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        // Set MGR visibility
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.registerMgr.visibility = View.GONE
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) binding.registerMgr.visibility = View.GONE
                else binding.registerMgr.visibility = View.VISIBLE
            }

        }

        // Image Listener
        binding.registerPhoto.setOnClickListener {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // on Register click
        binding.registerSignup.setOnClickListener {
            authViewModel.register(
                binding.registerMail.text.toString(),
                binding.registerPassword.text.toString(),
                binding.registerPassword2.text.toString(),
                binding.registerName.text.toString(),
                binding.registerSurname.text.toString(),
                binding.registerJobType.selectedItemPosition,
                binding.registerMgr.text.toString(),
                if (hasImage) binding.registerPhoto.drawable.toBitmap() else null
            )
        }

        // ViewModel auth
        authViewModel.authModel.observe(this) {
            showHome(binding.registerJobType.selectedItemPosition==0)
        }

        // ViewModel error
        authViewModel.errorModel.observe(this) { error ->
            when (error.type) {
                "firebase" -> showAlert(error.msg.toString())
                "email_validation" -> {
                    showAlert(getString(R.string.email_validation))
                    binding.registerMail.error = getString(R.string.email_validation)
                }
                "password_blank" -> {
                    showAlert(getString(R.string.password_blank))
                    binding.registerPassword.error = getString(R.string.password_blank)
                }
                "password_match" -> {
                    showAlert(getString(R.string.password_match))
                    binding.registerPassword.error = getString(R.string.password_match)
                }
                "name_blank" -> {
                    showAlert(getString(R.string.name_blank))
                    binding.registerName.error = getString(R.string.name_blank)
                }
                "surname_blank" -> {
                    showAlert(getString(R.string.surname_blank))
                    binding.registerSurname.error = getString(R.string.surname_blank)
                }
                "mgr_validation" -> {
                    showAlert(getString(R.string.mgr_validation))
                    binding.registerMgr.error = getString(R.string.mgr_validation)
                }
                "mgr_not_found" -> {
                    showAlert(getString(R.string.mgr_not_found))
                    binding.registerMgr.error = getString(R.string.mgr_not_found)
                }
                "not_mgr" -> {
                    showAlert(getString(R.string.not_mgr))
                    binding.registerMgr.error = getString(R.string.not_mgr)
                }
                "unexpected" -> showAlert(getString(R.string.error_msg))
            }
        }

    }

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