package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws FTB Library's texture icons through the vanilla {@code GuiGraphics.blit} path while the local
 * quest book is open.
 *
 * <p>FTB Library's {@code ImageIcon.draw} goes through {@code GuiHelper.drawTexturedRect}, which drives
 * {@code RenderSystem} / {@code Tesselator} by hand; in packs with vertex-pipeline mods that can draw
 * nothing. This covers every texture-based icon FTB Quests uses (task type icons such as the dimension
 * portal, fluid icons, theme icons), not just this mod's own.
 *
 * <p>Only icons whose UVs span the whole texture are redirected, and only outside a world; tiled
 * backgrounds and UV-sliced sprite sheets keep FTB Library's own drawing.
 */
@Mixin(value = ImageIcon.class, remap = false)
public abstract class ImageIconMixin {

    @Unique
    private static final java.util.Set<net.minecraft.resources.ResourceLocation> prelude$loggedTextures =
            new java.util.HashSet<>();

    @Unique
    private static int prelude$loggedCount;

    @Inject(method = "draw", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$blit(GuiGraphics graphics, int x, int y, int w, int h, CallbackInfo ci) {
        if (!LocalQuestSession.isLocalBook() || w <= 0 || h <= 0) {
            return;
        }

        ImageIcon self = (ImageIcon) (Object) this;
        if (self.tileSize > 0.0 || self.minU != 0.0f || self.minV != 0.0f
                || self.maxU != 1.0f || self.maxV != 1.0f) {
            return;
        }

        // Diagnostic: one line per distinct texture (capped), so it is possible to tell which icons
        // actually take this path and whether their resource resolves.
        if (prelude$loggedCount < 60 && prelude$loggedTextures.add(self.texture)) {
            prelude$loggedCount++;
            boolean present = Minecraft.getInstance().getResourceManager().getResource(self.texture).isPresent();
            FTBQuestsPrelude.LOGGER.info("[Prelude] Texture icon via blit: texture={}, resourcePresent={}, color={}",
                    self.texture, present, self.color);
        }

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0);
        // Any consistent square source size samples the whole texture; the pose does the scaling.
        pose.scale(w / 16.0f, h / 16.0f, 1.0f);

        Color4I color = self.color;
        if (color != null) {
            graphics.setColor(color.redf(), color.greenf(), color.bluef(), color.alphaf());
        }
        graphics.blit(self.texture, 0, 0, 0, 0, 16, 16, 16, 16);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        pose.popPose();
        ci.cancel();
    }
}
