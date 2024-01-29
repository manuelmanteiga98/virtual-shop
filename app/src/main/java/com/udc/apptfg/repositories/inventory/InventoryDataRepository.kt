package com.udc.apptfg.repositories.inventory

import com.udc.apptfg.model.inventory.ItemModel

interface InventoryDataRepository{
    fun addItem(item:ItemModel, callback: (Boolean) -> Unit)
}