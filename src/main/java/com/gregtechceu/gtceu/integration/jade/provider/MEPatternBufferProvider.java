package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.MEPatternBufferRecipeHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class MEPatternBufferProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof MEPatternBufferPartMachine buffer) {

                CompoundTag serverData = blockAccessor.getServerData();

                iTooltip.add(Component.translatable("gtceu.top.proxies_bound", serverData.getInt("proxies"))
                        .withStyle(ChatFormatting.LIGHT_PURPLE));

                ListTag itemTags = serverData.getList("items", Tag.TAG_COMPOUND);
                ListTag fluidTags = serverData.getList("fluids", Tag.TAG_COMPOUND);
                for (int i = 0; i < itemTags.size(); ++i) {
                    CompoundTag itemTag = itemTags.getCompound(i);
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemTag.getString("item")));
                    long count = itemTag.getLong("count");
                    if (item != Items.AIR) {
                        iTooltip.add(item.getDescription()
                                .copy()
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.literal(" * ").withStyle(ChatFormatting.WHITE))
                                .append(Component.literal("" + count).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                }
                for (int i = 0; i < fluidTags.size(); ++i) {
                    CompoundTag fluidTag = fluidTags.getCompound(i);
                    FluidType fluid = NeoForgeRegistries.FLUID_TYPES
                            .get(ResourceLocation.parse(fluidTag.getString("fluid")));
                    long count = fluidTag.getLong("count");
                    if (fluid != null) {
                        iTooltip.add(fluid
                                .getDescription()
                                .copy()
                                .withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(" * ").withStyle(ChatFormatting.WHITE))
                                .append(Component.literal("" + count).withStyle(ChatFormatting.LIGHT_PURPLE)));
                    }
                }
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof MEPatternBufferPartMachine buffer) {
                compoundTag.putInt("proxies", buffer.getProxies().size());

                var merged = MEPatternBufferRecipeHandler.mergeInternalSlot(buffer.getInternalInventory());
                var items = merged.getLeft();
                var fluids = merged.getRight();

                ListTag itemTags = new ListTag();
                for (Item item : items.keySet()) {
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                    if (key != null) {
                        CompoundTag itemTag = new CompoundTag();
                        itemTag.putString("item", key.toString());
                        itemTag.putInt("count", items.getInt(item));
                        itemTags.add(itemTag);
                    }
                }
                compoundTag.put("items", itemTags);

                ListTag fluidTags = new ListTag();
                for (Fluid fluid : fluids.keySet()) {
                    ResourceLocation key = NeoForgeRegistries.FLUID_TYPES.getKey(fluid.getFluidType());
                    if (key != null) {
                        CompoundTag fluidTag = new CompoundTag();
                        fluidTag.putString("fluid", key.toString());
                        fluidTag.putInt("count", fluids.getInt(fluid));
                        fluidTags.add(fluidTag);
                    }
                }
                compoundTag.put("fluids", fluidTags);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("me_pattern_buffer");
    }
}
