package com.jam2330.weekendboost;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ModConfig {

    private static final Path CONFIG_PATH = Path.of("config/weekendboost.json");

    // Values loaded from config file (defaults match WeekendBoostConfig)
    public static double normalShinyRate        = WeekendBoostConfig.NORMAL_SHINY_RATE;
    public static double normalPokemonPerChunk  = WeekendBoostConfig.NORMAL_POKEMON_PER_CHUNK;
    public static int    normalMaxSpawns        = WeekendBoostConfig.NORMAL_MAX_SPAWNS;
    public static double normalExpMultiplier    = WeekendBoostConfig.NORMAL_EXP_MULTIPLIER;
    public static double normalLuckyEgg         = WeekendBoostConfig.NORMAL_LUCKY_EGG;

    public static double weekendShinyRate        = WeekendBoostConfig.WEEKEND_SHINY_RATE;
    public static double weekendPokemonPerChunk  = WeekendBoostConfig.WEEKEND_POKEMON_PER_CHUNK;
    public static int    weekendMaxSpawns        = WeekendBoostConfig.WEEKEND_MAX_SPAWNS;
    public static double weekendExpMultiplier    = WeekendBoostConfig.WEEKEND_EXP_MULTIPLIER;
    public static double weekendLuckyEgg         = WeekendBoostConfig.WEEKEND_LUCKY_EGG;

    public static double normalCommonWeight    = 94.3;
    public static double normalUncommonWeight  = 5.0;
    public static double normalRareWeight      = 0.5;
    public static double normalUltraRareWeight = 0.2;

    public static final int ANNOUNCE_INTERVAL_TICKS = 20 * 60 * 60 * 6;
    public static final int CHECK_INTERVAL_TICKS    = 20 * 60;

    public static double weekendCommonWeight    = 75.0;
    public static double weekendUncommonWeight  = 12.0;
    public static double weekendRareWeight      = 8.0;
    public static double weekendUltraRareWeight = 5.0;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            createDefault();
            return;
        }
        try {
            String content = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();

            JsonObject normal = json.getAsJsonObject("weekday");
            normalShinyRate        = normal.get("shinyRate").getAsDouble();
            normalPokemonPerChunk  = normal.get("pokemonPerChunk").getAsDouble();
            normalMaxSpawns        = normal.get("maxSpawnsPerPass").getAsInt();
            normalExpMultiplier    = normal.get("expMultiplier").getAsDouble();
            normalLuckyEgg         = normal.get("luckyEggMultiplier").getAsDouble();
            normalCommonWeight     = normal.get("commonWeight").getAsDouble();
            normalUncommonWeight   = normal.get("uncommonWeight").getAsDouble();
            normalRareWeight       = normal.get("rareWeight").getAsDouble();
            normalUltraRareWeight  = normal.get("ultraRareWeight").getAsDouble();

            JsonObject weekend = json.getAsJsonObject("weekend");
            weekendShinyRate        = weekend.get("shinyRate").getAsDouble();
            weekendPokemonPerChunk  = weekend.get("pokemonPerChunk").getAsDouble();
            weekendMaxSpawns        = weekend.get("maxSpawnsPerPass").getAsInt();
            weekendExpMultiplier    = weekend.get("expMultiplier").getAsDouble();
            weekendLuckyEgg         = weekend.get("luckyEggMultiplier").getAsDouble();
            weekendCommonWeight     = weekend.get("commonWeight").getAsDouble();
            weekendUncommonWeight   = weekend.get("uncommonWeight").getAsDouble();
            weekendRareWeight       = weekend.get("rareWeight").getAsDouble();
            weekendUltraRareWeight  = weekend.get("ultraRareWeight").getAsDouble();

            WeekendBoost.LOGGER.info("Weekend Boost: config loaded from {}", CONFIG_PATH);
        } catch (Exception e) {
            WeekendBoost.LOGGER.error("Weekend Boost: failed to load config, using defaults", e);
        }
    }

    private static void createDefault() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject root = new JsonObject();

            JsonObject normal = new JsonObject();
            normal.addProperty("shinyRate",         normalShinyRate);
            normal.addProperty("pokemonPerChunk",   normalPokemonPerChunk);
            normal.addProperty("maxSpawnsPerPass",  normalMaxSpawns);
            normal.addProperty("expMultiplier",     normalExpMultiplier);
            normal.addProperty("luckyEggMultiplier",normalLuckyEgg);
            normal.addProperty("commonWeight",      normalCommonWeight);
            normal.addProperty("uncommonWeight",    normalUncommonWeight);
            normal.addProperty("rareWeight",        normalRareWeight);
            normal.addProperty("ultraRareWeight",   normalUltraRareWeight);
            root.add("weekday", normal);

            JsonObject weekend = new JsonObject();
            weekend.addProperty("shinyRate",         weekendShinyRate);
            weekend.addProperty("pokemonPerChunk",   weekendPokemonPerChunk);
            weekend.addProperty("maxSpawnsPerPass",  weekendMaxSpawns);
            weekend.addProperty("expMultiplier",     weekendExpMultiplier);
            weekend.addProperty("luckyEggMultiplier",weekendLuckyEgg);
            weekend.addProperty("commonWeight",      weekendCommonWeight);
            weekend.addProperty("uncommonWeight",    weekendUncommonWeight);
            weekend.addProperty("rareWeight",        weekendRareWeight);
            weekend.addProperty("ultraRareWeight",   weekendUltraRareWeight);
            root.add("weekend", weekend);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(CONFIG_PATH, gson.toJson(root), StandardCharsets.UTF_8);
            WeekendBoost.LOGGER.info("Weekend Boost: created default config at {}", CONFIG_PATH);
        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: failed to create default config", e);
        }
    }

    public static String buildSpawnerConfig(double common, double uncommon, double rare, double ultraRare) {
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
