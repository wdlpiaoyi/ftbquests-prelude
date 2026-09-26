package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.quest.task.BiomeTask;
import dev.wdlpiaoyi.ftbquestsprelude.client.ClientRegistryAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets a "visit biome" task be created outside a world.
 *
 * <p>{@code BiomeTask.getKnownBiomes()} reads
 * {@code FTBQuestsClient.getClientPlayer().level().registryAccess()}, and the client player is null
 * at the main menu, so constructing the task threw and the task could not be added at all. When there
 * is no world we build the list from the last registry access we saw (see {@link ClientRegistryAccess}),
 * and otherwise from just the default biome - so the task is at least creatable.
 */
@Mixin(value = BiomeTask.class, remap = false)
public abstract class BiomeTaskMixin {

    @Inject(method = "getKnownBiomes", remap = false, at = @At("HEAD"), cancellable = true)
    private void prelude$knownBiomesWithoutWorld(CallbackInfoReturnable<List<String>> cir) {
        if (Minecraft.getInstance().player != null) {
            return; // in a world the original path works
        }

        RegistryAccess access = ClientRegistryAccess.get();
        if (access == null) {
            // Never joined a world this session, so there is no dynamic registry at all.
            // The vanilla biome constants are still a far better fallback than the default.
            cir.setReturnValue(vanillaBiomes());
            return;
        }

        var biomeRegistry = access.registryOrThrow(Registries.BIOME);
        List<String> biomes = new ArrayList<>();
        biomeRegistry.registryKeySet().stream()
                .map(key -> key.location().toString())
                .sorted()
                .forEach(biomes::add);
        biomeRegistry.getTagNames()
                .map(tag -> "#" + tag.location())
                .sorted()
                .forEach(biomes::add);
        cir.setReturnValue(biomes);
    }

    /**
     * Every vanilla biome id, read from the {@code Biomes} constants. Used when the client has no
     * registry access yet; modded biomes appear once a world has been joined in this session.
     */
    private static List<String> vanillaBiomes() {
        List<String> ids = new ArrayList<>();
        for (Field field : Biomes.class.getDeclaredFields()) {
            if (field.getType() != ResourceKey.class) {
                continue;
            }
            try {
                field.setAccessible(true);
                if (field.get(null) instanceof ResourceKey<?> key) {
                    ids.add(key.location().toString());
                }
            } catch (Throwable ignored) {
                // skip anything we cannot read
            }
        }
        ids.sort(String::compareTo);
        return ids;
    }
}
