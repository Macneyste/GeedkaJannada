package com.geedkajannada.reminder

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.geedkajannada.data.SettingsStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Faylka aadaanka ee isticmaaluhu ka doorto telefoonkiisa. Waxaa lagu koobiyeeyaa kaydka app-ka,
 * si uu u shaqeeyo xitaa haddii faylka asalka ah la beddelo. App-ku wax duubis ah kuma wato.
 */
object AdhanAudio {
    private const val MAX_BYTES = 20L * 1024 * 1024

    fun file(context: Context): File = File(context.filesDir, "adhan_audio")

    /** Koobi faylka la doortay. false haddii la akhrin waayo ama uu aad u weyn yahay. */
    suspend fun import(context: Context, uri: Uri, store: SettingsStore): Boolean = withContext(Dispatchers.IO) {
        val tmp = File(context.filesDir, "adhan_audio.tmp")
        val ok = runCatching {
            val input = context.contentResolver.openInputStream(uri) ?: return@runCatching false
            var total = 0L
            input.use { source ->
                tmp.outputStream().use { out ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val n = source.read(buffer)
                        if (n < 0) break
                        total += n
                        if (total > MAX_BYTES) return@runCatching false
                        out.write(buffer, 0, n)
                    }
                }
            }
            total > 0 && tmp.renameTo(file(context))
        }.getOrDefault(false)
        tmp.delete()
        if (ok) store.setAdhanName(displayName(context, uri))
        ok
    }

    private fun displayName(context: Context, uri: Uri): String =
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0) else null
            }
        }.getOrNull() ?: "audio"
}
