package com.gregtechceu.gtceu.common.machine.multiblock.part.monitor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionResult;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

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

    @Getter
    @Setter
    private boolean clickedThisFrame = false;

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
        clickedThisFrame = true;
        Vector2d clickPos = getMousePos(context.getHitResult());
        clickPosX = clickPos.x();
        clickPosY = clickPos.y();
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
