package com.udc.apptfg.view.employees.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.databinding.EmployeeItemBinding
import com.udc.apptfg.model.employees.EmployeeModel

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = EmployeeItemBinding.bind(view)
    var email = ""
    private var name = binding.employeeName
    private var lastName = binding.employeeSurname
    private var photo = binding.employeePhoto

    fun render(item: EmployeeModel) {
        email = item.email
        name.text = item.name
        lastName.text = item.lastName

        if (item.photo == null) {
            photo.setImageResource(R.drawable.ic_article)
        } else {
            photo.setImageBitmap(
                item.photo!!
            )
        }
    }

}