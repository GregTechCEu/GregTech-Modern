package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LaserHatchPartMachine extends TieredIOPartMachine implements IDataInfoProvider {

    @SaveField
    private NotifiableLaserContainer buffer;

    public LaserHatchPartMachine(BlockEntityCreationInfo info, IO io, int tier, int amperage) {
        super(info, tier, io);
        if (io == IO.OUT) {
            this.buffer = NotifiableLaserContainer.emitterContainer(this, GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            this.buffer.setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.buffer = NotifiableLaserContainer.receiverContainer(this, GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
            this.buffer.setSideInputCondition(s -> s == getFrontFacing());
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return Collections.singletonList(Component.translatable(
                    String.format("%d/%d EU", buffer.getEnergyStored(), buffer.getEnergyCapacity())));
        }
        return new ArrayList<>();
    }

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }
}
