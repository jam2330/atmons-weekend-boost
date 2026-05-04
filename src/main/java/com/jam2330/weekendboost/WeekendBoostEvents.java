package com.jam2330.weekendboost;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;

public class WeekendBoostEvents {

    private boolean weekendBoostActive = false;
    private int ticksSinceLastAnnounce = 0;
    private int weekdayTicksSinceLastAnnounce = 0;
    private boolean forceWeekend = false;
    private boolean forceWeekday = false;

    // ============================================================
    // Public accessors for commands
    // ============================================================
    public boolean isWeekendPublic()               { return weekendBoostActive; }
    public boolean isForcedPublic()                { return forceWeekend || forceWeekday; }
    public void setForceWeekend(boolean value)     { this.forceWeekend = value; }
    public void setForceWeekday(boolean value)     { this.forceWeekday = value; }
    public String getCountdownPublic()             { return getCountdownToWeekend(); }

    // ============================================================
    // Runs before server starts - writes spawner config
    // ============================================================
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        WeekendBoost.LOGGER.info("Weekend Boost: onServerStarting fired!");
        ModConfig.loadOrCreate();

        if (ModConfig.DISABLE_CATCH_RESTRICTIONS) {
            ConfigFileUtils.disableKubeJsScript("kubejs/startup_scripts/catch_restrictions.js");
        }
        if (ModConfig.DISABLE_MONS) {
            ConfigFileUtils.disableKubeJsScript("kubejs/server_scripts/Tweaks/disable_mons.js");
        }

        if (!ModConfig.WEEKEND_BOOST_ENABLED) {
            WeekendBoost.LOGGER.info("Weekend Boost: weekend_boost_enabled is false — skipping boost logic");
            return;
        }

        Path configDir = Path.of("config");
        boolean isWeekend = isWeekend();
        weekendBoostActive = isWeekend;

