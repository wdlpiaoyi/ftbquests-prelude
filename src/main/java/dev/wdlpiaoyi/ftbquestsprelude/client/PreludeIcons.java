package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

/**
 * Icons shared by the local quest book screens.
 *
 * <p>These reuse FTB Quests / FTB Teams' own icons instead of custom art, so they are drawn through
 * the vanilla texture pipeline: resource packs and FTB Quests themes that override the originals
 * apply here too.
 */
public final class PreludeIcons {

    /** FTB Teams' "my team" icon, as used by its own sidebar button. */
    public static final Icon TEAM = Icon.getIcon("ftbteams:textures/teams.png");

    private PreludeIcons() {
    }

    /**
     * FTB Quests' themed {@code save_icon}, as used by its own save actions. Resolved on each call so
     * it follows the active theme; falls back to the default texture if the theme does not provide one.
     */
    public static Icon save() {
        Icon icon = ThemeProperties.SAVE_ICON.get();
        return icon == null ? Icon.getIcon("ftbquests:textures/gui/save.png") : icon;
    }
}
