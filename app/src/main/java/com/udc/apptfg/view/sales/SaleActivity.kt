package com.udc.apptfg.view.sales

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.core.ActivityScope
import com.google.zxing.integration.android.IntentIntegrator
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivitySaleBinding
import com.udc.apptfg.model.ErrorModel
import com.udc.apptfg.view.sales.adapter.ItemAdapter
import com.udc.apptfg.viewmodel.sales.SaleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaleBinding
    private val saleViewModel: SaleViewModel by viewModels()
    private var idSale: String = ""
    private var completed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idSale = intent.getStringExtra("idSale").toString()
        completed = intent.getStringExtra("completed").toBoolean()
        this.title = idSale

        // Recycler view config
        binding.saleRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.saleRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.saleRecyclerView.context, DividerItemDecoration.VERTICAL
            )
        )
        binding.saleRecyclerView.adapter = ItemAdapter(ArrayList(), completed,
            { idItem -> deleteItem(idItem) },
            { idItem -> increase(idItem) },
            { idItem -> decrease(idItem) })

        // List listener
        saleViewModel.saleItems.observe(this) { items ->
            binding.saleRecyclerView.adapter = ItemAdapter(items, completed,
                { idItem -> deleteItem(idItem) },
                { idItem -> increase(idItem) },
                { idItem -> decrease(idItem) })
        }

        // Error listener
        saleViewModel.error.observe(this) { error->
            when(error.type){
                null -> showDialog()
                "empty_sale" -> showDialog(getString(R.string.empty_sale))
                "not_enough_units" ->
                    showDialog(getString(R.string.not_enough_units) + " item=" + error.msg)
                "item_not_found" ->
                    showDialog(getString(R.string.item_not_found) + " item=" + error.msg)
            }
        }

        // Complete listener
        saleViewModel.complete.observe(this) {
            finish()
        }

        // Init List
        saleViewModel.getAll(idSale)

    }

    private fun showDialog(msg:String?=null, title:String?=null) {
        val builder = AlertDialog.Builder(this)

        if(title==null) {
            builder.setTitle(getString(R.string.error))
        } else{
            builder.setTitle(title.toString())
        }

        if(msg==null) {
            builder.setMessage(getString(R.string.error_msg))
        } else{
            builder.setMessage(msg)
        }

        builder.setPositiveButton(getString(R.string.accept), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteSale(sale: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.sale_delete_msg) + " " +
                    getString(R.string.code) + "=$sale"
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            lifecycleScope.launch {
                saleViewModel.deleteSale(sale)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    // Menu config
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (!completed) {
            menuInflater.inflate(R.menu.menu_sale, menu)

            // Delete listener
            menu?.findItem(R.id.saleDelete)?.setOnMenuItemClickListener {
                deleteSale(this.idSale)
                false
            }

            // Add item listener
            menu?.findItem(R.id.saleAddBarcode)?.setOnMenuItemClickListener {
                addItemBarcode()
                false
            }

            // Add item manual listener
            menu?.findItem(R.id.saleAddBCManual)?.setOnMenuItemClickListener {
                addItemBarcodeManual()
                false
            }

            // Complete listener
            menu?.findItem(R.id.saleComplete)?.setOnMenuItemClickListener {
                complete()
                false
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    // Add Item by Barcode function
    private fun addItemBarcode() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(
            IntentIntegrator.EAN_8, IntentIntegrator.EAN_13
        )
        integrator.setPrompt(getString(R.string.barcode_scanner_prompt))
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    // Add item introducing the barcode manually
    private fun addItemBarcodeManual(){
        val dialogBuilder = AlertDialog.Builder(this)
        val dialog = dialogBuilder.create()
        val view = layoutInflater.inflate(R.layout.dialog_barcode, null)
        dialog.setView(view)
        view.findViewById<Button>(R.id.itemCancel).setOnClickListener {dialog.dismiss()}
        view.findViewById<Button>(R.id.itemAdd).setOnClickListener {
            //Hacer intent a la otra vista
            val id = view.findViewById<EditText>(R.id.itemBarcode).text.toString()
            add(id)
            dialog.dismiss()
        }
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
                add(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun complete() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.sale_complete_msg)
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            lifecycleScope.launch{
                saleViewModel.complete(idSale)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

    private fun increase(idItem: String) {
        saleViewModel.increase(idSale, idItem)
    }

    private fun decrease(idItem: String) {
        saleViewModel.decrease(idSale, idItem)
    }

    private fun add(idItem: String) {
        val intent = Intent(this, AddItemSaleActivity::class.java)
            .putExtra("idSale", idSale)
            .putExtra("idItem", idItem)
        refreshSale.launch(intent)
    }

    private fun deleteItem(idItem: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.item_delete_msg)
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            saleViewModel.deleteItem(idSale, idItem)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private var refreshSale =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { this.recreate() }

}