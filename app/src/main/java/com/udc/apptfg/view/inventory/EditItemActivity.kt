package com.udc.apptfg.view.inventory

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityEditItemBinding
import com.udc.apptfg.model.inventory.ItemModel
import com.udc.apptfg.viewmodel.inventory.EditItemViewModel

class EditItemActivity : AppCompatActivity() {

    private var hasImage: Boolean = false
    private var currentImage: Bitmap? = null
    private lateinit var binding: ActivityEditItemBinding
    private val editItemViewModel: EditItemViewModel by viewModels()

    @SuppressLint("UseCompatLoadingForDrawables")
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // Image selected
                hasImage = true
                binding.editItemSelectImage.background = null
                binding.editItemSelectImage.setImageURI(uri)
            } else {
                // Image not selected
                hasImage = false
                if (currentImage == null) {
                    binding.editItemSelectImage.background = getDrawable(R.drawable.image_border)
                    binding.editItemSelectImage.setImageResource(R.drawable.ic_camera)
                } else {
                    binding.editItemSelectImage.background = null
                    binding.editItemSelectImage.setImageBitmap(currentImage)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Activity startup
        val id: String = intent.getStringExtra("id").toString()
        editItemViewModel.initActivity(id)
        this.title = id

        editItemViewModel.item.observe(this) {
            binding.editItemName.setText(it.name)
            binding.editItemCost.setText(it.cost.toString())
            binding.editItemPrice.setText(it.price.toString())
            binding.editItemUnits.setText(it.units.toString())
            if (editItemViewModel.categories.value != null) {
                // If categories are loaded, set item category to first pos
                // in the spinner
                val list = editItemViewModel.categories.value!!
                list.remove(it.category)
                list.add(0, it.category)
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    list
                )
                binding.editItemCategory.adapter = adapter
            }
            if (it.unitsLimit != null) binding.editItemUnitsLimit.setText(it.unitsLimit.toString())
            if (it.image != null) {
                binding.editItemSelectImage.background = null
                binding.editItemSelectImage.setImageBitmap(it.image)
                currentImage = it.image
            }
        }

        // Image Listener
        binding.editItemSelectImage.setOnClickListener {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // On Edit
        binding.editItemButton.setOnClickListener {
            try {
                var limit: Int? = null
                if (binding.editItemUnitsLimit.text.toString().isNotBlank())
                    limit = binding.editItemUnitsLimit.text.toString().toInt()
                val item = ItemModel(
                    id,
                    binding.editItemName.text.toString(),
                    binding.editItemCost.text.toString().toDouble(),
                    binding.editItemPrice.text.toString().toDouble(),
                    binding.editItemCategory.selectedItem.toString(),
                    binding.editItemUnits.text.toString().toInt(),
                    limit
                )
                if (hasImage)
                    item.image = binding.editItemSelectImage.drawable.toBitmap()
                editItemViewModel.edit(item)
            } catch (e: java.lang.NumberFormatException) {
                binding.editItemPrice.error = getString(R.string.item_input_validation)
                binding.editItemUnits.error = getString(R.string.item_input_validation)
                binding.editItemCost.error = getString(R.string.item_input_validation)
                showAlert(getString(R.string.item_input_validation))
            }
        }

        // On Error
        editItemViewModel.error.observe(this) { error ->
            when (error.type) {
                "input_validation" -> {
                    binding.editItemPrice.error = getString(R.string.item_input_validation)
                    binding.editItemUnits.error = getString(R.string.item_input_validation)
                    showAlert(getString(R.string.item_input_validation))
                }
                "invalid_cost" -> {
                    binding.editItemCost.error = getString(R.string.cost_format_error)
                    showAlert(getString(R.string.cost_format_error))
                }
                "invalid_price" -> {
                    binding.editItemPrice.error = getString(R.string.price_format_error)
                    showAlert(getString(R.string.price_format_error))
                }
                "invalid_units" -> {
                    binding.editItemUnits.error = getString(R.string.units_format_error)
                    showAlert(getString(R.string.units_format_error))
                }
                else -> showAlert()
            }

        }

        // Finish
        editItemViewModel.finish.observe(this) { finish() }

        // Observes categories
        editItemViewModel.categories.observe(this) { categories ->
            if (!categories.contains(getString(R.string.generic)))
                categories.add(getString(R.string.generic))
            // If item is loaded set item category to first pos
            if (editItemViewModel.item.value != null) {
                categories.remove(editItemViewModel.item.value!!.category)
                categories.add(0, editItemViewModel.item.value!!.category)
            }
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
            binding.editItemCategory.adapter = adapter
        }

        editItemViewModel.getAllCategories()

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

}