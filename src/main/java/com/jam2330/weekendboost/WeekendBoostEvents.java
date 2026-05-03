package com.jam2330.weekendboost;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
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
            WeekendBoost.LOGGER.info("Weekend Boost: It's the weekend! Applying boosted spawner config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                WeekendBoostConfig.buildSpawnerConfig(
                    WeekendBoostConfig.WEEKEND_COMMON_WEIGHT,
                    WeekendBoostConfig.WEEKEND_UNCOMMON_WEIGHT,
                    WeekendBoostConfig.WEEKEND_RARE_WEIGHT,
                    WeekendBoostConfig.WEEKEND_ULTRA_RARE_WEIGHT
                )
            );
        } else {
            WeekendBoost.LOGGER.info("Weekend Boost: Weekday. Applying normal spawner config...");
            ConfigFileUtils.writeSpawnerConfig(configDir,
                WeekendBoostConfig.buildSpawnerConfig(
                    WeekendBoostConfig.NORMAL_COMMON_WEIGHT,
                    WeekendBoostConfig.NORMAL_UNCOMMON_WEIGHT,
                    WeekendBoostConfig.NORMAL_RARE_WEIGHT,
                    WeekendBoostConfig.NORMAL_ULTRA_RARE_WEIGHT
                )
            );
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        WeekendBoost.LOGGER.info("Weekend Boost: onServerStarted fired — applying main.json now");
        Path configDir = Path.of("config");
        boolean isWeekend = isWeekend();
        weekendBoostActive = isWeekend;

        if (isWeekend) {
            ConfigFileUtils.updateMainConfig(configDir,
                WeekendBoostConfig.WEEKEND_SHINY_RATE,
                WeekendBoostConfig.WEEKEND_POKEMON_PER_CHUNK,
                WeekendBoostConfig.WEEKEND_MAX_SPAWNS,
                WeekendBoostConfig.WEEKEND_EXP_MULTIPLIER,
                WeekendBoostConfig.WEEKEND_LUCKY_EGG
            );
        } else {
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
            net.minecraft.server.level.ServerPlayer serverPlayer =
                (net.minecraft.server.level.ServerPlayer) event.getEntity();
            serverPlayer.getServer().tell(
                new net.minecraft.server.TickTask(
                    serverPlayer.getServer().getTickCount() + 100,
                    () -> {
                        sendBoostMessage(serverPlayer);
                        sendBoostTitle(serverPlayer);
                    }
                )
            );
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
                .append(Component.literal("x2").withStyle(s -> s.withColor(0x55FF55).withBold(true)))
                .append(Component.literal(" this weekend. Good luck! \uD83C\uDF1F")
                    .withStyle(s -> s.withColor(0xAAAAAA)))
        );
        player.sendSystemMessage(Component.literal(""));

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.Holder.direct(net.minecraft.sounds.SoundEvent.createVariableRangeEvent(
                    new net.minecraft.resources.ResourceLocation("minecraft", "entity.experience_orb.pickup")
                )),
                net.minecraft.sounds.SoundSource.MASTER,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                1.0f, 1.0f, 0L
            ));
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

    private void broadcastBoostMessage(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(this::sendBoostMessage);
    }
}