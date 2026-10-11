package com.example.bobstore.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Query("SELECT productId FROM wishlist_items ORDER BY addedAt DESC")
    fun getAllWishlistProductIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId)")
    suspend fun isWishlisted(productId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun deleteWishlistItem(productId: String)

    @Query("DELETE FROM wishlist_items")
    suspend fun clearAll()
}
