package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import dev.wdlpiaoyi.ftbquestsprelude.compat.FTBQuestsCompat;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalEditBridge;
import dev.wdlpiaoyi.ftbquestsprelude.compat.LocalQuestSession;
import dev.wdlpiaoyi.ftbquestsprelude.compat.SaveProgress;
import dev.wdlpiaoyi.ftbquestsprelude.compat.UnsafeMods;
import dev.wdlpiaoyi.ftbquestsprelude.config.PreludeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;
import java.util.List;

/**
 * Client entry points for the local quest book: a small book button in the top-right corner of the
 * title screen and the world selection/creation screens.
 *
 * <p>On the select-world screen, with a highlighted save, the button opens that save's quest progress
 * by default; the plain local book is used elsewhere. Browsing any save's progress is available from a
 * button inside the quest book itself (see {@link SaveProgressButton}).
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
            // Let the compat layer report an on-demand save without depending on the UI package.
            LocalEditBridge.setSaveNotifier(Notifications::saved);

            Screen screen = event.getScreen();

            // Keep third-party compatibility workarounds applied only while the local book is on
            // screen. Done from screen events so it does not rely on the client tick running.
            if (LocalQuestSession.isLocalQuestScreenOpen()) {
                if (UnsafeMods.isPresent()) {
                    UnsafeMods.ensureSafe();
                }
            } else {
                UnsafeMods.release();
            }

            if (!isSupportedScreen(screen)) {
                return;
            }

            if (!PreludeConfig.COMMON.showEntryButtons.get()) {
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
                || screen instanceof CreateWorldScreen;
    }

    /**
     * Drops the local session when a world is joined. From that point the native quest book is the
     * server-synced one, and every change this mod makes to the FTB Quests UI must stop applying.
     */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        try {
            LocalQuestSession.invalidate();
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to close the local quest book on login", t);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        try {
            ClientRegistryAccess.capture();
            LocalQuestSession.tick();
            LocalEditBridge.tick();
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Client tick handling failed", t);
        }
    }

    private static void tryOpen() {
        try {
            // From the select-world screen, default to the highlighted save.
            Path worldRoot = SaveProgress.selectedWorldRoot();
            if (worldRoot != null) {
                openSaveOrDefault(worldRoot);
                return;
            }

            if (!LocalQuestSession.openAndShow()) {
                notifyOpenFailed();
            }
        } catch (Throwable t) {
            FTBQuestsPrelude.LOGGER.error("[Prelude] Failed to open the local quest book", t);
        }
    }

    private static void openSaveOrDefault(Path worldRoot) {
        List<SaveProgress.TeamInfo> teams = SaveProgress.listTeams(worldRoot);
        if (teams.isEmpty()) {
            // That save has no progress yet - show the plain local book instead of refusing to open.
            if (!LocalQuestSession.openAndShow()) {
                notifyOpenFailed();
            }
        } else if (teams.size() == 1) {
            if (!LocalQuestSession.openSaveProgress(worldRoot, teams.get(0).id())) {
                notifyOpenFailed();
            }
        } else {
            new TeamPickerScreen(worldRoot, teams, Minecraft.getInstance().screen).openGui();
        }
    }

    private static void notifyOpenFailed() {
        if (UnsafeMods.blocked()) {
            Notifications.incompatibleMod();
        } else if (FTBQuestsCompat.canUseLocalQuestBook()) {
            Notifications.noData();
        } else {
            Notifications.unavailable();
        }
    }
}
