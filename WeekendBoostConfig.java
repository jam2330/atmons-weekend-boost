package com.jam2330.weekendboost;

public class WeekendBoostConfig {

    // ============================================================
    // Weekday values (normal rates)
    // ============================================================
    public static final double NORMAL_SHINY_RATE         = 8192.0;
    public static final double NORMAL_POKEMON_PER_CHUNK  = 1.0;
    public static final int    NORMAL_MAX_SPAWNS         = 8;
    public static final double NORMAL_EXP_MULTIPLIER     = 2.0;
    public static final double NORMAL_LUCKY_EGG          = 1.5;

    // ============================================================
    // Weekend values (boosted rates)
    // ============================================================
    public static final double WEEKEND_SHINY_RATE        = 2048.0;  // 4x more shinies
    public static final double WEEKEND_POKEMON_PER_CHUNK = 3.0;
    public static final int    WEEKEND_MAX_SPAWNS        = 16;
    public static final double WEEKEND_EXP_MULTIPLIER    = 4.0;
    public static final double WEEKEND_LUCKY_EGG         = 3.0;

    // ============================================================
    // Spawner bucket configs
    // ============================================================
    public static final String WEEKDAY_SPAWNER_CONFIG =
        "{\"version\":1,\"replaceWithNewVersion\":false," +
        "\"spawnablePositionTypeWeights\":{\"grounded\":1.0,\"submerged\":1.0,\"surface\":1.0}," +
        "\"buckets\":[" +
        "{\"name\":\"common\",\"weight\":94.3}," +
        "{\"name\":\"uncommon\",\"weight\":5.0}," +
        "{\"name\":\"rare\",\"weight\":0.5}," +
        "{\"name\":\"ultra-rare\",\"weight\":0.2}" +
        "]}";

    public static final String WEEKEND_SPAWNER_CONFIG =
        "{\"version\":1,\"replaceWithNewVersion\":false," +
        "\"spawnablePositionTypeWeights\":{\"grounded\":1.0,\"submerged\":1.0,\"surface\":1.0}," +
        "\"buckets\":[" +
        "{\"name\":\"common\",\"weight\":75.0}," +
        "{\"name\":\"uncommon\",\"weight\":12.0}," +
        "{\"name\":\"rare\",\"weight\":8.0}," +
        "{\"name\":\"ultra-rare\",\"weight\":5.0}" +
        "]}";

    // ============================================================
    // Announcement interval (6 hours in ticks)
    // ============================================================
    public static final int ANNOUNCE_INTERVAL_TICKS = 20 * 60 * 60 * 6;
    public static final int CHECK_INTERVAL_TICKS    = 20 * 60; // every 60 seconds
}
