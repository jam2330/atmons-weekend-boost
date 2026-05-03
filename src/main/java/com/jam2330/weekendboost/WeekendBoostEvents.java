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
    private int weekdayTicksSinceLastAnnounce = 0;

    // ============================================================
    // Runs before server starts - writes spawner config
    // ============================================================
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        WeekendBoost.LOGGER.info("Weekend Boost: onServerStarting fired!");
        ModConfig.loadOrCreate();
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
    }

    // ============================================================
    // Send announcement when player logs in (delayed 5 seconds)
    // ============================================================
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        net.minecraft.server.level.ServerPlayer serverPlayer =
            (net.minecraft.server.level.ServerPlayer) event.getEntity();
        serverPlayer.getServer().tell(
            new net.minecraft.server.TickTask(
                serverPlayer.getServer().getTickCount() + 100,
                () -> {
                    if (weekendBoostActive) {
                        sendBoostMessage(serverPlayer, true);
                        sendBoostTitle(serverPlayer);
                    } else {
                        sendNormalMessage(serverPlayer, true);
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
            broadcastBoostMessage(server);
            ticksSinceLastAnnounce = 0;
            weekdayTicksSinceLastAnnounce = 0;
        }

        // Weekend active - re-announce every 6 hours with XP orb ding
        if (isWeekend && wasActive) {
            ticksSinceLastAnnounce += ModConfig.CHECK_INTERVAL_TICKS;
            if (ticksSinceLastAnnounce >= ModConfig.ANNOUNCE_INTERVAL_TICKS) {
                broadcastBoostMessage(server);
                ticksSinceLastAnnounce = 0;
            }
        }

        // Weekday active - re-announce every 8 hours with toast sound, no ding
        if (!isWeekend && wasActive == false) {
            weekdayTicksSinceLastAnnounce += ModConfig.CHECK_INTERVAL_TICKS;
            if (weekdayTicksSinceLastAnnounce >= ModConfig.WEEKDAY_ANNOUNCE_TICKS) {
                server.getPlayerList().getPlayers().forEach(p -> sendNormalMessage(p, false));
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
            server.getPlayerList().getPlayers().forEach(p -> {
                p.sendSystemMessage(Component.literal(""));
                p.sendSystemMessage(
                    Component.literal("  [ ")
                        .append(Component.literal("Weekend Boost")
                            .withStyle(s -> s.withColor(0xFFAA00).withBold(true)))
                        .append(Component.literal(" ] "))
                        .append(Component.literal(
                            "The weekend boost has ended. Normal rates resume on next restart.")
                            .withStyle(s -> s.withColor(0xAAAAAA)))
                );
                p.sendSystemMessage(Component.literal(""));
                p.playNotifySound(
                    net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    net.minecraft.sounds.SoundSource.MASTER,
                    1.0f, 1.0f
                );
            });
            ticksSinceLastAnnounce = 0;
            weekdayTicksSinceLastAnnounce = 0;
        }
    }

    // ============================================================
    // Helpers
    // ============================================================
    private boolean isWeekend() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    private String getCountdownToWeekend() {
        Calendar now = Calendar.getInstance();
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

    // Weekend message - isLogin true = levelup sound, false = XP orb sound
    private void sendBoostMessage(net.minecraft.world.entity.player.Player player, boolean isLogin) {
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
            serverPlayer.playNotifySound(
                isLogin
                    ? net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP
                    : net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                net.minecraft.sounds.SoundSource.MASTER,
                1.0f, 1.0f
            );
        }
    }

    // Weekday message - isLogin true = pling sound, false = toast sound
    private void sendNormalMessage(net.minecraft.world.entity.player.Player player, boolean isLogin) {
        String countdown = getCountdownToWeekend();
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(
            Component.literal("  \uD83C\uDF19 ")
                .append(Component.literal("NO WEEKEND BOOST")
                    .withStyle(s -> s.withColor(0xAAAAAA).withBold(true)))
                .append(Component.literal(" \uD83C\uDF19")
                    .withStyle(s -> s.withColor(0xAAAAAA)))
        );
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

    private void broadcastBoostMessage(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(p -> sendBoostMessage(p, false));
    }
}