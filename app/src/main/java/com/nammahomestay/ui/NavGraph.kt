package com.nammahomestay.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nammahomestay.ui.screens.*
import com.nammahomestay.viewmodel.AuthViewModel
import com.nammahomestay.viewmodel.HomeStayViewModel

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    homeStayViewModel: HomeStayViewModel
) {
    val navController = rememberNavController()

    // Determine start destination based on login status
    val startDestination = if (authViewModel.isLoggedIn) {
        // Already logged in — init data listeners and go straight to main
        authViewModel.currentUid?.let { homeStayViewModel.init(it) }
        "main"
    } else {
        "splash"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeStayViewModel = homeStayViewModel
            )
        }

        composable("register") {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeStayViewModel = homeStayViewModel
            )
        }

        composable("main") {
            MainAppScreen(
                navController = navController,
                authViewModel = authViewModel,
                homeStayViewModel = homeStayViewModel
            )
        }

        composable(
            route = "add_menu?itemId={itemId}",
            arguments = listOf(navArgument("itemId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            AddMenuScreen(
                navController = navController,
                homeStayViewModel = homeStayViewModel,
                uid = authViewModel.currentUid ?: "",
                itemId = backStackEntry.arguments?.getString("itemId")
            )
        }

        composable("local_guide") {
            LocalGuideScreen(
                navController = navController,
                homeStayViewModel = homeStayViewModel,
                uid = authViewModel.currentUid ?: ""
            )
        }

        composable(
            route = "add_place?placeId={placeId}",
            arguments = listOf(navArgument("placeId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            AddPlaceScreen(
                navController = navController,
                homeStayViewModel = homeStayViewModel,
                uid = authViewModel.currentUid ?: "",
                placeId = backStackEntry.arguments?.getString("placeId")
            )
        }

        composable("edit_profile") {
            EditProfileScreen(
                navController = navController,
                homeStayViewModel = homeStayViewModel,
                uid = authViewModel.currentUid ?: ""
            )
        }

        composable("availability") {
            AvailabilityScreen(
                navController = navController,
                homeStayViewModel = homeStayViewModel,
                uid = authViewModel.currentUid ?: ""
            )
        }

        // ── Rooms ──────────────────────────────────────────────────────
        composable("rooms") {
            RoomsScreen(
                navController = navController,
                homeStayViewModel = homeStayViewModel,
                uid = authViewModel.currentUid ?: ""
            )
        }

        composable("notifications") {
            NotificationPrefsScreen(navController)
        }

        composable("delete_account") {
            DeleteAccountScreen(
                navController = navController,
                onConfirmDelete = {
                    authViewModel.deleteAccountAndLogout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(
            route = "add_room?roomId={roomId}",
            arguments = listOf(navArgument("roomId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            AddRoomScreen(
                navController = navController,
                homeStayViewModel = homeStayViewModel,
                uid = authViewModel.currentUid ?: "",
                roomId = backStackEntry.arguments?.getString("roomId")
            )
        }
    }
}
