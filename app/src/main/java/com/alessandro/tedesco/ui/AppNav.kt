package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.ui.StudioVeloceScreen
import com.alessandro.tedesco.ui.TedescoViewModel

/**
 * V2 — Solo una schermata: Studio Veloce.
 * Le impostazioni sono accessibili da un menu nascosto.
 */
@Composable
fun AppNav(
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as TedescoApp
    val factory = remember { ViewModelProvider.AndroidViewModelFactory(app) }
    val vm = viewModel<TedescoViewModel>(factory = factory)

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
