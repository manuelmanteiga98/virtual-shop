package com.udc.apptfg.view.orders.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.databinding.OrderItemBinding
import com.udc.apptfg.databinding.SaleItemBinding
import com.udc.apptfg.model.orders.ItemOrderModel

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = OrderItemBinding.bind(view)
    lateinit var itemID: String
    private val itemName = binding.orderItemName
    private val units = binding.orderItemUnits
    private val photo = binding.orderItemPhoto

    fun render(item: ItemOrderModel) {
        itemID = item.id
        itemName.text = item.name
        units.text = units.text.toString() + ": " + item.units.toString()
        if (item.image == null) {
            photo.setImageResource(R.drawable.ic_article)
        } else {
            photo.setImageBitmap(
                item.image!!
            )
        }
    }
}