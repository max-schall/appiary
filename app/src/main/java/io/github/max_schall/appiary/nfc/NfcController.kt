package io.github.max_schall.appiary.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Optional NFC support. Reading a hive tag opens that hive (or surfaces the tag
 * id so it can be linked to a hive). Everything degrades gracefully: if the
 * device has no NFC adapter, [isAvailable] is false and nothing is wired up.
 */
object NfcController {
    /** Hive ids to open, emitted when a scanned tag matches a hive. */
    val openHiveRequests = MutableSharedFlow<String>(extraBufferCapacity = 1)

    /** The most recently scanned tag id (hex), for linking to a hive. */
    val lastScannedTag = MutableStateFlow<String?>(null)

    fun isAvailable(activity: Activity): Boolean = NfcAdapter.getDefaultAdapter(activity) != null

    fun enableForeground(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else 0
        val intent = Intent(activity, activity.javaClass)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(activity, 0, intent, flags)
        adapter.enableForegroundDispatch(activity, pending, null, null)
    }

    fun disableForeground(activity: Activity) {
        NfcAdapter.getDefaultAdapter(activity)?.disableForegroundDispatch(activity)
    }

    /** Extract a stable hex id from an NFC discovery intent, if present. */
    fun tagIdFrom(intent: Intent): String? {
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        return tag?.id?.joinToString("") { "%02X".format(it) }
    }
}
