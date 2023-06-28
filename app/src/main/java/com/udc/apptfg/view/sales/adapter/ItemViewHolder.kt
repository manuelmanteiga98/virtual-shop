package com.udc.apptfg.view.sales.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.databinding.SaleItemBinding
import com.udc.apptfg.model.sales.ItemSaleModel

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = SaleItemBinding.bind(view)
    lateinit var itemID: String
    private val itemName = binding.saleItemName
    private val price = binding.saleItemPrice
    private val units = binding.saleItemUnits
    private val photo = binding.saleItemPhoto

    fun render(item: ItemSaleModel) {
        itemID = item.id
        itemName.text = item.name
        price.text = price.text.toString() + ": " + item.price.toString() + "€"
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