package com.example.freshtrack.presentation

import com.example.freshtrack.data.local.model.ItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class InventoryItemUi(
    val id: Int,
    val name: String,
    val quantityText: String,
    val expirationDate: Long,
    val formattedExpiryDate: String,
    val status: ItemStatus
)

private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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
        formattedExpiryDate = dateFormatter.format(Date(expirationDate)),
        status = status
    )
}