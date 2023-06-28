package com.udc.apptfg.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityHomeBinding
import com.udc.apptfg.view.employees.EmployeeCalendarActivity
import com.udc.apptfg.view.employees.EmployeesActivity
import com.udc.apptfg.view.inventory.InventoryActivity
import com.udc.apptfg.view.notifications.NotificationsActivity
import com.udc.apptfg.view.orders.OrdersActivity
import com.udc.apptfg.view.sales.SalesActivity
import com.udc.apptfg.view.statistics.StatisticsActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var mgr = false
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mgr = intent.getBooleanExtra("mgr", false)
        email = intent.getStringExtra("email").toString()

        // Employees do not have this functionalities
        if (!mgr) {
            // Removing all mgr functionalities
            for(i in 1..5){
                binding.homeLayout.removeViewAt(0)
            }
            binding.homeCalendar.setOnClickListener {
                val calendarIntent = Intent(this, EmployeeCalendarActivity::class.java)
                calendarIntent.putExtra("mgr", mgr)
                calendarIntent.putExtra("idEmployee", email)
                startActivity(calendarIntent)
            }
        } else {
            binding.calendar.visibility = View.GONE

            binding.homeInventory.setOnClickListener {
                val inventoryIntent = Intent(this, InventoryActivity::class.java)
                startActivity(inventoryIntent)
            }
            binding.homeSales.setOnClickListener {
                val salesIntent = Intent(this, SalesActivity::class.java)
                startActivity(salesIntent)
            }
            binding.homeOrders.setOnClickListener {
                val ordersIntent = Intent(this, OrdersActivity::class.java)
                startActivity(ordersIntent)
            }
            binding.homeEmployees.setOnClickListener {
                val employeesIntent = Intent(this, EmployeesActivity::class.java)
                employeesIntent.putExtra("mgr", mgr)
                startActivity(employeesIntent)
            }
            binding.homeStats.setOnClickListener {
                val statsIntent = Intent(this, StatisticsActivity::class.java)
                startActivity(statsIntent)
            }

        }

        binding.homeNotifications.setOnClickListener {
            val notificationsIntent = Intent(this, NotificationsActivity::class.java)
            startActivity(notificationsIntent)
        }

        this.title = getString(R.string.homePageTitle)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)

        // Logout listener
        menu?.findItem(R.id.homeLogout)?.setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

}