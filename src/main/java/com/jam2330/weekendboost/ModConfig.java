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