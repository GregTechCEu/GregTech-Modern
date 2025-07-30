package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockFluid;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

public class PortableScannerBehavior implements IInteractionItem, IAddInformation {

    private int debugLevel = 0;

    @Getter
    public enum DisplayMode {

        SHOW_ALL("behavior.portable_scanner.mode.show_all_info"),
        SHOW_BLOCK_INFO("behavior.portable_scanner.mode.show_block_info"),
        SHOW_MACHINE_INFO("behavior.portable_scanner.mode.show_machine_info"),
        SHOW_ELECTRICAL_INFO("behavior.portable_scanner.mode.show_electrical_info"),
        SHOW_RECIPE_INFO("behavior.portable_scanner.mode.show_recipe_info"),
        SHOW_ENVIRONMENTAL_INFO("behavior.portable_scanner.mode.show_environmental_info");

        private final String langKey;

        DisplayMode(String langKey) {
            this.langKey = langKey;
        }
    }

    public PortableScannerBehavior(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();

        if (!level.isClientSide() && !level.isEmptyBlock(pos) && player != null) {

            List<Component> list = new ArrayList<>();
            int energyCost = addScannerInfo(player, level, pos, getMode(stack), list);

            if (player.isCreative()) {
                energyCost = 0;
            }

            if (energyCost > 0 && !drainEnergy(stack, energyCost, true)) {
                player.sendSystemMessage(Component.translatable("behavior.prospector.not_enough_energy"));
                return InteractionResult.CONSUME;
            }

            drainEnergy(stack, energyCost, false);
            for (var line : list) {
                player.sendSystemMessage(line);
            }

            GTSoundEntries.PORTABLE_SCANNER.play(level, null, player.position(), 1.0f, 1.0f);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                setNextMode(heldItem);
                var mode = getMode(heldItem);
                player.sendSystemMessage(Component.translatable("behavior.portable_scanner.mode.caption",
                        Component.translatable(mode.getLangKey())));
            }
            return InteractionResultHolder.success(heldItem);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    protected boolean drainEnergy(@Nonnull ItemStack stack, int amount, boolean simulate) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;
        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    protected void setNextMode(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        tag.putInt("Mode", (tag.getInt("Mode") + 1) % DisplayMode.values().length);
    }

    @Nonnull
    protected DisplayMode getMode(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return DisplayMode.SHOW_ALL;
        }
        var tag = stack.getTag();
        if (tag == null) {
            return DisplayMode.SHOW_ALL;
        }
        return DisplayMode.values()[tag.getInt("Mode") % DisplayMode.values().length];
    }

    public int addScannerInfo(Player player, Level level, BlockPos pos, DisplayMode mode, List<Component> list) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        int energyCost = 0;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // General information
        if (mode == DisplayMode.SHOW_ALL || mode == DisplayMode.SHOW_BLOCK_INFO) {

            // Coordinates of the block
            list.add(Component.translatable("behavior.portable_scanner.position",
                    Component.translatable(FormattingUtil.formatNumbers(pos.getX()))
                            .withStyle(ChatFormatting.AQUA),
                    Component.translatable(FormattingUtil.formatNumbers(pos.getY()))
                            .withStyle(ChatFormatting.AQUA),
                    Component.translatable(FormattingUtil.formatNumbers(pos.getZ()))
                            .withStyle(ChatFormatting.AQUA),
                    Component.translatable(level.dimension().location().toString())
                            .withStyle(ChatFormatting.AQUA)));

            // Hardness and blast resistance
            list.add(Component.translatable("behavior.portable_scanner.block_hardness",
                    Component.translatable(
                            FormattingUtil.formatNumbers(block.defaultDestroyTime()))
                            .withStyle(ChatFormatting.YELLOW),
                    Component.translatable(FormattingUtil.formatNumbers(block.getExplosionResistance()))
                            .withStyle(ChatFormatting.YELLOW)));

            // Possible block states
            if (debugLevel > 2) {
                state.getProperties().forEach((property) -> {
                    list.add(Component.translatable("behavior.portable_scanner.state",
                            Component.translatable(property.getName()),
                            Component.translatable(state.getValue(property).toString())
                                    .withStyle(ChatFormatting.AQUA)));
                });
            }
        }

