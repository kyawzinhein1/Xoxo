package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.CategoryEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_manager_db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDefaultCategories(database.categoryDao())
                    }
                }
            }

            suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
                val defaults = listOf(
                    // Expense Categories
                    CategoryEntity(name = "Food & Dining", type = TransactionType.EXPENSE, iconName = "restaurant", colorHex = "#FF5722", isDefault = true),
                    CategoryEntity(name = "Shopping", type = TransactionType.EXPENSE, iconName = "shopping_bag", colorHex = "#E91E63", isDefault = true),
                    CategoryEntity(name = "Housing & Bills", type = TransactionType.EXPENSE, iconName = "home", colorHex = "#3F51B5", isDefault = true),
                    CategoryEntity(name = "Transportation", type = TransactionType.EXPENSE, iconName = "directions_bus", colorHex = "#00BCD4", isDefault = true),
                    CategoryEntity(name = "Entertainment", type = TransactionType.EXPENSE, iconName = "movie", colorHex = "#9C27B0", isDefault = true),
                    CategoryEntity(name = "Health & Fitness", type = TransactionType.EXPENSE, iconName = "fitness_center", colorHex = "#4CAF50", isDefault = true),
                    CategoryEntity(name = "Education", type = TransactionType.EXPENSE, iconName = "school", colorHex = "#FF9800", isDefault = true),
                    CategoryEntity(name = "Other Expenses", type = TransactionType.EXPENSE, iconName = "more_horiz", colorHex = "#607D8B", isDefault = true),

                    // Income Categories
                    CategoryEntity(name = "Salary", type = TransactionType.INCOME, iconName = "account_balance_wallet", colorHex = "#2E7D32", isDefault = true),
                    CategoryEntity(name = "Freelance", type = TransactionType.INCOME, iconName = "laptop", colorHex = "#0288D1", isDefault = true),
                    CategoryEntity(name = "Investments", type = TransactionType.INCOME, iconName = "trending_up", colorHex = "#7B1FA2", isDefault = true),
                    CategoryEntity(name = "Gifts & Grants", type = TransactionType.INCOME, iconName = "card_giftcard", colorHex = "#C2185B", isDefault = true),
                    CategoryEntity(name = "Other Income", type = TransactionType.INCOME, iconName = "attach_money", colorHex = "#F57C00", isDefault = true)
                )
                categoryDao.insertCategories(defaults)
            }
        }
    }
}
