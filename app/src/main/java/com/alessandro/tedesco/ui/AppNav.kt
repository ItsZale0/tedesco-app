package com.alessandro.tedesco.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alessandro.tedesco.TedescoApp

private sealed class Dest(val route: String, val label: String, val icona: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home : Dest("home", "Ripasso", Icons.Filled.Home)
    data object Nuove : Dest("nuove", "Parole", Icons.Filled.List)
    data object Stats : Dest("stats", "Statistiche", Icons.Filled.BarChart)
    data object Impostazioni : Dest("impostazioni", "Impostazioni", Icons.Filled.Settings)
}

private class TedescoViewModelFactory(private val app: TedescoApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TedescoViewModel(app) as T
    }
}

@Composable
fun AppNav() {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as TedescoApp
    val factory = remember { TedescoViewModelFactory(application) }
    val vm = viewModel<TedescoViewModel>(factory = factory)

    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val messaggio by vm.messaggio.collectAsStateWithLifecycle("")

    LaunchedEffect(messaggio) {
        messaggio?.let {
            snackbar.showSnackbar(it)
            vm.pulisciMessaggio()
        }
    }

    val backStack by nav.currentBackStackEntryAsState()
    val schermataCorrente = backStack?.destination?.route

    val mostraBarra = schermataCorrente?.startsWith("ripasso") != true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (mostraBarra) {
                NavigationBar {
                    listOf(Dest.Home, Dest.Nuove, Dest.Stats, Dest.Impostazioni)
                        .forEach { d ->
                            NavigationBarItem(
                                selected = schermataCorrente == d.route,
                                onClick = {
                                    nav.navigate(d.route) {
                                        popUpTo(Dest.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(d.icona, contentDescription = d.label) },
                                label = { Text(d.label) }
                            )
                        }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(Dest.Home.route) {
                HomeScreen(vm, onIniziaRipasso = { nav.navigate("ripasso") })
            }
            composable(Dest.Nuove.route) { NuoveParoleScreen(vm) }
            composable(Dest.Stats.route) { StatsScreen(vm) }
            composable(Dest.Impostazioni.route) { ImpostazioniScreen(vm) }
            composable("ripasso") { RipassoScreen(vm, onIndietro = { nav.popBackStack() }) }
        }
    }
}