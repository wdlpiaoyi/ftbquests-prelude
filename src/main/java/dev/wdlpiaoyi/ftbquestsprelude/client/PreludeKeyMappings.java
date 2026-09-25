package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.wdlpiaoyi.ftbquestsprelude.FTBQuestsPrelude;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Key binding registration for the local quest book.
 */
@Mod.EventBusSubscriber(modid = FTBQuestsPrelude.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PreludeKeyMappings {

    public static final String CATEGORY = "key.categories.ftbquests_prelude";

    public static final KeyMapping OPEN_LOCAL_BOOK = new KeyMapping(
            "key.ftbquests_prelude.open_local_book",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    private PreludeKeyMappings() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_LOCAL_BOOK);
    }
}
