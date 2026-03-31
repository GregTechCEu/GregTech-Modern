package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.value.sync.DoubleSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.FluidSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.*;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets.createTankWidget;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CokeOvenMachine extends PrimitiveWorkableMachine implements IMuiMachine {

    public CokeOvenMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.defaultPanelBuilder(this).addTraitConfigurators(false)
                .addDefaultConfigurators(false);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        ITheme uiTheme = ThemeAPI.INSTANCE.getTheme(getDefinition().getThemeId());

        DoubleSyncValue progressPercent = syncManager.getOrCreateSyncHandler("progressPercent", DoubleSyncValue.class,
                () -> new DoubleSyncValue(() -> {
                    if (recipeLogic == null) return -1f;
                    return recipeLogic.getProgressPercent();
                }));

        Flow row = Flow.row().coverChildren();

        row.child(new ItemSlot().syncHandler(new ItemSlotSyncHandler(
                new ModularSlot(importItems.storage, 0)
                        .slotGroup(new SlotGroup("import_items", 1))))
                .background(uiTheme.getItemSlotTheme().getTheme().getBackground(),
                        GTGuiTextures.PRIMITIVE_FURNACE_OVERLAY))
                .child(new ProgressWidget()
                        .value(progressPercent)
                        .size(20, 15)
                        .texture(GTGuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, 18)
                        .margin(4, 0))

                .child(new ItemSlot().syncHandler(new ItemSlotSyncHandler(
                        new ModularSlot(exportItems.storage, 0)
                                .slotGroup(new SlotGroup("export_items", 1))
                                .accessibility(false, true)))
                        .background(uiTheme.getItemSlotTheme().getTheme().getBackground(),
                                GTGuiTextures.PRIMITIVE_FURNACE_OVERLAY))

                .child(createTankWidget()
                        .overlay(GTGuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY)
                        .background(GTGuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
                        .syncHandler(new FluidSlotSyncHandler(
                                exportFluids.getStorages()[0])
                                .canFillSlot(false))
                        .marginLeft(20));

        mainWidget.child(row);
    }

    @Override
    public void animateTick(RandomSource random) {
        if (this.isActive()) {
            final BlockPos pos = getBlockPos();
            float x = pos.getX() + 0.5F;
            float z = pos.getZ() + 0.5F;

            final var facing = getFrontFacing();
            final float horizontalOffset = GTValues.RNG.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + GTValues.RNG.nextFloat() * 0.375F + 0.3F;

            if (facing.getAxis() == Direction.Axis.X) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == Direction.Axis.Z) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            if (ConfigHolder.INSTANCE.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                getLevel().playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F,
                        false);
            }
            getLevel().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            getLevel().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isRemote()) {
            if (super.onUse(state, world, pos, player, hand, hit) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
            if (FluidUtil.interactWithFluidHandler(player, hand, exportFluids)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
