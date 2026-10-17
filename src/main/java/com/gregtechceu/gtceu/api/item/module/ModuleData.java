package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Objects;

/**
 * The data for an item module attached to a specific item.<br>
 * Inheriting classes must be immutable.<br>
 * Equals and hashcode must be implemented by inheritors.
 */
public abstract class ModuleData {

    // spotless:off
    public static final Codec<ModuleData> DISPATCH_CODEC = GTRegistries.ITEM_MODULES.byNameCodec()
            .dispatch("module", ModuleData::getModule, ItemModule::moduleDataCodec);

    public static final MapCodec<BaseData> BASE_CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance).apply(instance, ModuleData.BaseData::new));

    public static <T extends ModuleData> Products.P4<RecordCodecBuilder.Mu<T>, Integer, ItemModule, ItemStack, Boolean> baseCodec(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.INT.fieldOf("slot").forGetter(ModuleData::getSlot),
                GTRegistries.ITEM_MODULES.byNameCodec().fieldOf("module").forGetter(ModuleData::getModule),
                ItemStack.OPTIONAL_CODEC.fieldOf("moduleItem").forGetter(ModuleData::getModuleItem),
                Codec.BOOL.fieldOf("enabled").forGetter(ModuleData::isEnabled)
        );
    }
    //spotless:on

    /**
     * The slot this module is in.
     */
    @Getter
    protected final int slot;

    /**
     * The {@link ItemModule} class for this module.
     */
    @Getter
    protected final ItemModule module;

    /**
     * The ItemStack currently in this module slot.<br>
     * <b>Do not modify</b>, changes will not be saved.
     */
    @Getter
    protected final ItemStack moduleItem;

    @Getter
    protected final boolean enabled;

    public abstract ModuleData withModuleItem(ItemStack moduleItem);

    public abstract ModuleData withEnabled(boolean enabled);

    /**
     * Must create a copy of most object fields, rather than using the same reference.
     */
    public abstract ModuleData copy();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ModuleData other)) return false;
        return slot == other.slot && module.equals(other.module) && moduleItem.equals(other.moduleItem) &&
                enabled == other.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, module, moduleItem, enabled);
    }

    public ModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled) {
        this.slot = slot;
        this.module = module;
        this.moduleItem = moduleItem;
        this.enabled = enabled;
    }

    public static class BaseData extends ModuleData {

        public BaseData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled) {
            super(slot, module, moduleItem, enabled);
        }

        @Override
        public ModuleData copy() {
            return new BaseData(slot, module, moduleItem.copy(), enabled);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new BaseData(slot, module, moduleItem, enabled);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new BaseData(getSlot(), getModule(), getModuleItem(), enabled);
        }
    }
}
