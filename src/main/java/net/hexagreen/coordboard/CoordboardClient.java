package net.hexagreen.coordboard;

import net.hexagreen.coordboard.cadr_journeymap.JourneyMapContractWaypoints;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Coordboard.MODID, dist = {Dist.CLIENT})
public class CoordboardClient {
    public CoordboardClient(IEventBus modEventBus, ModContainer container) {
        if (ModList.get().isLoaded("journeymap")) {
            NeoForge.EVENT_BUS.addListener(JourneyMapContractWaypoints::onClientTick);
        }

    }
}
