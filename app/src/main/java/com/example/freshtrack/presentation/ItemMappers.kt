package com.example.freshtrack.presentation

import com.example.freshtrack.data.local.model.ItemWithCategory
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
    val status: ItemStatus,
    val categoryName: String?
)

private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

fun ItemWithCategory.toUi(nowMillis: Long = System.currentTimeMillis()): InventoryItemUi {
    val i = item
    val twoDaysMillis = TimeUnit.DAYS.toMillis(2)
    val status = when {
        i.isConsumed -> ItemStatus.CONSUMED
        i.expirationDate < nowMillis -> ItemStatus.EXPIRED
        i.expirationDate - nowMillis <= twoDaysMillis -> ItemStatus.EXPIRING_SOON
        else -> ItemStatus.FRESH
    }

    return InventoryItemUi(
        id = i.id,
        name = i.name,
        quantityText = "${i.quantity} ${i.unit}",
        expirationDate = i.expirationDate,
        formattedExpiryDate = dateFormatter.format(Date(i.expirationDate)),
        status = status,
        categoryName = category?.name
    )
}