package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.PreparationOptions
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    isArabic: Boolean,
    options: PreparationOptions,
    onOptionsChanged: (PreparationOptions) -> Unit,
    onDndSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text(if (isArabic) "إعدادات التحضير الفعلي" else "Game preparation settings", color = PureWhite, style = MaterialTheme.typography.titleLarge) }
        item { PreparationToggle(if (isArabic) "التحضير قبل تشغيل اللعبة" else "Prepare before launching", options.autoPrepare) { onOptionsChanged(options.copy(autoPrepare = it)) } }
        item { PreparationToggle(if (isArabic) "حذف كاش GlassBoost القابل للحذف" else "Delete disposable GlassBoost cache", options.cleanCache) { onOptionsChanged(options.copy(cleanCache = it)) } }
        item { PreparationToggle(if (isArabic) "قياس اتصال TCP قبل وبعد التحضير" else "Measure TCP connectivity before and after", options.checkNetwork) { onOptionsChanged(options.copy(checkNetwork = it)) } }
        item { PreparationToggle(if (isArabic) "عرض ملخص النتائج المقاسة" else "Show measured result summary", options.showResults) { onOptionsChanged(options.copy(showResults = it)) } }
        item { PreparationToggle(if (isArabic) "عدم الإزعاج عند تشغيل اللعبة" else "Do Not Disturb on game launch", options.dndOnLaunch) { onOptionsChanged(options.copy(dndOnLaunch = it)) } }
        item {
            Text(
                if (isArabic) "عدم الإزعاج يتطلب إذنك ويقلل المقاطعات وفق استثناءات هاتفك. يظل مفعلاً حتى توقفه من وضع الألعاب. التنظيف يخص كاش هذا التطبيق، ولا يحذف بيانات الألعاب أو كاش التطبيقات الأخرى."
                else "Do Not Disturb requires your permission and follows your phone's priority exceptions. End it in Game Mode when finished. Cleanup deletes this app's cache, never game data or other apps' caches.",
                color = TextSecondary
            )
        }
        item { OutlinedButton(onClick = onDndSettings) { Text(if (isArabic) "إذن عدم الإزعاج" else "Do Not Disturb permission") } }
    }
}

@Composable
private fun PreparationToggle(title: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, modifier = Modifier.weight(1f).padding(end = 12.dp), color = PureWhite)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
