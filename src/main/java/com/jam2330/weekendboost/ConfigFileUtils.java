package com.jam2330.weekendboost;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.Pattern;

public class ConfigFileUtils {

    private static final Pattern SHINY_RATE_PATTERN        = Pattern.compile("\"shinyRate\":\\s*[0-9.]+");
    private static final Pattern POKEMON_PER_CHUNK_PATTERN = Pattern.compile("\"pokemonPerChunk\":\\s*[0-9.]+");
    private static final Pattern MAX_SPAWNS_PATTERN        = Pattern.compile("\"maximumSpawnsPerPass\":\\s*[0-9]+");
    private static final Pattern EXP_MULTIPLIER_PATTERN    = Pattern.compile("\"experienceMultiplier\":\\s*[0-9.]+");
    private static final Pattern LUCKY_EGG_PATTERN         = Pattern.compile("\"luckyEggMultiplier\":\\s*[0-9.]+");

    public static void writeSpawnerConfig(Path configDir, String content) {
        Path spawnerConfig = configDir.resolve("cobblemon/spawning/best-spawner-config.json");
        try {
            Files.createDirectories(spawnerConfig.getParent());
            Files.writeString(spawnerConfig, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            WeekendBoost.LOGGER.info("Weekend Boost: spawner config written successfully");
        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: failed to write spawner config", e);
        }
    }

    public static void updateMainConfig(Path configDir, double shinyRate, double pokemonPerChunk,
                                        int maxSpawns, double expMultiplier, double luckyEgg) {
        Path mainConfig = configDir.resolve("cobblemon/main.json");
        try {
            if (!Files.exists(mainConfig)) {
                WeekendBoost.LOGGER.warn("Weekend Boost: main.json not found at {} — skipping", mainConfig);
                return;
            }

            String content = Files.readString(mainConfig, StandardCharsets.UTF_8);

            content = SHINY_RATE_PATTERN.matcher(content)
                .replaceAll("\"shinyRate\": " + shinyRate);
            content = POKEMON_PER_CHUNK_PATTERN.matcher(content)
                .replaceAll("\"pokemonPerChunk\": " + pokemonPerChunk);
            content = MAX_SPAWNS_PATTERN.matcher(content)
                .replaceAll("\"maximumSpawnsPerPass\": " + maxSpawns);
            content = EXP_MULTIPLIER_PATTERN.matcher(content)
                .replaceAll("\"experienceMultiplier\": " + expMultiplier);
            content = LUCKY_EGG_PATTERN.matcher(content)
                .replaceAll("\"luckyEggMultiplier\": " + luckyEgg);

            Files.writeString(mainConfig, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            WeekendBoost.LOGGER.info("Weekend Boost: main.json updated successfully");

        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: failed to update main.json", e);
        }
    }
}
