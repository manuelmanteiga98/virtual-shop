package com.udc.apptfg.view.inventory

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityInventoryBinding
import com.udc.apptfg.model.inventory.ItemModel
import com.udc.apptfg.view.inventory.adapter.ItemAdapter
import com.udc.apptfg.viewmodel.inventory.InventoryViewModel
import java.util.*
import kotlin.collections.ArrayList


class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()
    private var categorizedList = ArrayList<ItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        this.title = getString(R.string.inventory)
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recycler view config
        binding.inventoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.inventoryRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.inventoryRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        // Item list Observer
        inventoryViewModel.items.observe(this) { list ->
            // Hides initial loading animation
            if (binding.loadingPanel.visibility == View.VISIBLE)
                binding.loadingPanel.visibility = View.GONE
            initRecyclerView(list, binding.inventoryRecyclerView)
            categorizedList = list
        }

        // Category selector
        inventoryViewModel.categories.observe(this) { categories ->
            categories.add(0, getString(R.string.all_categories))
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
            binding.inventoryCategorySelector.adapter = adapter
        }

        binding.inventoryCategorySelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (inventoryViewModel.items.value != null) {
                        val category = binding.inventoryCategorySelector.selectedItem.toString()
                        val list = inventoryViewModel.items.value!!
                        if (category == getString(R.string.all_categories)) {
                            binding.inventoryRecyclerView.adapter = ItemAdapter(list,
                                { idItem, name -> deleteItem(idItem, name) },
                                { idItem -> showItem(idItem) },
                                { idItem -> editItem(idItem) }
                            )
                            categorizedList = list
                        } else {
                            categorizedList = ArrayList()
                            for (item in list) {
                                if (item.category == category) {
                                    categorizedList.add(item)
                                }
                            }
                            binding.inventoryRecyclerView.adapter = ItemAdapter(categorizedList,
                                { idItem, name -> deleteItem(idItem, name) },
                                { idItem -> showItem(idItem) },
                                { idItem -> editItem(idItem) }
                            )
                        }
                    }
                }

            }

        // Hide loading animation
        inventoryViewModel.loaded.observe(this){
            binding.loadingPanel.visibility = View.GONE
        }

        // Initial load
        inventoryViewModel.getAll()
        inventoryViewModel.getAllCategories()

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
                val intent = Intent(this, ShowItemActivity::class.java)
                    .putExtra("id", result.contents)
                refreshInventory.launch(intent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Menu config
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action_bar, menu)

        // Add item listener
        menu?.findItem(R.id.addItem)?.setOnMenuItemClickListener {
            addItem()
            false
        }

        // Category management listener
        menu?.findItem(R.id.manageCategories)?.setOnMenuItemClickListener {
            showCategories()
            false
        }

        // CB listener
        menu?.findItem(R.id.searchItem)?.setOnMenuItemClickListener {
            searchItem()
            false
        }

        // Search listener
        (menu?.findItem(R.id.searchIcon)?.actionView as SearchView).setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    p0?.let {
                        val currentList = ArrayList<ItemModel>()
                        for (item in categorizedList) {
                            if (item.name.lowercase().contains(p0.lowercase())) {
                                currentList.add(item)
                            }
                        }
                        binding.inventoryRecyclerView.adapter = ItemAdapter(currentList,
                            { id, name -> deleteItem(id, name) },
                            { id -> showItem(id) },
                            { id -> editItem(id) }
                        )
                    }
                    return false
                }

            })

        return super.onCreateOptionsMenu(menu)
    }

    private fun initRecyclerView(list: List<ItemModel>, recyclerView: RecyclerView) {

        recyclerView.adapter = ItemAdapter(list,
            { id, name -> deleteItem(id, name) },
            { id -> showItem(id) },
            { id -> editItem(id) }
        )

    }

    private fun showItem(id: String) {
        val showItem = Intent(this, ShowItemActivity::class.java).putExtra(
            "id", id
        )
        refreshInventory.launch(showItem)
    }

    private fun deleteItem(id: String, name: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.item_delete_msg) + " " +
                    getString(R.string.name) + "=$name"
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            inventoryViewModel.deleteItem(id)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun editItem(id: String) {
        val editIntent = Intent(this, EditItemActivity::class.java).putExtra(
            "id", id
        )
        refreshInventory.launch(editIntent)
    }

    private var refreshInventory =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { inventoryViewModel.getAll() }


    private fun addItem() {
        val addIntent = Intent(this, AddItemActivity::class.java)
        refreshInventory.launch(addIntent)
    }

    private fun searchItem() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(
            IntentIntegrator.EAN_8,
            IntentIntegrator.EAN_13
        )
        integrator.setPrompt(getString(R.string.barcode_scanner_prompt))
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    private fun showCategories() {
        intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
    }


}