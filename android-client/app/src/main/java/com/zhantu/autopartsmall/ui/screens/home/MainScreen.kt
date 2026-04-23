package com.zhantu.autopartsmall.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.zhantu.autopartsmall.ui.screens.cart.CartScreen
import com.zhantu.autopartsmall.ui.screens.profile.ProfileScreen

sealed class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : MainTab("home", "首页", Icons.Default.Home)
    object Category : MainTab("category", "分类", Icons.Default.List)
    object Cart : MainTab("cart", "购物车", Icons.Default.ShoppingCart)
    object Profile : MainTab("profile", "我的", Icons.Default.Person)
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(MainTab.Home, MainTab.Category, MainTab.Cart, MainTab.Profile).forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                MainTab.Home -> HomeScreen()
                MainTab.Category -> CategoryScreen()
                MainTab.Cart -> CartScreen()
                MainTab.Profile -> ProfileScreen()
            }
        }
    }
}
