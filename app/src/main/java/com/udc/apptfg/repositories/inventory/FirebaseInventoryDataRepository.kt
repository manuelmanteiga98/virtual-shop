package com.udc.apptfg.repositories.inventory

import com.udc.apptfg.model.inventory.ItemModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseInventoryDataRepository @Inject constructor(): InventoryDataRepository {
    override fun addItem(item: ItemModel, callback: (Boolean) -> Unit) {
        print("Hola")
    }
}