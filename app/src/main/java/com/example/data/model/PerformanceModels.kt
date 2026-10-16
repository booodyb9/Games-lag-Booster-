package com.example.data.model

import java.util.UUID

/**
 * Performance metrics snapshot taken at a single point in time during a game session.
 */
data class PerformanceSnapshot(
    val timestamp: Long,
    val fps: Int,
    val pingMs: Int,
    val cpuUsagePercent: Int,
    val gpuUsagePercent: Int,
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val batteryTemp: Float,
    val thermalState: String = "Normal"
)

/**
 * Recorded gaming session containing telemetry data and performance analytics.
 */
data class GamingSession(
    val id: String = UUID.randomUUID().toString(),
    val gameTitle: String,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val averageFps: Float,
    val minFps: Int,
    val maxFps: Int,
    val averagePing: Int,
    val maxPing: Int,
    val averageCpu: Float,
    val averageGpu: Float,
    val ramFreedMb: Long,
    val stabilityScore: Int = 96,
    val snapshots: List<PerformanceSnapshot> = emptyList()
)

/**
 * Represents a background app/process detected as consuming system memory or CPU.
 */
data class HeavyProcessItem(
    val packageName: String,
    val appName: String,
    val pid: Int,
    val memoryUsageMb: Long,
    val cpuUsagePercent: Float,
    val isHeavy: Boolean,
    val isSystem: Boolean,
    val isKilled: Boolean = false,
    val category: String = "Background"
)

/**
 * Result details from executing the Auto-Boost optimization cycle.
 */
data class BoostResult(
    val ramFreedMb: Long,
    val processesKilledCount: Int,
    val killedProcessNames: List<String>,
    val initialPingMs: Int,
    val optimizedPingMs: Int,
    val networkJitterReducedMs: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Ping test target representing regional gaming servers and DNS clusters.
 */
data class PingServerTarget(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val location: String,
    val host: String,
    val port: Int,
    val iconEmoji: String,
    val currentPingMs: Int? = null,
    val jitterMs: Int? = null,
    val packetLossPercent: Int? = null,
    val status: String = "Idle",
    val isFailed: Boolean = false,
    val lastTestedTimestamp: Long? = null
)

val DEFAULT_PING_SERVERS = listOf(
    PingServerTarget(
        id = "riyadh",
        nameAr = "السعودية — الرياض",
        nameEn = "Saudi Arabia — Riyadh",
        location = "Riyadh (KSA Hub)",
        host = "stc.com.sa",
        port = 443,
        iconEmoji = "🇸🇦"
    ),
    PingServerTarget(
        id = "dubai",
        nameAr = "الإمارات — دبي",
        nameEn = "UAE — Dubai",
        location = "Dubai (Middle East Cloud)",
        host = "ec2.me-central-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇦🇪"
    ),
    PingServerTarget(
        id = "cairo",
        nameAr = "مصر — القاهرة",
        nameEn = "Egypt — Cairo",
        location = "Cairo (North Africa)",
        host = "www.orange.eg",
        port = 443,
        iconEmoji = "🇪🇬"
    ),
    PingServerTarget(
        id = "us_east",
        nameAr = "أمريكا — شرق الولايات المتحدة",
        nameEn = "USA — US East",
        location = "N. Virginia (US East)",
        host = "ec2.us-east-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇺🇸"
    ),
    PingServerTarget(
        id = "us_west",
        nameAr = "أمريكا — غرب الولايات المتحدة",
        nameEn = "USA — US West",
        location = "Oregon (US West)",
        host = "ec2.us-west-2.amazonaws.com",
        port = 443,
        iconEmoji = "🇺🇸"
    ),
    PingServerTarget(
        id = "london",
        nameAr = "بريطانيا — لندن",
        nameEn = "UK — London",
        location = "London (West Europe)",
        host = "ec2.eu-west-2.amazonaws.com",
        port = 443,
        iconEmoji = "🇬🇧"
    ),
    PingServerTarget(
        id = "frankfurt",
        nameAr = "ألمانيا — فرانكفورت",
        nameEn = "Germany — Frankfurt",
        location = "Frankfurt (Central Europe)",
        host = "ec2.eu-central-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇩🇪"
    ),
    PingServerTarget(
        id = "singapore",
        nameAr = "سنغافورة",
        nameEn = "Singapore",
        location = "Singapore (Asia Pacific)",
        host = "ec2.ap-southeast-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇸🇬"
    )
)

/**
 * Game configuration and launch item.
 */
data class GameItem(
    val packageName: String,
    val appName: String,
    val isCustom: Boolean = false,
    val targetFps: Int = 90,
    val resolutionPreset: String = "FHD (1080p)",
    val isTurboEnabled: Boolean = true,
    val lastPlayedDate: String = "Today"
)

enum class BoosterMode(val labelAr: String, val labelEn: String, val descAr: String, val descEn: String) {
    ULTRA("وضع التوربو الأقصى", "Ultra Turbo", "أقصى أداء مع إزالة اللاغ تماماً وتثبيت الإطارات", "Max performance, zero lag, frame stability locked"),
    BALANCED("الوضع المتوازن", "Balanced", "توازن مثالي بين الأداء واستهلاك البطارية", "Optimal balance between speed and battery efficiency"),
    BATTERY_SAVER("توفير الطاقة للألعاب", "Battery Saver", "لعب سلس مع خفض حرارة المعالج واستهلاك الطاقة", "Smooth gameplay while reducing thermal heat and drain"),
    ESPORTS("وضع بطولات الرياضات الإلكترونية", "Esports Tournament", "تثبيت زمن الاستجابة (Ping) مع حظر الإشعارات", "Lowest latency lock with background process freeze")
}
