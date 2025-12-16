package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class DataItemBehavior implements IInteractionItem, IAddInformation, IDataItem {

    private final boolean requireDataBank;
    @Getter
    private final int capacity;

    public DataItemBehavior(boolean requireDataBank, int capacity) {
        this.requireDataBank = requireDataBank;
        this.capacity = capacity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(usedHand);
            stack.getOrCreateTag().putString("boundPlayerName", Component.Serializer.toJson(player.getDisplayName()));
            int perm = 0;
            while (player.hasPermissions(perm)) perm++;
            stack.getOrCreateTag().putInt("boundPlayerPermLevel", perm - 1);
            stack.getOrCreateTag().putString("boundPlayerUUID", player.getStringUUID());
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public boolean requireDataBank() {
        return requireDataBank;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        if (stack.getOrCreateTag().contains("boundPlayerName")) {
            MutableComponent name = Component.Serializer.fromJson(stack.getOrCreateTag().getString("boundPlayerName"));
            tooltipComponents.add(Component.translatable("gtceu.tooltip.player_bind", name));
        }
        if (stack.getOrCreateTag().contains("targetX")) {
            tooltipComponents.add(Component.translatable(
                    "gtceu.tooltip.wireless_transmitter_bind",
                    Component.literal("" + stack.getOrCreateTag().getInt("targetX")).withStyle(ChatFormatting.GOLD),
                    Component.literal("" + stack.getOrCreateTag().getInt("targetY")).withStyle(ChatFormatting.GOLD),
                    Component.literal("" + stack.getOrCreateTag().getInt("targetZ")).withStyle(ChatFormatting.GOLD),
                    Component.literal(stack.getOrCreateTag().getString("face")).withStyle(ChatFormatting.DARK_PURPLE),
                    Component.literal(stack.getOrCreateTag().getString("dim")).withStyle(ChatFormatting.GREEN)));
        }
        if (stack.getOrCreateTag().contains("computer_monitor_cover_config")) {
            tooltipComponents.add(Component.translatable("gtceu.tooltip.computer_monitor_config"));
        }
        if (stack.getOrCreateTag().contains("computer_monitor_cover_data")) {
            tooltipComponents.add(
                    Component.translatable("gtceu.tooltip.computer_monitor_data",
                            GTStringUtils.toComponent(
                                    stack.getOrCreateTag().getList("computer_monitor_cover_data", Tag.TAG_STRING))));
        }
        ResearchManager.ResearchItem researchData = ResearchManager.readResearchId(stack);
        if (researchData == null) {
            if (stack.getOrCreateTag().contains("pos", Tag.TAG_INT_ARRAY) && stack.hasTag()) {
                int[] posArray = stack.getOrCreateTag().getIntArray("pos");
                tooltipComponents.add(Component.translatable(
                        "gtceu.tooltip.proxy_bind",
                        Component.literal("" + posArray[0]).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray[1]).withStyle(ChatFormatting.LIGHT_PURPLE),
                        Component.literal("" + posArray[2]).withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
        } else {
            Collection<GTRecipe> recipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
            if (recipes != null && !recipes.isEmpty()) {
                tooltipComponents.add(Component.translatable("behavior.data_item.title",
                        Component.translatable(researchData.recipeType().registryName.toLanguageKey())));
                Collection<ItemStack> addedItems = new ObjectOpenHashSet<>();
                Collection<FluidStack> addedFluids = new ObjectOpenHashSet<>();
                outerItems:
                for (GTRecipe recipe : recipes) {
                    var contents = recipe.getOutputContents(ItemRecipeCapability.CAP);
                    if (contents.isEmpty()) continue;
                    ItemStack outputItems = ItemRecipeCapability.CAP
                            .of(contents.get(0).content).getItems()[0];
                    for (var item : addedItems) {
                        if (outputItems.is(item.getItem())) continue outerItems;
                    }
                    if (addedItems.add(outputItems)) {
                        tooltipComponents.add(
                                Component.translatable("behavior.data_item.data",
                                        outputItems.getDisplayName()));
                    }
                }
                outerFluids:
                for (GTRecipe recipe : recipes) {
                    var contents = recipe.getOutputContents(FluidRecipeCapability.CAP);
                    if (contents.isEmpty()) continue;
                    FluidStack outputFluids = FluidRecipeCapability.CAP
                            .of(contents.get(0).content).getStacks()[0];
                    for (var fluid : addedFluids) {
                        if (outputFluids.isFluidStackIdentical(fluid)) continue outerFluids;
                    }
                    if (addedFluids.add(outputFluids)) {
                        tooltipComponents.add(
                                Component.translatable("behavior.data_item.data",
                                        outputFluids.getDisplayName()));
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        ICoverable coverable = GTCapabilityHelper.getCoverable(context.getLevel(), context.getClickedPos(),
                context.getClickedFace());
        if (coverable != null &&
                coverable.getCoverAtSide(context.getClickedFace()) instanceof IDataStickInteractable interactable) {
            if (context.isSecondaryUseActive()) {
                if (ResearchManager.readResearchId(itemStack) == null) {
                    return interactable.onDataStickShiftUse(context.getPlayer(), itemStack);
                }
            } else {
                return interactable.onDataStickUse(context.getPlayer(), itemStack);
            }
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity blockEntity) {
            var machine = blockEntity.getMetaMachine();
            if (!MachineOwner.canOpenOwnerMachine(context.getPlayer(), machine)) {
                return InteractionResult.FAIL;
            }
            if (machine instanceof IDataStickInteractable interactable) {
                if (context.isSecondaryUseActive()) {
                    if (ResearchManager.readResearchId(itemStack) == null) {
                        return interactable.onDataStickShiftUse(context.getPlayer(), itemStack);
                    }
                } else {
                    return interactable.onDataStickUse(context.getPlayer(), itemStack);
                }
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
