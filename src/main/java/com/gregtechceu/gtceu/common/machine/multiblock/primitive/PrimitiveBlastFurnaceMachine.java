package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IFluidRenderMulti;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveBlastFurnaceMachine extends PrimitiveWorkableMachine implements IMuiMachine, IFluidRenderMulti {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            PrimitiveBlastFurnaceMachine.class, PrimitiveWorkableMachine.MANAGED_FIELD_HOLDER);

    private TickableSubscription hurtSubscription;

    @Getter
    @Setter
    @DescSynced
    @RequireRerender
    private @NotNull Set<BlockPos> fluidBlockOffsets = new HashSet<>();

    public PrimitiveBlastFurnaceMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxInputs(ItemRecipeCapability.CAP), IO.IN,
                IO.NONE);
    }

    @Override
    protected NotifiableItemStackHandler createExportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP), IO.OUT,
                IO.NONE);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unsubscribe(hurtSubscription);
        hurtSubscription = null;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        IFluidRenderMulti.super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        IFluidRenderMulti.super.onStructureInvalid();
    }

    @Override
    public void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        super.notifyStatusChanged(oldStatus, newStatus);
        if (newStatus == RecipeLogic.Status.WORKING) {
            this.hurtSubscription = subscribeServerTick(this.hurtSubscription, this::hurtEntitiesAndBreakSnow);
        } else if (oldStatus == RecipeLogic.Status.WORKING && hurtSubscription != null) {
            unsubscribe(hurtSubscription);
            hurtSubscription = null;
        }
    }

    @Override
    public @NotNull Set<BlockPos> saveOffsets() {
        return Collections.singleton(new BlockPos(getFrontFacing().getOpposite().getNormal()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        if (isFormed) {
            var pos = this.getPos();
            var facing = this.getFrontFacing().getOpposite();
            float xPos = facing.getStepX() * 0.76F + pos.getX() + 0.5F;
            float yPos = facing.getStepY() * 0.76F + pos.getY() + 0.25F;
            float zPos = facing.getStepZ() * 0.76F + pos.getZ() + 0.5F;

            var up = RelativeDirection.UP.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped());
            var sign = up.getAxisDirection().getStep();
            var shouldX = up.getAxis() == Direction.Axis.X;
            var shouldY = up.getAxis() == Direction.Axis.Y;
            var shouldZ = up.getAxis() == Direction.Axis.Z;
            var speed = ((shouldY ? facing.getStepY() : shouldX ? facing.getStepX() : facing.getStepZ()) * 0.1F + 0.2F +
                    0.1F * GTValues.RNG.nextFloat()) * sign;
            if (getOffsetTimer() % 20 == 0) {
                getLevel().addParticle(ParticleTypes.LAVA, xPos, yPos, zPos,
                        shouldX ? speed * 2 : 0,
                        shouldY ? speed * 2 : 0,
                        shouldZ ? speed * 2 : 0);
            }
            if (isActive()) {
                getLevel().addParticle(ParticleTypes.LARGE_SMOKE, xPos, yPos, zPos,
                        shouldX ? speed : 0,
                        shouldY ? speed : 0,
                        shouldZ ? speed : 0);
            }
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {


        return new ModularPanel(this.getDefinition().getName())
                .size(176,166)
                .background(GTGuiTextures.BACKGROUND_PRIMITIVE)
                        // Top half of the screen
                                // Box that has the display texture BG +
                                // the buttons / text / etc

                                .child(createImportItemSlot(syncManager).margin(52,16))

                                .child(new ProgressWidget().progress(recipeLogic::getProgressPercent) // 77, 39, 20, 15,
                                        .texture(GTGuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR,0).margin(77,39,34,15))

                                .child(createExportItemSlot(syncManager).margin(104,34))


                .child(GTMuiWidgets.createTitleBar(getDefinition(), 176, GTGuiTextures.BACKGROUND_PRIMITIVE))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
/*
        var a = new ModularUI(176, 166, this)
                .background(GuiTextures.PRIMITIVE_BACKGROUND)
                .widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()))

                .widget(new SlotWidget(importItems.storage, 0, 52, 20, true, true)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_INGOT_OVERLAY)))
                .widget(new SlotWidget(importItems.storage, 1, 52, 38, true, true)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_DUST_OVERLAY)))
                .widget(new SlotWidget(importItems.storage, 2, 52, 56, true, true)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY)))
                .widget(new ProgressWidget(recipeLogic::getProgressPercent, 77, 39, 20, 15,
                        GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR))
                .widget(new SlotWidget(exportItems.storage, 0, 104, 38, true, false)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_INGOT_OVERLAY)))
                .widget(new SlotWidget(exportItems.storage, 1, 122, 38, true, false)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_DUST_OVERLAY)))
                .widget(new SlotWidget(exportItems.storage, 2, 140, 38, true, false)
                        .setBackgroundTexture(
                                new GuiTextureGroup(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_DUST_OVERLAY)))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory()
                        GuiTextures.PRIMITIVE_SLOT, 7, 84, true));*/
    }

    private SlotGroupWidget createImportItemSlot(PanelSyncManager syncManager) {
        int size = importItems.storage.getSlots();
        SlotGroup slotGroup = new SlotGroup("import", size);
        String[] matrix = new String[size];
        char key = 'I';
        Arrays.fill(matrix, String.valueOf(key));
        return SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> {
                    ModularSlot slot = new ModularSlot(importItems.storage, i);
                    ItemSlotSH syncHandler = new ItemSlotSH(slot.slotGroup(slotGroup));
                    syncManager.syncValue("import", i, syncHandler);
                    return new ItemSlot()
                            .syncHandler("import", i).background((i == 0)? GTGuiTextures.PRIMITIVE_INGOT_OVERLAY : (i == 1)? GTGuiTextures.PRIMITIVE_DUST_OVERLAY : GTGuiTextures.PRIMITIVE_FURNACE_OVERLAY);
                })
                .build().background(GTGuiTextures.SLOT_PRIMITIVE);
    }

    private SlotGroupWidget createExportItemSlot(PanelSyncManager syncManager) {
        int size = exportItems.storage.getSlots();
        SlotGroup slotGroup = new SlotGroup("export", size);
        String[] matrix = new String[1];
        char key = 'I';
        matrix[0] = String.valueOf(key).repeat(size);
        return SlotGroupWidget.builder()
                .matrix(matrix)
                .key(key, i -> {
                    ModularSlot slot = new ModularSlot(exportItems.storage, i);
                    slot.accessibility(false,true);
                    ItemSlotSH syncHandler = new ItemSlotSH(slot.slotGroup(slotGroup));
                    syncManager.syncValue("export", i, syncHandler);
                    return new ItemSlot()
                            .syncHandler("export", i).background((i == 0)? GTGuiTextures.PRIMITIVE_INGOT_OVERLAY : GTGuiTextures.PRIMITIVE_DUST_OVERLAY);
                })
                .build().background(GTGuiTextures.SLOT_PRIMITIVE);
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
                getLevel().playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE,
                        SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            getLevel().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            getLevel().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    private void hurtEntitiesAndBreakSnow() {
        BlockPos middlePos = self().getPos().offset(getFrontFacing().getOpposite().getNormal());
        getLevel().getEntities(null, new AABB(middlePos)).forEach(e -> e.hurt(e.damageSources().lava(), 3.0f));

        if (getOffsetTimer() % 10 == 0) {
            BlockState state = getLevel().getBlockState(middlePos);
            GTUtil.tryBreakSnow(getLevel(), middlePos, state, true);
        }
    }
}
