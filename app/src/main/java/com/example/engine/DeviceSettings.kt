package com.example.engine

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

/** Open user-operated system controls; no hidden force-stop or permission bypass. */
fun openDeviceSettings(context: Context, action: String) {
    for (candidate in listOf(action, Settings.ACTION_SETTINGS).distinct()) {
        try {
            context.startActivity(Intent(candidate).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: ActivityNotFoundException) {
        } catch (_: SecurityException) {
        }
    }
    Toast.makeText(context, "تعذر فتح إعدادات الهاتف / Cannot open device settings", Toast.LENGTH_LONG).show()
}
