package com.jam2330.weekendboost;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import java.nio.file.Path;

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
            CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH.toFile())
                .writingMode(WritingMode.REPLACE)
                .build();

            if (!CONFIG_PATH.toFile().exists()) {
                WeekendBoost.LOGGER.info("Weekend Boost: No config found, creating default weekendboost.toml");
                writeDefaults(config);
                config.save();
                config.close();
                return;
            }

            config.load();
            readValues(config);
            config.close();
            WeekendBoost.LOGGER.info("Weekend Boost: Config loaded from weekendboost.toml");

        } catch (Exception e) {
            WeekendBoost.LOGGER.error("Weekend Boost: Failed to load config", e);
        }
    }

    private static void writeDefaults(CommentedFileConfig config) {
        // Feature toggles
        config.setComment("feature_toggles", " Master switches for the mod");
        config.set("feature_toggles.weekend_boost_enabled", WEEKEND_BOOST_ENABLED);
        config.setComment("feature_toggles.weekend_boost_enabled", " Master switch for all boost logic. Set false to disable the mod entirely");
        config.set("feature_toggles.notifications_enabled", NOTIFICATIONS_ENABLED);
        config.setComment("feature_toggles.notifications_enabled", " Master switch for all chat/banner/sound announcements");

        // KubeJS settings
        config.setComment("kubejs_settings", " KubeJS script management");
        config.set("kubejs_settings.disable_catch_restrictions", DISABLE_CATCH_RESTRICTIONS);
        config.setComment("kubejs_settings.disable_catch_restrictions",
            " Renames kubejs/startup_scripts/catch_restrictions.js to .disabled on every boot.\n" +
            " Pack updates that restore the file are handled automatically.");
        config.set("kubejs_settings.disable_mons", DISABLE_MONS);
        config.setComment("kubejs_settings.disable_mons",
            " Renames kubejs/server_scripts/Tweaks/disable_mons.js to .disabled on every boot.\n" +
            " Pack updates that restore the file are handled automatically.");

        // Interval settings
        config.setComment("interval_settings", " How often announcements are sent (20 ticks = 1 second)");
        config.set("interval_settings.weekend_announce_interval_ticks", ANNOUNCE_INTERVAL_TICKS);
        config.setComment("interval_settings.weekend_announce_interval_ticks", " Default: 432000 = 6 hours");
        config.set("interval_settings.weekday_announce_interval_ticks", WEEKDAY_ANNOUNCE_TICKS);
        config.setComment("interval_settings.weekday_announce_interval_ticks", " Default: 576000 = 8 hours");

        // Notification settings
        config.setComment("notification_settings", " Control which messages and banners are shown");
        config.set("notification_settings.show_weekend_banner",   SHOW_BANNER);
        config.setComment("notification_settings.show_weekend_banner", " Show title/subtitle banner on login during weekends");
        config.set("notification_settings.show_weekend_message",  SHOW_MESSAGE);
        config.setComment("notification_settings.show_weekend_message", " Show chat message on login during weekends");
        config.set("notification_settings.show_weekday_banner",   SHOW_WEEKDAY_BANNER);
        config.setComment("notification_settings.show_weekday_banner", " Show title/subtitle banner on login during weekdays");
        config.set("notification_settings.show_weekday_message",  SHOW_WEEKDAY_MESSAGE);
        config.setComment("notification_settings.show_weekday_message", " Show chat message on login during weekdays");
        config.set("notification_settings.login_delay_ticks",     LOGIN_DELAY_TICKS);
        config.setComment("notification_settings.login_delay_ticks", " Delay before showing login message. 20 ticks = 1 second. Default: 100 (5 seconds)");

        // Custom messages
        config.setComment("custom_messages", " Override the default chat messages. Leave blank to use built-in defaults");
        config.set("custom_messages.weekend_line1", CUSTOM_WEEKEND_LINE1);
        config.setComment("custom_messages.weekend_line1", " First line of weekend chat message. Leave blank for default");
        config.set("custom_messages.weekend_line2", CUSTOM_WEEKEND_LINE2);
        config.setComment("custom_messages.weekend_line2", " Second line of weekend chat message. Leave blank for default");
        config.set("custom_messages.weekday_line1", CUSTOM_WEEKDAY_LINE1);
        config.setComment("custom_messages.weekday_line1", " First line of weekday chat message. Leave blank for default");
        config.set("custom_messages.weekday_line2", CUSTOM_WEEKDAY_LINE2);
        config.setComment("custom_messages.weekday_line2", " Second line of weekday chat message. Leave blank for default");

        // Weekday settings
        config.setComment("weekday_settings", " Normal (weekday) Cobblemon values");
        config.set("weekday_settings.shiny_rate",           NORMAL_SHINY_RATE);
        config.setComment("weekday_settings.shiny_rate", " Higher = rarer shinies. Default: 8192 (1 in 8192 chance)");
        config.set("weekday_settings.pokemon_per_chunk",    NORMAL_POKEMON_PER_CHUNK);
        config.setComment("weekday_settings.pokemon_per_chunk", " How many Pokemon can spawn per chunk. Default: 1.0");
        config.set("weekday_settings.max_spawns_per_pass",  NORMAL_MAX_SPAWNS);
        config.setComment("weekday_settings.max_spawns_per_pass", " Max spawns checked per pass. Default: 8");
        config.set("weekday_settings.exp_multiplier",       NORMAL_EXP_MULTIPLIER);
        config.setComment("weekday_settings.exp_multiplier", " EXP gain multiplier. Default: 2.0");
        config.set("weekday_settings.lucky_egg_multiplier", NORMAL_LUCKY_EGG);
        config.setComment("weekday_settings.lucky_egg_multiplier", " Lucky Egg bonus multiplier. Default: 1.5");
        config.set("weekday_settings.common_weight",        NORMAL_COMMON_WEIGHT);
        config.setComment("weekday_settings.common_weight", " Spawn bucket weight for common tier. Higher = more likely");
        config.set("weekday_settings.uncommon_weight",      NORMAL_UNCOMMON_WEIGHT);
        config.setComment("weekday_settings.uncommon_weight", " Spawn bucket weight for uncommon tier");
        config.set("weekday_settings.rare_weight",          NORMAL_RARE_WEIGHT);
        config.setComment("weekday_settings.rare_weight", " Spawn bucket weight for rare tier");
        config.set("weekday_settings.ultra_rare_weight",    NORMAL_ULTRA_RARE_WEIGHT);
        config.setComment("weekday_settings.ultra_rare_weight", " Spawn bucket weight for ultra-rare tier. Default: 0.2");

        // Weekend settings
        config.setComment("weekend_settings", " Boosted (weekend) Cobblemon values");
        config.set("weekend_settings.shiny_rate",           WEEKEND_SHINY_RATE);
        config.setComment("weekend_settings.shiny_rate", " Higher = rarer shinies. Default: 2048 (4x more shinies than weekday)");
        config.set("weekend_settings.pokemon_per_chunk",    WEEKEND_POKEMON_PER_CHUNK);
        config.setComment("weekend_settings.pokemon_per_chunk", " How many Pokemon can spawn per chunk. Default: 3.0");
        config.set("weekend_settings.max_spawns_per_pass",  WEEKEND_MAX_SPAWNS);
        config.setComment("weekend_settings.max_spawns_per_pass", " Max spawns checked per pass. Default: 16");
        config.set("weekend_settings.exp_multiplier",       WEEKEND_EXP_MULTIPLIER);
        config.setComment("weekend_settings.exp_multiplier", " EXP gain multiplier. Default: 4.0");
        config.set("weekend_settings.lucky_egg_multiplier", WEEKEND_LUCKY_EGG);
        config.setComment("weekend_settings.lucky_egg_multiplier", " Lucky Egg bonus multiplier. Default: 3.0");
        config.set("weekend_settings.common_weight",        WEEKEND_COMMON_WEIGHT);
        config.setComment("weekend_settings.common_weight", " Spawn bucket weight for common tier");
        config.set("weekend_settings.uncommon_weight",      WEEKEND_UNCOMMON_WEIGHT);
        config.setComment("weekend_settings.uncommon_weight", " Spawn bucket weight for uncommon tier");
        config.set("weekend_settings.rare_weight",          WEEKEND_RARE_WEIGHT);
        config.setComment("weekend_settings.rare_weight", " Spawn bucket weight for rare tier");
        config.set("weekend_settings.ultra_rare_weight",    WEEKEND_ULTRA_RARE_WEIGHT);
        config.setComment("weekend_settings.ultra_rare_weight", " Spawn bucket weight for ultra-rare tier. Default: 5.0 (25x more than weekday)");
    }

    private static void readValues(CommentedFileConfig config) {
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