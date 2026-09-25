package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.architectury.networking.simple.BaseC2SMessage;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalEditBridge;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts client-to-server messages while the client is not connected to a server.
 *
 * <p>When the native quest screen is opened outside a world, the GUI can emit edit messages. Those
 * can never be delivered, so:
 * <ul>
 *   <li>in local editor mode, {@link LocalEditBridge} applies them to the local quest file; and</li>
 *   <li>in every other case the send is simply skipped instead of throwing
 *       {@code IllegalStateException: Unable to send packet to the server while not in game!}.</li>
 * </ul>
 */
@Mixin(value = BaseC2SMessage.class, remap = false)
public abstract class BaseC2SMessageMixin {

    @Inject(method = "sendToServer", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$handleWhenDisconnected(CallbackInfo ci) {
        if (Minecraft.getInstance().getConnection() == null) {
            LocalEditBridge.handle((BaseC2SMessage) (Object) this);
            ci.cancel();
        }
    }
}
