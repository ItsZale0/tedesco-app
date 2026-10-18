package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * V2 — Solo una schermata: Studio Veloce.
 * Le impostazioni sono accessibili da un menu nascosto.
 */
@Composable
fun AppNav(
    vm: TedescoViewModel,
    modifier: Modifier = Modifier
) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "studio",
        modifier = modifier.padding(bottom = 8.dp)
    ) {
        composable("studio") {
            StudioVeloceScreen(vm = vm)
        }
    }
}
