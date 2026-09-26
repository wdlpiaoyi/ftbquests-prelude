package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractThreePanelScreen;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Draws FTB Library's texture icons through the vanilla {@code GuiGraphics.blit} path, but only inside
 * FTB Library's own three-panel list screens (this mod's save/team pickers and FTB Library's item and
 * fluid selectors) while the local quest book is active.
 *
 * <p>Those screens are where {@code ImageIcon} fails to draw; FTB Quests' own quest screen draws its
 * icons (the quest shape textures in particular) correctly, and redirecting them here loses the tint -
 * {@code blit} uses a shader with no vertex colour - which turned the shapes solid white. So the quest
 * screen is deliberately excluded.
 *
 * <p>Tiled and UV-sliced icons always keep FTB Library's own drawing.
 */
@Mixin(value = ImageIcon.class, remap = false)
public abstract class ImageIconMixin {

    @Unique
    private static final Set<ResourceLocation> prelude$loggedTextures = new HashSet<>();

    @Unique
    private static int prelude$loggedCount;

    @Inject(method = "draw", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$blit(GuiGraphics graphics, int x, int y, int w, int h, CallbackInfo ci) {
        if (!LocalQuestSession.isLocalBook() || w <= 0 || h <= 0) {
            return;
        }

        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof IScreenWrapper wrapper)
                || !(wrapper.getGui() instanceof AbstractThreePanelScreen)) {
            return;
        }

        ImageIcon self = (ImageIcon) (Object) this;
        if (self.tileSize > 0.0 || self.minU != 0.0f || self.minV != 0.0f
                || self.maxU != 1.0f || self.maxV != 1.0f) {
            return;
        }

        // Diagnostic: one line per distinct texture (capped), to tell which icons take this path.
        if (prelude$loggedCount < 60 && prelude$loggedTextures.add(self.texture)) {
            prelude$loggedCount++;
            boolean present = Minecraft.getInstance().getResourceManager().getResource(self.texture).isPresent();
            FTBQuestsPrelude.LOGGER.info("[Prelude] Texture icon via blit: texture={}, resourcePresent={}, color={}",
                    self.texture, present, self.color);
        }

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0);
        // Any consistent square source samples the whole texture; the pose does the scaling.
        pose.scale(w / 16.0f, h / 16.0f, 1.0f);

        Color4I color = self.color;
        if (color != null && !color.isEmpty()) {
            // blit cannot tint through GuiGraphics, so apply the colour to the shader directly.
            RenderSystem.setShaderColor(color.redf(), color.greenf(), color.bluef(), color.alphaf());
        }
        graphics.blit(self.texture, 0, 0, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        pose.popPose();
        ci.cancel();
    }
}
