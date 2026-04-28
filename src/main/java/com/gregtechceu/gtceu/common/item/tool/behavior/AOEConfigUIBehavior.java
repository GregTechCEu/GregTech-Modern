package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolUIBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public class AOEConfigUIBehavior implements IToolUIBehavior<AOEConfigUIBehavior> {

    public static final AOEConfigUIBehavior INSTANCE = new AOEConfigUIBehavior();
    public static final Codec<AOEConfigUIBehavior> CODEC = MapCodec.unitCodec(INSTANCE);
    public static final StreamCodec<ByteBuf, AOEConfigUIBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean openUI(@NotNull Player player, @NotNull InteractionHand hand) {
        return player.isShiftKeyDown() && !player.getItemInHand(hand)
                .getOrDefault(GTDataComponents.AOE, AoESymmetrical.ZERO).isZero();
    }

    @Override
    public Object createUI(Player player, GTHeldItemUIHolder holder) {
        return AOEConfigUIFactory.create(player, holder);
    }

    @Override
    public ToolBehaviorType<AOEConfigUIBehavior> getType() {
        return GTToolBehaviors.AOE_CONFIG_UI;
    }
}
