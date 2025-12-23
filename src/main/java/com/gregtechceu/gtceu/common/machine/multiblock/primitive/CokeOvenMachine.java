package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CokeOvenMachine extends PrimitiveWorkableMachine implements IMuiMachine {

    public CokeOvenMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new ModularPanel(this.getDefinition().getName())
                .size(176, 166)
                .background(GTGuiTextures.BACKGROUND_PRIMITIVE)
                // Top half of the screen
                .child(createImportSlot(syncManager).margin(52, 0, 30, 0))
                .child(new ProgressWidget().progress(recipeLogic::getProgressPercent).size(20, 15)
                        .texture(GTGuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, 18).margin(76, 32))
                .child(createExportSlot(syncManager).margin(103, 0, 30, 0))
                .child(GTMuiWidgets.createTitleBar(getDefinition(), 176, GTGuiTextures.BACKGROUND_PRIMITIVE))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * return new ModularUI(176, 166, this, entityPlayer)
     * .background(GuiTextures.PRIMITIVE_BACKGROUND)
     * .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))
     * .widget(new SlotWidget(importItems.storage, 0, 52, 30, true, true)
     * .setBackgroundTexture(
     * new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
     * .widget(new ProgressWidget(recipeLogic::getProgressPercent, 76, 32, 20, 15,
     * GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR))
     * .widget(new SlotWidget(exportItems.storage, 0, 103, 30, true, false)
     * .setBackgroundTexture(
     * new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
     * .widget(new TankWidget(exportFluids.getStorages()[0], 134, 13, 20, 58, true, false)
     * .setBackground(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
     * .setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP)
     * .setShowAmountOverlay(false)
     * .setOverlay(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY))
     * .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.PRIMITIVE_SLOT, 7, 84,
     * true));
     * }
     */
    private ItemSlot createImportSlot(PanelSyncManager syncManager) {
        syncManager.syncValue("import", new ItemSlotSH(
                new ModularSlot(importItems.storage, 0)
                        .slotGroup(new SlotGroup("import", 1))
                        .accessibility(true, true)));
        return new ItemSlot()
                .syncHandler("import", 0)
                .background(GTGuiTextures.SLOT_PRIMITIVE, GTGuiTextures.PRIMITIVE_FURNACE_OVERLAY);
    }

    private ItemSlot createExportSlot(PanelSyncManager syncManager) {
        syncManager.syncValue("export", new ItemSlotSH(
                new ModularSlot(exportItems.storage, 0)
                        .slotGroup(new SlotGroup("export", 1))
                        .accessibility(false, true)));
        return new ItemSlot()
                .syncHandler("export", 0)
                .background(GTGuiTextures.SLOT_PRIMITIVE, GTGuiTextures.PRIMITIVE_FURNACE_OVERLAY);
    }

    @Override
    public void animateTick(RandomSource random) {
        if (this.isActive()) {
            final BlockPos pos = getPos();
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
