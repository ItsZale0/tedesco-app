package com.alessandro.tedesco.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.remote.BackupViewModel
import com.alessandro.tedesco.data.remote.UpdaterViewModel

/**
 * V2 - una sola schermata principale (Studio Veloce).
 * Impostazioni, progresso, tutor e backup vivono in un menu nascosto e sono
 * raggiungibili da qui: nessuna bottom bar, nessuna navigazione in vista.
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
        modifier = modifier
    ) {
        composable("studio") {
            StudioVeloceScreen(
                vm = vm,
                onVaiAImpostazioni = { nav.navigate("impostazioni") },
                onVaiAProgresso = { nav.navigate("progresso") },
                onVaiATutor = { nav.navigate("tutor") },
                onVaiABackup = { nav.navigate("backup") }
            )
        }
        composable("progresso") {
            ProgressoScreen(vm = vm, onIndietro = { nav.popBackStack() })
        }
        composable("tutor") {
            TutorChatScreen(vm = vm, onIndietro = { nav.popBackStack() })
        }
        composable("impostazioni") {
            ImpostazioniScreen(
                vm = vm,
                updater = viewModel<UpdaterViewModel>(factory = factory),
                onVaiABackup = { nav.navigate("backup") },
                onIndietro = { nav.popBackStack() }
            )
        }
        composable("backup") {
            BackupScreen(
                vm = viewModel<BackupViewModel>(factory = factory),
                onIndietro = { nav.popBackStack() }
            )
        }
    }
}
