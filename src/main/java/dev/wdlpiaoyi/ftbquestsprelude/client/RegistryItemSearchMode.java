package dev.wdlpiaoyi.ftbquestsprelude.client;

import dev.ftb.mods.ftblibrary.config.ui.ResourceSearchMode;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An "all items" search mode backed by the item registry.
 *
 * <p>FTB Library's own {@code ALL_ITEMS} enumerates creative tabs, whose contents are only populated
 * once a world has been loaded - so at the main menu the list is empty and no item task or reward can
 * be created. The item registry is available at any time.
 */
public final class RegistryItemSearchMode implements ResourceSearchMode<ItemStack> {

    public static final RegistryItemSearchMode INSTANCE = new RegistryItemSearchMode();
    private List<SelectableResource<ItemStack>> cached;

    private RegistryItemSearchMode() {
    }

    @Override
    public Icon getIcon() {
        return ResourceSearchMode.ALL_ITEMS.getIcon();
    }

    @Override
    public MutableComponent getDisplayName() {
        return ResourceSearchMode.ALL_ITEMS.getDisplayName();
    }

    @Override
    public Collection<? extends SelectableResource<ItemStack>> getAllResources() {
        List<SelectableResource<ItemStack>> list = cached;
        if (list == null) {
            list = new ArrayList<>();
            for (var item : BuiltInRegistries.ITEM) {
                if (item != Items.AIR) {
                    list.add(SelectableResource.item(new ItemStack(item)));
                }
            }
            cached = list;
        }
        return list;
    }
}
