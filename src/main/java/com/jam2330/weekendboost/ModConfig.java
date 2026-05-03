package com.jam2330.weekendboost;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ModConfig {

    private static final Path CONFIG_PATH = Path.of("config/weekendboost.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Tick intervals
    public static int CHECK_INTERVAL_TICKS    = 1200;
    public static int ANNOUNCE_INTERVAL_TICKS = 432000;  // 6 hours weekend
    public static int WEEKDAY_ANNOUNCE_TICKS  = 576000;  // 8 hours weekday

    // Weekday values
    public static double NORMAL_SHINY_RATE        = 8192.0;
    public static double NORMAL_POKEMON_PER_CHUNK = 1.0;
    public static int    NORMAL_MAX_SPAWNS        = 8;
    public static double NORMAL_EXP_MULTIPLIER    = 2.0;
    public static double NORMAL_LUCKY_EGG         = 1.5;
    public static double NORMAL_COMMON_WEIGHT     = 94.3;
    public static double NORMAL_UNCOMMON_WEIGHT   = 5.0;
    public static double NORMAL_RARE_WEIGHT       = 0.5;
    public static double NORMAL_ULTRA_RARE_WEIGHT = 0.2;

    // Weekend values
    public static double WEEKEND_SHINY_RATE        = 2048.0;
    public static double WEEKEND_POKEMON_PER_CHUNK = 3.0;
    public static int    WEEKEND_MAX_SPAWNS        = 16;
    public static double WEEKEND_EXP_MULTIPLIER    = 4.0;
    public static double WEEKEND_LUCKY_EGG         = 3.0;
    public static double WEEKEND_COMMON_WEIGHT     = 75.0;
    public static double WEEKEND_UNCOMMON_WEIGHT   = 12.0;
    public static double WEEKEND_RARE_WEIGHT       = 8.0;
    public static double WEEKEND_ULTRA_RARE_WEIGHT = 5.0;

    public static void loadOrCreate() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                WeekendBoost.LOGGER.info("Weekend Boost: No config found, creating default weekendboost.json");
                save();
                return;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                JsonObject obj = GSON.fromJson(reader, JsonObject.class);

                NORMAL_SHINY_RATE        = getDouble(obj, "normal_shiny_rate",        NORMAL_SHINY_RATE);
                NORMAL_POKEMON_PER_CHUNK = getDouble(obj, "normal_pokemon_per_chunk",  NORMAL_POKEMON_PER_CHUNK);
                NORMAL_MAX_SPAWNS        = getInt   (obj, "normal_max_spawns",         NORMAL_MAX_SPAWNS);
                NORMAL_EXP_MULTIPLIER    = getDouble(obj, "normal_exp_multiplier",     NORMAL_EXP_MULTIPLIER);
                NORMAL_LUCKY_EGG         = getDouble(obj, "normal_lucky_egg",          NORMAL_LUCKY_EGG);
                NORMAL_COMMON_WEIGHT     = getDouble(obj, "normal_common_weight",      NORMAL_COMMON_WEIGHT);
                NORMAL_UNCOMMON_WEIGHT   = getDouble(obj, "normal_uncommon_weight",    NORMAL_UNCOMMON_WEIGHT);
                NORMAL_RARE_WEIGHT       = getDouble(obj, "normal_rare_weight",        NORMAL_RARE_WEIGHT);
                NORMAL_ULTRA_RARE_WEIGHT = getDouble(obj, "normal_ultra_rare_weight",  NORMAL_ULTRA_RARE_WEIGHT);

                WEEKEND_SHINY_RATE        = getDouble(obj, "weekend_shiny_rate",        WEEKEND_SHINY_RATE);
                WEEKEND_POKEMON_PER_CHUNK = getDouble(obj, "weekend_pokemon_per_chunk", WEEKEND_POKEMON_PER_CHUNK);
                WEEKEND_MAX_SPAWNS        = getInt   (obj, "weekend_max_spawns",        WEEKEND_MAX_SPAWNS);
                WEEKEND_EXP_MULTIPLIER    = getDouble(obj, "weekend_exp_multiplier",    WEEKEND_EXP_MULTIPLIER);
                WEEKEND_LUCKY_EGG         = getDouble(obj, "weekend_lucky_egg",         WEEKEND_LUCKY_EGG);
                WEEKEND_COMMON_WEIGHT     = getDouble(obj, "weekend_common_weight",     WEEKEND_COMMON_WEIGHT);
                WEEKEND_UNCOMMON_WEIGHT   = getDouble(obj, "weekend_uncommon_weight",   WEEKEND_UNCOMMON_WEIGHT);
                WEEKEND_RARE_WEIGHT       = getDouble(obj, "weekend_rare_weight",       WEEKEND_RARE_WEIGHT);
                WEEKEND_ULTRA_RARE_WEIGHT = getDouble(obj, "weekend_ultra_rare_weight", WEEKEND_ULTRA_RARE_WEIGHT);

                WeekendBoost.LOGGER.info("Weekend Boost: Config loaded from weekendboost.json");
            }
        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: Failed to load config", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject obj = new JsonObject();

            obj.addProperty("normal_shiny_rate",        NORMAL_SHINY_RATE);
            obj.addProperty("normal_pokemon_per_chunk", NORMAL_POKEMON_PER_CHUNK);
            obj.addProperty("normal_max_spawns",        NORMAL_MAX_SPAWNS);
            obj.addProperty("normal_exp_multiplier",    NORMAL_EXP_MULTIPLIER);
            obj.addProperty("normal_lucky_egg",         NORMAL_LUCKY_EGG);
            obj.addProperty("normal_common_weight",     NORMAL_COMMON_WEIGHT);
            obj.addProperty("normal_uncommon_weight",   NORMAL_UNCOMMON_WEIGHT);
            obj.addProperty("normal_rare_weight",       NORMAL_RARE_WEIGHT);
            obj.addProperty("normal_ultra_rare_weight", NORMAL_ULTRA_RARE_WEIGHT);

            obj.addProperty("weekend_shiny_rate",        WEEKEND_SHINY_RATE);
            obj.addProperty("weekend_pokemon_per_chunk", WEEKEND_POKEMON_PER_CHUNK);
            obj.addProperty("weekend_max_spawns",        WEEKEND_MAX_SPAWNS);
            obj.addProperty("weekend_exp_multiplier",    WEEKEND_EXP_MULTIPLIER);
            obj.addProperty("weekend_lucky_egg",         WEEKEND_LUCKY_EGG);
            obj.addProperty("weekend_common_weight",     WEEKEND_COMMON_WEIGHT);
            obj.addProperty("weekend_uncommon_weight",   WEEKEND_UNCOMMON_WEIGHT);
            obj.addProperty("weekend_rare_weight",       WEEKEND_RARE_WEIGHT);
            obj.addProperty("weekend_ultra_rare_weight", WEEKEND_ULTRA_RARE_WEIGHT);

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(obj, writer);
            }
            WeekendBoost.LOGGER.info("Weekend Boost: Config saved to weekendboost.json");
        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: Failed to save config", e);
        }
    }

    private static double getDouble(JsonObject obj, String key, double fallback) {
        return obj.has(key) ? obj.get(key).getAsDouble() : fallback;
    }

    private static int getInt(JsonObject obj, String key, int fallback) {
        return obj.has(key) ? obj.get(key).getAsInt() : fallback;
    }

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