package com.udc.apptfg.view.orders

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityOrderBinding
import com.udc.apptfg.view.orders.adapter.ItemAdapter
import com.udc.apptfg.viewmodel.orders.OrderViewModel
import kotlinx.coroutines.launch

class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private val orderViewModel: OrderViewModel by viewModels()
    var idOrder = ""
    var completed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idOrder = intent.getStringExtra("idOrder").toString()
        completed = intent.getStringExtra("completed").toBoolean()
        this.title = idOrder

        // Recycler view config
        binding.orderRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.orderRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.orderRecyclerView.context, DividerItemDecoration.VERTICAL
            )
        )
        binding.orderRecyclerView.adapter = ItemAdapter(ArrayList(), completed,
            { idItem -> deleteItem(idItem) },
            { idItem -> increase(idItem) },
            { idItem -> decrease(idItem) })

        // List listener
        orderViewModel.orderItems.observe(this) { items ->
            binding.orderRecyclerView.adapter = ItemAdapter(items, completed,
                { idItem -> deleteItem(idItem) },
                { idItem -> increase(idItem) },
                { idItem -> decrease(idItem) })
        }

        // Error listener
        orderViewModel.error.observe(this) { error->
            when(error.type){
                null -> showDialog()
                "empty_order" -> showDialog(getString(R.string.empty_order))
                "not_enough_units" ->
                    showDialog(getString(R.string.not_enough_units) + " item=" + error.msg)
                "item_not_found" ->
                    showDialog(getString(R.string.item_not_found) + " item=" + error.msg)
            }
        }

        // Complete listener
        orderViewModel.complete.observe(this) {
            finish()
        }

        // Init List
        orderViewModel.getAll(idOrder)

    }

    private fun increase(idItem: String) {
        orderViewModel.increase(idOrder, idItem)
    }

    private fun decrease(idItem: String) {
        orderViewModel.decrease(idOrder, idItem)
    }

    private fun deleteItem(idItem: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.item_delete_msg)
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            orderViewModel.deleteItem(idOrder, idItem)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
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

    // Menu config
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (!completed) {
            menuInflater.inflate(R.menu.menu_order, menu)

            // Delete listener
            menu?.findItem(R.id.orderDelete)?.setOnMenuItemClickListener {
                deleteOrder(this.idOrder)
                false
            }

            // Add item listener
            menu?.findItem(R.id.orderAddBarcode)?.setOnMenuItemClickListener {
                addItemBarcode()
                false
            }

            // Add item listener
            menu?.findItem(R.id.orderAddBCManual)?.setOnMenuItemClickListener {
                addItemBarcodeManual()
                false
            }

            // Complete listener
            menu?.findItem(R.id.orderComplete)?.setOnMenuItemClickListener {
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

    private fun add(idItem: String) {
        val intent = Intent(this, AddItemOrderActivity::class.java)
            .putExtra("idOrder", idOrder)
            .putExtra("idItem", idItem)
        refreshOrder.launch(intent)
    }

    private fun deleteOrder(order: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.order_delete_msg) + " " +
                    getString(R.string.code) + "=$order"
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            orderViewModel.deleteOrder(order)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun complete() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.warning))
        builder.setMessage(
            getString(R.string.order_complete_msg)
        )
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int ->
            orderViewModel.complete(idOrder)
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private var refreshOrder =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { this.recreate() }

}