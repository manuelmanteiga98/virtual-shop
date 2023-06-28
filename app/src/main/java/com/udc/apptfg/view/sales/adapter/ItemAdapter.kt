package com.udc.apptfg.view.sales.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.udc.apptfg.R
import com.udc.apptfg.model.sales.ItemSaleModel

class ItemAdapter(
    private val itemList: List<ItemSaleModel>,
    private val completed: Boolean,
    private val delete: (String) -> Unit,
    private val increase: (String) -> Unit,
    private val decrease: (String) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(layoutInflater.inflate(R.layout.sale_item, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.render(item)
        if(!completed) {
            holder.binding.saleDeleteItem.setOnClickListener {
                delete(holder.itemID)
            }
            holder.binding.saleIncreaseItemUnits.setOnClickListener {
                increase(holder.itemID)
            }
            holder.binding.saleDecreaseItemUnits.setOnClickListener {
                decrease(holder.itemID)
            }
        } else{
            holder.binding.saleDeleteItem.visibility = View.GONE
            holder.binding.saleIncreaseItemUnits.visibility = View.GONE
            holder.binding.saleDecreaseItemUnits.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = itemList.size
}