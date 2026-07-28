package com.example.data.model

data class TransactionWithCategory(
    val id: Long,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val colorHex: String,
    val dateMillis: Long,
    val note: String
)

data class CategoryExpenseSummary(
    val categoryId: Long,
    val categoryName: String,
    val iconName: String,
    val colorHex: String,
    val totalAmount: Double,
    val percentage: Float
)

data class MonthlyBarData(
    val monthName: String,
    val income: Double,
    val expense: Double
)
