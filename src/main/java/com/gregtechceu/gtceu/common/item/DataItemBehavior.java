package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxy;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class DataItemBehavior implements IInteractionItem, IAddInformation, IDataItem {

    private final boolean requireDataBank;

    public DataItemBehavior() {
        this.requireDataBank = false;
    }

    public DataItemBehavior(boolean requireDataBank) {
        this.requireDataBank = requireDataBank;
    }

    @Override
    public boolean requireDataBank() {
        return requireDataBank;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        Pair<GTRecipeType, String> researchData = ResearchManager.readResearchId(stack);
        if (researchData == null){
            if (stack.getOrCreateTag().contains("pos", Tag.TAG_INT_ARRAY) && stack.hasTag()) {
                int[] posArray = stack.getOrCreateTag().getIntArray("pos");
                tooltipComponents.add(Component.translatable(
                        "gtceu.tooltip.proxy_bind",
                        Component.literal("" + posArray[0]).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray[1]).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray[2]).withStyle(ChatFormatting.LIGHT_PURPLE)));
            } return;
        } else {
        Collection<GTRecipe> recipes = researchData.getFirst().getDataStickEntry(researchData.getSecond());
        if (recipes != null && !recipes.isEmpty()) {
            tooltipComponents.add(Component.translatable("behavior.data_item.assemblyline.title"));
            Collection<ItemStack> added = new ObjectOpenHashSet<>();
            for (GTRecipe recipe : recipes) {
                ItemStack output = ItemRecipeCapability.CAP
                        .of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                if (added.add(output)) {
                    tooltipComponents.add(
                            Component.translatable("behavior.data_item.assemblyline.data", output.getDisplayName()));
                }
            }
        }
       }
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack stack = context.getItemInHand();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
            MetaMachine machine = machineBlockEntity.getMetaMachine();
            Pair<GTRecipeType, String> researchData = ResearchManager.readResearchId(stack);
            if (machine instanceof MEPatternBufferPartMachine && researchData == null) {
                stack.getOrCreateTag().putIntArray("pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
                return InteractionResult.SUCCESS;
            } else if (machine instanceof MEPatternBufferProxy proxy) {
                if (stack.hasTag()) {
                    if (stack.getOrCreateTag().contains("pos", Tag.TAG_INT_ARRAY)) {
                        int[] posArray = stack.getOrCreateTag().getIntArray("pos");
                        BlockPos bufferPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
                        proxy.setIOBuffer(bufferPos);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return IInteractionItem.super.useOn(context);
    }
}
