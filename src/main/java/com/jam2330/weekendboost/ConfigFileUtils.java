package com.jam2330.weekendboost;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
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
                WeekendBoost.LOGGER.warn("Weekend Boost: main.json not found at {}", mainConfig);
                return;
            }

            List<String> lines = Files.readAllLines(mainConfig, StandardCharsets.UTF_8);
            List<String> updated = new ArrayList<>();

            for (String line : lines) {
                line = SHINY_RATE_PATTERN.matcher(line)
                    .replaceAll("\"shinyRate\": " + shinyRate);
                line = POKEMON_PER_CHUNK_PATTERN.matcher(line)
                    .replaceAll("\"pokemonPerChunk\": " + pokemonPerChunk);
                line = MAX_SPAWNS_PATTERN.matcher(line)
                    .replaceAll("\"maximumSpawnsPerPass\": " + maxSpawns);
                line = EXP_MULTIPLIER_PATTERN.matcher(line)
                    .replaceAll("\"experienceMultiplier\": " + expMultiplier);
                line = LUCKY_EGG_PATTERN.matcher(line)
                    .replaceAll("\"luckyEggMultiplier\": " + luckyEgg);
                updated.add(line);
            }

            Files.writeString(mainConfig, String.join("\n", updated), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            WeekendBoost.LOGGER.info("Weekend Boost: main.json updated successfully");
        } catch (IOException e) {
            WeekendBoost.LOGGER.error("Weekend Boost: failed to update main.json", e);
        }
    }
}
