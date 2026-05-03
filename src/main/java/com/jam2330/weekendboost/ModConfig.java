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
    public static int ANNOUNCE_INTERVAL_TICKS = 432000;
    public static int WEEKDAY_ANNOUNCE_TICKS  = 576000;

    // Notification toggles
    public static boolean SHOW_BANNER          = true;
    public static boolean SHOW_MESSAGE         = true;
    public static boolean SHOW_WEEKDAY_BANNER  = true;
    public static boolean SHOW_WEEKDAY_MESSAGE = true;

    // Custom messages (empty string = use default)
    public static String CUSTOM_WEEKEND_LINE1 = "";
    public static String CUSTOM_WEEKEND_LINE2 = "";
    public static String CUSTOM_WEEKDAY_LINE1 = "";
    public static String CUSTOM_WEEKDAY_LINE2 = "";

    // Login delay in ticks (20 ticks = 1 second)
    public static int LOGIN_DELAY_TICKS = 100;

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
            } else {
                load();
            }
        } catch (Exception e) {
            WeekendBoost.LOGGER.error("Weekend Boost: Failed to load config", e);
        }
    }

    public static void load() throws IOException {
        try (Reader reader = new InputStreamReader(
                Files.newInputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
            JsonObject obj = GSON.fromJson(reader, JsonObject.class);
            if (obj == null) return;

            if (obj.has("checkIntervalTicks"))       CHECK_INTERVAL_TICKS    = obj.get("checkIntervalTicks").getAsInt();
            if (obj.has("announceIntervalTicks"))    ANNOUNCE_INTERVAL_TICKS = obj.get("announceIntervalTicks").getAsInt();
            if (obj.has("weekdayAnnounceTicks"))     WEEKDAY_ANNOUNCE_TICKS  = obj.get("weekdayAnnounceTicks").getAsInt();

            if (obj.has("showBanner"))               SHOW_BANNER          = obj.get("showBanner").getAsBoolean();
            if (obj.has("showMessage"))              SHOW_MESSAGE         = obj.get("showMessage").getAsBoolean();
            if (obj.has("showWeekdayBanner"))        SHOW_WEEKDAY_BANNER  = obj.get("showWeekdayBanner").getAsBoolean();
            if (obj.has("showWeekdayMessage"))       SHOW_WEEKDAY_MESSAGE = obj.get("showWeekdayMessage").getAsBoolean();

            if (obj.has("customWeekendLine1"))       CUSTOM_WEEKEND_LINE1 = obj.get("customWeekendLine1").getAsString();
            if (obj.has("customWeekendLine2"))       CUSTOM_WEEKEND_LINE2 = obj.get("customWeekendLine2").getAsString();
            if (obj.has("customWeekdayLine1"))       CUSTOM_WEEKDAY_LINE1 = obj.get("customWeekdayLine1").getAsString();
            if (obj.has("customWeekdayLine2"))       CUSTOM_WEEKDAY_LINE2 = obj.get("customWeekdayLine2").getAsString();

            if (obj.has("loginDelayTicks"))          LOGIN_DELAY_TICKS = obj.get("loginDelayTicks").getAsInt();

            if (obj.has("normalShinyRate"))          NORMAL_SHINY_RATE        = obj.get("normalShinyRate").getAsDouble();
            if (obj.has("normalPokemonPerChunk"))    NORMAL_POKEMON_PER_CHUNK = obj.get("normalPokemonPerChunk").getAsDouble();
            if (obj.has("normalMaxSpawns"))          NORMAL_MAX_SPAWNS        = obj.get("normalMaxSpawns").getAsInt();
            if (obj.has("normalExpMultiplier"))      NORMAL_EXP_MULTIPLIER    = obj.get("normalExpMultiplier").getAsDouble();
            if (obj.has("normalLuckyEgg"))           NORMAL_LUCKY_EGG         = obj.get("normalLuckyEgg").getAsDouble();
            if (obj.has("normalCommonWeight"))       NORMAL_COMMON_WEIGHT     = obj.get("normalCommonWeight").getAsDouble();
            if (obj.has("normalUncommonWeight"))     NORMAL_UNCOMMON_WEIGHT   = obj.get("normalUncommonWeight").getAsDouble();
            if (obj.has("normalRareWeight"))         NORMAL_RARE_WEIGHT       = obj.get("normalRareWeight").getAsDouble();
            if (obj.has("normalUltraRareWeight"))    NORMAL_ULTRA_RARE_WEIGHT = obj.get("normalUltraRareWeight").getAsDouble();

            if (obj.has("weekendShinyRate"))         WEEKEND_SHINY_RATE        = obj.get("weekendShinyRate").getAsDouble();
            if (obj.has("weekendPokemonPerChunk"))   WEEKEND_POKEMON_PER_CHUNK = obj.get("weekendPokemonPerChunk").getAsDouble();
            if (obj.has("weekendMaxSpawns"))         WEEKEND_MAX_SPAWNS        = obj.get("weekendMaxSpawns").getAsInt();
            if (obj.has("weekendExpMultiplier"))     WEEKEND_EXP_MULTIPLIER    = obj.get("weekendExpMultiplier").getAsDouble();
            if (obj.has("weekendLuckyEgg"))          WEEKEND_LUCKY_EGG         = obj.get("weekendLuckyEgg").getAsDouble();
            if (obj.has("weekendCommonWeight"))      WEEKEND_COMMON_WEIGHT     = obj.get("weekendCommonWeight").getAsDouble();
            if (obj.has("weekendUncommonWeight"))    WEEKEND_UNCOMMON_WEIGHT   = obj.get("weekendUncommonWeight").getAsDouble();
            if (obj.has("weekendRareWeight"))        WEEKEND_RARE_WEIGHT       = obj.get("weekendRareWeight").getAsDouble();
            if (obj.has("weekendUltraRareWeight"))   WEEKEND_ULTRA_RARE_WEIGHT = obj.get("weekendUltraRareWeight").getAsDouble();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject obj = new JsonObject();

            obj.addProperty("checkIntervalTicks",    CHECK_INTERVAL_TICKS);
            obj.addProperty("announceIntervalTicks", ANNOUNCE_INTERVAL_TICKS);
            obj.addProperty("weekdayAnnounceTicks",  WEEKDAY_ANNOUNCE_TICKS);

            obj.addProperty("showBanner",            SHOW_BANNER);
            obj.addProperty("showMessage",           SHOW_MESSAGE);
            obj.addProperty("showWeekdayBanner",     SHOW_WEEKDAY_BANNER);
            obj.addProperty("showWeekdayMessage",    SHOW_WEEKDAY_MESSAGE);

            obj.addProperty("customWeekendLine1",    CUSTOM_WEEKEND_LINE1);
            obj.addProperty("customWeekendLine2",    CUSTOM_WEEKEND_LINE2);
            obj.addProperty("customWeekdayLine1",    CUSTOM_WEEKDAY_LINE1);
            obj.addProperty("customWeekdayLine2",    CUSTOM_WEEKDAY_LINE2);

            obj.addProperty("loginDelayTicks",       LOGIN_DELAY_TICKS);

            obj.addProperty("normalShinyRate",       NORMAL_SHINY_RATE);
            obj.addProperty("normalPokemonPerChunk", NORMAL_POKEMON_PER_CHUNK);
            obj.addProperty("normalMaxSpawns",       NORMAL_MAX_SPAWNS);
            obj.addProperty("normalExpMultiplier",   NORMAL_EXP_MULTIPLIER);
            obj.addProperty("normalLuckyEgg",        NORMAL_LUCKY_EGG);
            obj.addProperty("normalCommonWeight",    NORMAL_COMMON_WEIGHT);
            obj.addProperty("normalUncommonWeight",  NORMAL_UNCOMMON_WEIGHT);
            obj.addProperty("normalRareWeight",      NORMAL_RARE_WEIGHT);
            obj.addProperty("normalUltraRareWeight", NORMAL_ULTRA_RARE_WEIGHT);

            obj.addProperty("weekendShinyRate",      WEEKEND_SHINY_RATE);
            obj.addProperty("weekendPokemonPerChunk",WEEKEND_POKEMON_PER_CHUNK);
            obj.addProperty("weekendMaxSpawns",      WEEKEND_MAX_SPAWNS);
            obj.addProperty("weekendExpMultiplier",  WEEKEND_EXP_MULTIPLIER);
            obj.addProperty("weekendLuckyEgg",       WEEKEND_LUCKY_EGG);
            obj.addProperty("weekendCommonWeight",   WEEKEND_COMMON_WEIGHT);
            obj.addProperty("weekendUncommonWeight", WEEKEND_UNCOMMON_WEIGHT);
            obj.addProperty("weekendRareWeight",     WEEKEND_RARE_WEIGHT);
            obj.addProperty("weekendUltraRareWeight",WEEKEND_ULTRA_RARE_WEIGHT);

            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
                GSON.toJson(obj, writer);
            }
        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: Failed to save config", e);
        }
    }
}