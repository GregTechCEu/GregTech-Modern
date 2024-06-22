package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class MachineModeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag serverData = blockAccessor.getServerData();
        if (serverData.contains("RecipeTypes") && serverData.contains("CurrentRecipeType")) {
            int currentRecipeTypeIndex = serverData.getInt("CurrentRecipeType");
            ListTag recipeTypesTagList = serverData.getList("RecipeTypes", StringTag.TAG_STRING);
            if (blockAccessor.showDetails()) {
                iTooltip.add(Component.translatable("gtceu.top.machine_mode"));
                for (int i = 0; i < recipeTypesTagList.size(); i++) {
                    ResourceLocation recipeType = new ResourceLocation(recipeTypesTagList.getString(i));
                    MutableComponent text;
                    if (currentRecipeTypeIndex == i) {
                        text = Component.literal(" > ").withStyle(ChatFormatting.BLUE);
                    } else {
                        text = Component.literal("   ");
                    }
                    text.append(
                            Component.translatable("%s.%s".formatted(recipeType.getNamespace(), recipeType.getPath())));
                    iTooltip.add(text);
                }
            } else {
                ResourceLocation recipeType = new ResourceLocation(
                        recipeTypesTagList.getString(currentRecipeTypeIndex));
                iTooltip.add(Component.translatable("gtceu.top.machine_mode").append(
                        Component.translatable("%s.%s".formatted(recipeType.getNamespace(), recipeType.getPath()))));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            @Nullable
            GTRecipeType[] recipeTypes = blockEntity.getMetaMachine().getDefinition().getRecipeTypes();
            if (recipeTypes != null && recipeTypes.length > 1) {
                if (blockEntity.getMetaMachine() instanceof IRecipeLogicMachine recipeLogicMachine) {
                    ListTag recipeTypesTagList = new ListTag();
                    GTRecipeType currentRecipeType = recipeLogicMachine.getRecipeType();
                    int currentRecipeTypeIndex = -1;
                    for (int i = 0; i < recipeTypes.length; i++) {
                        if (recipeTypes[i] == currentRecipeType) {
                            currentRecipeTypeIndex = i;
                        }
                        recipeTypesTagList.add(StringTag.valueOf(recipeTypes[i].registryName.toString()));
                    }
                    compoundTag.put("RecipeTypes", recipeTypesTagList);
                    compoundTag.putInt("CurrentRecipeType", currentRecipeTypeIndex);
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("machine_mode");
    }
}
