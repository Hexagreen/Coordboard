package net.hexagreen.coordboard.cadr_journeymap;

import io.github.ildimas.createdelivery.network.ClientboundContractWaypointPayload.Action;
import io.github.ildimas.createdelivery.network.ClientboundContractWaypointPayload.WaypointKind;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.hexagreen.coordboard.Coordboard;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

@JourneyMapPlugin(apiVersion = "2.0.0")
public final class JourneyMapContractWaypoints implements IClientPlugin {

    private static final String MOD_ID = "coordboard";

    private static final String CONTRACT_PREFIX = "Delivery: ";
    private static final String MARKET_PICKUP_PREFIX = "Pickup: ";
    private static final String PLAYER_MARKET_SUPPLY_PREFIX = "Supply: ";
    private static final String P2P_REQUEST_PREFIX = "P2P: ";
    private static final String P2P_PICKUP_PREFIX = "P2P Buy: ";

    private static volatile JourneyMapContractWaypoints INSTANCE;

    private IClientAPI jmAPI;

    private static final List<PendingUpdate> PENDING_UPDATES = new ArrayList<>();

    public JourneyMapContractWaypoints() {
        INSTANCE = this;
    }

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void initialize(IClientAPI jmAPI) {
        Coordboard.LOGGER.info("Journey Map Contract Waypoint provider initialized.");
        this.jmAPI = jmAPI;
        flushPending();
    }

    public static void updateWaypoint(Action action, WaypointKind kind, int id, String name, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isSameThread()) {
            minecraft.execute(() -> updateWaypoint(action, kind, id, name, pos));
            return;
        }

        JourneyMapContractWaypoints instance = INSTANCE;
        if (instance == null || instance.jmAPI == null) {
            PENDING_UPDATES.add(new PendingUpdate(action, kind, id, name, pos));
            return;
        }

        instance.applyUpdate(action, kind, id, name, pos);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        JourneyMapContractWaypoints instance = INSTANCE;
        if (instance == null || instance.jmAPI == null || PENDING_UPDATES.isEmpty()) {
            return;
        }
        if (Minecraft.getInstance().level == null) {
            return;
        }
        instance.flushPending();
    }

    private void flushPending() {
        if (PENDING_UPDATES.isEmpty()) {
            return;
        }
        for (PendingUpdate update : List.copyOf(PENDING_UPDATES)) {
            applyUpdate(update.action(), update.kind(), update.id(), update.name(), update.pos());
        }
        PENDING_UPDATES.clear();
    }

    private void applyUpdate(Action action, WaypointKind kind, int id, String name, BlockPos pos) {
        if (action == Action.CLEAR_OWNED) {
            removeOwnedWaypoints();
        } else if (action == Action.REMOVE) {
            removeWaypoint(waypointName(kind, id, name));
        } else {
            removeWaypoint(waypointName(kind, id, name));
            addWaypoint(kind, id, name, pos);
        }
    }

    private void addWaypoint(WaypointKind kind, int id, String name, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        ResourceKey<Level> dimension = minecraft.level.dimension();
        String displayName = waypointName(kind, id, name);
        
        Waypoint waypoint = WaypointFactory.createWaypoint(
            MOD_ID,
            pos,
            displayName,
            dimension,
            false
        );
        waypoint.setColor(color(kind));

        try {
            jmAPI.addWaypoint(MOD_ID, waypoint);
        } catch (Exception e) {
            Coordboard.LOGGER.warn("Failed to add JourneyMap waypoint {}", name, e);
        }
    }

    private void removeWaypoint(String name) {
        try {
            jmAPI.getWaypoints(MOD_ID).stream()
                .filter(
                    w -> w.getName().equals(name)
                )
                .findFirst()
                .ifPresent(
                    waypoint -> jmAPI.removeWaypoint(MOD_ID, waypoint)
                );
        } catch (Exception e) {
            Coordboard.LOGGER.warn("Failed to remove JourneyMap waypoint {}", name, e);
        }
    }

    private void removeOwnedWaypoints() {
        try {
            jmAPI.removeAllWaypoints(MOD_ID);
        } catch (Exception e) {
            Coordboard.LOGGER.warn("Failed to remove JourneyMap waypoint during clear", e);
        }
    }

    private static String waypointName(WaypointKind kind, int id, String name) {
        return prefix(kind) + name + " #" + id;
    }

    private static String prefix(WaypointKind kind) {
        return switch (kind) {
            case MARKET_PICKUP -> MARKET_PICKUP_PREFIX;
            case PLAYER_MARKET_SUPPLY -> PLAYER_MARKET_SUPPLY_PREFIX;
            case CONTRACT -> CONTRACT_PREFIX;
            case P2P_REQUEST -> P2P_REQUEST_PREFIX;
            case P2P_PICKUP -> P2P_PICKUP_PREFIX;
        };
    }

    private static int color(WaypointKind kind) {
        return switch (kind) {
            case MARKET_PICKUP -> 5636095;
            case PLAYER_MARKET_SUPPLY -> 16733695;
            case CONTRACT -> 16755200;
            case P2P_REQUEST, P2P_PICKUP -> 11184810;
        };
    }

    private record PendingUpdate(Action action, WaypointKind kind, int id, String name, BlockPos pos) {
    }
}