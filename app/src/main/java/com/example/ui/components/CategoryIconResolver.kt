package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconResolver {
    val AVAILABLE_ICONS = listOf(
        "restaurant", "shopping_bag", "home", "directions_bus", "movie",
        "fitness_center", "school", "account_balance_wallet", "laptop",
        "trending_up", "card_giftcard", "attach_money", "local_cafe",
        "flight", "sports_esports", "medical_services", "build", "pets"
    )

    fun getIconVector(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "restaurant" -> Icons.Default.Restaurant
            "shopping_bag" -> Icons.Default.ShoppingBag
            "home" -> Icons.Default.Home
            "directions_bus" -> Icons.Default.DirectionsCar
            "movie" -> Icons.Default.Movie
            "fitness_center" -> Icons.Default.FitnessCenter
            "school" -> Icons.Default.School
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "laptop" -> Icons.Default.Laptop
            "trending_up" -> Icons.Default.TrendingUp
            "card_giftcard" -> Icons.Default.CardGiftcard
            "attach_money" -> Icons.Default.AttachMoney
            "local_cafe" -> Icons.Default.LocalCafe
            "flight" -> Icons.Default.Flight
            "sports_esports" -> Icons.Default.SportsEsports
            "medical_services" -> Icons.Default.MedicalServices
            "build" -> Icons.Default.Build
            "pets" -> Icons.Default.Pets
            else -> Icons.Default.MoreHoriz
        }
    }
}

@Composable
fun CategoryIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Icon(
        imageVector = CategoryIconResolver.getIconVector(iconName),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
