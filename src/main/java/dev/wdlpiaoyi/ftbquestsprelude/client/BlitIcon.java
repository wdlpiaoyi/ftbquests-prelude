package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * An {@link Icon} that draws through the vanilla {@code GuiGraphics.blit} path rather than FTB
 * Library's own {@code GuiHelper.drawTexturedRect}.
 *
 * <p>FTB Library's path drives {@code RenderSystem} and {@code Tesselator} by hand. In heavily modded
 * packs with renderers that take over the vertex pipeline (Accelerated Rendering, Florescent,
 * Chloride, ImmediatelyFast, ...) such draws can silently produce nothing, while the vanilla blit
 * path - the one used by this mod's menu button - keeps working.
 *
 * <p>The whole texture is scaled into the requested rectangle, which is what {@code ImageIcon} does
 * for a plain texture: {@code ftbquests:textures/gui/save.png} (32x32) and
 * {@code ftbteams:textures/teams.png} (256x256) are both a single icon, not a sprite sheet.
 */
public class BlitIcon extends Icon {

    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;

    public BlitIcon(ResourceLocation texture, int textureWidth, int textureHeight) {
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0);
        pose.scale(w / (float) textureWidth, h / (float) textureHeight, 1.0f);
        graphics.blit(texture, 0, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        pose.popPose();
    }

    /** Never empty: an "empty" icon renders as nothing at all, which is what we are working around. */
    @Override
    public boolean isEmpty() {
        return false;
    }
}
