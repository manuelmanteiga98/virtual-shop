package com.udc.apptfg.view.inventory.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.databinding.InventoryItemBinding
import com.udc.apptfg.model.inventory.ItemModel

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = InventoryItemBinding.bind(view)
    lateinit var itemID: String
    private val itemName = binding.inventoryItemName
    private val price = binding.inventoryItemPrice
    private val units = binding.inventoryItemUnits
    private val photo = binding.inventoryItemPhoto

    fun render(item: ItemModel) {
        itemID = item.id
        itemName.text = item.name
        price.text = binding.price.text.toString() + ": " + item.price.toString() + "€"
        units.text = binding.units.text.toString() + ": " + item.units.toString()
        if (item.image == null) {
            photo.setImageResource(R.drawable.ic_article)
        } else {
            binding.inventoryItemPhoto.setImageBitmap(
                item.image!!
            )
        }
    }

}