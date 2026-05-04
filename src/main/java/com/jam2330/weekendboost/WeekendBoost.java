package com.jam2330.weekendboost;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WeekendBoost.MOD_ID)
public class WeekendBoost {

    public static final String MOD_ID = "weekendboost";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public WeekendBoost(IEventBus modEventBus) {
        modEventBus.addListener(this::setup);
        WeekendBoostEvents events = new WeekendBoostEvents();
        NeoForge.EVENT_BUS.register(events);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) ->
            WeekendBoostCommands.register(e.getDispatcher(), events)
        );
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("ATMons Weekend Boost mod loaded!");
    }
}