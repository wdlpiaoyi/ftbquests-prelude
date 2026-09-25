package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.TabButton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * A button added to the native quest screen's bottom-right button panel that opens the save
 * progress picker, so browsing a save's progress is part of the quest book UI instead of a
 * modifier-click on the entry icon.
 */
public class SaveProgressButton extends TabButton {

    public SaveProgressButton(Panel panel) {
        super(panel, Component.translatable("ftbquests_prelude.save_progress.title"), Icons.INFO_GRAY);
    }

    @Override
    public void onClicked(MouseButton button) {
        playClickSound();
        Minecraft.getInstance().setScreen(SaveProgressScreen.forSaves());
    }
}
