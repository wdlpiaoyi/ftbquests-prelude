package dev.wdlpiaoyi.ftbquestsprelude.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;

/**
 * Remembers the last {@link RegistryAccess} the client saw.
 *
 * <p>Dynamic registries (biomes, for instance) are only available while a world or server
 * connection is active, but some option lists are built from them even at the main menu. Keeping the
 * last one makes those lists work once a world has been joined in this game session.
 */
public final class ClientRegistryAccess {

    @Nullable
    private static RegistryAccess cached;

    private ClientRegistryAccess() {
    }

    /** Called from the client tick: remembers the registry access while one is available. */
    public static void capture() {
        RegistryAccess access = current();
        if (access != null) {
            cached = access;
        }
    }

    @Nullable
    public static RegistryAccess get() {
        RegistryAccess access = current();
        return access != null ? access : cached;
    }

    @Nullable
    private static RegistryAccess current() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null ? mc.level.registryAccess() : null;
    }
}
