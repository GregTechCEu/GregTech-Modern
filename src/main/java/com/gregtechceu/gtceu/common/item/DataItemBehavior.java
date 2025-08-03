package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.items.IItemHandler;

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
                    Component.literal(stack.getOrCreateTag().getString("face")).withStyle(ChatFormatting.DARK_PURPLE)));
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
        if (stack.getOrCreateTag().contains("machineConfig")) {
            tooltipComponents.add(
                    Component.translatable(
                            "gtceu.tooltip.machine_config_data",
                            ItemStack.of(stack.getOrCreateTagElement("machineConfig").getCompound("item"))
                                    .getDisplayName()));
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
                if (context.isSecondaryUseActive()) {
                    saveMachineConfig(machine, itemStack.getOrCreateTagElement("machineConfig"));
                } else if (itemStack.getOrCreateTag().contains("machineConfig")) {
                    loadMachineConfig(machine, itemStack.getTagElement("machineConfig"), null);
                }
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    protected void saveMachineConfig(MetaMachine machine, CompoundTag nbt) {
        nbt.put("item", machine.getDefinition().asStack().serializeNBT());
        if (machine instanceof IAutoOutputItem output) {
            CompoundTag tag = new CompoundTag();
            Direction direction = output.getOutputFacingItems();
            if (direction != null)
                tag.putString("direction", direction.toString());
            tag.putBoolean("allowInputFromOutputSide", output.isAllowInputFromOutputSideItems());
            tag.putBoolean("auto", output.isAutoOutputItems());
            nbt.put("itemOutput", tag);
        }
        if (machine instanceof IAutoOutputFluid output) {
            CompoundTag tag = new CompoundTag();
            Direction direction = output.getOutputFacingFluids();
            if (direction != null)
                tag.putString("direction", direction.toString());
            tag.putBoolean("allowInputFromOutputSide", output.isAllowInputFromOutputSideFluids());
            tag.putBoolean("auto", output.isAutoOutputFluids());
            nbt.put("fluidOutput", tag);
        }
        if (machine instanceof SimpleTieredMachine simpleTieredMachine) {
            CompoundTag tag = new CompoundTag();
            tag.put("storage", simpleTieredMachine.getCircuitInventory().storage.serializeNBT());
            nbt.put("circuitInventory", tag);
        }
        {
            CompoundTag tag = new CompoundTag();
            for (Direction face : Direction.values()) {
                CoverBehavior cover = machine.getCoverContainer().getCoverAtSide(face);
                if (cover == null) continue;
                CompoundTag coverTag = new CompoundTag();
                coverTag.put("item", cover.getAttachItem().serializeNBT());
                tag.put(face.getName(), coverTag);
            }
            nbt.put("covers", tag);
        }
    }

    protected void loadMachineConfig(MetaMachine machine, CompoundTag nbt, @Nullable IItemHandler itemHandler) {
        if (machine instanceof IAutoOutputItem output && nbt.contains("itemOutput")) {
            CompoundTag tag = nbt.getCompound("itemOutput");
            if (tag.contains("direction"))
                output.setOutputFacingItems(Direction.byName(tag.getString("direction")));
            output.setAllowInputFromOutputSideItems(tag.getBoolean("allowInputFromOutputSide"));
            output.setAutoOutputItems(tag.getBoolean("auto"));
        }
        if (machine instanceof IAutoOutputFluid output && nbt.contains("fluidOutput")) {
            CompoundTag tag = nbt.getCompound("fluidOutput");
            if (tag.contains("direction"))
                output.setOutputFacingFluids(Direction.byName(tag.getString("direction")));
            output.setAllowInputFromOutputSideFluids(tag.getBoolean("allowInputFromOutputSide"));
            output.setAutoOutputFluids(tag.getBoolean("auto"));
        }
        if (machine instanceof SimpleTieredMachine simpleTieredMachine && nbt.contains("circuitInventory")) {
            CompoundTag tag = nbt.getCompound("circuitInventory");
            simpleTieredMachine.getCircuitInventory().storage.deserializeNBT(tag.getCompound("storage"));
        }
        if (itemHandler != null) {
            CompoundTag tag = nbt.getCompound("covers");
            for (Direction face : Direction.values()) {
                if (!tag.contains(face.getSerializedName())) continue;
                ItemStack coverStack = ItemStack.of(tag.getCompound(face.getSerializedName()).getCompound("item"));
                if (coverStack.isEmpty()) continue;
                boolean foundItem = false;
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    ItemStack stack = itemHandler.extractItem(slot, 1, true);
                    if (stack.is(coverStack.getItem())) {
                        itemHandler.extractItem(slot, 1, false);
                        foundItem = true;
                    }
                }
                if (!foundItem) continue;
                if (coverStack.getItem() instanceof IComponentItem item) {
                    for (IItemComponent component : item.getComponents()) {
                        if (component instanceof CoverPlaceBehavior coverPlaceBehavior) {
                            coverPlaceBehavior.coverDefinition().createCoverBehavior(machine.getCoverContainer(), face);
                        }
                    }
                }
            }
        }
    }
}
