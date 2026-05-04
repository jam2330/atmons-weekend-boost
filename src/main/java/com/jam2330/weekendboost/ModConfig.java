package com.jam2330.weekendboost;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ModConfig {

    private static final Path CONFIG_PATH = Path.of("config/weekendboost.toml");

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

    // KubeJS integration
    public static boolean DISABLE_CATCH_RESTRICTIONS = true;
    public static boolean DISABLE_MONS               = true;

    // Feature toggles
    public static boolean WEEKEND_BOOST_ENABLED = true;
    public static boolean NOTIFICATIONS_ENABLED = true;

    public static void loadOrCreate() {
        try {
            if (!CONFIG_PATH.toFile().exists()) {
                WeekendBoost.LOGGER.info("Weekend Boost: No config found, creating default weekendboost.toml");
                saveDefault();
                return;
            }

            CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH.toFile()).build();
            config.load();

            WEEKEND_BOOST_ENABLED = config.getOrElse("feature_toggles.weekend_boost_enabled", WEEKEND_BOOST_ENABLED);
            NOTIFICATIONS_ENABLED = config.getOrElse("feature_toggles.notifications_enabled", NOTIFICATIONS_ENABLED);

            DISABLE_CATCH_RESTRICTIONS = config.getOrElse("kubejs_settings.disable_catch_restrictions", DISABLE_CATCH_RESTRICTIONS);
            DISABLE_MONS               = config.getOrElse("kubejs_settings.disable_mons",               DISABLE_MONS);

            ANNOUNCE_INTERVAL_TICKS = config.getOrElse("interval_settings.weekend_announce_interval_ticks", ANNOUNCE_INTERVAL_TICKS);
            WEEKDAY_ANNOUNCE_TICKS  = config.getOrElse("interval_settings.weekday_announce_interval_ticks",  WEEKDAY_ANNOUNCE_TICKS);

            SHOW_BANNER          = config.getOrElse("notification_settings.show_weekend_banner",   SHOW_BANNER);
            SHOW_MESSAGE         = config.getOrElse("notification_settings.show_weekend_message",  SHOW_MESSAGE);
            SHOW_WEEKDAY_BANNER  = config.getOrElse("notification_settings.show_weekday_banner",   SHOW_WEEKDAY_BANNER);
            SHOW_WEEKDAY_MESSAGE = config.getOrElse("notification_settings.show_weekday_message",  SHOW_WEEKDAY_MESSAGE);
            LOGIN_DELAY_TICKS    = config.getOrElse("notification_settings.login_delay_ticks",     LOGIN_DELAY_TICKS);

            CUSTOM_WEEKEND_LINE1 = config.getOrElse("custom_messages.weekend_line1", CUSTOM_WEEKEND_LINE1);
            CUSTOM_WEEKEND_LINE2 = config.getOrElse("custom_messages.weekend_line2", CUSTOM_WEEKEND_LINE2);
            CUSTOM_WEEKDAY_LINE1 = config.getOrElse("custom_messages.weekday_line1", CUSTOM_WEEKDAY_LINE1);
            CUSTOM_WEEKDAY_LINE2 = config.getOrElse("custom_messages.weekday_line2", CUSTOM_WEEKDAY_LINE2);

            NORMAL_SHINY_RATE        = config.getOrElse("weekday_settings.shiny_rate",           NORMAL_SHINY_RATE);
            NORMAL_POKEMON_PER_CHUNK = config.getOrElse("weekday_settings.pokemon_per_chunk",    NORMAL_POKEMON_PER_CHUNK);
            NORMAL_MAX_SPAWNS        = config.getOrElse("weekday_settings.max_spawns_per_pass",  NORMAL_MAX_SPAWNS);
            NORMAL_EXP_MULTIPLIER    = config.getOrElse("weekday_settings.exp_multiplier",       NORMAL_EXP_MULTIPLIER);
            NORMAL_LUCKY_EGG         = config.getOrElse("weekday_settings.lucky_egg_multiplier", NORMAL_LUCKY_EGG);
            NORMAL_COMMON_WEIGHT     = config.getOrElse("weekday_settings.common_weight",        NORMAL_COMMON_WEIGHT);
            NORMAL_UNCOMMON_WEIGHT   = config.getOrElse("weekday_settings.uncommon_weight",      NORMAL_UNCOMMON_WEIGHT);
            NORMAL_RARE_WEIGHT       = config.getOrElse("weekday_settings.rare_weight",          NORMAL_RARE_WEIGHT);
            NORMAL_ULTRA_RARE_WEIGHT = config.getOrElse("weekday_settings.ultra_rare_weight",    NORMAL_ULTRA_RARE_WEIGHT);

            WEEKEND_SHINY_RATE        = config.getOrElse("weekend_settings.shiny_rate",           WEEKEND_SHINY_RATE);
            WEEKEND_POKEMON_PER_CHUNK = config.getOrElse("weekend_settings.pokemon_per_chunk",    WEEKEND_POKEMON_PER_CHUNK);
            WEEKEND_MAX_SPAWNS        = config.getOrElse("weekend_settings.max_spawns_per_pass",  WEEKEND_MAX_SPAWNS);
            WEEKEND_EXP_MULTIPLIER    = config.getOrElse("weekend_settings.exp_multiplier",       WEEKEND_EXP_MULTIPLIER);
            WEEKEND_LUCKY_EGG         = config.getOrElse("weekend_settings.lucky_egg_multiplier", WEEKEND_LUCKY_EGG);
            WEEKEND_COMMON_WEIGHT     = config.getOrElse("weekend_settings.common_weight",        WEEKEND_COMMON_WEIGHT);
            WEEKEND_UNCOMMON_WEIGHT   = config.getOrElse("weekend_settings.uncommon_weight",      WEEKEND_UNCOMMON_WEIGHT);
            WEEKEND_RARE_WEIGHT       = config.getOrElse("weekend_settings.rare_weight",          WEEKEND_RARE_WEIGHT);
            WEEKEND_ULTRA_RARE_WEIGHT = config.getOrElse("weekend_settings.ultra_rare_weight",    WEEKEND_ULTRA_RARE_WEIGHT);

            config.close();
            WeekendBoost.LOGGER.info("Weekend Boost: Config loaded from weekendboost.toml");

        } catch (Exception e) {
            WeekendBoost.LOGGER.error("Weekend Boost: Failed to load config", e);
        }
    }

    private static void saveDefault() throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        String toml =
            "# =============================================================\n" +
            "# ATMons Weekend Boost Configuration\n" +
            "# Edit values and restart the server to apply changes.\n" +
            "# =============================================================\n" +
            "\n" +
            "# ── Feature Toggles ───────────────────────────────────────────\n" +
            "[feature_toggles]\n" +
            "\t# Master switch for all boost logic. Set to false to disable the mod entirely.\n" +
            "\tweekend_boost_enabled = " + WEEKEND_BOOST_ENABLED + "\n" +
            "\t# Master switch for all chat messages, banners, and sounds.\n" +
            "\tnotifications_enabled = " + NOTIFICATIONS_ENABLED + "\n" +
            "\n" +
            "# ── KubeJS Script Management ──────────────────────────────────\n" +
            "# These options rename KubeJS scripts to .disabled on every boot,\n" +
            "# preventing them from loading. Pack updates that restore the original\n" +
            "# files are handled automatically — the mod re-disables them each restart.\n" +
            "[kubejs_settings]\n" +
            "\t# Disables: kubejs/startup_scripts/catch_restrictions.js\n" +
            "\tdisable_catch_restrictions = " + DISABLE_CATCH_RESTRICTIONS + "\n" +
            "\t# Disables: kubejs/server_scripts/Tweaks/disable_mons.js\n" +
            "\tdisable_mons = " + DISABLE_MONS + "\n" +
            "\n" +
            "# ── Announcement Intervals ────────────────────────────────────\n" +
            "# How often repeat announcements are sent while the server is running.\n" +
            "# 20 ticks = 1 second, 72000 ticks = 1 hour.\n" +
            "[interval_settings]\n" +
            "\t# How often the weekend boost message is re-announced. Default: 432000 (6 hours)\n" +
            "\tweekend_announce_interval_ticks = " + ANNOUNCE_INTERVAL_TICKS + "\n" +
            "\t# How often the weekday no-boost message is re-announced. Default: 576000 (8 hours)\n" +
            "\tweekday_announce_interval_ticks = " + WEEKDAY_ANNOUNCE_TICKS + "\n" +
            "\n" +
            "# ── Notification Settings ─────────────────────────────────────\n" +
            "# Control which messages and banners are shown to players.\n" +
            "[notification_settings]\n" +
            "\t# Delay before showing login message/banner. 20 ticks = 1 second. Default: 100 (5 seconds)\n" +
            "\tlogin_delay_ticks = " + LOGIN_DELAY_TICKS + "\n" +
            "\t# Show title/subtitle screen banner on login during weekends.\n" +
            "\tshow_weekend_banner = " + SHOW_BANNER + "\n" +
            "\t# Show chat message on login during weekends.\n" +
            "\tshow_weekend_message = " + SHOW_MESSAGE + "\n" +
            "\t# Show title/subtitle screen banner on login during weekdays.\n" +
            "\tshow_weekday_banner = " + SHOW_WEEKDAY_BANNER + "\n" +
            "\t# Show chat message on login during weekdays.\n" +
            "\tshow_weekday_message = " + SHOW_WEEKDAY_MESSAGE + "\n" +
            "\n" +
            "# ── Custom Messages ───────────────────────────────────────────\n" +
            "# Override the default chat message lines.\n" +
            "# Leave blank (\"\") to use the built-in defaults.\n" +
            "[custom_messages]\n" +
            "\t# Line 1 of the weekend chat message.\n" +
            "\tweekend_line1 = \"" + CUSTOM_WEEKEND_LINE1 + "\"\n" +
            "\t# Line 2 of the weekend chat message.\n" +
            "\tweekend_line2 = \"" + CUSTOM_WEEKEND_LINE2 + "\"\n" +
            "\t# Line 1 of the weekday chat message.\n" +
            "\tweekday_line1 = \"" + CUSTOM_WEEKDAY_LINE1 + "\"\n" +
            "\t# Line 2 of the weekday chat message.\n" +
            "\tweekday_line2 = \"" + CUSTOM_WEEKDAY_LINE2 + "\"\n" +
            "\n" +
            "# ── Weekday Settings ──────────────────────────────────────────\n" +
            "# Normal Cobblemon values applied Monday through Friday.\n" +
            "[weekday_settings]\n" +
            "\t# Shiny rate denominator. Higher = rarer. Default: 8192 (1 in 8192 chance)\n" +
            "\tshiny_rate = " + NORMAL_SHINY_RATE + "\n" +
            "\t# How many Pokemon can spawn per chunk. Default: 1.0\n" +
            "\tpokemon_per_chunk = " + NORMAL_POKEMON_PER_CHUNK + "\n" +
            "\t# Max spawns checked per pass. Default: 8\n" +
            "\tmax_spawns_per_pass = " + NORMAL_MAX_SPAWNS + "\n" +
            "\t# EXP gain multiplier. Default: 2.0\n" +
            "\texp_multiplier = " + NORMAL_EXP_MULTIPLIER + "\n" +
            "\t# Lucky Egg bonus multiplier. Default: 1.5\n" +
            "\tlucky_egg_multiplier = " + NORMAL_LUCKY_EGG + "\n" +
            "\t# Spawn bucket weights. Higher = more likely to pick that rarity tier.\n" +
            "\tcommon_weight = " + NORMAL_COMMON_WEIGHT + "\n" +
            "\tuncommon_weight = " + NORMAL_UNCOMMON_WEIGHT + "\n" +
            "\trare_weight = " + NORMAL_RARE_WEIGHT + "\n" +
            "\tultra_rare_weight = " + NORMAL_ULTRA_RARE_WEIGHT + "\n" +
            "\n" +
            "# ── Weekend Settings ──────────────────────────────────────────\n" +
            "# Boosted Cobblemon values applied Saturday and Sunday.\n" +
            "[weekend_settings]\n" +
            "\t# Shiny rate denominator. Higher = rarer. Default: 2048 (4x more shinies than weekday)\n" +
            "\tshiny_rate = " + WEEKEND_SHINY_RATE + "\n" +
            "\t# How many Pokemon can spawn per chunk. Default: 3.0\n" +
            "\tpokemon_per_chunk = " + WEEKEND_POKEMON_PER_CHUNK + "\n" +
            "\t# Max spawns checked per pass. Default: 16\n" +
            "\tmax_spawns_per_pass = " + WEEKEND_MAX_SPAWNS + "\n" +
            "\t# EXP gain multiplier. Default: 4.0\n" +
            "\texp_multiplier = " + WEEKEND_EXP_MULTIPLIER + "\n" +
            "\t# Lucky Egg bonus multiplier. Default: 3.0\n" +
            "\tlucky_egg_multiplier = " + WEEKEND_LUCKY_EGG + "\n" +
            "\t# Spawn bucket weights. Higher = more likely to pick that rarity tier.\n" +
            "\t# ultra_rare_weight of 5.0 vs weekday 0.2 = 25x more ultra-rare spawns!\n" +
            "\tcommon_weight = " + WEEKEND_COMMON_WEIGHT + "\n" +
            "\tuncommon_weight = " + WEEKEND_UNCOMMON_WEIGHT + "\n" +
            "\trare_weight = " + WEEKEND_RARE_WEIGHT + "\n" +
            "\tultra_rare_weight = " + WEEKEND_ULTRA_RARE_WEIGHT + "\n";

        Files.writeString(CONFIG_PATH, toml, StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        WeekendBoost.LOGGER.info("Weekend Boost: Default config written to weekendboost.toml");
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