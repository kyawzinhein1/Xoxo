package com.example.repository

import com.example.data.dao.CategoryDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.CategoryEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

class MoneyRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<TransactionWithCategory>> = transactionDao.getAllTransactionsWithCategory()

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    fun getTransactionsByRange(startMillis: Long, endMillis: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsByRange(startMillis, endMillis)

    fun getTotalIncome(startMillis: Long, endMillis: Long): Flow<Double?> =
        transactionDao.getTotalIncome(startMillis, endMillis)

    fun getTotalExpense(startMillis: Long, endMillis: Long): Flow<Double?> =
        transactionDao.getTotalExpense(startMillis, endMillis)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    suspend fun checkAndSeedDefaults() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaults = listOf(
                CategoryEntity(name = "Food & Dining", type = TransactionType.EXPENSE, iconName = "restaurant", colorHex = "#FF5722", isDefault = true),
                CategoryEntity(name = "Shopping", type = TransactionType.EXPENSE, iconName = "shopping_bag", colorHex = "#E91E63", isDefault = true),
                CategoryEntity(name = "Housing & Bills", type = TransactionType.EXPENSE, iconName = "home", colorHex = "#3F51B5", isDefault = true),
                CategoryEntity(name = "Transportation", type = TransactionType.EXPENSE, iconName = "directions_bus", colorHex = "#00BCD4", isDefault = true),
                CategoryEntity(name = "Entertainment", type = TransactionType.EXPENSE, iconName = "movie", colorHex = "#9C27B0", isDefault = true),
                CategoryEntity(name = "Health & Fitness", type = TransactionType.EXPENSE, iconName = "fitness_center", colorHex = "#4CAF50", isDefault = true),
                CategoryEntity(name = "Education", type = TransactionType.EXPENSE, iconName = "school", colorHex = "#FF9800", isDefault = true),
                CategoryEntity(name = "Other Expenses", type = TransactionType.EXPENSE, iconName = "more_horiz", colorHex = "#607D8B", isDefault = true),

                CategoryEntity(name = "Salary", type = TransactionType.INCOME, iconName = "account_balance_wallet", colorHex = "#2E7D32", isDefault = true),
                CategoryEntity(name = "Freelance", type = TransactionType.INCOME, iconName = "laptop", colorHex = "#0288D1", isDefault = true),
                CategoryEntity(name = "Investments", type = TransactionType.INCOME, iconName = "trending_up", colorHex = "#7B1FA2", isDefault = true),
                CategoryEntity(name = "Gifts & Grants", type = TransactionType.INCOME, iconName = "card_giftcard", colorHex = "#C2185B", isDefault = true),
                CategoryEntity(name = "Other Income", type = TransactionType.INCOME, iconName = "attach_money", colorHex = "#F57C00", isDefault = true)
            )
            categoryDao.insertCategories(defaults)
        }
    }

    suspend fun seedSampleData() {
        checkAndSeedDefaults()
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L

        // Get category IDs
        val categories = categoryDao.getCategoryCount()
        if (categories > 0) {
            val sampleTransactions = listOf(
                TransactionEntity(title = "Monthly Salary", amount = 3500.00, type = TransactionType.INCOME, categoryId = 9, dateMillis = now - dayMillis * 2, note = "Tech Corp Payroll"),
                TransactionEntity(title = "Grocery Market", amount = 124.50, type = TransactionType.EXPENSE, categoryId = 1, dateMillis = now - dayMillis * 1, note = "Fresh vegetables & dairy"),
                TransactionEntity(title = "Electricity & Water", amount = 85.20, type = TransactionType.EXPENSE, categoryId = 3, dateMillis = now - dayMillis * 3, note = "Utility bill"),
                TransactionEntity(title = "Freelance UI Design", amount = 650.00, type = TransactionType.INCOME, categoryId = 10, dateMillis = now - dayMillis * 4, note = "App client project"),
                TransactionEntity(title = "Dinner with Friends", amount = 68.00, type = TransactionType.EXPENSE, categoryId = 1, dateMillis = now - dayMillis * 5, note = "Italian bistro"),
                TransactionEntity(title = "Subway Pass", amount = 45.00, type = TransactionType.EXPENSE, categoryId = 4, dateMillis = now - dayMillis * 6, note = "Monthly transit card"),
                TransactionEntity(title = "New Headphones", amount = 149.99, type = TransactionType.EXPENSE, categoryId = 2, dateMillis = now - dayMillis * 7, note = "Noise cancelling headphones")
            )
            for (tx in sampleTransactions) {
                transactionDao.insertTransaction(tx)
            }
        }
    }

    suspend fun clearAll() {
        transactionDao.clearAllTransactions()
    }
}
