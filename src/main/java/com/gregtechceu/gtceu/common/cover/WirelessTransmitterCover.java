package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.placeholder.IPlaceholderInfoProviderCover;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.GlobalPosWithRot;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessTransmitterCover extends CoverBehavior
                                      implements IDataStickInteractable, IPlaceholderInfoProviderCover {

    @Getter
    private final List<MutableComponent> createDisplayTargetBuffer = new ArrayList<>();
    @Getter
    private final List<MutableComponent> computerCraftTextBuffer = new ArrayList<>();

    public WirelessTransmitterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        for (int i = 0; i < 100; i++) {
            createDisplayTargetBuffer.add(Component.empty());
            computerCraftTextBuffer.add(Component.empty());
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        dataStick.set(GTDataComponents.MONITOR_TARGET,
                new GlobalPosWithRot(coverHolder.getBlockPos(), attachedSide, coverHolder.getLevel().dimension()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public long getTicksSincePlaced() {
        return coverHolder.getOffsetTimer();
    }

    @Override
    public void setDisplayTargetBufferLine(int line, MutableComponent component) {
        createDisplayTargetBuffer.set(line, component);
    }

    @Override
    public void setComputerCraftTextBufferLine(int line, MutableComponent component) {
        computerCraftTextBuffer.set(line, component);
    }
}
