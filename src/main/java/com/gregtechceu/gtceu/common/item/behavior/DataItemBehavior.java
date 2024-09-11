package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        Pair<GTRecipeType, String> researchData = ResearchManager.readResearchId(stack);
        if (researchData == null) {
            if (stack.has(GTDataComponents.DATA_COPY_POS)) {
                BlockPos posArray = stack.get(GTDataComponents.DATA_COPY_POS);
                tooltipComponents.add(Component.translatable(
                        "gtceu.tooltip.proxy_bind",
                        Component.literal("" + posArray.getX()).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray.getY()).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE)));
            }

        } else {
            Collection<GTRecipe> recipes = researchData.getFirst().getDataStickEntry(researchData.getSecond());
            if (recipes != null && !recipes.isEmpty()) {
                tooltipComponents.add(Component.translatable("behavior.data_item.assemblyline.title"));
                Collection<ItemStack> added = new ObjectOpenHashSet<>();
                outer:
                for (GTRecipe recipe : recipes) {
                    ItemStack output = ItemRecipeCapability.CAP
                            .of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                    for (var item : added) {
                        if (output.is(item.getItem())) continue outer;
                    }
                    if (added.add(output)) {
                        tooltipComponents.add(
                                Component.translatable("behavior.data_item.assemblyline.data",
                                        output.getDisplayName()));
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (!level.isClientSide) {
            MetaMachine machine = MetaMachine.getMachine(level, pos);
            Pair<GTRecipeType, String> researchData = ResearchManager.readResearchId(stack);
            if (machine instanceof MEPatternBufferPartMachine && researchData == null) {
                stack.set(GTDataComponents.DATA_COPY_POS, pos);
            } else if (machine instanceof MEPatternBufferProxyPartMachine proxy) {
                if (stack.has(GTDataComponents.DATA_COPY_POS)) {
                    BlockPos bufferPos = stack.get(GTDataComponents.DATA_COPY_POS);
                    proxy.setBuffer(bufferPos);
                }
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
