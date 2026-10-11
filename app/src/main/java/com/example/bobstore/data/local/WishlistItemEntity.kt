package com.example.bobstore.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)
