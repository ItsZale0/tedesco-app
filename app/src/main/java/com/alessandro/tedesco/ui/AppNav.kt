package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.remote.UpdateState
import com.alessandro.tedesco.data.remote.UpdaterViewModel

private sealed class Dest(
    val route: String,
    val label: String,
    val icona: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home : Dest("home", "Ripasso", Icons.Filled.Home)
    data object Nuove : Dest("nuove", "Parole", Icons.Filled.Translate)
    data object Guida : Dest("guida", "Guida", Icons.AutoMirrored.Filled.MenuBook)
    data object Stats : Dest("stats", "Statistiche", Icons.Filled.BarChart)
    data object Impostazioni : Dest("impostazioni", "Impostazioni", Icons.Filled.Settings)
}

private class TedescoViewModelFactory(private val app: TedescoApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TedescoViewModel(app) as T
    }
}

private class UpdaterViewModelFactory(private val app: TedescoApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return com.alessandro.tedesco.data.remote.UpdaterViewModel(app) as T
    }
}

@Composable
fun AppNav() {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as TedescoApp
    val factory = remember { TedescoViewModelFactory(application) }
    val vm = viewModel<TedescoViewModel>(factory = factory)

    val updaterFactory = remember { UpdaterViewModelFactory(application) }
    val updater = viewModel<UpdaterViewModel>(factory = updaterFactory)

    // check aggiornamenti a ogni apertura dell'app (una volta per processo)
    LaunchedEffect(Unit) {
        vm.controllaAggiornamentiAllAvvio()
        updater.checkForUpdate()
    }

    val updateState by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)

    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val messaggio by vm.messaggio.collectAsStateWithLifecycle(null)

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
                    listOf(Dest.Home, Dest.Nuove, Dest.Guida, Dest.Stats, Dest.Impostazioni)
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
            composable(Dest.Guida.route) { GuidaScreen(vm) }
            composable(Dest.Stats.route) { StatsScreen(vm) }
            composable(Dest.Impostazioni.route) { ImpostazioniScreen(vm) }
            composable("ripasso") { RipassoScreen(vm, onIndietro = { nav.popBackStack() }) }
        }
    }

    // Dialog di aggiornamento
    when (val s = updateState) {
        is UpdateState.Available -> {
            AlertDialog(
                onDismissRequest = { updater.dismiss() },
                title = { Text("Aggiornamento disponibile") },
                text = {
                    Column {
                        Text("Versione ${s.version.versionName} disponibile")
                        if (s.version.changelog.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                s.version.changelog,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { updater.downloadUpdate(s.version) }) {
                        Text("Scarica e installa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { updater.dismiss() }) {
                        Text("Dopo")
                    }
                }
            )
        }
        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Download in corso") },
                text = {
                    Column {
                        LinearProgressIndicator(
                            progress = { s.progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("${s.progress}%")
                    }
                },
                confirmButton = {}
            )
        }
        is UpdateState.ReadyToInstall -> {
            AlertDialog(
                onDismissRequest = { updater.dismiss() },
                title = { Text("Aggiornamento pronto") },
                text = { Text("L'APK è stato scaricato. Tocca 'Installa' per aggiornare l'app.") },
                confirmButton = {
                    Button(onClick = { updater.installUpdate(s.file) }) {
                        Text("Installa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { updater.dismiss() }) {
                        Text("Annulla")
                    }
                }
            )
        }
        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = { updater.dismiss() },
                title = { Text("Errore") },
                text = { Text(s.message) },
                confirmButton = {
                    TextButton(onClick = { updater.dismiss() }) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }
}
