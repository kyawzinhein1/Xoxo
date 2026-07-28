package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TransactionEntity
import com.example.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.id, t.title, t.amount, t.type, t.categoryId, 
               c.name AS categoryName, c.iconName AS iconName, c.colorHex AS colorHex, 
               t.dateMillis, t.note
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        ORDER BY t.dateMillis DESC
    """)
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>

    @Query("""
        SELECT t.id, t.title, t.amount, t.type, t.categoryId, 
               c.name AS categoryName, c.iconName AS iconName, c.colorHex AS colorHex, 
               t.dateMillis, t.note
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.dateMillis >= :startMillis AND t.dateMillis <= :endMillis
        ORDER BY t.dateMillis DESC
    """)
    fun getTransactionsByRange(startMillis: Long, endMillis: Long): Flow<List<TransactionWithCategory>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND dateMillis >= :startMillis AND dateMillis <= :endMillis")
    fun getTotalIncome(startMillis: Long, endMillis: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND dateMillis >= :startMillis AND dateMillis <= :endMillis")
    fun getTotalExpense(startMillis: Long, endMillis: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}
