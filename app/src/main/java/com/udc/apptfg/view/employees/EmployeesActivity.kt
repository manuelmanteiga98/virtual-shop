package com.udc.apptfg.view.employees

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.udc.apptfg.R
import com.udc.apptfg.databinding.ActivityEmployeesBinding
import com.udc.apptfg.view.employees.adapter.ItemAdapter
import com.udc.apptfg.viewmodel.employees.EmployeesViewModel

class EmployeesActivity : AppCompatActivity() {

    lateinit var binding: ActivityEmployeesBinding
    private val employeesViewModel: EmployeesViewModel by viewModels()
    private var mgr = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.employees)
        mgr = intent.getBooleanExtra("mgr", false)

        // Employee list config
        binding.employeesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.employeesRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.employeesRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        // List observer
        employeesViewModel.employees.observe(this){ employees->
            binding.employeesRecyclerView.adapter = ItemAdapter(employees){ id ->
                showEmployeeCalendar(id)
            }
        }

        // Loads the employees
        employeesViewModel.getAll()
    }

    private fun showEmployeeCalendar(id:String){
        val intent = Intent(this, EmployeeCalendarActivity::class.java)
        intent.putExtra("idEmployee", id)
        intent.putExtra("mgr", mgr)
        startActivity(intent)
    }

}