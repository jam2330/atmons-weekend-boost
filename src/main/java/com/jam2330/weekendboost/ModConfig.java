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

    // ── KubeJS integration ──────────────────────────────────────────────────────
    // When true the mod will rename the KubeJS script to .disabled on every
    // server start, keeping it inactive even after a pack update restores it.
    // Set to false if you want the vanilla ATM10 behaviour for that script.
    public static boolean DISABLE_CATCH_RESTRICTIONS = true;
    public static boolean DISABLE_MONS               = true;

    // ── Feature toggles ─────────────────────────────────────────────────────────
    public static boolean WEEKEND_BOOST_ENABLED  = true;
    public static boolean NOTIFICATIONS_ENABLED  = true;

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

            // KubeJS settings
            if (obj.has("kubejs_settings")) {
                JsonObject kjs = obj.getAsJsonObject("kubejs_settings");
                if (kjs.has("disable_catch_restrictions")) DISABLE_CATCH_RESTRICTIONS = kjs.get("disable_catch_restrictions").getAsBoolean();
                if (kjs.has("disable_mons"))               DISABLE_MONS               = kjs.get("disable_mons").getAsBoolean();
            }

            // Feature toggles
            if (obj.has("feature_toggles")) {
                JsonObject ft = obj.getAsJsonObject("feature_toggles");
                if (ft.has("weekend_boost_enabled")) WEEKEND_BOOST_ENABLED = ft.get("weekend_boost_enabled").getAsBoolean();
                if (ft.has("notifications_enabled")) NOTIFICATIONS_ENABLED = ft.get("notifications_enabled").getAsBoolean();
            }
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject obj = new JsonObject();

            // ── Readme ──────────────────────────────────────────────────────────────────
            JsonObject readme = new JsonObject();
            readme.addProperty("about",
                "ATMons Weekend Boost — config/weekendboost.json");
            readme.addProperty("feature_toggles",
                "weekend_boost_enabled: master switch for all boost logic. " +
                "notifications_enabled: master switch for all chat/banner/sound announcements.");
            readme.addProperty("kubejs_settings",
                "disable_catch_restrictions: renames kubejs/startup_scripts/catch_restrictions.js to .disabled on every boot. " +
                "disable_mons: renames kubejs/server_scripts/Tweaks/disable_mons.js to .disabled on every boot. " +
                "Pack updates that restore these files are handled automatically — the mod re-disables them each restart.");
            readme.addProperty("interval_settings",
                "announceIntervalTicks: ticks between weekend re-announcements (default 432000 = 6 hours). " +
                "weekdayAnnounceTicks: ticks between weekday re-announcements (default 576000 = 8 hours).");
            readme.addProperty("notification_settings",
                "showBanner/showMessage: weekend login banner and chat message. " +
                "showWeekdayBanner/showWeekdayMessage: weekday login banner and chat message.");
            readme.addProperty("custom_messages",
                "Leave blank to use the built-in defaults. " +
                "weekend_line1/2: override weekend chat lines. weekday_line1/2: override weekday chat lines.");
            obj.add("_readme", readme);

            // ── Feature toggles ─────────────────────────────────────────────────────────
            JsonObject featureToggles = new JsonObject();
            featureToggles.addProperty("weekend_boost_enabled", WEEKEND_BOOST_ENABLED);
            featureToggles.addProperty("notifications_enabled", NOTIFICATIONS_ENABLED);
            obj.add("feature_toggles", featureToggles);

            // ── KubeJS settings ──────────────────────────────────────────────────────────
            JsonObject kubejsSettings = new JsonObject();
            kubejsSettings.addProperty("disable_catch_restrictions", DISABLE_CATCH_RESTRICTIONS);
            kubejsSettings.addProperty("disable_mons", DISABLE_MONS);
            obj.add("kubejs_settings", kubejsSettings);

            // ── Interval settings ────────────────────────────────────────────────────────
            obj.addProperty("checkIntervalTicks",    CHECK_INTERVAL_TICKS);
            obj.addProperty("announceIntervalTicks", ANNOUNCE_INTERVAL_TICKS);
            obj.addProperty("weekdayAnnounceTicks",  WEEKDAY_ANNOUNCE_TICKS);

            // ── Notification settings ────────────────────────────────────────────────────
            obj.addProperty("showBanner",            SHOW_BANNER);
            obj.addProperty("showMessage",           SHOW_MESSAGE);
            obj.addProperty("showWeekdayBanner",     SHOW_WEEKDAY_BANNER);
            obj.addProperty("showWeekdayMessage",    SHOW_WEEKDAY_MESSAGE);
            obj.addProperty("loginDelayTicks",       LOGIN_DELAY_TICKS);

            // ── Custom messages ──────────────────────────────────────────────────────────
            obj.addProperty("customWeekendLine1",    CUSTOM_WEEKEND_LINE1);
            obj.addProperty("customWeekendLine2",    CUSTOM_WEEKEND_LINE2);
            obj.addProperty("customWeekdayLine1",    CUSTOM_WEEKDAY_LINE1);
            obj.addProperty("customWeekdayLine2",    CUSTOM_WEEKDAY_LINE2);

            // ── Weekday values ───────────────────────────────────────────────────────────
            obj.addProperty("normalShinyRate",       NORMAL_SHINY_RATE);
            obj.addProperty("normalPokemonPerChunk", NORMAL_POKEMON_PER_CHUNK);
            obj.addProperty("normalMaxSpawns",       NORMAL_MAX_SPAWNS);
            obj.addProperty("normalExpMultiplier",   NORMAL_EXP_MULTIPLIER);
            obj.addProperty("normalLuckyEgg",        NORMAL_LUCKY_EGG);
            obj.addProperty("normalCommonWeight",    NORMAL_COMMON_WEIGHT);
            obj.addProperty("normalUncommonWeight",  NORMAL_UNCOMMON_WEIGHT);
            obj.addProperty("normalRareWeight",      NORMAL_RARE_WEIGHT);
            obj.addProperty("normalUltraRareWeight", NORMAL_ULTRA_RARE_WEIGHT);

            // ── Weekend values ───────────────────────────────────────────────────────────
            obj.addProperty("weekendShinyRate",       WEEKEND_SHINY_RATE);
            obj.addProperty("weekendPokemonPerChunk", WEEKEND_POKEMON_PER_CHUNK);
            obj.addProperty("weekendMaxSpawns",       WEEKEND_MAX_SPAWNS);
            obj.addProperty("weekendExpMultiplier",   WEEKEND_EXP_MULTIPLIER);
            obj.addProperty("weekendLuckyEgg",        WEEKEND_LUCKY_EGG);
            obj.addProperty("weekendCommonWeight",    WEEKEND_COMMON_WEIGHT);
            obj.addProperty("weekendUncommonWeight",  WEEKEND_UNCOMMON_WEIGHT);
            obj.addProperty("weekendRareWeight",      WEEKEND_RARE_WEIGHT);
            obj.addProperty("weekendUltraRareWeight", WEEKEND_ULTRA_RARE_WEIGHT);

            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
                GSON.toJson(obj, writer);
            }
        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: Failed to save config", e);
        }
    }

    // ── Spawner config builder (unchanged) ──────────────────────────────────────
    public static String buildSpawnerConfig(double common, double uncommon, double rare, double ultraRare) {
        return "{\n" +
            "  \"commonWeight\": " + common + ",\n" +
            "  \"uncommonWeight\": " + uncommon + ",\n" +
            "  \"rareWeight\": " + rare + ",\n" +
            "  \"ultraRareWeight\": " + ultraRare + "\n" +
            "}\n";
    }
}
