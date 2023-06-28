package com.udc.apptfg.view.employees.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.udc.apptfg.model.employees.AssignmentModel

class AssignmentAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val assignments: List<AssignmentModel>):
    ArrayAdapter<AssignmentModel>(context, layoutResource, assignments) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
        view.text = assignments[position].entry + " - " + assignments[position].exit
        return view
    }
}