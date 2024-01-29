package com.udc.apptfg.view.sales

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivitySalesBinding
import com.udc.apptfg.model.sales.SaleModel
import com.udc.apptfg.view.sales.adapter.SaleAdapter
import com.udc.apptfg.viewmodel.sales.SalesViewModel
import java.text.SimpleDateFormat
import java.util.Locale


class SalesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesBinding
    private val salesViewModel: SalesViewModel by viewModels()
    private var state: State = State.NOT_COMPLETED
    private var currentList = ArrayList<SaleModel>()

    enum class State {
        ALL, COMPLETED, NOT_COMPLETED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.sales)
        binding.salesMenu.menu.getItem(0).isChecked = true

        // Error listener
        salesViewModel.error.observe(this) { error ->
            if (error.type == "sale_has_items") showDialog(getString(R.string.sale_has_items))
            else showDialog()
        }

        // Sale status changes
        binding.salesMenu.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.salesMenuCompleted -> {
                    binding.salesMenu.menu.getItem(1).isChecked = true
                    state = State.COMPLETED
                    getCompleted()
                }
                R.id.salesMenuNotCompleted -> {
                    binding.salesMenu.menu.getItem(0).isChecked = true
                    state = State.NOT_COMPLETED
                    getNotCompleted()
                }
                R.id.salesMenuAll -> {
                    binding.salesMenu.menu.getItem(2).isChecked = true
                    state = State.ALL
                    getAll()
                }
            }
            false
        }

        // Observes new sales
        salesViewModel.sales.observe(this) { sales ->
            currentList = ArrayList()
            for (sale in sales) {
                if (((sale.completed) && (state != State.NOT_COMPLETED))
                    || ((!sale.completed) && (state != State.COMPLETED))
                ) {
                    currentList.add(sale)
                }
            }

            // Order list by date desc
            val formatter = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
            currentList.sortBy { saleModel -> formatter.parse(saleModel.date)}
            currentList.reverse()

            binding.salesList.adapter = SaleAdapter(
                this,
                android.R.layout.simple_list_item_1,
                currentList
            )
        }

        // Sale click listener
        binding.salesList.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, SaleActivity::class.java)
            intent.putExtra("idSale", currentList[i].id)
            intent.putExtra("completed", currentList[i].completed.toString())
            refreshSales.launch(intent)
        }

        // Initial load
        salesViewModel.getAll()

    }

    private var refreshSales =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            currentList = ArrayList()
            binding.salesMenu.menu.getItem(0).isChecked = true
            this.recreate()
        }

    private fun getAll() {
        currentList = ArrayList()
        if(salesViewModel.sales.value!=null) {
            for (sale in salesViewModel.sales.value!!) {
                currentList.add(sale)
            }
        }
        binding.salesList.adapter = SaleAdapter(
            this,
            android.R.layout.simple_list_item_1,
            currentList
        )
    }

    private fun getCompleted() {
        currentList = ArrayList()
        if(salesViewModel.sales.value!=null) {
            for (sale in salesViewModel.sales.value!!) {
                if (sale.completed) currentList.add(sale)
            }
        }
        binding.salesList.adapter = SaleAdapter(
            this,
            android.R.layout.simple_list_item_1,
            currentList
        )
    }

    private fun getNotCompleted() {
        currentList = ArrayList()
        if(salesViewModel.sales.value!=null) {
            for (sale in salesViewModel.sales.value!!) {
                if (!sale.completed) currentList.add(sale)
            }
        }
        binding.salesList.adapter = SaleAdapter(
            this,
            android.R.layout.simple_list_item_1,
            currentList
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sales, menu)
        menu?.findItem(R.id.salesNewSale)?.setOnMenuItemClickListener {
            salesViewModel.newSale()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun showDialog(
        msg: String = getString(R.string.error_msg),
        title: String = getString(R.string.error)
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(getString(R.string.accept)) { _: DialogInterface, _: Int -> }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}