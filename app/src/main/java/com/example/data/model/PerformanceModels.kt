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
    val timestamp: Long = System.currentTimeMillis(),
    val cacheDeletedBytes: Long = 0L,
    val cleanupFailures: Int = 0
)

/**
 * Public TCP connectivity test target. These are not game servers; measured latency is not a game's in-match ping.
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
    val lastTestedTimestamp: Long? = null,
    val category: String = "ultra" // "ultra" or "regional"
)

val ULTRA_LOW_PING_SERVERS = listOf(
    PingServerTarget(
        id = "ultra_google_core",
        nameAr = "اختبار اتصال Google DNS",
        nameEn = "Google DNS TCP endpoint",
        location = "Global Anycast Gaming POP",
        host = "8.8.8.8",
        port = 53,
        iconEmoji = "🚀",
        category = "ultra"
    ),
    PingServerTarget(
        id = "ultra_cloudflare_1",
        nameAr = "اختبار اتصال Cloudflare DNS",
        nameEn = "Cloudflare DNS TCP endpoint",
        location = "Direct Anycast Backbone",
        host = "1.1.1.1",
        port = 53,
        iconEmoji = "⚡",
        category = "ultra"
    ),
    PingServerTarget(
        id = "ultra_cloudflare_route",
        nameAr = "اختبار اتصال Cloudflare البديل",
        nameEn = "Cloudflare alternate TCP endpoint",
        location = "Ultra-Low Latency POP",
        host = "1.0.0.1",
        port = 53,
        iconEmoji = "🌐",
        category = "ultra"
    ),
    PingServerTarget(
        id = "ultra_google_backup",
        nameAr = "اختبار اتصال Google DNS البديل",
        nameEn = "Google alternate TCP endpoint",
        location = "Low-Delay Gaming Node",
        host = "8.8.4.4",
        port = 53,
        iconEmoji = "🎯",
        category = "ultra"
    ),
    PingServerTarget(
        id = "ultra_google_https",
        nameAr = "اختبار HTTPS إلى Google DNS",
        nameEn = "Google DNS HTTPS endpoint",
        location = "Encrypted Gaming Gateway",
        host = "dns.google",
        port = 443,
        iconEmoji = "🔒",
        category = "ultra"
    ),
    PingServerTarget(
        id = "ultra_cloudflare_shield",
        nameAr = "اختبار Cloudflare DNS الآمن",
        nameEn = "Cloudflare security DNS endpoint",
        location = "Anti-DDoS Low-Latency Node",
        host = "1.1.1.2",
        port = 53,
        iconEmoji = "🛡️",
        category = "ultra"
    ),
    PingServerTarget(
        id = "ultra_cloudflare_direct2",
        nameAr = "اختبار Cloudflare DNS الإضافي",
        nameEn = "Cloudflare additional TCP endpoint",
        location = "Zero-Jitter Gaming Transit",
        host = "1.0.0.2",
        port = 53,
        iconEmoji = "🏎️",
        category = "ultra"
    ),
    PingServerTarget(
        id = "ultra_aws_cloudfront",
        nameAr = "اختبار اتصال موقع AWS",
        nameEn = "AWS website TCP endpoint",
        location = "Amazon Global Edge Network",
        host = "aws.amazon.com",
        port = 443,
        iconEmoji = "☁️",
        category = "ultra"
    )
)

val REGIONAL_PING_SERVERS = listOf(
    PingServerTarget(
        id = "riyadh",
        nameAr = "السعودية — الرياض",
        nameEn = "Saudi Arabia — Riyadh",
        location = "Riyadh (KSA Hub)",
        host = "stc.com.sa",
        port = 443,
        iconEmoji = "🇸🇦",
        category = "regional"
    ),
    PingServerTarget(
        id = "dubai",
        nameAr = "الإمارات — دبي",
        nameEn = "UAE — Dubai",
        location = "Dubai (Middle East Cloud)",
        host = "ec2.me-central-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇦🇪",
        category = "regional"
    ),
    PingServerTarget(
        id = "cairo",
        nameAr = "مصر — القاهرة",
        nameEn = "Egypt — Cairo",
        location = "Cairo (North Africa)",
        host = "www.orange.eg",
        port = 443,
        iconEmoji = "🇪🇬",
        category = "regional"
    ),
    PingServerTarget(
        id = "us_east",
        nameAr = "أمريكا — شرق الولايات المتحدة",
        nameEn = "USA — US East",
        location = "N. Virginia (US East)",
        host = "ec2.us-east-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇺🇸",
        category = "regional"
    ),
    PingServerTarget(
        id = "us_west",
        nameAr = "أمريكا — غرب الولايات المتحدة",
        nameEn = "USA — US West",
        location = "Oregon (US West)",
        host = "ec2.us-west-2.amazonaws.com",
        port = 443,
        iconEmoji = "🇺🇸",
        category = "regional"
    ),
    PingServerTarget(
        id = "london",
        nameAr = "بريطانيا — لندن",
        nameEn = "UK — London",
        location = "London (West Europe)",
        host = "ec2.eu-west-2.amazonaws.com",
        port = 443,
        iconEmoji = "🇬🇧",
        category = "regional"
    ),
    PingServerTarget(
        id = "frankfurt",
        nameAr = "ألمانيا — فرانكفورت",
        nameEn = "Germany — Frankfurt",
        location = "Frankfurt (Central Europe)",
        host = "ec2.eu-central-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇩🇪",
        category = "regional"
    ),
    PingServerTarget(
        id = "singapore",
        nameAr = "سنغافورة",
        nameEn = "Singapore",
        location = "Singapore (Asia Pacific)",
        host = "ec2.ap-southeast-1.amazonaws.com",
        port = 443,
        iconEmoji = "🇸🇬",
        category = "regional"
    )
)

val DEFAULT_PING_SERVERS = ULTRA_LOW_PING_SERVERS

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
    ULTRA("وضع التوربو الأقصى", "Ultra Turbo", "تحضير اللعبة وفق الخيارات المحفوظة", "Prepare using saved options"),
    BALANCED("الوضع المتوازن", "Balanced", "توازن مثالي بين الأداء واستهلاك البطارية", "Optimal balance between speed and battery efficiency"),
    BATTERY_SAVER("توفير الطاقة للألعاب", "Battery Saver", "راجع إعدادات الطاقة في الهاتف", "Review phone power settings"),
    ESPORTS("وضع بطولات الرياضات الإلكترونية", "Esports Tournament", "تقليل المقاطعات بعد إذن عدم الإزعاج", "Reduce interruptions with approved Do Not Disturb")
}
