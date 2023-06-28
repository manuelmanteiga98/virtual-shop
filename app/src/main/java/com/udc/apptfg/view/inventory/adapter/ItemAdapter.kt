package com.udc.apptfg.view.inventory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.model.inventory.ItemModel

class ItemAdapter(
    private val itemList: List<ItemModel>,
    private val delete: (String, String) -> Unit,
    private val show: (String) -> Unit,
    private val edit: (String) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.inventory_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.render(item)
        holder.binding.inventoryDeleteItem.setOnClickListener {
            delete(
                holder.itemID,
                holder.binding.inventoryItemName.text.toString()
            )
        }
        holder.binding.inventoryItemPhoto.setOnClickListener {
            show(holder.itemID)
        }
        holder.binding.inventoryEditItem.setOnClickListener {
            edit(holder.itemID)
        }
    }

    override fun getItemCount(): Int = itemList.size
}