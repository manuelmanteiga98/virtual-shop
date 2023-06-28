package com.udc.apptfg.view.inventory

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityShowItemBinding
import com.udc.apptfg.viewmodel.inventory.InventoryViewModel
import java.io.File

class ShowItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowItemBinding

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val inventoryViewModel: InventoryViewModel by viewModels()
    private var refreshItem =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { this.recreate() }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityShowItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gets the id
        val itemID = intent.getStringExtra("id").toString()

        binding.showItemCode.text = itemID
        this.title = itemID

        if (auth.currentUser != null) {
            val user = auth.currentUser!!.email.toString()
            val userRef = db.collection("users").document(user)
            userRef.collection("items").document(itemID).get().addOnSuccessListener { item ->
                if (item.exists()) {
                    binding.showItemName.text = item.getString("name")
                    binding.showItemCost.text = item.getDouble("cost").toString()
                    binding.showItemPrice.text = item.getDouble("price").toString()
                    binding.showItemUnits.text = item.getLong("units").toString()
                    binding.showItemCategory.text = item.getString("category")
                    val limit = item.get("units_limit") as Long?
                    if (limit == null) binding.showItemUnitsLimit.text = getString(R.string.no)
                    else binding.showItemUnitsLimit.text = limit.toString()

                    val localfile = File.createTempFile(itemID, "jpg")
                    val path = auth.currentUser!!.email + "/items/" + itemID
                    val reference = storage.reference.child(path)
                    reference.getFile(localfile).addOnSuccessListener {
                        binding.showItemImage.setImageBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeFile(localfile.absolutePath), 200, 200, false
                            )
                        )
                    }.addOnFailureListener {
                        binding.showItemImage.setImageResource(R.drawable.ic_article)
                    }
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.error))
                    builder.setMessage(getString(R.string.item_not_found))
                    builder.setPositiveButton(
                        "OK"
                    ) { _, _ -> finish() }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }.addOnFailureListener {
                finish()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_show_item, menu)
        val edit = menu?.findItem(R.id.showItemEditItem)
        val delete = menu?.findItem(R.id.showItemDeleteItem)


        edit?.setOnMenuItemClickListener {
            editItem()
            false
        }

        delete?.setOnMenuItemClickListener {
            deleteItem()
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun editItem() {
        intent = Intent(this, EditItemActivity::class.java).putExtra(
            "id", binding.showItemCode.text.toString()
        )
        refreshItem.launch(intent)
    }

    private fun deleteItem() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.item_delete_msg) + " " +
                    getString(R.string.name) + "=${binding.showItemName.text}"
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            inventoryViewModel.deleteItem(binding.showItemCode.text.toString())
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}