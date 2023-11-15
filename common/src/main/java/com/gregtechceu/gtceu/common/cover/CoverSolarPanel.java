package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class CoverSolarPanel extends CoverBehavior {
    private final long EUt;
    protected TickableSubscription subscription;

    public CoverSolarPanel(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);
        this.EUt = GTValues.V[tier];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public boolean canAttach() {
        return attachedSide == Direction.UP && getEnergyContainer() != null;
    }


    protected void update() {
        Level level = coverHolder.getLevel();
        BlockPos blockPos = coverHolder.getPos();
        if (GTUtil.canSeeSunClearly(level, blockPos)) {
            IEnergyContainer energyContainer = getEnergyContainer();
            if (energyContainer != null) {
                energyContainer.acceptEnergyFromNetwork(null, EUt, 1);
            }
        }

    }

    @Nullable
    protected IEnergyContainer getEnergyContainer() {
        return GTCapabilityHelper.getEnergyContainer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }
}
