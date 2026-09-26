package dev.wdlpiaoyi.ftbquestsprelude.mixin;

import dev.ftb.mods.ftbquests.quest.task.BiomeTask;
import dev.wdlpiaoyi.ftbquestsprelude.client.ClientRegistryAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
            cir.setReturnValue(List.of(Biomes.PLAINS.location().toString()));
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
}
