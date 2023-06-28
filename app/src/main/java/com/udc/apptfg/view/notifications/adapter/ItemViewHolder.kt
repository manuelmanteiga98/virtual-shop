package com.udc.apptfg.view.notifications.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.databinding.NotificationBinding
import com.udc.apptfg.databinding.OrderItemBinding
import com.udc.apptfg.databinding.SaleItemBinding
import com.udc.apptfg.model.notifications.NotificationModel
import com.udc.apptfg.model.orders.ItemOrderModel

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = NotificationBinding.bind(view)
    lateinit var id: String
    var subject = ""
    var content = ""
    var read = false

    fun render(item: NotificationModel) {
        id = item.id
        subject = item.subject
        content = item.content
        read = item.read

        binding.notificationSubject.text = item.subject
        binding.notificationContent.text = item.content
    }
}