package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChildProfileEntity::class,
        ScreenTimeRuleEntity::class,
        AppRuleEntity::class,
        WebRuleEntity::class,
        GeofenceEntity::class,
        LocationEventEntity::class,
        SafetyAlertEntity::class,
        AccessRequestEntity::class,
        PairingCodeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SafeGuardDatabase : RoomDatabase() {
    abstract fun childDao(): ChildDao
    abstract fun rulesDao(): RulesDao
    abstract fun locationDao(): LocationDao
    abstract fun alertsDao(): AlertsDao
    abstract fun requestsDao(): RequestsDao

    companion object {
        @Volatile
        private var INSTANCE: SafeGuardDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SafeGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeGuardDatabase::class.java,
                    "safeguard_kids_database"
                )
                    .addCallback(SafeGuardDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SafeGuardDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: SafeGuardDatabase) {
            val now = System.currentTimeMillis()
            val childDao = db.childDao()
            val rulesDao = db.rulesDao()
            val locationDao = db.locationDao()
            val alertsDao = db.alertsDao()
            val requestsDao = db.requestsDao()

            // 1. Initial Child Profiles
            val leo = ChildProfileEntity(
                id = "child_leo_01",
                familyId = "family_anderson_01",
                name = "Leo",
                age = 12,
                avatarEmoji = "👦",
                deviceModel = "Google Pixel 8a",
                onlineStatus = DeviceOnlineStatus.ONLINE,
                batteryPct = 78,
                isCharging = false,
                lastSeenTimestamp = now - 120_000,
                todayUsedMinutes = 95,
                dailyLimitMinutes = 150,
                isDevicePaused = false,
                isBedtimeActive = false,
                isStudyModeActive = true,
                isLocationSharingEnabled = true,
                isScreenViewingActive = false,
                locationAddress = "Lincoln Middle School, 450 Elm St",
                latitude = 37.7749,
                longitude = -122.4194
            )

            val maya = ChildProfileEntity(
                id = "child_maya_02",
                familyId = "family_anderson_01",
                name = "Maya",
                age = 15,
                avatarEmoji = "👧",
                deviceModel = "Samsung Galaxy A54",
                onlineStatus = DeviceOnlineStatus.ONLINE,
                batteryPct = 42,
                isCharging = false,
                lastSeenTimestamp = now - 60_000,
                todayUsedMinutes = 145,
                dailyLimitMinutes = 180,
                isDevicePaused = false,
                isBedtimeActive = false,
                isStudyModeActive = false,
                isLocationSharingEnabled = true,
                isScreenViewingActive = false,
                locationAddress = "Central City Library, 100 Main St",
                latitude = 37.7833,
                longitude = -122.4167
            )

            childDao.insertChild(leo)
            childDao.insertChild(maya)

            // 2. Screen Time Day Rules for Leo
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            days.forEach { day ->
                val isWeekend = day == "Sat" || day == "Sun"
                rulesDao.insertScreenTimeRule(
                    ScreenTimeRuleEntity(
                        childId = "child_leo_01",
                        dayOfWeek = day,
                        maxMinutes = if (isWeekend) 210 else 150,
                        bedTimeStart = if (isWeekend) "22:00" else "21:00",
                        bedTimeEnd = "07:00",
                        studyHoursStart = if (isWeekend) "10:00" else "08:30",
                        studyHoursEnd = if (isWeekend) "12:00" else "15:00",
                        isEnabled = true
                    )
                )
                rulesDao.insertScreenTimeRule(
                    ScreenTimeRuleEntity(
                        childId = "child_maya_02",
                        dayOfWeek = day,
                        maxMinutes = if (isWeekend) 240 else 180,
                        bedTimeStart = if (isWeekend) "23:00" else "22:00",
                        bedTimeEnd = "07:00",
                        studyHoursStart = if (isWeekend) "11:00" else "08:30",
                        studyHoursEnd = if (isWeekend) "13:00" else "15:30",
                        isEnabled = true
                    )
                )
            }

            // 3. App Rules for Leo
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_leo_01",
                    appName = "YouTube",
                    packageName = "com.google.android.youtube",
                    category = "Entertainment",
                    iconEmoji = "▶️",
                    usedMinutesToday = 40,
                    limitMinutes = 45,
                    isBlocked = false,
                    isAlwaysAllowed = false
                )
            )
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_leo_01",
                    appName = "Minecraft",
                    packageName = "com.mojang.minecraftpe",
                    category = "Gaming",
                    iconEmoji = "⛏️",
                    usedMinutesToday = 30,
                    limitMinutes = 60,
                    isBlocked = false,
                    isAlwaysAllowed = false
                )
            )
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_leo_01",
                    appName = "Duolingo",
                    packageName = "com.duolingo",
                    category = "Education",
                    iconEmoji = "🦉",
                    usedMinutesToday = 15,
                    limitMinutes = 0,
                    isBlocked = false,
                    isAlwaysAllowed = true
                )
            )
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_leo_01",
                    appName = "TikTok",
                    packageName = "com.zhiliaoapp.musically",
                    category = "Social",
                    iconEmoji = "🎵",
                    usedMinutesToday = 0,
                    limitMinutes = 0,
                    isBlocked = true,
                    isAlwaysAllowed = false
                )
            )
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_leo_01",
                    appName = "Phone & Emergency",
                    packageName = "com.android.dialer",
                    category = "Utility",
                    iconEmoji = "📞",
                    usedMinutesToday = 5,
                    limitMinutes = 0,
                    isBlocked = false,
                    isAlwaysAllowed = true
                )
            )

            // App Rules for Maya
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_maya_02",
                    appName = "Instagram",
                    packageName = "com.instagram.android",
                    category = "Social",
                    iconEmoji = "📸",
                    usedMinutesToday = 55,
                    limitMinutes = 60,
                    isBlocked = false,
                    isAlwaysAllowed = false
                )
            )
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_maya_02",
                    appName = "Spotify",
                    packageName = "com.spotify.music",
                    category = "Entertainment",
                    iconEmoji = "🎧",
                    usedMinutesToday = 60,
                    limitMinutes = 0,
                    isBlocked = false,
                    isAlwaysAllowed = true
                )
            )
            rulesDao.insertAppRule(
                AppRuleEntity(
                    childId = "child_maya_02",
                    appName = "Khan Academy",
                    packageName = "org.khanacademy.android",
                    category = "Education",
                    iconEmoji = "📚",
                    usedMinutesToday = 30,
                    limitMinutes = 0,
                    isBlocked = false,
                    isAlwaysAllowed = true
                )
            )

            // 4. Web Rules
            rulesDao.insertWebRule(
                WebRuleEntity(
                    childId = "child_leo_01",
                    urlOrDomain = "khanacademy.org",
                    category = "Education",
                    isBlocked = false,
                    isCustomRule = false
                )
            )
            rulesDao.insertWebRule(
                WebRuleEntity(
                    childId = "child_leo_01",
                    urlOrDomain = "wikipedia.org",
                    category = "Education",
                    isBlocked = false,
                    isCustomRule = false
                )
            )
            rulesDao.insertWebRule(
                WebRuleEntity(
                    childId = "child_leo_01",
                    urlOrDomain = "twitch.tv",
                    category = "Video Streaming",
                    isBlocked = true,
                    isCustomRule = true
                )
            )
            rulesDao.insertWebRule(
                WebRuleEntity(
                    childId = "child_leo_01",
                    urlOrDomain = "discord.com",
                    category = "Social Media",
                    isBlocked = true,
                    isCustomRule = true
                )
            )

            // 5. Geofences
            locationDao.insertGeofence(
                GeofenceEntity(
                    childId = "child_leo_01",
                    name = "Home",
                    address = "742 Evergreen Terrace",
                    latitude = 37.7730,
                    longitude = -122.4210,
                    radiusMeters = 150,
                    isSafeZone = true,
                    notifyOnEnter = true,
                    notifyOnExit = true,
                    iconEmoji = "🏠"
                )
            )
            locationDao.insertGeofence(
                GeofenceEntity(
                    childId = "child_leo_01",
                    name = "Lincoln Middle School",
                    address = "450 Elm St",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    radiusMeters = 200,
                    isSafeZone = true,
                    notifyOnEnter = true,
                    notifyOnExit = true,
                    iconEmoji = "🏫"
                )
            )
            locationDao.insertGeofence(
                GeofenceEntity(
                    childId = "child_leo_01",
                    name = "City Skate Park",
                    address = "1200 Recreational Way",
                    latitude = 37.7710,
                    longitude = -122.4150,
                    radiusMeters = 250,
                    isSafeZone = true,
                    notifyOnEnter = true,
                    notifyOnExit = true,
                    iconEmoji = "🛹"
                )
            )

            // 6. Location Events
            locationDao.insertLocationEvent(
                LocationEventEntity(
                    childId = "child_leo_01",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    locationName = "Lincoln Middle School",
                    timestamp = now - 35 * 60_000,
                    eventType = "Safe Zone Arrival"
                )
            )
            locationDao.insertLocationEvent(
                LocationEventEntity(
                    childId = "child_leo_01",
                    latitude = 37.7730,
                    longitude = -122.4210,
                    locationName = "Home",
                    timestamp = now - 85 * 60_000,
                    eventType = "Safe Zone Departure"
                )
            )

            // 7. Safety Alerts
            alertsDao.insertAlert(
                SafetyAlertEntity(
                    childId = "child_leo_01",
                    type = AlertType.GEOFENCE_ENTER,
                    title = "Arrived at School",
                    message = "Leo arrived safely at Lincoln Middle School on time.",
                    timestamp = now - 35 * 60_000,
                    severity = AlertSeverity.INFO,
                    isRead = false
                )
            )
            alertsDao.insertAlert(
                SafetyAlertEntity(
                    childId = "child_maya_02",
                    type = AlertType.EXCESSIVE_SCREEN_TIME,
                    title = "Screen Time Warning",
                    message = "Maya has reached 80% (145/180 mins) of her daily screen limit.",
                    timestamp = now - 15 * 60_000,
                    severity = AlertSeverity.WARNING,
                    isRead = false
                )
            )
            alertsDao.insertAlert(
                SafetyAlertEntity(
                    childId = "child_leo_01",
                    type = AlertType.BLOCKED_WEBSITE_ATTEMPT,
                    title = "Blocked Site Filtered",
                    message = "Chrome browser safely blocked access to 'twitch.tv' during Study Mode.",
                    timestamp = now - 50 * 60_000,
                    severity = AlertSeverity.INFO,
                    isRead = true
                )
            )

            // 8. Access Requests
            requestsDao.insertRequest(
                AccessRequestEntity(
                    childId = "child_leo_01",
                    type = RequestType.APP_UNLOCK,
                    targetName = "Minecraft",
                    requestedMinutes = 30,
                    reason = "Finished math homework early! Can I play with Sam?",
                    status = RequestStatus.PENDING,
                    createdAt = now - 20 * 60_000
                )
            )
            requestsDao.insertRequest(
                AccessRequestEntity(
                    childId = "child_maya_02",
                    type = RequestType.SCREEN_TIME_EXTENSION,
                    targetName = "Extra Study & Research",
                    requestedMinutes = 30,
                    reason = "Biology presentation due tomorrow morning at 9am.",
                    status = RequestStatus.PENDING,
                    createdAt = now - 10 * 60_000
                )
            )

            // 9. Initial active pairing code for easy demo testing
            childDao.insertPairingCode(
                PairingCodeEntity(
                    code = "842915",
                    childId = "child_leo_01",
                    childName = "Leo",
                    deviceModel = "Google Pixel 8a",
                    expiresAt = now + 86400_000
                )
            )
        }
    }
}
