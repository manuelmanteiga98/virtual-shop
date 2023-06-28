package com.udc.apptfg.view.notifications.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.model.notifications.NotificationModel

class ItemAdapter(
    private val itemList: List<NotificationModel>,
    private val read: (String) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.notification, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.render(item)
        holder.binding.notificationRead.setOnClickListener {
            read(holder.id)
        }
        if(holder.read){
            holder.binding.notificationRead.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = itemList.size
}