        if (isWeekend) {
            WeekendBoost.LOGGER.info("Weekend Boost: It's the weekend! Applying boosted spawner config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.WEEKEND_COMMON_WEIGHT,
                    ModConfig.WEEKEND_UNCOMMON_WEIGHT,
                    ModConfig.WEEKEND_RARE_WEIGHT,
                    ModConfig.WEEKEND_ULTRA_RARE_WEIGHT
                )
            );
        } else {
            WeekendBoost.LOGGER.info("Weekend Boost: Weekday. Applying normal spawner config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.NORMAL_COMMON_WEIGHT,
                    ModConfig.NORMAL_UNCOMMON_WEIGHT,
                    ModConfig.NORMAL_RARE_WEIGHT,
                    ModConfig.NORMAL_ULTRA_RARE_WEIGHT
                )
            );
        }
    }

    // ============================================================
    // Runs after server fully started - writes main.json
    // ============================================================
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!ModConfig.WEEKEND_BOOST_ENABLED) return;

        WeekendBoost.LOGGER.info("Weekend Boost: onServerStarted fired — applying main.json now");
        Path configDir = Path.of("config");
        boolean isWeekend = isWeekend();
        weekendBoostActive = isWeekend;

        if (isWeekend) {
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.WEEKEND_SHINY_RATE,
                ModConfig.WEEKEND_POKEMON_PER_CHUNK,
                ModConfig.WEEKEND_MAX_SPAWNS,
                ModConfig.WEEKEND_EXP_MULTIPLIER,
                ModConfig.WEEKEND_LUCKY_EGG
            );
        } else {
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.NORMAL_SHINY_RATE,
                ModConfig.NORMAL_POKEMON_PER_CHUNK,
                ModConfig.NORMAL_MAX_SPAWNS,
                ModConfig.NORMAL_EXP_MULTIPLIER,
                ModConfig.NORMAL_LUCKY_EGG
            );
        }

        logConfigCheck(configDir);
    }

    // ============================================================
    // Send announcement when player logs in
    // ============================================================
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModConfig.NOTIFICATIONS_ENABLED) return;

        net.minecraft.server.level.ServerPlayer serverPlayer =
            (net.minecraft.server.level.ServerPlayer) event.getEntity();

        serverPlayer.getServer().tell(
            new net.minecraft.server.TickTask(
                serverPlayer.getServer().getTickCount() + ModConfig.LOGIN_DELAY_TICKS,
                () -> {
                    if (weekendBoostActive) {
                        if (ModConfig.SHOW_MESSAGE) sendBoostMessage(serverPlayer, true);
                        if (ModConfig.SHOW_BANNER)  sendBoostTitle(serverPlayer);
                    } else {
                        if (ModConfig.SHOW_WEEKDAY_MESSAGE) sendNormalMessage(serverPlayer, true);
                        if (ModConfig.SHOW_WEEKDAY_BANNER)  sendNormalTitle(serverPlayer);
                    }
                }
            )
        );
    }

    // ============================================================
    // Tick loop - handles re-announcements and day switchovers
    // ============================================================
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.WEEKEND_BOOST_ENABLED) return;

        MinecraftServer server = event.getServer();
        long ticks = server.getTickCount();
        if (ticks % ModConfig.CHECK_INTERVAL_TICKS != 0) return;

        boolean isWeekend = isWeekend();
        boolean wasActive = weekendBoostActive;
        weekendBoostActive = isWeekend;

        Path configDir = Path.of("config");

        // Boost just turned ON (Friday -> Saturday)
        if (isWeekend && !wasActive) {
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.WEEKEND_COMMON_WEIGHT,
                    ModConfig.WEEKEND_UNCOMMON_WEIGHT,
                    ModConfig.WEEKEND_RARE_WEIGHT,
                    ModConfig.WEEKEND_ULTRA_RARE_WEIGHT
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.WEEKEND_SHINY_RATE,
                ModConfig.WEEKEND_POKEMON_PER_CHUNK,
                ModConfig.WEEKEND_MAX_SPAWNS,
                ModConfig.WEEKEND_EXP_MULTIPLIER,
                ModConfig.WEEKEND_LUCKY_EGG
            );
            if (ModConfig.NOTIFICATIONS_ENABLED && ModConfig.SHOW_MESSAGE) broadcastBoostMessage(server);
            ticksSinceLastAnnounce = 0;
            weekdayTicksSinceLastAnnounce = 0;
        }

        // Weekend active - re-announce every 6 hours with XP orb ding
        if (isWeekend && wasActive) {
            ticksSinceLastAnnounce += ModConfig.CHECK_INTERVAL_TICKS;
            if (ticksSinceLastAnnounce >= ModConfig.ANNOUNCE_INTERVAL_TICKS) {
                if (ModConfig.NOTIFICATIONS_ENABLED && ModConfig.SHOW_MESSAGE) broadcastBoostMessage(server);
                ticksSinceLastAnnounce = 0;
            }
        }

        // Weekday active - re-announce every 8 hours with toast sound
        if (!isWeekend && !wasActive) {
            weekdayTicksSinceLastAnnounce += ModConfig.CHECK_INTERVAL_TICKS;
            if (weekdayTicksSinceLastAnnounce >= ModConfig.WEEKDAY_ANNOUNCE_TICKS) {
                if (ModConfig.NOTIFICATIONS_ENABLED && ModConfig.SHOW_WEEKDAY_MESSAGE) {
                    server.getPlayerList().getPlayers().forEach(p -> sendNormalMessage(p, false));
                }
                weekdayTicksSinceLastAnnounce = 0;
            }
        }

        // Boost just turned OFF (Sunday -> Monday)
        if (!isWeekend && wasActive) {
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.NORMAL_COMMON_WEIGHT,
                    ModConfig.NORMAL_UNCOMMON_WEIGHT,
                    ModConfig.NORMAL_RARE_WEIGHT,
                    ModConfig.NORMAL_ULTRA_RARE_WEIGHT
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.NORMAL_SHINY_RATE,
                ModConfig.NORMAL_POKEMON_PER_CHUNK,
                ModConfig.NORMAL_MAX_SPAWNS,
                ModConfig.NORMAL_EXP_MULTIPLIER,
                ModConfig.NORMAL_LUCKY_EGG
            );
            if (ModConfig.NOTIFICATIONS_ENABLED) {
                server.getPlayerList().getPlayers().forEach(p -> {
                    p.sendSystemMessage(Component.literal(""));
                    p.sendSystemMessage(
                        Component.literal("  \uD83C\uDF19 ")
                            .append(Component.literal("Weekend Boost has ended.")
                                .withStyle(s -> s.withColor(0xAAAAAA).withBold(true)))
                    );
                    p.sendSystemMessage(
                        Component.literal("  Normal rates are now active. See you next weekend! \uD83C\uDF19")
                            .withStyle(s -> s.withColor(0x777777))
                    );
                    p.sendSystemMessage(Component.literal(""));
                    p.playNotifySound(
                        net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,
                        net.minecraft.sounds.SoundSource.MASTER,
                        1.0f, 1.0f
                    );
                });
            }
            ticksSinceLastAnnounce = 0;
            weekdayTicksSinceLastAnnounce = 0;
        }
    }

    // ============================================================
    // Helpers
    // ============================================================
    private boolean isWeekend() {
        if (forceWeekend) return true;
        if (forceWeekday) return false;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    String getCountdownToWeekend() {
        Calendar now  = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        int day = now.get(Calendar.DAY_OF_WEEK);

        int daysUntilSaturday = (Calendar.SATURDAY - day + 7) % 7;
        if (daysUntilSaturday == 0) daysUntilSaturday = 7;

        next.add(Calendar.DAY_OF_YEAR, daysUntilSaturday);
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);

        long diffMs  = next.getTimeInMillis() - now.getTimeInMillis();
        long hours   = diffMs / (1000 * 60 * 60);
        long minutes = (diffMs % (1000 * 60 * 60)) / (1000 * 60);

        if (hours >= 24) {
            long days = hours / 24;
            hours = hours % 24;
            return days + "d " + hours + "h " + minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }

    private void sendBoostMessage(net.minecraft.world.entity.player.Player player, boolean isLogin) {
        String line1 = ModConfig.CUSTOM_WEEKEND_LINE1.isEmpty()
            ? "  All Pok\u00e9mon spawn rates, shiny chances, and EXP"
            : "  " + ModConfig.CUSTOM_WEEKEND_LINE1;
        String line2 = ModConfig.CUSTOM_WEEKEND_LINE2.isEmpty()
            ? null
            : "  " + ModConfig.CUSTOM_WEEKEND_LINE2;

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(
            Component.literal("  \u2728 ")
                .append(Component.literal("WEEKEND BOOST")
                    .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                .append(Component.literal(" IS ACTIVE! \u2728")
                    .withStyle(s -> s.withColor(0xFFFF55)))
        );
        player.sendSystemMessage(
            Component.literal(line1).withStyle(s -> s.withColor(0xAAAAAA))
        );
        if (line2 != null) {
            player.sendSystemMessage(
                Component.literal(line2).withStyle(s -> s.withColor(0xAAAAAA))
            );
        } else {
            player.sendSystemMessage(
                Component.literal("  boosted by a minimum ")
                    .withStyle(s -> s.withColor(0xAAAAAA))
                    .append(Component.literal("x2").withStyle(s -> s.withColor(0x55FF55).withBold(true)))
                    .append(Component.literal(" this weekend. Good luck! \uD83C\uDF1F")
                        .withStyle(s -> s.withColor(0xAAAAAA)))
            );
        }
        player.sendSystemMessage(Component.literal(""));

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(
                isLogin
                    ? net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP
                    : net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                net.minecraft.sounds.SoundSource.MASTER,
                1.0f, 1.0f
            );
        }
    }

    private void sendNormalMessage(net.minecraft.world.entity.player.Player player, boolean isLogin) {
        String countdown = getCountdownToWeekend();
        String line1 = ModConfig.CUSTOM_WEEKDAY_LINE1.isEmpty()
            ? null
            : "  " + ModConfig.CUSTOM_WEEKDAY_LINE1 + " " + countdown;
        String line2 = ModConfig.CUSTOM_WEEKDAY_LINE2.isEmpty()
            ? null
            : "  " + ModConfig.CUSTOM_WEEKDAY_LINE2;

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(
            Component.literal("  \uD83C\uDF19 ")
                .append(Component.literal("NO WEEKEND BOOST")
                    .withStyle(s -> s.withColor(0xAAAAAA).withBold(true)))
                .append(Component.literal(" \uD83C\uDF19")
                    .withStyle(s -> s.withColor(0xAAAAAA)))
        );

        if (line1 != null) {
            player.sendSystemMessage(
                Component.literal(line1).withStyle(s -> s.withColor(0x777777))
            );
        } else {
            player.sendSystemMessage(
                Component.literal("  Boosted rates are active on ")
                    .withStyle(s -> s.withColor(0x777777))
                    .append(Component.literal("Saturdays & Sundays")
                        .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                    .append(Component.literal(". Next boost in: ")
                        .withStyle(s -> s.withColor(0x777777)))
                    .append(Component.literal(countdown)
                        .withStyle(s -> s.withColor(0x55FF55).withBold(true)))
            );
        }

        if (line2 != null) {
            player.sendSystemMessage(
                Component.literal(line2).withStyle(s -> s.withColor(0x777777))
            );
        }
        player.sendSystemMessage(Component.literal(""));

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.playNotifySound(
                isLogin
                    ? net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value()
                    : net.minecraft.sounds.SoundEvents.UI_TOAST_IN,
                net.minecraft.sounds.SoundSource.MASTER,
                1.0f, 1.0f
            );
        }
    }

    private void sendBoostTitle(net.minecraft.server.level.ServerPlayer player) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
            Component.literal("\u2728 WEEKEND BOOST \u2728")
                .withStyle(s -> s.withColor(0x55FFFF).withBold(true))
        ));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
            Component.literal("Boosted spawns, shinies & EXP all weekend!")
                .withStyle(s -> s.withColor(0xFFFF55))
        ));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(
            10, 60, 20
        ));
    }

    private void sendNormalTitle(net.minecraft.server.level.ServerPlayer player) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
            Component.literal("\uD83C\uDF19 No Boost Active \uD83C\uDF19")
                .withStyle(s -> s.withColor(0xAAAAAA).withBold(true))
        ));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
            Component.literal("Next boost in: " + getCountdownToWeekend())
                .withStyle(s -> s.withColor(0x55FFFF))
        ));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(
            10, 60, 20
        ));
    }

    private void broadcastBoostMessage(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(p -> sendBoostMessage(p, false));
    }

    private void logConfigCheck(Path configDir) {
        WeekendBoost.LOGGER.info("=============================================================");
        WeekendBoost.LOGGER.info("  ATMons Weekend Boost — Config Verification");
        WeekendBoost.LOGGER.info("=============================================================");
        WeekendBoost.LOGGER.info("  Mode: {}", weekendBoostActive ? "WEEKEND (boosted)" : "WEEKDAY (normal)");
        WeekendBoost.LOGGER.info("  Weekend Boost Enabled: {}", ModConfig.WEEKEND_BOOST_ENABLED);
        WeekendBoost.LOGGER.info("  Notifications Enabled: {}", ModConfig.NOTIFICATIONS_ENABLED);
        WeekendBoost.LOGGER.info("-------------------------------------------------------------");

        Path mainConfig = configDir.resolve("cobblemon/main.json");
        if (!Files.exists(mainConfig)) {
            WeekendBoost.LOGGER.warn("  main.json: NOT FOUND at {}", mainConfig.toAbsolutePath());
        } else {
            try {
                String content = Files.readString(mainConfig, StandardCharsets.UTF_8);
                double expectedShiny  = weekendBoostActive ? ModConfig.WEEKEND_SHINY_RATE        : ModConfig.NORMAL_SHINY_RATE;
                double expectedChunk  = weekendBoostActive ? ModConfig.WEEKEND_POKEMON_PER_CHUNK  : ModConfig.NORMAL_POKEMON_PER_CHUNK;
                int    expectedSpawns = weekendBoostActive ? ModConfig.WEEKEND_MAX_SPAWNS         : ModConfig.NORMAL_MAX_SPAWNS;
                double expectedExp    = weekendBoostActive ? ModConfig.WEEKEND_EXP_MULTIPLIER     : ModConfig.NORMAL_EXP_MULTIPLIER;
                double expectedEgg    = weekendBoostActive ? ModConfig.WEEKEND_LUCKY_EGG          : ModConfig.NORMAL_LUCKY_EGG;

                logCheckValue(content, "shinyRate",           String.valueOf(expectedShiny),  "\"shinyRate\":\\s*([0-9.]+)");
                logCheckValue(content, "pokemonPerChunk",      String.valueOf(expectedChunk),  "\"pokemonPerChunk\":\\s*([0-9.]+)");
                logCheckValue(content, "maximumSpawnsPerPass", String.valueOf(expectedSpawns), "\"maximumSpawnsPerPass\":\\s*([0-9]+)");
                logCheckValue(content, "experienceMultiplier", String.valueOf(expectedExp),    "\"experienceMultiplier\":\\s*([0-9.]+)");
                logCheckValue(content, "luckyEggMultiplier",   String.valueOf(expectedEgg),    "\"luckyEggMultiplier\":\\s*([0-9.]+)");
            } catch (IOException e) {
                WeekendBoost.LOGGER.error("  main.json: Failed to read for verification", e);
            }
        }

        Path spawnerConfig = configDir.resolve("cobblemon/spawning/best-spawner-config.json");
        WeekendBoost.LOGGER.info("  best-spawner-config.json: {}",
            Files.exists(spawnerConfig) ? "EXISTS ✔" : "NOT FOUND ✘");
        WeekendBoost.LOGGER.info("=============================================================");
    }

    private void logCheckValue(String content, String fieldName, String expected, String pattern) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(content);
        if (matcher.find()) {
            String actual = matcher.group(1);
            if (actual.equals(expected)) {
                WeekendBoost.LOGGER.info("  {} = {} ✔", fieldName, actual);
            } else {
                WeekendBoost.LOGGER.warn("  {} = {} ✘ (expected {})", fieldName, actual, expected);
            }
        } else {
            WeekendBoost.LOGGER.warn("  {}: field not found in main.json ✘", fieldName);
        }
    }
}