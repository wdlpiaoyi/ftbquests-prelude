package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client entry points for the local quest book: a small icon button in the top-right corner of the
 * title screen and the world selection/creation screens, plus a key binding.
 */
@Mod.EventBusSubscriber(modid = FTBQuestsPrelude.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PreludeClientEvents {

    /** FTB Quests' own quest book texture (16x16). */
    private static final ResourceLocation BOOK_ICON = new ResourceLocation("ftbquests", "textures/item/book.png");
    private static final int ICON_SIZE = 16;
    private static final int BUTTON_SIZE = 18;
    private static final int MARGIN = 4;

    private PreludeClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof TitleScreen) && !(screen instanceof CreateWorldScreen) && !(screen instanceof SelectWorldScreen)) {
            return;
        }

        // Top-right corner, so it does not overlap the tab bar on the world creation screen.
        event.addListener(new IconButton(
                screen.width - BUTTON_SIZE - MARGIN, MARGIN,
                BUTTON_SIZE, BOOK_ICON, ICON_SIZE,
                button -> tryOpen(),
                Component.translatable("ftbquests_prelude.button.open_local_book")));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LocalQuestSession.tick();

        while (PreludeKeyMappings.OPEN_LOCAL_BOOK.consumeClick()) {
            tryOpen();
        }
    }

    private static void tryOpen() {
        LocalQuestSession.openAndShow();
    }
}
