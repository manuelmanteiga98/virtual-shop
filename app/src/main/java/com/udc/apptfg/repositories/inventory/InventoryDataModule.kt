package com.udc.apptfg.repositories.inventory

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
interface InventoryDataModule {
    @Binds
    fun bindDataRepository(impl: FirebaseInventoryDataRepository): InventoryDataRepository
}