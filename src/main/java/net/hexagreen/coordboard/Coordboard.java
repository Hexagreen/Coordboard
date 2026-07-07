package net.hexagreen.coordboard;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import dev.simulated_team.simulated.Simulated;
import net.hexagreen.coordboard.clipboard.ClipboardNavigationTarget;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(Coordboard.MODID)
public class Coordboard {
    public static final String MODID = "coordboard";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Coordboard(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerEvent);
    }

    private void registerEvent(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            Simulated.getRegistrate().navTarget("clipboard", ClipboardNavigationTarget::new, AllBlocks.CLIPBOARD.asItem());
            LOGGER.info("Create Aeronautics Navigation Table Clipboard Integration Loaded.");
        }
    }
}
