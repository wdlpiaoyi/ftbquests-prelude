package dev.wdlpiaoyi.ftbquestsprelude.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A compact, text-less button that draws a single icon, used as the local quest book entry point.
 */
public class IconButton extends Button {

    private final ResourceLocation icon;
    private final int iconTextureSize;

    public IconButton(int x, int y, int size, ResourceLocation icon, int iconTextureSize,
                      OnPress onPress, Component message) {
        super(x, y, size, size, message, onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.iconTextureSize = iconTextureSize;
        setTooltip(Tooltip.create(message));
    }

    /**
     * Renders this button manually. Needed on screens whose {@code render} does not call
     * {@code super.render} (e.g. {@code LevelLoadingScreen}), so widgets are otherwise never drawn.
     */
    public void renderSelf(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        boolean hovered = isHoveredOrFocused();

        // Semi-transparent background so the icon stays readable over any screen behind it.
        graphics.fill(x, y, x + w, y + h, hovered ? 0xC0404040 : 0x90000000);

        // Subtle 1px border.
        int border = hovered ? 0xFFFFFFFF : 0x60FFFFFF;
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + h - 1, x + w, y + h, border);
        graphics.fill(x, y, x + 1, y + h, border);
        graphics.fill(x + w - 1, y, x + w, y + h, border);

        int size = Math.min(iconTextureSize, Math.min(w, h));
        int iconX = x + (w - size) / 2;
        int iconY = y + (h - size) / 2;
        graphics.blit(icon, iconX, iconY, 0, 0, size, size, iconTextureSize, iconTextureSize);
    }
}
