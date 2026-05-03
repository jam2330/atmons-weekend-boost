package com.jam2330.weekendboost;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.nio.file.Path;
import java.util.Calendar;

public class WeekendBoostEvents {

    private boolean weekendBoostActive = false;
    private int ticksSinceLastAnnounce = 0;

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        WeekendBoost.LOGGER.info("Weekend Boost: onServerStarting fired!");
        Path configDir = Path.of("config");
        boolean isWeekend = isWeekend();
        weekendBoostActive = isWeekend;

        if (isWeekend) {
            WeekendBoost.LOGGER.info("Weekend Boost: It's the weekend! Applying boosted config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                WeekendBoostConfig.buildSpawnerConfig(
                    WeekendBoostConfig.WEEKEND_COMMON_WEIGHT,
                    WeekendBoostConfig.WEEKEND_UNCOMMON_WEIGHT,
                    WeekendBoostConfig.WEEKEND_RARE_WEIGHT,
                    WeekendBoostConfig.WEEKEND_ULTRA_RARE_WEIGHT
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.WEEKEND_SHINY_RATE,
                WeekendBoostConfig.WEEKEND_POKEMON_PER_CHUNK,
                WeekendBoostConfig.WEEKEND_MAX_SPAWNS,
                WeekendBoostConfig.WEEKEND_EXP_MULTIPLIER,
                WeekendBoostConfig.WEEKEND_LUCKY_EGG
            );
        } else {
            WeekendBoost.LOGGER.info("Weekend Boost: Weekday. Applying normal config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                WeekendBoostConfig.buildSpawnerConfig(
                    WeekendBoostConfig.NORMAL_COMMON_WEIGHT,
                    WeekendBoostConfig.NORMAL_UNCOMMON_WEIGHT,
                    WeekendBoostConfig.NORMAL_RARE_WEIGHT,
                    WeekendBoostConfig.NORMAL_ULTRA_RARE_WEIGHT
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.NORMAL_SHINY_RATE,
                WeekendBoostConfig.NORMAL_POKEMON_PER_CHUNK,
                WeekendBoostConfig.NORMAL_MAX_SPAWNS,
                WeekendBoostConfig.NORMAL_EXP_MULTIPLIER,
                WeekendBoostConfig.NORMAL_LUCKY_EGG
            );
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (weekendBoostActive) {
            sendBoostMessage(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long ticks = server.getTickCount();
        if (ticks % WeekendBoostConfig.CHECK_INTERVAL_TICKS != 0) return;

        boolean isWeekend = isWeekend();
        boolean wasActive = weekendBoostActive;
        weekendBoostActive = isWeekend;

        Path configDir = Path.of("config");

        if (isWeekend && !wasActive) {
            ConfigFileUtils.writeSpawnerConfig(configDir,
                WeekendBoostConfig.buildSpawnerConfig(
                    WeekendBoostConfig.WEEKEND_COMMON_WEIGHT,
                    WeekendBoostConfig.WEEKEND_UNCOMMON_WEIGHT,
                    WeekendBoostConfig.WEEKEND_RARE_WEIGHT,
                    WeekendBoostConfig.WEEKEND_ULTRA_RARE_WEIGHT
                )
            );
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

        if (isWeekend && wasActive) {
            ticksSinceLastAnnounce += WeekendBoostConfig.CHECK_INTERVAL_TICKS;
            if (ticksSinceLastAnnounce >= WeekendBoostConfig.ANNOUNCE_INTERVAL_TICKS) {
                broadcastBoostMessage(server);
                ticksSinceLastAnnounce = 0;
            }
        }

        if (!isWeekend && wasActive) {
            ConfigFileUtils.writeSpawnerConfig(configDir,
                WeekendBoostConfig.buildSpawnerConfig(
                    WeekendBoostConfig.NORMAL_COMMON_WEIGHT,
                    WeekendBoostConfig.NORMAL_UNCOMMON_WEIGHT,
                    WeekendBoostConfig.NORMAL_RARE_WEIGHT,
                    WeekendBoostConfig.NORMAL_ULTRA_RARE_WEIGHT
                )
            );
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.NORMAL_SHINY_RATE,
                WeekendBoostConfig.NORMAL_POKEMON_PER_CHUNK,
                WeekendBoostConfig.NORMAL_MAX_SPAWNS,
                WeekendBoostConfig.NORMAL_EXP_MULTIPLIER,
                WeekendBoostConfig.NORMAL_LUCKY_EGG
            );
            server.getPlayerList().getPlayers().forEach(p ->
                p.sendSystemMessage(
                    Component.literal("[ ")
                        .append(Component.literal("Weekend Boost")
                            .withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                        .append(Component.literal(" ] "))
                        .append(Component.literal(
                            "The weekend boost has ended. Normal rates resume on next restart."))
                )
            );
            ticksSinceLastAnnounce = 0;
        }
    }

    private boolean isWeekend() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    private void sendBoostMessage(net.minecraft.world.entity.player.Player player) {
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(
            Component.literal("  \u2728 ")
                .append(Component.literal("WEEKEND BOOST")
                    .withStyle(s -> s.withColor(0x55FFFF).withBold(true)))
                .append(Component.literal(" IS ACTIVE! \u2728")
                    .withStyle(s -> s.withColor(0xFFFF55)))
        );
        player.sendSystemMessage(
            Component.literal("  All Pok\u00e9mon spawn rates, shiny chances, and EXP")
                .withStyle(s -> s.withColor(0xAAAAAA))
        );
        player.sendSystemMessage(
            Component.literal("  boosted by a minimum ")
                .withStyle(s -> s.withColor(0xAAAAAA))
                .append(Component.literal("x2")
                    .withStyle(s -> s.withColor(0x55FF55).withBold(true)))
                .append(Component.literal(" this weekend. Good luck! \uD83C\uDF1F")
                    .withStyle(s -> s.withColor(0xAAAAAA)))
        );
        player.sendSystemMessage(Component.literal(""));
    }

    private void broadcastBoostMessage(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(this::sendBoostMessage);
    }
}
