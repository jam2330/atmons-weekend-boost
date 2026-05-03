package com.jam2330.weekendboost;

public class WeekendBoostConfig {

    // Tick intervals
    public static final int CHECK_INTERVAL_TICKS    = 1200;
    public static final int ANNOUNCE_INTERVAL_TICKS = 432000;

    // Weekday values
    public static final double NORMAL_SHINY_RATE        = 8192.0;
    public static final double NORMAL_POKEMON_PER_CHUNK = 1.0;
    public static final int    NORMAL_MAX_SPAWNS        = 8;
    public static final double NORMAL_EXP_MULTIPLIER    = 2.0;
    public static final double NORMAL_LUCKY_EGG         = 1.5;
    public static final double NORMAL_COMMON_WEIGHT     = 94.3;
    public static final double NORMAL_UNCOMMON_WEIGHT   = 5.0;
    public static final double NORMAL_RARE_WEIGHT       = 0.5;
    public static final double NORMAL_ULTRA_RARE_WEIGHT = 0.2;

    // Weekend values
    public static final double WEEKEND_SHINY_RATE        = 2048.0;
    public static final double WEEKEND_POKEMON_PER_CHUNK = 3.0;
    public static final int    WEEKEND_MAX_SPAWNS        = 16;
    public static final double WEEKEND_EXP_MULTIPLIER    = 4.0;
    public static final double WEEKEND_LUCKY_EGG         = 3.0;
    public static final double WEEKEND_COMMON_WEIGHT     = 75.0;
    public static final double WEEKEND_UNCOMMON_WEIGHT   = 12.0;
    public static final double WEEKEND_RARE_WEIGHT       = 8.0;
    public static final double WEEKEND_ULTRA_RARE_WEIGHT = 5.0;

    public static String buildSpawnerConfig(double common, double uncommon,
                                            double rare, double ultraRare) {
        return "{\"version\":1,\"replaceWithNewVersion\":false," +
            "\"spawnablePositionTypeWeights\":{\"grounded\":1.0,\"submerged\":1.0,\"surface\":1.0}," +
            "\"buckets\":[" +
            "{\"name\":\"common\",\"weight\":" + common + "}," +
            "{\"name\":\"uncommon\",\"weight\":" + uncommon + "}," +
            "{\"name\":\"rare\",\"weight\":" + rare + "}," +
            "{\"name\":\"ultra-rare\",\"weight\":" + ultraRare + "}" +
            "]}";
    }
}
