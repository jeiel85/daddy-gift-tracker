package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viewmodel.GyeongjosaViewModel
import com.example.ui.theme.*

@Composable
fun MainScreen(viewModel: GyeongjosaViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "tabs",
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. The persistent shell (contains 5 tabs)
        composable("tabs") {
            var selectedTab by remember { mutableStateOf(0) } // 0: Home, 1: Contacts, 2: Schedule, 3: Stats, 4: Settings

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = NavyDark,
                        contentColor = TextLight
                    ) {
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "홈", tint = TextLight) },
                            label = { Text("홈", color = TextLight) },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "인맥부", tint = TextLight) },
                            label = { Text("인맥부", color = TextLight) },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "일정", tint = TextLight) },
                            label = { Text("일정", color = TextLight) },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "통계", tint = TextLight) },
                            label = { Text("통계", color = TextLight) },
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 }
                        )
                        NavigationBarItem(
                            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "설정", tint = TextLight) },
                            label = { Text("설정", color = TextLight) },
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 }
                        )
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToPersonAdd = { navController.navigate("person_add") },
                            onNavigateToRecordAdd = { navController.navigate("record_add") },
                            onNavigateToPersonDetail = { pid -> navController.navigate("person_detail/$pid") },
                            onNavigateToReminders = { selectedTab = 2 }
                        )
                        1 -> ContactsScreen(
                            viewModel = viewModel,
                            onNavigateToPersonAdd = { navController.navigate("person_add") },
                            onNavigateToPersonDetail = { pid -> navController.navigate("person_detail/$pid") }
                        )
                        2 -> ScheduleRemindersScreen(
                            viewModel = viewModel,
                            onNavigateToPersonDetail = { pid -> navController.navigate("person_detail/$pid") }
                        )
                        3 -> StatisticsScreen(
                            viewModel = viewModel
                        )
                        4 -> SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // 2. Add Person Route
        composable("person_add") {
            PersonRegisterScreen(
                viewModel = viewModel,
                personId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Edit Person Route
        composable(
            route = "person_edit/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 0
            PersonRegisterScreen(
                viewModel = viewModel,
                personId = personId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Add Event Record Route
        composable("record_add") {
            EventRecordScreen(
                viewModel = viewModel,
                personId = null,
                recordId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Add Event Record specifc to Person Route
        composable(
            route = "record_add_for_person/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 0
            EventRecordScreen(
                viewModel = viewModel,
                personId = personId,
                recordId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Edit Event Record Route
        composable(
            route = "record_edit/{personId}/{recordId}",
            arguments = listOf(
                navArgument("personId") { type = NavType.IntType },
                navArgument("recordId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 0
            val recordId = backStackEntry.arguments?.getInt("recordId") ?: 0
            EventRecordScreen(
                viewModel = viewModel,
                personId = personId,
                recordId = recordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Person Detail Route
        composable(
            route = "person_detail/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 0
            PersonDetailScreen(
                viewModel = viewModel,
                personId = personId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditPerson = { pid -> navController.navigate("person_edit/$pid") },
                onNavigateToAddRecordForPerson = { pid -> navController.navigate("record_add_for_person/$pid") },
                onNavigateToEditRecord = { pid, rid -> navController.navigate("record_edit/$pid/$rid") }
            )
        }
    }
}
