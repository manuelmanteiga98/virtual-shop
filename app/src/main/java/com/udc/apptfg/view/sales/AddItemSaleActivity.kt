package com.udc.apptfg.view.sales

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityAddItemSaleBinding
import com.udc.apptfg.viewmodel.sales.AddItemSaleViewModel

class AddItemSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemSaleBinding
    private val addItemSaleViewModel: AddItemSaleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idSale = intent.getStringExtra("idSale").toString()
        val idItem = intent.getStringExtra("idItem").toString()

        // Image
        addItemSaleViewModel.photo.observe(this) { image ->
            binding.addItemSaleImage.setImageBitmap(image)
        }

        // Max units
        addItemSaleViewModel.maxUnits.observe(this) { currentUnits ->
            binding.addItemSaleCurrentUnits.text = currentUnits.toString()
        }

        // Price
        addItemSaleViewModel.unitPrice.observe(this) { price ->
            binding.addItemSalePrice.text = price.toString()
        }

        // Finish
        addItemSaleViewModel.finish.observe(this) {
            finish()
        }

        // Error observer
        addItemSaleViewModel.error.observe(this) { error ->
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
                "not_enough_units" -> {
                    Toast.makeText(this, getString(R.string.not_enough_units), Toast.LENGTH_SHORT)
                        .show()
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
        binding.addItemSaleButton.setOnClickListener {
            try {
                addItemSaleViewModel.addItemToSale(
                    idSale,
                    idItem,
                    binding.addItemSaleUnits.text.toString().toInt()
                )
            } catch (e: java.lang.NumberFormatException) {
                Toast.makeText(this, getString(R.string.sale_units_null), Toast.LENGTH_SHORT).show()
            }
        }

        // Get info
        addItemSaleViewModel.getInfo(idItem)

    }
}