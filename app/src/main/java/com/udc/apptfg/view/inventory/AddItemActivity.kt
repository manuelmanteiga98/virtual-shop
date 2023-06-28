package com.udc.apptfg.view.inventory

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import com.google.zxing.integration.android.IntentIntegrator
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityAddItemBinding
import com.udc.apptfg.model.inventory.ItemModel
import com.udc.apptfg.viewmodel.inventory.InventoryViewModel

class AddItemActivity : AppCompatActivity() {

    private var hasImage = false
    private val inventoryViewModel: InventoryViewModel by viewModels()
    private lateinit var binding: ActivityAddItemBinding

    @SuppressLint("UseCompatLoadingForDrawables")
    private val imagePicker = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            // Image selected
            hasImage = true
            binding.addItemSelectImage.background = null
            binding.addItemSelectImage.setImageURI(uri)
        } else {
            // Image not selected
            hasImage = false
            binding.addItemSelectImage.background = getDrawable(R.drawable.image_border)
            binding.addItemSelectImage.setImageResource(R.drawable.ic_camera)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.add)

        // Add Button
        binding.addItemButton.setOnClickListener {
            addItem()
        }

        // Scan listener
        binding.addItemCodeScan.setOnClickListener {
            initScanner()
        }

        // Image Listener
        binding.addItemSelectImage.setOnClickListener {
            imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        // Error listener
        inventoryViewModel.error.observe(this) { error ->
            when (error.type) {
                "code_blank" -> {
                    showAlert(getString(R.string.code_blank))
                    binding.addItemCode.error = getString(R.string.code_blank)
                }
                "name_blank" -> {
                    binding.addItemName.error = getString(R.string.name_blank)
                    showAlert(getString(R.string.name_blank))
                }
                "invalid_cost" -> {
                    binding.addItemCost.error = getString(R.string.cost_format_error)
                    showAlert(getString(R.string.cost_format_error))
                }
                "invalid_price" -> {
                    binding.addItemPrice.error = getString(R.string.price_format_error)
                    showAlert(getString(R.string.price_format_error))
                }
                "invalid_units" -> {
                    binding.addItemUnits.error = getString(R.string.units_format_error)
                    showAlert(getString(R.string.units_format_error))
                }
                else -> showAlert()
            }
        }

        // Finish listener
        inventoryViewModel.finish.observe(this) {
            finish()
        }

        // Observe categories
        inventoryViewModel.categories.observe(this) { categories ->
            categories.remove(getString(R.string.generic))
            categories.add(0, getString(R.string.generic))
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
            binding.addItemCategory.adapter = adapter
        }

        inventoryViewModel.getAllCategories()

    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(
            IntentIntegrator.EAN_8,
            IntentIntegrator.EAN_13
        )
        integrator.setPrompt(getString(R.string.barcode_scanner_prompt))
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
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

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // Canceled
                Toast.makeText(this, getString(R.string.barcode_null), Toast.LENGTH_SHORT).show()
            } else {
                // Barcode scanned
                binding.addItemCode.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun addItem() {
        try {
            var limit: Int? = null
            if (binding.addItemUnitsLimit.text.toString().isNotBlank())
                limit = binding.addItemUnitsLimit.text.toString().toInt()
            val builder = AlertDialog.Builder(this)
            val item = ItemModel(
                binding.addItemCode.text.toString(),
                binding.addItemName.text.toString(),
                binding.addItemCost.text.toString().toDouble(),
                binding.addItemPrice.text.toString().toDouble(),
                binding.addItemCategory.selectedItem.toString(),
                binding.addItemUnits.text.toString().toInt(),
                limit
            )
            if (hasImage)
                item.image = binding.addItemSelectImage.drawable.toBitmap()
            inventoryViewModel.addItem(
                item, builder,
                mapOf(
                    Pair("msg", getString(R.string.item_replace_alert)),
                    Pair("accept", getString(R.string.accept)),
                    Pair("cancel", getString(R.string.cancel))
                )
            )
        } catch (e: java.lang.NumberFormatException) {
            binding.addItemPrice.error = getString(R.string.item_input_validation)
            binding.addItemUnits.error = getString(R.string.item_input_validation)
            binding.addItemCost.error = getString(R.string.item_input_validation)
            showAlert(getString(R.string.item_input_validation))
        }
    }

}