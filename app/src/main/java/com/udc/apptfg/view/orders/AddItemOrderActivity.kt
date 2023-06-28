package com.udc.apptfg.view.orders

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityAddItemOrderBinding
import com.udc.apptfg.viewmodel.orders.AddItemOrderViewModel

class AddItemOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddItemOrderBinding
    private val addItemOrderViewModel: AddItemOrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idOrder = intent.getStringExtra("idOrder").toString()
        val idItem = intent.getStringExtra("idItem").toString()

        // Image
        addItemOrderViewModel.photo.observe(this) { image ->
            binding.addItemOrderImage.setImageBitmap(image)
        }

        // Finish
        addItemOrderViewModel.finish.observe(this) {
            finish()
        }

        // Error observer
        addItemOrderViewModel.error.observe(this) { error ->
            when (error.type) {
                "item_not_found" -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.error))
                    builder.setMessage(getString(R.string.item_not_found))
                    builder.setPositiveButton(getString(R.string.accept)){ _,_ ->
                        this.finish()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
                "units_zero" -> {
                    Toast.makeText(this, getString(R.string.sale_units_zero), Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    Toast.makeText(this, getString(R.string.error_msg), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // On Add Listener
        binding.addItemOrderButton.setOnClickListener {
            try {
                addItemOrderViewModel.addItemToOrder(
                    idOrder,
                    idItem,
                    binding.addItemOrderUnits.text.toString().toInt()
                )
            } catch (e: java.lang.NumberFormatException) {
                Toast.makeText(this, getString(R.string.sale_units_null), Toast.LENGTH_SHORT).show()
            }
        }

        // Get info
        addItemOrderViewModel.getInfo(idItem)

    }
}