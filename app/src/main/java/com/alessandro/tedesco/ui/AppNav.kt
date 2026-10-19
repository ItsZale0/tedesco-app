package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

/**
 * V2 - una sola schermata principale (Studio Veloce).
 * Impostazioni, progresso, tutor e backup vivono in un menu nascosto e sono
 * raggiungili da qui: nessuna bottom bar, nessuna navigazione in vista.
 *
 * Sopra il NavHost vivono due overlay sempre disponibili:
 * - TutorBubble: il tutor come bolla laterale (stile Meta AI su WhatsApp)
 * - il banner di aggiornamento, dentro la schermata principale
 */
@Composable
fun AppNav(
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as TedescoApp
    val factory = remember { ViewModelProvider.AndroidViewModelFactory(app) }
    val vm = viewModel<TedescoViewModel>(factory = factory)
    val updater = viewModel<UpdaterViewModel>(factory = factory)

    // Controllo automatico aggiornamenti all'apertura (una volta per avvio)
    LaunchedEffect(Unit) {
        delay(2500)
        updater.checkForUpdate()
    }

    val nav = rememberNavController()

    Box(modifier) {
        NavHost(
            navController = nav,
            startDestination = "studio"
        ) {
            composable("studio") {
                StudioVeloceScreen(
                    vm = vm,
                    updater = updater,
                    onVaiAEsercizi = { nav.navigate("esercizi") },
                    onVaiAImpostazioni = { nav.navigate("impostazioni") },
                    onVaiAProgresso = { nav.navigate("progresso") },
                    onVaiATutor = { nav.navigate("tutor") },
                    onVaiABackup = { nav.navigate("backup") }
                )
            }
            composable("esercizi") {
                EserciziScreen(
                    vm = vm,
                    onIndietro = { nav.popBackStack() },
                    onVaiA = { route -> nav.navigate(route) }
                )
            }
            composable("grammatica") {
                GrammaticaScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            composable("comprensione") {
                ComprensioneScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            composable("produzione") {
                ProduzioneScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            composable("verifica") {
                VerificaScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("ascolto") {
                AscoltoScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            composable("ripasso") {
                RipassoScreen(vm = vm, onIndietro = { nav.popBackStack() })
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
                    updater = updater,
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

        // Bolla del tutor: sopra ogni schermata, come Meta AI su WhatsApp
        TutorBubble(vm = vm)
    }
}
