package com.alessandro.tedesco.data

import android.content.Context
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.remote.BackupGitHubClient
import com.alessandro.tedesco.sync.NotificheSettings
import kotlinx.coroutines.flow.first
import java.security.MessageDigest

/**
 * Esegue il backup cloud. Unico punto usato sia dal pulsante manuale sia dal
 * backup automatico periodico, cosi' la logica non diverge.
 */
object BackupEsecutore {

    /** Esito dell'operazione. */
    sealed interface Esito {
        data class Ok(val byteCaricati: Int) : Esito
        /** Nulla di nuovo da salvare: il contenuto non e' cambiato. */
        data object NessunCambiamento : Esito
        data class NonConfigurato(val motivo: String) : Esito
        data class Errore(val messaggio: String) : Esito
    }

    /**
     * Carica il backup se il contenuto e' cambiato rispetto all'ultimo inviato.
     *
     * @param forza true = carica sempre (pulsante manuale); false = salta se
     *        l'hash del contenuto e' identico (backup automatico, evita di
     *        consumare richieste GitHub quando l'utente non ha fatto nulla).
     */
    suspend fun esegui(
        context: Context,
        versioneApp: String,
        forza: Boolean
    ): Esito {
        val app = context.applicationContext as TedescoApp
        val settings = BackupSettings(app)

        val token = settings.tokenFlow.first().trim()
        if (token.isBlank()) return Esito.NonConfigurato("Token GitHub mancante")
        val repo = settings.repoFlow.first().trim().ifBlank { BackupCloud.REPO_DEFAULT }

        val profiloAttivoId = app.profileManagerInstance.profiloAttivoId()
        val nomeFile = BackupCloud.nomeFilePerProfilo(profiloAttivoId)

        val repository = app.profileManagerInstance.repositoryFlow.first()
        if (repository.profili.isEmpty()) return Esito.NonConfigurato("Nessun profilo da salvare")

        val notifiche = NotificheSettings(app).leggi()
        val json = BackupCloud.serializza(repository, versioneApp, System.currentTimeMillis(), notifiche)
        val hash = sha256(json)

        // Salta se identico all'ultimo backup (solo in automatico)
        if (!forza && hash == settings.ultimoHash()) {
            return Esito.NessunCambiamento
        }

        return try {
            val client = BackupGitHubClient(token, repo, nomeFile)
            client.carica(json, "Backup automatico Tedesco App")
            settings.salvaHash(hash)
            settings.registraBackup(System.currentTimeMillis())
            if (!forza) settings.registraAuto(System.currentTimeMillis())
            Esito.Ok(json.length)
        } catch (e: Exception) {
            Esito.Errore(e.message ?: "Errore sconosciuto")
        }
    }

    private fun sha256(testo: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(testo.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
