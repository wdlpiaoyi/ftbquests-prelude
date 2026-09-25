package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.architectury.networking.simple.BaseC2SMessage;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses client-to-server messages while the client is not connected to a server.
 *
 * <p>When the native quest screen is opened outside a world, clicking a button that normally talks
 * to the server (for example "auto-pin") would throw
 * {@code IllegalStateException: Unable to send packet to the server while not in game!}.
 * Since such a message can never be delivered in that state, skipping it is both safe and correct.
 */
@Mixin(value = BaseC2SMessage.class, remap = false)
public abstract class BaseC2SMessageMixin {

    @Inject(method = "sendToServer", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$skipWhenDisconnected(CallbackInfo ci) {
        if (Minecraft.getInstance().getConnection() == null) {
            ci.cancel();
        }
    }
}
