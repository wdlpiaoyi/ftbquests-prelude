package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import net.minecraft.resources.ResourceLocation;

/**
 * Icons shared by the local quest book screens.
 *
 * <p>They reference FTB Quests' / FTB Teams' own textures and draw them through the vanilla
 * {@code GuiGraphics.blit} path (see {@link BlitIcon}), so resource packs that override the textures
 * apply here too, and they survive render-optimisation mods that break FTB Library's own drawing.
 *
 * <p>Two earlier approaches are deliberately avoided: a theme lookup
 * ({@code ThemeProperties.SAVE_ICON.get()}) resolves to an <b>empty</b> icon when there is no quest
 * object rendering context ({@code IconProperty} defaults to {@code Color4I.empty()}), and
 * {@code Icon.getIcon(texture)} draws through FTB Library's raw {@code RenderSystem} path.
 */
public final class PreludeIcons {

    /** FTB Quests' "save" icon, as used by its own save actions. */
    public static final Icon SAVE = new BlitIcon(new ResourceLocation("ftbquests", "textures/gui/save.png"), 32, 32);

    /** FTB Teams' "my team" icon, as used by its own sidebar button. */
    public static final Icon TEAM = new BlitIcon(new ResourceLocation("ftbteams", "textures/teams.png"), 256, 256);

    static {
        // Logged once, because a missing / invisible icon is otherwise impossible to tell apart.
        FTBQuestsPrelude.LOGGER.info("[Prelude] Icons ready: save={} (empty={}), team={} (empty={})",
                SAVE.getClass().getSimpleName(), SAVE.isEmpty(),
                TEAM.getClass().getSimpleName(), TEAM.isEmpty());
    }

    private PreludeIcons() {
    }
}
