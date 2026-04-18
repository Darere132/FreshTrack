package com.example.freshtrack.presentation.inventory

import com.example.freshtrack.data.ItemEntity
import java.util.concurrent.TimeUnit

data class InventoryItemUi(
    val id: Int,
    val name: String,
    val quantityText: String,
    val expirationDate: Long,
    val status: ItemStatus
)

fun ItemEntity.toUi(nowMillis: Long = System.currentTimeMillis()): InventoryItemUi {
    val twoDaysMillis = TimeUnit.DAYS.toMillis(2)
    val status = when {
        isConsumed -> ItemStatus.CONSUMED
        expirationDate < nowMillis -> ItemStatus.EXPIRED
        expirationDate - nowMillis <= twoDaysMillis -> ItemStatus.EXPIRING_SOON
        else -> ItemStatus.FRESH
    }

    return InventoryItemUi(
        id = id,
        name = name,
        quantityText = "$quantity $unit",
        expirationDate = expirationDate,
        status = status
    )
}