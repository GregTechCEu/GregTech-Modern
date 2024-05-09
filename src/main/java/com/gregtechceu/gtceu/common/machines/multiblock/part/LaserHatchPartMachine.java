package com.gregtechceu.gtceu.common.machines.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machines.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machines.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machines.trait.NotifiableLaserContainer;
import com.gregtechceu.gtceu.common.items.PortableScannerBehavior;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class LaserHatchPartMachine extends TieredIOPartMachine implements IDataInfoProvider {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TieredIOPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private NotifiableLaserContainer buffer;

    public LaserHatchPartMachine(IMachineBlockEntity holder, IO io, int tier, int amperage) {
        super(holder, tier, io);
        if (io == IO.OUT) {
            this.buffer = NotifiableLaserContainer.emitterContainer(this, GTValues.V[tier] * 64L * amperage, GTValues.V[tier], amperage);
            this.buffer.setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.buffer = NotifiableLaserContainer.receiverContainer(this, GTValues.V[tier] * 64L * amperage, GTValues.V[tier], amperage);
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return Collections.singletonList(Component.translatable(
                String.format("%d/%d EU", buffer.getEnergyStored(), buffer.getEnergyCapacity())));
        }
        return new ArrayList<>();
    }
}
