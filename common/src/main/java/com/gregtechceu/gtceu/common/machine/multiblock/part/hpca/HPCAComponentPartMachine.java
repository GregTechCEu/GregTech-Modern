package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCAComputationProvider;
import com.gregtechceu.gtceu.api.machine.trait.hpca.IHPCACoolantProvider;
import com.gregtechceu.gtceu.client.TooltipHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public abstract class HPCAComponentPartMachine extends TieredPartMachine implements IHPCAComponentHatch, IMachineModifyDrops {

    @Persisted
    @DescSynced
    private boolean damaged;

    public HPCAComponentPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.ZPM);
    }

    public abstract boolean isAdvanced();

    public boolean doesAllowBridging() {
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer;
            var controller = getController();
            if (controller != null && controller.isActive()) {
                renderer = getFrontActiveOverlay();
            } else {
                renderer = getFrontOverlay();
            }
            if (renderer != null) {
                EnumFacing facing = getFrontFacing();
                // always render this outwards, in case it is not placed outwards in structure
                if (controller != null) {
                    facing = controller.getFrontFacing().rotateY();
                }
                renderer.renderSided(facing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return isAdvanced() ? Textures.ADVANCED_COMPUTER_CASING : Textures.COMPUTER_CASING;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        if (isBridge()) {
            tooltip.add(Component.translatable("gtceu.machine.hpca.component_type.bridge"));
        }

        final int upkeepEUt = getUpkeepEUt();
        final int maxEUt = getMaxEUt();
        if (upkeepEUt != 0 && upkeepEUt != maxEUt) {
            tooltip.add(Component.translatable("gtceu.machine.hpca.component_general.upkeep_eut", upkeepEUt));
        }
        if (maxEUt != 0) {
            tooltip.add(Component.translatable("gtceu.machine.hpca.component_general.max_eut", maxEUt));
        }

        if (this instanceof IHPCACoolantProvider provider) {
            if (provider.isActiveCooler()) {
                tooltip.add(Component.translatable("gtceu.machine.hpca.component_type.cooler_active"));
                tooltip.add(Component.translatable("gtceu.machine.hpca.component_type.cooler_active_coolant",
                        provider.getMaxCoolantPerTick(), Component.translatable(Materials.PCBCoolant.getUnlocalizedName())));
            } else {
                tooltip.add(Component.translatable("gtceu.machine.hpca.component_type.cooler_passive"));
            }
            tooltip.add(Component.translatable("gtceu.machine.hpca.component_type.cooler_cooling", provider.getCoolingAmount()));
        }

        if (this instanceof IHPCAComputationProvider provider) {
            tooltip.add(Component.translatable("gtceu.machine.hpca.component_type.computation_cwut", provider.getCWUPerTick()));
            tooltip.add(Component.translatable("gtceu.machine.hpca.component_type.computation_cooling", provider.getCoolingPerTick()));
        }

        if (canBeDamaged()) {
            tooltip.add(TooltipHelper.BLINKING_ORANGE + Component.translatable("gtceu.machine.hpca.component_type.damaged"));
        }
    }

    // Unnecessary to show this since it is just Wrench rotation
    @Override
    public boolean showToolUsages() {
        return false;
    }

    // Handle damaged state

    @Override
    public final boolean isBridge() {
        return doesAllowBridging() && !(canBeDamaged() && isDamaged());
    }

    @Override
    public boolean isDamaged() {
        return canBeDamaged() && damaged;
    }

    @Override
    public void setDamaged(boolean damaged) {
        if (!canBeDamaged()) return;
        if (this.damaged != damaged) {
            this.damaged = damaged;
            markDirty();
        }
    }


    @Override
    public String getMetaName() {
        if (canBeDamaged() && isDamaged()) {
            return super.getMetaName() + ".damaged";
        }
        return super.getMetaName();
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        if (canBeDamaged() && isDamaged()) {
            if (isAdvanced()) {
                drops.add(GTBlocks.ADVANCED_COMPUTER_CASING.asStack());
            } else {
                drops.add(GTBlocks.COMPUTER_CASING.asStack());
            }
        }
    }
}