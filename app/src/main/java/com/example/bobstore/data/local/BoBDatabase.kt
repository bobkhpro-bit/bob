package com.example.bobstore.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WishlistItemEntity::class], version = 1, exportSchema = false)
abstract class BoBDatabase : RoomDatabase() {

    abstract fun wishlistDao(): WishlistDao

    companion object {
        @Volatile
        private var INSTANCE: BoBDatabase? = null

        fun getDatabase(context: Context): BoBDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BoBDatabase::class.java,
                    "bob_store_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
