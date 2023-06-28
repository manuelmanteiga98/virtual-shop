package com.udc.apptfg.view.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.model.orders.ItemOrderModel

class ItemAdapter(
    private val itemList: List<ItemOrderModel>,
    private val completed: Boolean,
    private val delete: (String) -> Unit,
    private val increase: (String) -> Unit,
    private val decrease: (String) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.order_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.render(item)
        if(!completed) {
            holder.binding.orderDeleteItem.setOnClickListener {
                delete(holder.itemID)
            }
            holder.binding.orderIncreaseItemUnits.setOnClickListener {
                increase(holder.itemID)
            }
            holder.binding.orderDecreaseItemUnits.setOnClickListener {
                decrease(holder.itemID)
            }
        } else{
            holder.binding.orderDeleteItem.visibility = View.GONE
            holder.binding.orderIncreaseItemUnits.visibility = View.GONE
            holder.binding.orderDecreaseItemUnits.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = itemList.size
}