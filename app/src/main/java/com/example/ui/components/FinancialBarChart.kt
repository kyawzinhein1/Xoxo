package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MonthlyBarData
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import kotlin.math.max

@Composable
fun FinancialBarChart(
    monthlyData: List<MonthlyBarData>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (monthlyData.isEmpty()) {
        return
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(monthlyData) {
        progress.animateTo(1f, animationSpec = tween(700))
    }

    val maxVal = remember(monthlyData) {
        monthlyData.maxOfOrNull { max(it.income, it.expense) }?.toFloat() ?: 100f
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income vs Expense Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Income", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expense", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val chartHeight = size.height - 30.dp.toPx()
                val groupWidth = size.width / monthlyData.size
                val barWidth = 14.dp.toPx()
                val spacing = 4.dp.toPx()

                monthlyData.forEachIndexed { index, data ->
                    val groupCenterX = groupWidth * index + groupWidth / 2f

                    val incomeHeight = if (maxVal > 0) ((data.income / maxVal) * chartHeight * progress.value).toFloat() else 0f
                    val expenseHeight = if (maxVal > 0) ((data.expense / maxVal) * chartHeight * progress.value).toFloat() else 0f

                    // Draw Income Bar
                    val incomeX = groupCenterX - barWidth - (spacing / 2f)
                    val incomeY = chartHeight - incomeHeight
                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(incomeX, incomeY),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Expense Bar
                    val expenseX = groupCenterX + (spacing / 2f)
                    val expenseY = chartHeight - expenseHeight
                    drawRoundRect(
                        color = ExpenseRed,
                        topLeft = Offset(expenseX, expenseY),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                monthlyData.forEach { data ->
                    Text(
                        text = data.monthName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
