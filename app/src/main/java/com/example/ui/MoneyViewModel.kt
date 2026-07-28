package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.CategoryEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.CategoryExpenseSummary
import com.example.data.model.MonthlyBarData
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithCategory
import com.example.repository.MoneyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MoneyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = MoneyRepository(database.categoryDao(), database.transactionDao())

    // Active Year and Month filter
    private val calendar = Calendar.getInstance()
    val selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH)) // 0-based

    // User preferences
    val currencySymbol = MutableStateFlow("$")
    val darkThemeOverride = MutableStateFlow<Boolean?>(null) // null = system, true = dark, false = light

    // Navigation state
    val selectedTab = MutableStateFlow(0) // 0: Home, 1: Analytics, 2: Categories, 3: Settings

    // Search and filter
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow<TransactionType?>(null)

    // Add/Edit transaction sheet state
    val isAddEditSheetOpen = MutableStateFlow(false)
    val editingTransaction = MutableStateFlow<TransactionWithCategory?>(null)

    init {
        viewModelScope.launch {
            repository.checkAndSeedDefaults()
        }
    }

    // Date Range calculation for active month
    private val activeMonthRange = combine(selectedYear, selectedMonth) { year, month ->
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endMillis = cal.timeInMillis

        Pair(startMillis, endMillis)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthTransactions: StateFlow<List<TransactionWithCategory>> = activeMonthRange
        .flatMapLatest { range ->
            repository.getTransactionsByRange(range.first, range.second)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<TransactionWithCategory>> = combine(
        monthTransactions,
        searchQuery,
        filterType
    ) { transactions, query, typeFilter ->
        transactions.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.categoryName.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true)
            val matchesType = typeFilter == null || tx.type == typeFilter
            matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = monthTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = monthTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expenseBreakdown: StateFlow<List<CategoryExpenseSummary>> = combine(monthTransactions, totalExpense) { list, totalExp ->
        val expenses = list.filter { it.type == TransactionType.EXPENSE }
        if (totalExp <= 0.0) return@combine emptyList()

        expenses.groupBy { it.categoryId }
            .map { (catId, txList) ->
                val first = txList.first()
                val sum = txList.sumOf { it.amount }
                val pct = ((sum / totalExp) * 100).toFloat()
                CategoryExpenseSummary(
                    categoryId = catId,
                    categoryName = first.categoryName,
                    iconName = first.iconName,
                    colorHex = first.colorHex,
                    totalAmount = sum,
                    percentage = pct
                )
            }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyComparison: StateFlow<List<MonthlyBarData>> = repository.allTransactions.map { allTx ->
        val result = mutableListOf<MonthlyBarData>()
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        for (i in 3 downTo 0) {
            val targetCal = Calendar.getInstance()
            targetCal.add(Calendar.MONTH, -i)
            val year = targetCal.get(Calendar.YEAR)
            val month = targetCal.get(Calendar.MONTH)

            val monthLabel = monthFormat.format(targetCal.time)

            val inc = allTx.filter {
                cal.timeInMillis = it.dateMillis
                cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month && it.type == TransactionType.INCOME
            }.sumOf { it.amount }

            val exp = allTx.filter {
                cal.timeInMillis = it.dateMillis
                cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month && it.type == TransactionType.EXPENSE
            }.sumOf { it.amount }

            result.add(MonthlyBarData(monthLabel, inc, exp))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month Navigation
    fun previousMonth() {
        if (selectedMonth.value == 0) {
            selectedMonth.value = 11
            selectedYear.value -= 1
        } else {
            selectedMonth.value -= 1
        }
    }

    fun nextMonth() {
        if (selectedMonth.value == 11) {
            selectedMonth.value = 0
            selectedYear.value += 1
        } else {
            selectedMonth.value += 1
        }
    }

    fun getSelectedMonthLabel(): String {
        val cal = Calendar.getInstance()
        cal.set(selectedYear.value, selectedMonth.value, 1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(cal.time)
    }

    // Actions
    fun openAddTransactionSheet() {
        editingTransaction.value = null
        isAddEditSheetOpen.value = true
    }

    fun openEditTransactionSheet(transaction: TransactionWithCategory) {
        editingTransaction.value = transaction
        isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        isAddEditSheetOpen.value = false
        editingTransaction.value = null
    }

    fun saveTransaction(
        id: Long,
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        dateMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                title = title.ifBlank { "Transaction" },
                amount = amount,
                type = type,
                categoryId = categoryId,
                dateMillis = dateMillis,
                note = note
            )
            if (id == 0L) {
                repository.insertTransaction(entity)
            } else {
                repository.updateTransaction(entity)
            }
            closeAddEditSheet()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            closeAddEditSheet()
        }
    }

    fun addCategory(name: String, type: TransactionType, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val category = CategoryEntity(
                name = name,
                type = type,
                iconName = iconName,
                colorHex = colorHex,
                isDefault = false
            )
            repository.insertCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
