package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.FTBQuestsCompat;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import dev.wdlpiaoyi.ftbquestsprelude.compat.SaveProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
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

import java.nio.file.Path;
import java.util.List;

/**
 * Client entry points for the local quest book: a small icon button in the top-right corner of the
 * title screen, the world selection/creation screens and the world loading screen, plus a key
 * binding.
 *
 * <p>On the select-world screen (with a highlighted save) and the level loading screen, the button
 * opens the quest progress of that save by default; the plain local book is used elsewhere. Browsing
 * any save's progress is available from a button inside the quest book itself (see
 * {@link SaveProgressButton}).
 *
 * <p>Every handler is defensive: any failure is logged and degrades to "no entry point" rather than
 * crashing the game.
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
        try {
            Screen screen = event.getScreen();
            if (!isSupportedScreen(screen)) {
                return;
            }

            // Top-right corner, so it does not overlap the tab bar on the world creation screen.
            event.addListener(new IconButton(
                    screen.width - BUTTON_SIZE - MARGIN, MARGIN,
                    BUTTON_SIZE, BOOK_ICON, ICON_SIZE,
                    button -> tryOpen(),
                    Component.translatable("ftbquests_prelude.button.open_local_book")));
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to add the local quest book entry button", t);
        }
    }

    private static boolean isSupportedScreen(Screen screen) {
        return screen instanceof TitleScreen
                || screen instanceof SelectWorldScreen
                || screen instanceof CreateWorldScreen
                || screen instanceof LevelLoadingScreen;
    }

    /**
     * The world loading screen overrides {@code render} without calling {@code super.render}, so its
     * widgets (including our button) are never drawn. Clicking still works because input is
     * dispatched to children, so we just render the button ourselves.
     */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        try {
            Screen screen = event.getScreen();
            if (!(screen instanceof LevelLoadingScreen)) {
                return;
            }
            for (GuiEventListener listener : screen.children()) {
                if (listener instanceof IconButton button) {
                    button.renderSelf(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(),
                            event.getPartialTick());
                }
            }
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to render the local quest book button", t);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        try {
            LocalQuestSession.tick();

            while (PreludeKeyMappings.OPEN_LOCAL_BOOK.consumeClick()) {
                tryOpen();
            }
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Client tick handling failed", t);
        }
    }

    private static void tryOpen() {
        try {
            // From the select-world / loading screens, default to the highlighted/loading save.
            Path worldRoot = SaveProgress.selectedWorldRoot();
            if (worldRoot != null) {
                openSaveOrDefault(worldRoot);
                return;
            }

            if (!LocalQuestSession.openAndShow()) {
                notifyUnavailable();
            }
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to open the local quest book", t);
        }
    }

    private static void openSaveOrDefault(Path worldRoot) {
        if (!SaveProgress.hasProgress(worldRoot)) {
            notify("ftbquests_prelude.toast.no_save_progress.title", "ftbquests_prelude.toast.no_save_progress.desc");
            return;
        }

        List<SaveProgress.TeamInfo> teams = SaveProgress.listTeams(worldRoot);
        if (teams.size() == 1) {
            if (!LocalQuestSession.openSaveProgress(worldRoot, teams.get(0).id())) {
                notifyUnavailable();
            }
        } else if (teams.size() > 1) {
            Minecraft.getInstance().setScreen(SaveProgressScreen.forTeams(worldRoot, teams));
        } else {
            notify("ftbquests_prelude.toast.no_save_progress.title", "ftbquests_prelude.toast.no_save_progress.desc");
        }
    }

    private static void notifyUnavailable() {
        boolean ftbQuestsOk = FTBQuestsCompat.canUseLocalQuestBook();
        notify(ftbQuestsOk ? "ftbquests_prelude.toast.no_data.title" : "ftbquests_prelude.toast.unavailable.title",
                ftbQuestsOk ? "ftbquests_prelude.toast.no_data.desc" : "ftbquests_prelude.toast.unavailable.desc");
    }

    private static void notify(String titleKey, String descriptionKey) {
        SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.translatable(titleKey), Component.translatable(descriptionKey));
    }
}
