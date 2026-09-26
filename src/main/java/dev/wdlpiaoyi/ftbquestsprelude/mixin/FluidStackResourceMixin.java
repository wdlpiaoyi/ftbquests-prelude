package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives fluid entries an icon outside a world.
 *
 * <p>FTB Library builds them as {@code Icon.getIcon(ClientUtils.getStillTexture(stack)).withTint(...)},
 * and outside a world {@code getStillTexture} resolves the fluid's sprite from the block atlas, gets
 * nothing, and returns {@code null}. {@code Icon.getIcon(null)} is an empty {@code Color4I}, not an
 * {@code ImageIcon}, so the fluid picker draws nothing at all and the icon-drawing fix in
 * {@code ImageIconMixin} never even sees it.
 *
 * <p>When the normal icon comes back empty, the fluid's bucket item is used instead - that goes through
 * the vanilla item renderer, which works anywhere.
 */
@Mixin(value = SelectableResource.FluidStackResource.class, remap = false)
public abstract class FluidStackResourceMixin {

    @Inject(method = "getIcon", remap = false, at = @At("RETURN"), cancellable = true)
    private void prelude$iconFallback(CallbackInfoReturnable<Icon> cir) {
        if (!LocalQuestSession.isLocalBook()) {
            return;
        }
        Icon icon = cir.getReturnValue();
        if (icon != null && !icon.isEmpty()) {
            return;
        }

        SelectableResource.FluidStackResource self = (SelectableResource.FluidStackResource) (Object) this;
        var stack = self.stack();
        if (stack == null || stack.isEmpty()) {
            return;
        }

        Item bucket = stack.getFluid().getBucket();
        Item iconItem = bucket == Items.AIR ? Items.BUCKET : bucket;
        FTBQuestsPrelude.LOGGER.info("[Prelude] Fluid icon fallback: {} -> {}",
                stack.getFluid(), iconItem);
        cir.setReturnValue(ItemIcon.getItemIcon(new ItemStack(iconItem)));
    }
}
