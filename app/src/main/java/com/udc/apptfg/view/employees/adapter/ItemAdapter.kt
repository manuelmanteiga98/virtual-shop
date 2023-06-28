package com.udc.apptfg.view.employees.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.model.employees.EmployeeModel

class ItemAdapter(
    private val itemList: List<EmployeeModel>,
    private val calendar: (String) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.employee_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.render(item)
        holder.binding.employeeCalendar.setOnClickListener {
            calendar(holder.email)
        }
    }

    override fun getItemCount(): Int = itemList.size
}