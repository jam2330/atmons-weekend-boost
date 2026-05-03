package com.jam2330.weekendboost;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.nio.file.Path;
import java.util.Calendar;

public class WeekendBoostEvents {

    private boolean weekendBoostActive = false;
    private int ticksSinceLastAnnounce = 0;

    // ============================================================
    // Runs BEFORE server starts - loads config then writes cobblemon configs
    // ============================================================
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        ModConfig.load();

        Path configDir = Path.of("config");
        boolean isWeekend = isWeekend();

        if (isWeekend) {
            WeekendBoost.LOGGER.info("Weekend Boost: It's the weekend! Applying boosted config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.weekendCommonWeight,
                    ModConfig.weekendUncommonWeight,
                    ModConfig.weekendRareWeight,
                    ModConfig.weekendUltraRareWeight
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.weekendShinyRate,
                ModConfig.weekendPokemonPerChunk,
                ModConfig.weekendMaxSpawns,
                ModConfig.weekendExpMultiplier,
                ModConfig.weekendLuckyEgg
            );
        } else {
            WeekendBoost.LOGGER.info("Weekend Boost: It's a weekday. Applying normal config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.normalCommonWeight,
                    ModConfig.normalUncommonWeight,
                    ModConfig.normalRareWeight,
                    ModConfig.normalUltraRareWeight
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.normalShinyRate,
                ModConfig.normalPokemonPerChunk,
                ModConfig.normalMaxSpawns,
                ModConfig.normalExpMultiplier,
                ModConfig.normalLuckyEgg
            );
        }

        weekendBoostActive = isWeekend;
    }

    // ============================================================
    // Send announcement when player logs in
    // ============================================================
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (weekendBoostActive) {
            sendBoostMessage(event.getEntity());
        }
    }

    // ============================================================
    // Tick loop - handles re-announcements and Monday switchover
    // ============================================================
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long ticks = server.getTickCount();

        if (ticks % ModConfig.CHECK_INTERVAL_TICKS != 0) return;

        boolean isWeekend = isWeekend();
        boolean wasActive = weekendBoostActive;
        weekendBoostActive = isWeekend;

        // Boost just turned ON (Friday -> Saturday)
        if (isWeekend && !wasActive) {
            Path configDir = Path.of("config");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.weekendCommonWeight,
                    ModConfig.weekendUncommonWeight,
                    ModConfig.weekendRareWeight,
                    ModConfig.weekendUltraRareWeight
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.weekendShinyRate,
                ModConfig.weekendPokemonPerChunk,
                ModConfig.weekendMaxSpawns,
                ModConfig.weekendExpMultiplier,
                ModConfig.weekendLuckyEgg
            );
            broadcastBoostMessage(server);
            ticksSinceLastAnnounce = 0;
        }

        // Boost active - re-announce every 6 hours
        if (isWeekend && wasActive) {
            ticksSinceLastAnnounce += ModConfig.CHECK_INTERVAL_TICKS;
            if (ticksSinceLastAnnounce >= ModConfig.ANNOUNCE_INTERVAL_TICKS) {
                broadcastBoostMessage(server);
                ticksSinceLastAnnounce = 0;
            }
        }

        // Boost just turned OFF (Sunday -> Monday)
        if (!isWeekend && wasActive) {
            Path configDir = Path.of("config");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                ModConfig.buildSpawnerConfig(
                    ModConfig.normalCommonWeight,
                    ModConfig.normalUncommonWeight,
                    ModConfig.normalRareWeight,
                    ModConfig.normalUltraRareWeight
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                ModConfig.normalShinyRate,
                ModConfig.normalPokemonPerChunk,
                ModConfig.normalMaxSpawns,
                ModConfig.normalExpMultiplier,
                ModConfig.normalLuckyEgg
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

    private void sendBoostMessage(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(
            Component.literal("  \u2728 ")
                .append(Component.literal("WEEKEND BOOST").withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                .append(Component.literal(" IS ACTIVE! \u2728").withStyle(s -> s.withColor(0xFFFF55)))
        );
        player.sendSystemMessage(
            Component.literal("  All Pok\u00e9mon spawn rates, shiny chances, and EXP")
                .withStyle(s -> s.withColor(0xAAAAAA))
        );
        player.sendSystemMessage(
            Component.literal("  boosted by a minimum ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Component.literal("x2").withStyle(s -> s.withColor(0x55FF55).withBold(true)))
                .append(Component.literal(" this weekend. Good luck! \uD83C\uDF1F").withStyle(s -> s.withColor(0xAAAAAA)))
        );
        player.sendSystemMessage(Component.literal(""));
    }

    private void broadcastBoostMessage(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(this::sendBoostMessage);
    }
}
