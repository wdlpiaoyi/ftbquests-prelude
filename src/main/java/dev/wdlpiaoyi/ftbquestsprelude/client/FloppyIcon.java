package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A small hand-drawn floppy disk icon, used for the "save progress" entry points.
 */
public class FloppyIcon extends Icon {

    public static final Icon INSTANCE = new FloppyIcon();

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        // Body
        Color4I.rgb(0x2B6CB0).draw(graphics, x, y, w, h);
        // Metal shutter
        Color4I.rgb(0xD8DEE9).draw(graphics, x + w / 4, y, w / 2, Math.max(1, (int) (h * 0.55f)));
        Color4I.rgb(0x1B2733).draw(graphics, x + (int) (w * 0.38f), y + (int) (h * 0.08f),
                Math.max(1, (int) (w * 0.24f)), Math.max(1, (int) (h * 0.30f)));
        // Paper label
        Color4I.rgb(0xF2F4F7).draw(graphics, x + (int) (w * 0.15f), y + (int) (h * 0.65f),
                Math.max(1, (int) (w * 0.70f)), Math.max(1, (int) (h * 0.25f)));
    }
}
