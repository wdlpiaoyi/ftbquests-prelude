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

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveredOrFocused()) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40FFFFFF);
        }

        int size = Math.min(iconTextureSize, Math.min(getWidth(), getHeight()));
        int iconX = getX() + (getWidth() - size) / 2;
        int iconY = getY() + (getHeight() - size) / 2;
        graphics.blit(icon, iconX, iconY, 0, 0, size, size, iconTextureSize, iconTextureSize);
    }
}
