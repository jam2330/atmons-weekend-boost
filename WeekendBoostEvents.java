package com.jam2330.weekendboost;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.nio.file.Path;
import java.util.Calendar;

public class WeekendBoostEvents {

    private boolean weekendBoostActive = false;
    private int ticksSinceLastAnnounce = 0;
    private boolean firstCheck = true;

    // ============================================================
    // Runs BEFORE server starts - writes configs before Cobblemon loads
    // ============================================================
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        Path configDir = Path.of("config");
        boolean isWeekend = isWeekend();

        if (isWeekend) {
            WeekendBoost.LOGGER.info("Weekend Boost: It's the weekend! Applying boosted config...");
            ConfigFileUtils.writeSpawnerConfig(configDir, WeekendBoostConfig.WEEKEND_SPAWNER_CONFIG);
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.WEEKEND_SHINY_RATE,
                WeekendBoostConfig.WEEKEND_POKEMON_PER_CHUNK,
                WeekendBoostConfig.WEEKEND_MAX_SPAWNS,
                WeekendBoostConfig.WEEKEND_EXP_MULTIPLIER,
                WeekendBoostConfig.WEEKEND_LUCKY_EGG
            );
        } else {
            WeekendBoost.LOGGER.info("Weekend Boost: It's a weekday. Applying normal config...");
            ConfigFileUtils.writeSpawnerConfig(configDir, WeekendBoostConfig.WEEKDAY_SPAWNER_CONFIG);
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.NORMAL_SHINY_RATE,
                WeekendBoostConfig.NORMAL_POKEMON_PER_CHUNK,
                WeekendBoostConfig.NORMAL_MAX_SPAWNS,
                WeekendBoostConfig.NORMAL_EXP_MULTIPLIER,
                WeekendBoostConfig.NORMAL_LUCKY_EGG
            );
        }

        weekendBoostActive = isWeekend;
    }

    // ============================================================
    // Runs after server starts - send initial announcement
    // ============================================================
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (weekendBoostActive) {
            broadcastBoostMessage(event.getServer());
        }
    }

    // ============================================================
    // Tick loop - handles announcements and Monday switchover
    // ============================================================
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long ticks = server.getTickCount();

        if (ticks % WeekendBoostConfig.CHECK_INTERVAL_TICKS != 0) return;

        boolean isWeekend = isWeekend();
        boolean wasActive = weekendBoostActive;
        weekendBoostActive = isWeekend;

        // Skip the very first check since onServerStarted handles it
        if (firstCheck) {
            firstCheck = false;
            return;
        }

        // Boost just turned ON (Friday -> Saturday)
        if (isWeekend && !wasActive) {
            Path configDir = Path.of("config");
            ConfigFileUtils.writeSpawnerConfig(configDir, WeekendBoostConfig.WEEKEND_SPAWNER_CONFIG);
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.WEEKEND_SHINY_RATE,
                WeekendBoostConfig.WEEKEND_POKEMON_PER_CHUNK,
                WeekendBoostConfig.WEEKEND_MAX_SPAWNS,
                WeekendBoostConfig.WEEKEND_EXP_MULTIPLIER,
                WeekendBoostConfig.WEEKEND_LUCKY_EGG
            );
            broadcastBoostMessage(server);
            ticksSinceLastAnnounce = 0;
        }

        // Boost active - re-announce every 6 hours
        if (isWeekend && wasActive) {
            ticksSinceLastAnnounce += WeekendBoostConfig.CHECK_INTERVAL_TICKS;
            if (ticksSinceLastAnnounce >= WeekendBoostConfig.ANNOUNCE_INTERVAL_TICKS) {
                broadcastBoostMessage(server);
                ticksSinceLastAnnounce = 0;
            }
        }

        // Boost just turned OFF (Sunday -> Monday)
        if (!isWeekend && wasActive) {
            Path configDir = Path.of("config");
            ConfigFileUtils.writeSpawnerConfig(configDir, WeekendBoostConfig.WEEKDAY_SPAWNER_CONFIG);
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.NORMAL_SHINY_RATE,
                WeekendBoostConfig.NORMAL_POKEMON_PER_CHUNK,
                WeekendBoostConfig.NORMAL_MAX_SPAWNS,
                WeekendBoostConfig.NORMAL_EXP_MULTIPLIER,
                WeekendBoostConfig.NORMAL_LUCKY_EGG
            );
            server.sendSystemMessage(
                Component.literal("[ ")
                    .append(Component.literal("Weekend Boost").withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                    .append(Component.literal(" ] "))
                    .append(Component.literal("The weekend boost has ended. Normal rates resume on next restart."))
            );
            ticksSinceLastAnnounce = 0;
        }
    }

    // ============================================================
    // Helpers
    // ============================================================
    private boolean isWeekend() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    private void broadcastBoostMessage(MinecraftServer server) {
        server.sendSystemMessage(Component.literal(""));
        server.sendSystemMessage(
            Component.literal("  \u2728 ")
                .append(Component.literal("WEEKEND BOOST").withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                .append(Component.literal(" IS ACTIVE! \u2728").withStyle(s -> s.withColor(0xFFFF55)))
        );
        server.sendSystemMessage(
            Component.literal("  All Pok\u00e9mon spawn rates, shiny chances, and EXP")
                .withStyle(s -> s.withColor(0xAAAAAA))
        );
        server.sendSystemMessage(
            Component.literal("  boosted by a minimum ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Component.literal("x2").withStyle(s -> s.withColor(0x55FF55).withBold(true)))
                .append(Component.literal(" this weekend. Good luck! \uD83C\uDF1F").withStyle(s -> s.withColor(0xAAAAAA)))
        );
        server.sendSystemMessage(Component.literal(""));
    }
}
