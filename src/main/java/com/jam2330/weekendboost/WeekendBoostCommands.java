package com.jam2330.weekendboost;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class WeekendBoostCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                WeekendBoostEvents events) {

        // ============================================================
        // /wbstatus — available to all players
        // ============================================================
        dispatcher.register(
            Commands.literal("wbstatus")
                .executes(ctx -> {
                    sendPlayerStatus(ctx.getSource(), events);
                    return 1;
                })
        );

        // ============================================================
        // /wbadmin — OP only
        // ============================================================
        dispatcher.register(
            Commands.literal("wbadmin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                    .executes(ctx -> {
                        sendAdminStatus(ctx.getSource(), events);
                        return 1;
                    })
                )
                .then(Commands.literal("toggle")
                    .executes(ctx -> {
                        ModConfig.WEEKEND_BOOST_ENABLED = !ModConfig.WEEKEND_BOOST_ENABLED;
                        String state = ModConfig.WEEKEND_BOOST_ENABLED ? "ENABLED" : "DISABLED";
                        broadcastToOps(ctx.getSource(), Component.literal(""));
                        broadcastToOps(ctx.getSource(),
                            Component.literal("  [ ")
                                .append(Component.literal("Weekend Boost")
                                    .withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                                .append(Component.literal(" ] "))
                                .append(Component.literal("Boost toggled to " + state + " by ")
                                    .withStyle(s -> s.withColor(0xAAAAAA)))
                                .append(Component.literal(ctx.getSource().getTextName())
                                    .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                        );
                        broadcastToOps(ctx.getSource(),
                            Component.literal("  \u26A0 Restart required for Cobblemon config changes to take effect!")
                                .withStyle(s -> s.withColor(0xFF5555).withBold(true))
                        );
                        broadcastToOps(ctx.getSource(), Component.literal(""));
                        WeekendBoost.LOGGER.info("Weekend Boost: Boost toggled {} by {}",
                            state, ctx.getSource().getTextName());
                        return 1;
                    })
                )
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        ModConfig.loadOrCreate();
                        ctx.getSource().sendSuccess(() ->
                            Component.literal("  [ ")
                                .append(Component.literal("Weekend Boost")
                                    .withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                                .append(Component.literal(" ] "))
                                .append(Component.literal("Config reloaded from weekendboost.toml. ")
                                    .withStyle(s -> s.withColor(0xAAAAAA)))
                                .append(Component.literal("Restart required to apply Cobblemon changes.")
                                    .withStyle(s -> s.withColor(0xFF5555))),
                            false
                        );
                        WeekendBoost.LOGGER.info("Weekend Boost: Config reloaded by {}",
                            ctx.getSource().getTextName());
                        return 1;
                    })
                )
                .then(Commands.literal("forceweekend")
                    .executes(ctx -> {
                        events.setForceWeekend(true);
                        events.setForceWeekday(false);
                        broadcastToOps(ctx.getSource(), Component.literal(""));
                        broadcastToOps(ctx.getSource(),
                            Component.literal("  [ ")
                                .append(Component.literal("Weekend Boost")
                                    .withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                                .append(Component.literal(" ] "))
                                .append(Component.literal("Weekend mode FORCED by ")
                                    .withStyle(s -> s.withColor(0xAAAAAA)))
                                .append(Component.literal(ctx.getSource().getTextName())
                                    .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                        );
                        broadcastToOps(ctx.getSource(),
                            Component.literal("  \u26A0 Restart required for Cobblemon config changes to take effect!")
                                .withStyle(s -> s.withColor(0xFF5555).withBold(true))
                        );
                        broadcastToOps(ctx.getSource(), Component.literal(""));
                        WeekendBoost.LOGGER.info("Weekend Boost: Weekend mode forced by {}",
                            ctx.getSource().getTextName());
                        return 1;
                    })
                )
                .then(Commands.literal("forceweekday")
                    .executes(ctx -> {
                        events.setForceWeekend(false);
                        events.setForceWeekday(true);
                        broadcastToOps(ctx.getSource(), Component.literal(""));
                        broadcastToOps(ctx.getSource(),
                            Component.literal("  [ ")
                                .append(Component.literal("Weekend Boost")
                                    .withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                                .append(Component.literal(" ] "))
                                .append(Component.literal("Weekday mode FORCED by ")
                                    .withStyle(s -> s.withColor(0xAAAAAA)))
                                .append(Component.literal(ctx.getSource().getTextName())
                                    .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                        );
                        broadcastToOps(ctx.getSource(),
                            Component.literal("  \u26A0 Restart required for Cobblemon config changes to take effect!")
                                .withStyle(s -> s.withColor(0xFF5555).withBold(true))
                        );
                        broadcastToOps(ctx.getSource(), Component.literal(""));
                        WeekendBoost.LOGGER.info("Weekend Boost: Weekday mode forced by {}",
                            ctx.getSource().getTextName());
                        return 1;
                    })
                )
                .then(Commands.literal("clearforce")
                    .executes(ctx -> {
                        events.setForceWeekend(false);
                        events.setForceWeekday(false);
                        ctx.getSource().sendSuccess(() ->
                            Component.literal("  [ ")
                                .append(Component.literal("Weekend Boost")
                                    .withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                                .append(Component.literal(" ] "))
                                .append(Component.literal("Force override cleared. Now using real system clock.")
                                    .withStyle(s -> s.withColor(0xAAAAAA))),
                            false
                        );
                        WeekendBoost.LOGGER.info("Weekend Boost: Force override cleared by {}",
                            ctx.getSource().getTextName());
                        return 1;
                    })
                )
        );
    }

    // ============================================================
    // Player-facing status - friendly, no technical details
    // ============================================================
    private static void sendPlayerStatus(CommandSourceStack source, WeekendBoostEvents events) {
        boolean isWeekend = events.isWeekendPublic();

        source.sendSuccess(() -> Component.literal(""), false);

        if (isWeekend) {
            source.sendSuccess(() ->
                Component.literal("  \u2728 ")
                    .append(Component.literal("WEEKEND BOOST IS ACTIVE!")
                        .withStyle(s -> s.withColor(0x55FFFF).withBold(true))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  All Pok\u00e9mon spawn rates, shiny chances, and EXP")
                    .withStyle(s -> s.withColor(0xAAAAAA)),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  boosted by a minimum ")
                    .withStyle(s -> s.withColor(0xAAAAAA))
                    .append(Component.literal("x2")
                        .withStyle(s -> s.withColor(0x55FF55).withBold(true)))
                    .append(Component.literal(" this weekend. Good luck! \uD83C\uDF1F")
                        .withStyle(s -> s.withColor(0xAAAAAA))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  Boosts active this weekend:")
                    .withStyle(s -> s.withColor(0x777777)),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 Shiny rate: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("1/" + (int) ModConfig.WEEKEND_SHINY_RATE)
                        .withStyle(s -> s.withColor(0x55FF55))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 EXP multiplier: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("x" + ModConfig.WEEKEND_EXP_MULTIPLIER)
                        .withStyle(s -> s.withColor(0x55FF55))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 Lucky Egg multiplier: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("x" + ModConfig.WEEKEND_LUCKY_EGG)
                        .withStyle(s -> s.withColor(0x55FF55))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 Pokemon per chunk: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal(String.valueOf(ModConfig.WEEKEND_POKEMON_PER_CHUNK))
                        .withStyle(s -> s.withColor(0x55FF55))),
                false
            );
        } else {
            source.sendSuccess(() ->
                Component.literal("  \uD83C\uDF19 ")
                    .append(Component.literal("NO WEEKEND BOOST ACTIVE")
                        .withStyle(s -> s.withColor(0xAAAAAA).withBold(true))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  Boosted rates are active on ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("Saturdays & Sundays")
                        .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                    .append(Component.literal(". Next boost in: ")
                        .withStyle(s -> s.withColor(0x777777)))
                    .append(Component.literal(events.getCountdownPublic())
                        .withStyle(s -> s.withColor(0x55FF55).withBold(true))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  Normal rates active:")
                    .withStyle(s -> s.withColor(0x777777)),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 Shiny rate: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("1/" + (int) ModConfig.NORMAL_SHINY_RATE)
                        .withStyle(s -> s.withColor(0xAAAAAA))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 EXP multiplier: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("x" + ModConfig.NORMAL_EXP_MULTIPLIER)
                        .withStyle(s -> s.withColor(0xAAAAAA))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 Lucky Egg multiplier: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("x" + ModConfig.NORMAL_LUCKY_EGG)
                        .withStyle(s -> s.withColor(0xAAAAAA))),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  \u2022 Pokemon per chunk: ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal(String.valueOf(ModConfig.NORMAL_POKEMON_PER_CHUNK))
                        .withStyle(s -> s.withColor(0xAAAAAA))),
                false
            );
        }
        source.sendSuccess(() -> Component.literal(""), false);
    }

    // ============================================================
    // OP admin status - full config verification and warnings
    // ============================================================
    private static void sendAdminStatus(CommandSourceStack source, WeekendBoostEvents events) {
        boolean isWeekend = events.isWeekendPublic();
        boolean forced    = events.isForcedPublic();
        Path configDir    = Path.of("config");
        Path mainConfig   = configDir.resolve("cobblemon/main.json");

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() ->
            Component.literal("  ===== ")
                .append(Component.literal("Weekend Boost Admin Status")
                    .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                .append(Component.literal(" =====")),
            false
        );
        source.sendSuccess(() ->
            Component.literal("  Boost Active: ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Component.literal(isWeekend ? "YES" : "NO")
                    .withStyle(s -> s.withColor(isWeekend ? 0x55FF55 : 0xFF5555).withBold(true))),
            false
        );
        source.sendSuccess(() ->
            Component.literal("  Mode: ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Component.literal(forced ? "\u26A0 FORCED OVERRIDE" : "AUTO (system clock)")
                    .withStyle(s -> s.withColor(forced ? 0xFFAA00 : 0xAAAAAA))),
            false
        );
        source.sendSuccess(() ->
            Component.literal("  Boost Enabled: ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Component.literal(String.valueOf(ModConfig.WEEKEND_BOOST_ENABLED))
                    .withStyle(s -> s.withColor(ModConfig.WEEKEND_BOOST_ENABLED ? 0x55FF55 : 0xFF5555))),
            false
        );
        source.sendSuccess(() ->
            Component.literal("  Notifications: ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Component.literal(String.valueOf(ModConfig.NOTIFICATIONS_ENABLED))
                    .withStyle(s -> s.withColor(ModConfig.NOTIFICATIONS_ENABLED ? 0x55FF55 : 0xFF5555))),
            false
        );
        source.sendSuccess(() ->
            Component.literal("  ----- Cobblemon Config Verification -----")
                .withStyle(s -> s.withColor(0x777777)),
            false
        );

        boolean[] restartNeeded = {false};

        if (!Files.exists(mainConfig)) {
            source.sendSuccess(() ->
                Component.literal("  main.json: ")
                    .withStyle(s -> s.withColor(0xAAAAAA))
                    .append(Component.literal("NOT FOUND \u2718")
                        .withStyle(s -> s.withColor(0xFF5555).withBold(true))),
                false
            );
            restartNeeded[0] = true;
        } else {
            try {
                String content = Files.readString(mainConfig, StandardCharsets.UTF_8);

                double expectedShiny  = isWeekend ? ModConfig.WEEKEND_SHINY_RATE        : ModConfig.NORMAL_SHINY_RATE;
                double expectedChunk  = isWeekend ? ModConfig.WEEKEND_POKEMON_PER_CHUNK  : ModConfig.NORMAL_POKEMON_PER_CHUNK;
                int    expectedSpawns = isWeekend ? ModConfig.WEEKEND_MAX_SPAWNS         : ModConfig.NORMAL_MAX_SPAWNS;
                double expectedExp    = isWeekend ? ModConfig.WEEKEND_EXP_MULTIPLIER     : ModConfig.NORMAL_EXP_MULTIPLIER;
                double expectedEgg    = isWeekend ? ModConfig.WEEKEND_LUCKY_EGG          : ModConfig.NORMAL_LUCKY_EGG;

                restartNeeded[0] |= sendConfigLine(source, content, "shinyRate",           expectedShiny,  "\"shinyRate\":\\s*([0-9.]+)");
                restartNeeded[0] |= sendConfigLine(source, content, "pokemonPerChunk",      expectedChunk,  "\"pokemonPerChunk\":\\s*([0-9.]+)");
                restartNeeded[0] |= sendConfigLine(source, content, "maximumSpawnsPerPass", expectedSpawns, "\"maximumSpawnsPerPass\":\\s*([0-9]+)");
                restartNeeded[0] |= sendConfigLine(source, content, "experienceMultiplier", expectedExp,    "\"experienceMultiplier\":\\s*([0-9.]+)");
                restartNeeded[0] |= sendConfigLine(source, content, "luckyEggMultiplier",   expectedEgg,    "\"luckyEggMultiplier\":\\s*([0-9.]+)");

            } catch (IOException e) {
                source.sendSuccess(() ->
                    Component.literal("  main.json: failed to read \u2718")
                        .withStyle(s -> s.withColor(0xFF5555)),
                    false
                );
                restartNeeded[0] = true;
            }
        }

        Path spawnerConfig = configDir.resolve("cobblemon/spawning/best-spawner-config.json");
        source.sendSuccess(() ->
            Component.literal("  best-spawner-config.json: ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Files.exists(spawnerConfig)
                    ? Component.literal("EXISTS \u2714").withStyle(s -> s.withColor(0x55FF55))
                    : Component.literal("NOT FOUND \u2718").withStyle(s -> s.withColor(0xFF5555).withBold(true))),
            false
        );

        source.sendSuccess(() -> Component.literal(""), false);

        if (restartNeeded[0]) {
            source.sendSuccess(() ->
                Component.literal("  \u26A0 Config mismatch detected!")
                    .withStyle(s -> s.withColor(0xFF5555).withBold(true)),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  The Cobblemon configs on disk do not match expected values.")
                    .withStyle(s -> s.withColor(0xFF5555)),
                false
            );
            source.sendSuccess(() ->
                Component.literal("  Please restart the server to apply the correct settings.")
                    .withStyle(s -> s.withColor(0xFF5555).withBold(true)),
                false
            );
        } else {
            source.sendSuccess(() ->
                Component.literal("  \u2714 All configs are correct!")
                    .withStyle(s -> s.withColor(0x55FF55).withBold(true)),
                false
            );
        }

        source.sendSuccess(() ->
            Component.literal("  ----- Commands -----")
                .withStyle(s -> s.withColor(0x777777)),
            false
        );
        source.sendSuccess(() ->
            Component.literal("  /wbadmin toggle | forceweekend | forceweekday | clearforce | reload | status")
                .withStyle(s -> s.withColor(0x777777)),
            false
        );
        source.sendSuccess(() -> Component.literal(""), false);
    }

    // Returns true if value is wrong (restart needed)
    private static boolean sendConfigLine(CommandSourceStack source, String content,
                                          String fieldName, double expected, String pattern) {
        return sendConfigLine(source, content, fieldName, String.valueOf(expected), pattern);
    }

    private static boolean sendConfigLine(CommandSourceStack source, String content,
                                          String fieldName, int expected, String pattern) {
        return sendConfigLine(source, content, fieldName, String.valueOf(expected), pattern);
    }

    private static boolean sendConfigLine(CommandSourceStack source, String content,
                                          String fieldName, String expected, String pattern) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(content);
        if (matcher.find()) {
            String actual   = matcher.group(1);
            boolean correct = actual.equals(expected);
            source.sendSuccess(() ->
                Component.literal("  " + fieldName + ": ")
                    .withStyle(s -> s.withColor(0xAAAAAA))
                    .append(Component.literal(actual + (correct ? " \u2714" : " \u2718 (expected " + expected + ")"))
                        .withStyle(s -> s.withColor(correct ? 0x55FF55 : 0xFF5555))),
                false
            );
            return !correct;
        } else {
            source.sendSuccess(() ->
                Component.literal("  " + fieldName + ": not found \u2718")
                    .withStyle(s -> s.withColor(0xFF5555)),
                false
            );
            return true;
        }
    }

    private static void broadcastToOps(CommandSourceStack source, Component message) {
        source.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (source.getServer().getPlayerList().isOp(p.getGameProfile())) {
                p.sendSystemMessage(message);
            }
        });
    }
}