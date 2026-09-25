package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client entry points for the local quest book: a button on the title screen and the
 * world-creation screen, plus a key binding.
 */
@Mod.EventBusSubscriber(modid = FTBQuestsPrelude.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PreludeClientEvents {

    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;

    private PreludeClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof TitleScreen) && !(screen instanceof CreateWorldScreen)) {
            return;
        }

        // Top-left corner, away from the centred vanilla UI.
        event.addListener(Button.builder(
                        Component.translatable("ftbquests_prelude.button.open_local_book"),
                        button -> tryOpen())
                .bounds(4, 4, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        while (PreludeKeyMappings.OPEN_LOCAL_BOOK.consumeClick()) {
            tryOpen();
        }
    }

    private static void tryOpen() {
        LocalQuestSession.openAndShow();
    }
}
