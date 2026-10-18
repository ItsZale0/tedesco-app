package com.alessandro.tedesco.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.ui.StudioVeloceScreen
import com.alessandro.tedesco.ui.TedescoViewModel
import com.alessandro.tedesco.ui.TedescoViewModelFactory

/**
 * V2 — Solo una schermata: Studio Veloce.
 * Le impostazioni sono accessibili da un menu nascosto.
 */
@Composable
fun AppNav(
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as TedescoApp
    val vmFactory = remember { TedescoViewModelFactory(application) }
    val vm = viewModel<TedescoViewModel>(factory = vmFactory)

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