        if (tileEntity instanceof IMachineBlockEntity machineBlockEntity) {
            MetaMachine machine = machineBlockEntity.getMetaMachine();

            list.add(Component.translatable(state.getBlock().getDescriptionId()).withStyle(ChatFormatting.BLUE));

            // General machine information
            if (mode == DisplayMode.SHOW_ALL || mode == DisplayMode.SHOW_MACHINE_INFO) {
                if (machine.getOwner() != null) {
                    machine.getOwner().displayInfo(list);
                }

                if (machine.getDefinition().isAllowExtendedFacing()) {
                    list.add(Component.translatable("behavior.portable_scanner.divider"));

                    list.add(Component.translatable("behavior.portable_scanner.machine_front_facing",
                            machine.getFrontFacing().getSerializedName()));
                    list.add(Component.translatable("behavior.portable_scanner.machine_upwards_facing",
                            machineBlockEntity.self().getBlockState().getValue(GTBlockStateProperties.UPWARDS_FACING)
                                    .getSerializedName()));
                }

                // Fluid tanks
                Optional<IFluidHandler> fluidCap = tileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();
                if (fluidCap.isPresent()) {
                    list.add(Component.translatable("behavior.portable_scanner.divider"));
                    IFluidHandler fluidHandler = fluidCap.get();
                    boolean allTanksEmpty = true;

                    for (int i = 0; i < fluidHandler.getTanks(); i++) {
                        FluidStack fluidStack = fluidHandler.getFluidInTank(i);

                        if (fluidStack.getFluid() == null || fluidStack.isEmpty()) {
                            continue;
                        }

                        energyCost += 500;
                        allTanksEmpty = false;
                        list.add(Component.translatable("behavior.portable_scanner.tank", i,
                                Component.translatable(FormattingUtil.formatNumbers(fluidStack.getAmount()))
                                        .withStyle(ChatFormatting.GREEN),
                                Component.translatable(FormattingUtil.formatNumbers(fluidHandler.getTankCapacity(i)))
                                        .withStyle(ChatFormatting.YELLOW),
                                Component.translatable(fluidStack.getTranslationKey())
                                        .withStyle(ChatFormatting.GOLD)));
                    }

                    if (allTanksEmpty) {
                        list.add(Component.translatable("behavior.portable_scanner.tanks_empty"));
                    }
                }

                // Sound muffling
                if (machine instanceof IMufflableMachine mufflableMachine) {
                    energyCost += 500;
                    if (mufflableMachine.isMuffled()) {
                        list.add(Component.translatable("behavior.portable_scanner.divider"));
                        list.add(Component.translatable("behavior.portable_scanner.muffled")
                                .withStyle(ChatFormatting.GREEN));
                    }
                }
            }

            // Energy related information
            if (mode == DisplayMode.SHOW_ALL || mode == DisplayMode.SHOW_ELECTRICAL_INFO) {

                // Energy container
                Optional<IEnergyContainer> energyCap = tileEntity
                        .getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER).resolve();
                if (energyCap.isPresent()) {
                    IEnergyContainer energyContainer = energyCap.get();
                    if (energyContainer.getInputVoltage() > 0) {
                        list.add(Component.translatable("behavior.portable_scanner.divider"));
                        list.add(Component.translatable("behavior.portable_scanner.energy_container_in",
                                Component.translatable(FormattingUtil.formatNumbers(energyContainer.getInputVoltage()))
                                        .withStyle(ChatFormatting.RED),
                                Component.translatable(
                                        GTValues.VN[GTUtil.getTierByVoltage(energyContainer.getInputVoltage())])
                                        .withStyle(ChatFormatting.RED),
                                Component.translatable(FormattingUtil.formatNumbers(energyContainer.getInputAmperage()))
                                        .withStyle(ChatFormatting.RED)));
                    }
                    if (energyContainer.getOutputVoltage() > 0) {
                        list.add(Component.translatable("behavior.portable_scanner.divider"));
                        list.add(Component.translatable("behavior.portable_scanner.energy_container_out",
                                Component.translatable(FormattingUtil.formatNumbers(energyContainer.getOutputVoltage()))
                                        .withStyle(ChatFormatting.RED),
                                Component.translatable(
                                        GTValues.VN[GTUtil.getTierByVoltage(energyContainer.getOutputVoltage())])
                                        .withStyle(ChatFormatting.RED),
                                Component.translatable(
                                        FormattingUtil.formatNumbers(energyContainer.getOutputAmperage()))
                                        .withStyle(ChatFormatting.RED)));
                    }
                    list.add(Component.translatable("behavior.portable_scanner.energy_container_storage",
                            Component.translatable(FormattingUtil.formatNumbers(energyContainer.getEnergyStored()))
                                    .withStyle(ChatFormatting.GREEN),
                            Component.translatable(FormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()))
                                    .withStyle(ChatFormatting.YELLOW)));
                }
            }

            // Recipe related information
            if (mode == DisplayMode.SHOW_ALL || mode == DisplayMode.SHOW_RECIPE_INFO) {

                // Workable progress info
                if (machine instanceof IWorkable workableMachine) {
                    energyCost += 400;
                    if (!workableMachine.isWorkingEnabled()) {
                        list.add(Component.translatable("behavior.portable_scanner.divider"));
                        list.add(Component.translatable("behavior.portable_scanner.machine_disabled")
                                .withStyle(ChatFormatting.RED));
                    }
                    if (workableMachine.getMaxProgress() > 0) {
                        list.add(Component.translatable("behavior.portable_scanner.divider"));
                        list.add(Component.translatable("behavior.portable_scanner.machine_progress",
                                Component.translatable(FormattingUtil.formatNumbers(workableMachine.getProgress()))
                                        .withStyle(ChatFormatting.GREEN),
                                Component.translatable(FormattingUtil.formatNumbers(workableMachine.getMaxProgress()))
                                        .withStyle(ChatFormatting.YELLOW)));
                    }
                }

                // Recipe logic for EU production/consumption
                Optional<RecipeLogic> recipeLogicCap = tileEntity.getCapability(GTCapability.CAPABILITY_RECIPE_LOGIC)
                        .resolve();
                if (recipeLogicCap.isPresent()) {
                    RecipeLogic recipeLogic = recipeLogicCap.get();
                    GTRecipe recipe = recipeLogic.getLastRecipe();
                    if (recipeLogic.getStatus().equals(RecipeLogic.Status.WAITING)) {
                        list.add(Component.translatable("behavior.portable_scanner.divider"));
                        list.add(Component.translatable("gtceu.multiblock.waiting"));
                        list.addAll(recipeLogic.getFancyTooltip());
                    } else if (recipe != null) {
                        list.add(Component.translatable("behavior.portable_scanner.divider"));
                        var EUt = RecipeHelper.getRealEUtWithIO(recipe);

                        list.add(Component.translatable(
                                EUt.isInput() ? "behavior.portable_scanner.workable_consumption" :
                                        "behavior.portable_scanner.workable_production",
                                // TODO is this supposed to show voltage or total EU/t?
                                Component.translatable(FormattingUtil.formatNumbers(EUt.getTotalEU()))
                                        .withStyle(ChatFormatting.RED),
                                Component.translatable(
                                        FormattingUtil.formatNumbers(EUt.amperage()))
                                        .withStyle(ChatFormatting.RED)));
                    }
                }
            }

            // machine-specific info
            IDataInfoProvider provider = null;
            if (tileEntity instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) tileEntity;
            else if (machine instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) machine;

            if (provider != null) {
                list.add(Component.translatable("behavior.portable_scanner.divider"));
                list.addAll(provider.getDataInfo(mode));
            }

        } else if (tileEntity instanceof PipeBlockEntity<?, ?> pipe) {

            // Pipes need special name handling
            list.add(pipe.getPipeBlock().getName().withStyle(ChatFormatting.BLUE));

            // Pipe-specific info
            if (tileEntity instanceof IDataInfoProvider dataInfoProvider) {
                list.add(Component.translatable("behavior.portable_scanner.divider"));
                list.addAll(dataInfoProvider.getDataInfo(mode));
            }

            if (tileEntity instanceof FluidPipeBlockEntity) {
                // Getting fluid info always costs 500
                energyCost += 500;
            }
        } else if (tileEntity instanceof IDataInfoProvider dataInfoProvider) {
            list.add(Component.translatable("behavior.portable_scanner.divider"));
            list.addAll(dataInfoProvider.getDataInfo(mode));
        } else {
            list.add(Component.translatable(state.getBlock().getDescriptionId()).withStyle(ChatFormatting.BLUE));
        }

        // Environmental information
        if (mode == DisplayMode.SHOW_ALL || mode == DisplayMode.SHOW_ENVIRONMENTAL_INFO) {

            // Bedrock fluids
            if (level instanceof ServerLevel serverLevel) {
                list.add(Component.translatable("behavior.portable_scanner.divider"));
                var veinData = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
                int chunkX = pos.getX() >> 4;
                int chunkZ = pos.getZ() >> 4;
                Fluid fluid = veinData.getFluidInChunk(chunkX, chunkZ);

                if (fluid != null) {
                    FluidStack stack = new FluidStack(fluid,
                            veinData.getOperationsRemaining(chunkX, chunkZ));
                    double fluidPercent = stack.getAmount() * 100.0 / BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS;

                    var fluidInfo = ProspectorMode.FluidInfo
                            .fromVeinWorldEntry(veinData.getFluidVeinWorldEntry(chunkX, chunkZ));
                    var packet = new SPacketProspectBedrockFluid(level.dimension(), pos, fluidInfo);
                    GTNetwork.sendToPlayer((ServerPlayer) player, packet);

                    if (player.isCreative()) {
                        list.add(Component.translatable("behavior.portable_scanner.bedrock_fluid.amount",
                                Component.translatable(stack.getTranslationKey())
                                        .withStyle(ChatFormatting.GOLD),
                                Component.translatable(String.valueOf(
                                        veinData.getFluidYield(chunkX, chunkZ)))
                                        .withStyle(ChatFormatting.GOLD),
                                Component.translatable(String.valueOf(fluidPercent))
                                        .withStyle(ChatFormatting.YELLOW)));
                    } else {
                        list.add(Component.translatable("behavior.portable_scanner.bedrock_fluid.amount_unknown",
                                Component.translatable(String.valueOf(fluidPercent))
                                        .withStyle(ChatFormatting.YELLOW)));
                    }
                } else {
                    list.add(Component.translatable("behavior.portable_scanner.bedrock_fluid.nothing"));
                }

                // Pollution
                var environmental = EnvironmentalHazardSavedData.getOrCreate(serverLevel);
                var environmentHazardZone = environmental.getZoneByContainedPos(pos);
                if (environmentHazardZone != null) {
                    list.add(Component.translatable("behavior.portable_scanner.environmental_hazard",
                            Component.translatable("gtceu.medical_condition." + environmentHazardZone.condition().name),
                            Component.literal(FormattingUtil.formatNumbers(environmentHazardZone.strength()))));
                } else {
                    list.add(Component.translatable("behavior.portable_scanner.environmental_hazard.nothing"));
                }

                var local = LocalizedHazardSavedData.getOrCreate(serverLevel);
                var localHazardZone = local.getZoneByContainedPos(pos);
                if (localHazardZone != null) {
                    list.add(Component.translatable("behavior.portable_scanner.local_hazard",
                            Component.translatable("gtceu.medical_condition." + localHazardZone.condition().name),
                            Component.literal(FormattingUtil.formatNumbers(localHazardZone.strength()))));
                } else {
                    list.add(Component.translatable("behavior.portable_scanner.local_hazard.nothing"));
                }
            }
        }

        // Add optional debug info
        if (tileEntity instanceof IDataInfoProvider dataInfoProvider) {
            List<Component> debugInfo = dataInfoProvider.getDebugInfo(player, debugLevel, mode);
            if (debugInfo != null) {
                list.addAll(debugInfo);
            }
        }

        return energyCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("metaitem.behavior.mode_switch.tooltip"));
        tooltipComponents.add(Component.translatable("behavior.portable_scanner.mode.caption",
                Component.translatable(getMode(stack).getLangKey()).withStyle(ChatFormatting.AQUA)));
    }
}
