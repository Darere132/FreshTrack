package com.example.freshtrack.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

data class ItemWithCategory(
    @Embedded val item: ItemEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)