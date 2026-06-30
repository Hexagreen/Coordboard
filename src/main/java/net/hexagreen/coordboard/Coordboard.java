package net.hexagreen.coordboard;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import dev.simulated_team.simulated.Simulated;
import net.hexagreen.coordboard.clipboard.ClipboardNavigationTarget;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Coordboard.MODID)
public class Coordboard {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "coordboard";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Coordboard(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerEvent);
        modEventBus.addListener(this::commonSetup);
    }

    private void registerEvent(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            Simulated.getRegistrate().navTarget("clipboard", ClipboardNavigationTarget::new, AllBlocks.CLIPBOARD.asItem());
            LOGGER.info("Create Aeronautics Navigation Table Clipboard Integration Loaded.");
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("createdeliveryrequired")) {
            LOGGER.info("CA: Delivery Required detected. Transaction Receipt Navigation available.");
        }
    }
}
