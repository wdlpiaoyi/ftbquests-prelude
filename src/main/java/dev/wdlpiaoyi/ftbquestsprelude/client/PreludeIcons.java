package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.icon.Icon;

/**
 * Icons shared by the local quest book screens.
 *
 * <p>They reference FTB Quests / FTB Teams textures directly, so they are drawn through the vanilla
 * texture pipeline and resource packs that override those textures apply here too.
 *
 * <p>A theme lookup ({@code ThemeProperties.SAVE_ICON.get()}) was tried first, but that resolves to
 * an <b>empty</b> icon when the active theme does not define the property
 * ({@code IconProperty(String)} defaults to {@code Color4I.empty()}), which silently rendered
 * nothing. Referencing the texture is deterministic.
 */
public final class PreludeIcons {

    /** FTB Quests' "save" icon, as used by its own save actions. */
    public static final Icon SAVE = Icon.getIcon("ftbquests:textures/gui/save.png");

    /** FTB Teams' "my team" icon, as used by its own sidebar button. */
    public static final Icon TEAM = Icon.getIcon("ftbteams:textures/teams.png");

    private PreludeIcons() {
    }
}
