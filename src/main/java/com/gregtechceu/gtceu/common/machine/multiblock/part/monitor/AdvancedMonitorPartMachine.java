package com.gregtechceu.gtceu.common.machine.multiblock.part.monitor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedMonitorPartMachine extends MonitorPartMachine {

    @Getter
    @SaveField
    private double clickPosX;
    @Getter
    @SaveField
    private double clickPosY;
    @Getter
    @SaveField
    private boolean clicked;
    @SaveField
    private boolean resetClickedNextTick = false;

    @Nullable
    private TickableSubscription clickResetSubscription;

    public AdvancedMonitorPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        if (context.getClickedFace() != getFrontFacing()) return super.onUse(context);
        var hitLocation = context.getHitResult().getLocation();
        clicked = true;
        clickPosX = hitLocation
                .get(RelativeDirection.RIGHT.getRelative(getFrontFacing(), getUpwardsFacing(), false).getAxis());
        clickPosY = hitLocation
                .get(getFrontFacing().getAxis().isVertical() ? Direction.Axis.X : Direction.Axis.Y);
        clickPosX -= Math.floor(clickPosX);
        if (clickPosX < 0) clickPosX++;
        clickPosY -= Math.floor(clickPosY);
        if (clickPosY < 0) clickPosY++;
        return InteractionResult.SUCCESS;
    }

    public void resetClicked() {
        resetClickedNextTick = true;
    }

    private void unsetClicked() {
        if (resetClickedNextTick) {
            clicked = false;
        }
        resetClickedNextTick = false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        clickResetSubscription = subscribeServerTick(this::unsetClicked);
    }

    @Override
    public void onUnload() {
        unsubscribe(clickResetSubscription);
    }
}
