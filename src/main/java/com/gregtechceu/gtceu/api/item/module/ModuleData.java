package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

/**
 * The data for an item module attached to a specific item.
 */
public final class ModuleData {

    //spotless:off
    public static final Codec<ModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTRegistries.ITEM_MODULES.codec().fieldOf("module").forGetter(ModuleData::getModule),
            CompoundTag.CODEC.fieldOf("tag").forGetter(ModuleData::getTag),
            ItemStack.CODEC.fieldOf("moduleItem").forGetter(ModuleData::getModuleItem)
    ).apply(instance, ModuleData::new));
    //spotless:on

    @Getter
    private ItemModule module;

    @Getter
    private CompoundTag tag;

    @Getter
    private ItemStack moduleItem;

    /**
     * The stack that this module is applied to.
     * If this module is not applied to anything, this field is {@code null}.
     */
    @Getter
    @Setter
    private ItemStack appliedTo;

    /**
     * The {@link IModularItem} capability of the item this module is attached to.
     */
    @Getter
    @Setter
    private IModularItem modularItemStack;

    public ModuleData(ItemModule module, CompoundTag tag, ItemStack moduleItem) {
        this.module = module;
        this.moduleItem = moduleItem;
        this.tag = tag;
    }
}
