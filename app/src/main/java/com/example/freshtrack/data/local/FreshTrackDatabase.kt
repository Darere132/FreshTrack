package com.example.freshtrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freshtrack.data.local.dao.CategoryDao
import com.example.freshtrack.data.local.dao.ItemDao
import com.example.freshtrack.data.local.model.CategoryEntity
import com.example.freshtrack.data.local.model.ItemEntity

@Database(
    entities = [ItemEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = true
)
abstract class FreshTrackDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var Instance: FreshTrackDatabase? = null

        fun getDatabase(context: Context): FreshTrackDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FreshTrackDatabase::class.java,
                    "freshtrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            listOf(
                                "Dairy", "Meat", "Vegetables", "Fruit",
                                "Bakery", "Drinks", "Frozen", "Other"
                            ).forEach { name ->
                                db.execSQL("INSERT INTO categories (name) VALUES ('$name')")
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}