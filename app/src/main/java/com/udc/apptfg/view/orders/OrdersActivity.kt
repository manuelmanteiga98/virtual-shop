package com.udc.apptfg.view.orders

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityOrdersBinding
import com.udc.apptfg.model.orders.OrderModel
import com.udc.apptfg.view.orders.adapter.OrderAdapter
import com.udc.apptfg.viewmodel.orders.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val ordersViewModel: OrdersViewModel by viewModels()
    private var state: State = State.NOT_COMPLETED
    private var currentList = ArrayList<OrderModel>()

    enum class State {
        ALL, COMPLETED, NOT_COMPLETED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.orders)

        // Orders list observer
        ordersViewModel.orders.observe(this){ orders->

            currentList = ArrayList()
            for (order in orders) {
                if (((order.completed) && (state != State.NOT_COMPLETED))
                    || ((!order.completed) && (state != State.COMPLETED))
                ) {
                    currentList.add(order)
                }
            }

            // Order list by date desc
            val formatter = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
            currentList.sortBy { orderModel -> formatter.parse(orderModel.date)}
            currentList.reverse()

            binding.ordersList.adapter = OrderAdapter(
                this,
                android.R.layout.simple_list_item_1,
                currentList
            )
        }

        // Order status changes
        binding.ordersMenu.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.ordersMenuCompleted -> {
                    binding.ordersMenu.menu.getItem(1).isChecked = true
                    state = State.COMPLETED
                    getCompleted()
                }
                R.id.ordersMenuNotCompleted -> {
                    binding.ordersMenu.menu.getItem(0).isChecked = true
                    state = State.NOT_COMPLETED
                    getNotCompleted()
                }
                R.id.ordersMenuAll -> {
                    binding.ordersMenu.menu.getItem(2).isChecked = true
                    state = State.ALL
                    getAll()
                }
            }
            false
        }

        // Order click listener
        binding.ordersList.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, OrderActivity::class.java)
            intent.putExtra("idOrder", currentList[i].id)
            intent.putExtra("completed", currentList[i].completed.toString())
            refreshOrders.launch(intent)
        }

        // Initial loading
        ordersViewModel.getAll()

    }

    private var refreshOrders =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            currentList = ArrayList()
            binding.ordersMenu.menu.getItem(0).isChecked = true
            this.recreate()
        }

    private fun getAll() {
        currentList = ArrayList()
        if(ordersViewModel.orders.value!=null) {
            for (order in ordersViewModel.orders.value!!) {
                currentList.add(order)
            }
        }
        binding.ordersList.adapter = OrderAdapter(
            this,
            android.R.layout.simple_list_item_1,
            currentList
        )
    }

    private fun getCompleted() {
        currentList = ArrayList()
        if(ordersViewModel.orders.value!=null) {
            for (order in ordersViewModel.orders.value!!) {
                if (order.completed) currentList.add(order)
            }
        }
        binding.ordersList.adapter = OrderAdapter(
            this,
            android.R.layout.simple_list_item_1,
            currentList
        )
    }

    private fun getNotCompleted() {
        currentList = ArrayList()
        if(ordersViewModel.orders.value!=null){
            for (order in ordersViewModel.orders.value!!) {
                if (!order.completed) currentList.add(order)
            }
        }
        binding.ordersList.adapter = OrderAdapter(
            this,
            android.R.layout.simple_list_item_1,
            currentList
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_orders, menu)
        menu?.findItem(R.id.ordersNewOrder)?.setOnMenuItemClickListener {
            ordersViewModel.newOrder()
            false
        }
        return super.onCreateOptionsMenu(menu)
    }

}