package dev.wdlpiaoyi.ftbquestsprelude.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/** Small helpers for showing toast notifications from the local quest book screens. */
public final class Notifications {

    private Notifications() {
    }

    public static void show(String titleKey, String descriptionKey) {
        SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION,
                Component.translatable(titleKey), Component.translatable(descriptionKey));
    }

    public static void noSaveProgress() {
        show("ftbquests_prelude.toast.no_save_progress.title", "ftbquests_prelude.toast.no_save_progress.desc");
    }

    public static void noData() {
        show("ftbquests_prelude.toast.no_data.title", "ftbquests_prelude.toast.no_data.desc");
    }

    public static void saved() {
        show("ftbquests_prelude.toast.saved.title", "ftbquests_prelude.toast.saved.desc");
    }

    public static void unavailable() {
        show("ftbquests_prelude.toast.unavailable.title", "ftbquests_prelude.toast.unavailable.desc");
    }
}
