package com.alessandro.tedesco.ui

import com.alessandro.tedesco.ui.ProgressioneScreen
import com.alessandro.tedesco.ui.ProduzioneScrittaScreen

import com.alessandro.tedesco.ui.ContestoMedicoScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.alessandro.tedesco.ui.FeedbackScreen
import com.alessandro.tedesco.ui.PianoScreen
import com.alessandro.tedesco.ui.RoleplayScreen
import com.alessandro.tedesco.ui.SessioniScreen
import com.alessandro.tedesco.ui.TestAdattiviScreen

private sealed class Dest(
    val route: String,
    val label: String,
    val icona: androidx.compose.ui.graphics.vector.ImageVector,
    val descrizione: String? = null
) {
    data object Home : Dest("home", "Oggi", Icons.Filled.Home, "Dashboard e attività del giorno")
    data object Nuove : Dest("nuove", "Parole", Icons.Filled.Translate, "Vocabolario e nuove parole")
    data object Guida : Dest("guida", "Guida", Icons.AutoMirrored.Filled.MenuBook, "Guida e lezione del giorno")
    data object Impostazioni : Dest("impostazioni", "Profilo", Icons.Filled.Settings, "Profilo e impostazioni")
    data object Altro : Dest("altro", "Altro", Icons.Filled.Star, "Grammatica, test e statistiche")
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
        return UpdaterViewModel(app) as T
    }
}

@Composable
fun AppNav() {
    val application = LocalContext.current.applicationContext as TedescoApp
    val factory = remember { TedescoViewModelFactory(application) }
    val vm = viewModel<TedescoViewModel>(factory = factory)

    val updaterFactory = remember { UpdaterViewModelFactory(application) }
    val updater = viewModel<UpdaterViewModel>(factory = updaterFactory)

    val pronto by vm.pronto.collectAsStateWithLifecycle(false)
    val profiloAttivo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val profili by vm.profiliDisponibili.collectAsStateWithLifecycle(emptyList())
    val updateState by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)
    val messaggio by vm.messaggio.collectAsStateWithLifecycle(null)

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(messaggio) {
        messaggio?.let {
            snackbar.showSnackbar(it)
            vm.pulisciMessaggio()
        }
    }

    // Controllo aggiornamenti appena si entra nel corso
    LaunchedEffect(profiloAttivo?.id) {
        if (profiloAttivo != null) {
            vm.controllaAggiornamentiAllAvvio()
            updater.checkForUpdate()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            // 1) Caricamento iniziale dei profili
            !pronto -> SchermataCaricamento()

            // 2) Nessun profilo scelto: si sceglie prima di entrare
            profiloAttivo == null -> Box(Modifier.fillMaxSize()) {
                ProfileSelectionScreen(
                    profili = profili,
                    onSeleziona = { vm.selezionaProfilo(it) },
                    onRiprova = { vm.ricaricaProfili() },
                    onCreaProfilo = { vm.creaProfilo(it) }
                )
                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // 3) Dentro il corso, col profilo scelto
            else -> ContenutoApp(vm = vm, updater = updater, snackbar = snackbar)
        }
    }

    AggiornamentoDialogs(updateState = updateState, updater = updater, snackbar = snackbar)
}

