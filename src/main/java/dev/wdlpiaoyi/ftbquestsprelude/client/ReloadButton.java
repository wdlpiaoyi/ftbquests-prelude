package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.TabButton;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A button in the local quest book that re-reads the quest data from disk, for when the files were
 * edited outside the game. Icon: the vanilla disc fragment (as requested).
 */
public class ReloadButton extends TabButton {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/item/disc_fragment_5.png");

    public ReloadButton(Panel panel) {
        super(panel, Component.translatable("ftbquests_prelude.reload"), new BlitIcon(TEXTURE, 16, 16));
    }

    @Override
    public void onClicked(MouseButton button) {
        playClickSound();
        if (!LocalQuestSession.reloadFromDisk()) {
            Notifications.noData();
        }
    }
}
