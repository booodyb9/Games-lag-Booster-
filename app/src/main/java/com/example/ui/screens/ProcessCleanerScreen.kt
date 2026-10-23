package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.BoostResult
import com.example.data.model.HeavyProcessItem
import com.example.ui.theme.*

@Composable
fun ProcessCleanerScreen(
    processes: List<HeavyProcessItem>,
    isScanning: Boolean,
    isKilling: Boolean,
    usedRamMb: Long,
    totalRamMb: Long,
    isArabic: Boolean,
    lastResult: BoostResult? = null,
    onRefreshScan: () -> Unit,
    onKillAllHeavy: () -> Unit,
    onKillSingle: (String) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(if (isArabic) "الذاكرة وتنظيف الكاش" else "Memory and cache cleanup", color = PureWhite, style = MaterialTheme.typography.titleLarge) }
        item { Text(if (isArabic) "RAM المستخدم: $usedRamMb / $totalRamMb MB" else "System RAM used: $usedRamMb / $totalRamMb MB", color = TextSecondary) }
        item {
            Text(
                if (isArabic) "Android يقيّد عرض وإغلاق تطبيقات أخرى. يمكنك حذف كاش GlassBoost هنا أو فتح معلومات تطبيق لإدارته يدويًا. لا يوجد تفريغ شامل للرام بدون صلاحيات خاصة."
                else "Android restricts inspecting and stopping other apps. Clean GlassBoost cache here or open an app's system settings to manage it manually. A full RAM purge requires privileged access.",
                color = TextSecondary
            )
        }
        item {
            Button(enabled = !isKilling, onClick = onKillAllHeavy) {
                Text(if (isKilling) (if (isArabic) "جارٍ القياس والتنظيف..." else "Measuring and cleaning...")
                    else (if (isArabic) "تنظيف كاش GlassBoost وقياس النتيجة" else "Clean GlassBoost cache and measure"))
            }
        }
        lastResult?.let { result ->
            item { Text(
                if (isArabic) "كاش محذوف: ${result.cacheDeletedBytes} بايت • زيادة RAM المتاح المرصودة: ${result.ramFreedMb} MB • تعذر حذف: ${result.cleanupFailures}"
                else "Cache deleted: ${result.cacheDeletedBytes} bytes • Observed available RAM increase: ${result.ramFreedMb} MB • Failed deletions: ${result.cleanupFailures}",
                color = NeonCyan
            ) }
            item { Text(if (isArabic) "تغيّر RAM قد ينتج عن نشاط النظام ولا يثبت إنهاء تطبيقات." else "RAM changes can come from system activity and do not prove app termination.", color = TextSecondary) }
        }
        item { OutlinedButton(enabled = !isScanning, onClick = onRefreshScan) { Text(if (isArabic) "تحديث التطبيقات المرئية" else "Refresh visible processes") } }
        if (processes.isEmpty()) item { Text(if (isArabic) "لم يوفّر النظام عمليات أخرى قابلة للفحص." else "Android exposed no other inspectable processes.", color = TextSecondary) }
        items(processes, key = { "${it.packageName}:${it.pid}" }) { process ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(process.appName)
                    Text(if (process.memoryUsageMb > 0) "${process.memoryUsageMb} MB" else if (isArabic) "الذاكرة غير متاحة" else "Memory unavailable")
                    TextButton(onClick = { onKillSingle(process.packageName) }) { Text(if (isArabic) "فتح معلومات التطبيق" else "Open app settings") }
                }
            }
        }
    }
}