@Composable
private fun SchermataCaricamento() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                "Caricamento…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContenutoApp(
    vm: TedescoViewModel,
    updater: UpdaterViewModel,
    snackbar: SnackbarHostState
) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val schermataCorrente = backStack?.destination?.route
    val mostraBarra = schermataCorrente?.startsWith("ripasso") != true

    // Su schermi compatti accorcia "Statistiche" in "Progressi" per evitare troncamenti
    val configuration = LocalConfiguration.current
    val labelStats = if (configuration.screenWidthDp < 360) "Progressi" else "Statistiche"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (mostraBarra) {
                NavigationBar {
                    listOf(Dest.Home, Dest.Nuove, Dest.Guida, Dest.Impostazioni, Dest.Altro)
                        .forEach { d ->
                            val label = d.label
                            NavigationBarItem(
                                selected = schermataCorrente == d.route,
                                onClick = {
                                    nav.navigate(d.route) {
                                        popUpTo(Dest.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(d.icona, contentDescription = d.descrizione ?: d.label) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
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
                HomeScreen(
                    vm,
                    onIniziaRipasso = { nav.navigate("ripasso") },
                    onNavigate = { route -> nav.navigate(route) },
                    onCompletaPasso = { passoId ->
                        vm.completaPassoGiornaliero(passoId)
                        // Naviga alla schermata del passo completato
                        when (passoId) {
                            "ripasso" -> nav.navigate("ripasso")
                            "grammatica" -> nav.navigate("grammatica")
                            "comprensione" -> nav.navigate("quiz-comprensione")
                            "produzione" -> nav.navigate("quiz-produzione")
                            "vocabolario" -> nav.navigate("nuove")
                            "test-livello" -> nav.navigate("testb1")
                            "sessione" -> nav.navigate("sessioni")
                        }
                    }
                )
            }
            composable(Dest.Nuove.route) { NuoveParoleScreen(vm) }
            composable(Dest.Guida.route) { GuidaScreen(vm) }
            composable(Dest.Impostazioni.route) { ImpostazioniScreen(vm, updater) }
            composable(Dest.Altro.route) {
                AltroScreen(
                    onVaiAGrammatica = { nav.navigate("grammatica") },
                    onVaiATest = { nav.navigate("test-adattivi") },
                    onVaiATestB1 = { nav.navigate("testb1") },
                    onVaiAStats = { nav.navigate("stats") },
                    onVaiATraduttore = { nav.navigate("traduttore") },
                    onVaiATutor = { nav.navigate("tutor") },
                    onVaiAFeedback = { nav.navigate("feedback") },
                    onVaiACompetenze = { nav.navigate("competenze") },
                    onVaiAQuizComprensione = { nav.navigate("quiz-comprensione") },
                    onVaiAQuizProduzione = { nav.navigate("quiz-produzione") },
                    onVaiAPiano = { nav.navigate("piano") },
                    onVaiASessioni = { nav.navigate("sessioni") },
                    onVaiARoleplay = { nav.navigate("roleplay") },
                    onVaiAVoiceChat = { nav.navigate("voicechat") },
                    onVaiAAscolto = { nav.navigate("ascolto") },
                    onVaiAContestoMedico = { nav.navigate("contestoMedico") },
                    onVaiAProgressione = { nav.navigate("progressione") },
                    onVaiAProduzioneScritta = { nav.navigate("produzioneScritta") }
                )
            }
            // Piano di studio
            composable("piano") {
                val stats by vm.statistiche.collectAsStateWithLifecycle(null)
                PianoScreen(
                    onIndietro = { nav.popBackStack() },
                    piano = vm.piano.collectAsStateWithLifecycle().value,
                    progresso = vm.progressoFeed.collectAsStateWithLifecycle().value,
                    lezioneCorrente = vm.lezioneCorrente.collectAsStateWithLifecycle().value,
                    paroleApprese = stats?.paroleApprese ?: 0,
                    livelloCorrente = stats?.livelloStimato?.label?.substringBefore(" ") ?: "A0"
                )
            }
            // Sessioni strutturate
            composable("sessioni") {
                SessioniScreen(
                    onIndietro = { nav.popBackStack() },
                    sessioni = vm.sessioni.collectAsStateWithLifecycle().value,
                    sessioniAdattive = vm.sessioniAdattive.collectAsStateWithLifecycle().value,
                    onAvvia = { tipo ->
                        // Ogni sessione porta davvero da qualche parte
                        when (tipo.uppercase()) {
                            "SESSIONE", "SESSIONE_GUIDATA", "RIPASSO" -> nav.navigate("ripasso")
                            "TEST", "TEST_LIVELLO" -> nav.navigate("testb1")
                            "ROLEPLAY" -> nav.navigate("roleplay")
                            "GRAMMATICA" -> nav.navigate("grammatica")
                            "COMPRENSIONE" -> nav.navigate("quiz-comprensione")
                            "PRODUZIONE" -> nav.navigate("quiz-produzione")
                            "VOCABOLARIO" -> nav.navigate("nuove")
                            else -> nav.navigate("ripasso")
                        }
                    }
                )
            }
            // Feedback / Correzione risposte
            composable("feedback") {
                FeedbackScreen(
                    onIndietro = { nav.popBackStack() },
                    onInviaRisposta = { vm.inviaRisposta(it) },
                    risposte = vm.risposte.collectAsStateWithLifecycle().value,
                    correzioneInCorso = vm.correzioneInCorso.collectAsStateWithLifecycle().value,
                    apiKey = vm.tutorApiKey.collectAsStateWithLifecycle("").value
                )
            }
            composable("grammatica") { GrammaticaScreen(vm) }
            composable("testb1") { TestB1Screen(vm) }
            composable("test-adattivi") { TestAdattiviScreen(vm) }
            composable("stats") { StatsScreen(vm) }
            composable("traduttore") { TraduttoreScreen(vm) }
            composable("tutor") { TutorChatScreen(vm) }
            composable("roleplay") { RoleplayScreen(vm) }
            composable("voicechat") { VoiceChatScreen(vm, onIndietro = { nav.popBackStack() }) }
            composable("ripasso") { RipassoScreen(vm, onIndietro = { nav.popBackStack() }) }
            composable("competenze") { CompetenzeScreen(vm) }
            composable("quiz-comprensione") { ComprensioneScreen(vm) }
            composable("quiz-produzione") { ProduzioneScreen(vm) }
            composable("ascolto") { AscoltoScreen(vm) }
            composable("contestoMedico") { ContestoMedicoScreen(vm, onIndietro = { nav.popBackStack() }) }
            composable("progressione") { ProgressioneScreen(vm, onIndietro = { nav.popBackStack() }) }
            composable("produzioneScritta") { ProduzioneScrittaScreen(vm, onIndietro = { nav.popBackStack() }) }
        }
    }
}

@Composable
private fun AggiornamentoDialogs(
    updateState: UpdateState,
    updater: UpdaterViewModel,
    snackbar: SnackbarHostState
) {
    when (updateState) {
        is UpdateState.Available -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Aggiornamento disponibile") },
            text = {
                Column {
                    Text("Versione ${updateState.version.versionName} disponibile")
                    if (updateState.version.changelog.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(updateState.version.changelog, style = MaterialTheme.typography.bodySmall)
                    }
                    if (updater.apkGiaScaricato(updateState.version)) {
                        Spacer(Modifier.height(8.dp))
                        Text("✓ APK già scaricato, puoi installare subito.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                if (updater.apkGiaScaricato(updateState.version)) {
                    val dir = java.io.File(androidx.compose.ui.platform.LocalContext.current.cacheDir, "updates")
                    val apk = java.io.File(dir, "Tedesco-v${updateState.version.versionName}.apk")
                    Button(onClick = { updater.installUpdate(apk) }) {
                        Text("Installa ora")
                    }
                } else {
                    Button(onClick = { updater.downloadUpdate(updateState.version) }) {
                        Text("Scarica e installa")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { updater.dismiss() }) { Text("Dopo") }
            }
        )

        is UpdateState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Download in corso") },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = { updateState.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${updateState.progress}%")
                }
            },
            confirmButton = {}
        )

        is UpdateState.ReadyToInstall -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Aggiornamento pronto") },
            text = { Text("L'APK è stato scaricato. Tocca 'Installa' per aggiornare l'app.") },
            confirmButton = {
                Button(onClick = { updater.installUpdate(updateState.file) }) { Text("Installa") }
            },
            dismissButton = {
                TextButton(onClick = { updater.dismiss() }) { Text("Annulla") }
            }
        )

        is UpdateState.NeedPermission -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Permesso richiesto") },
            text = { Text("Per installare l'aggiornamento devi concedere il permesso 'Installa app sconosciuti' per questa app.") },
            confirmButton = {
                Button(onClick = { updater.apriImpostazioniInstallazione() }) { Text("Impostazioni") }
            },
            dismissButton = {
                TextButton(onClick = { updater.dismiss() }) { Text("Annulla") }
            }
        )

        is UpdateState.Error -> AlertDialog(
            onDismissRequest = { updater.dismiss() },
            title = { Text("Errore") },
            text = { Text(updateState.message) },
            confirmButton = {
                TextButton(onClick = { updater.dismiss() }) { Text("OK") }
            }
        )

        is UpdateState.UpToDate -> {
            LaunchedEffect(updateState) {
                snackbar.showSnackbar("Sei già aggiornato (v${updater.versioneCorrente})")
                updater.dismiss()
            }
        }

        else -> {}
    }
}
