package net.hexagreen.coordboard.mixin;

import io.github.ildimas.createdelivery.CreateDeliveryRequired;
import io.github.ildimas.createdelivery.network.ClientboundContractWaypointPayload;
import io.github.ildimas.createdelivery.network.ModNetworking;
import net.hexagreen.coordboard.cadr_journeymap.JourneyMapContractWaypoints;
import net.minecraft.core.BlockPos;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModNetworking.class)
public abstract class ModNetworkingMixin {
    @Inject(method = "updateWaypoint", at = @At("HEAD"), cancellable = true)
    private static void coordboard$updateWaypoint(ClientboundContractWaypointPayload.Action action, ClientboundContractWaypointPayload.WaypointKind kind, int id, String name, BlockPos pos, CallbackInfo ci) {
        if(FMLEnvironment.dist.isClient()) {
            JourneyMapContractWaypoints.updateWaypoint(action, kind, id, name, pos);
            ci.cancel();
        }
    }

    @Redirect(method = "updateWaypoint", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private static void coordboard$cancelWarn(Logger instance, String s, Throwable throwable) {
        CreateDeliveryRequired.LOGGER.warn("Unable to update Xaero delivery waypoint");
    }
}
