package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record ToolBehaviorsComponent(List<IToolBehavior<?>> behaviors) {
    public static final Codec<ToolBehaviorsComponent> CODEC = GTRegistries.TOOL_BEHAVIORS.codec()
        .dispatch(IToolBehavior::getType, type -> (MapCodec<IToolBehavior<?>>) type.getCodec())
        .listOf()
        .xmap(ToolBehaviorsComponent::new, ToolBehaviorsComponent::behaviors);
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolBehaviorsComponent> STREAM_CODEC = GTRegistries.TOOL_BEHAVIORS.streamCodec()
        .dispatch(IToolBehavior::getType, type -> (StreamCodec<RegistryFriendlyByteBuf, IToolBehavior<?>>) type.getStreamCodec())
        .apply(ByteBufCodecs.list())
        .map(ToolBehaviorsComponent::new, ToolBehaviorsComponent::behaviors);

    public boolean hasBehavior(ToolBehaviorType<?> type) {
        for (IToolBehavior<?> behavior : behaviors) {
            if (behavior.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public ToolBehaviorsComponent withBehavior(IToolBehavior<?> behavior) {
        List<IToolBehavior<?>> behaviors = new ArrayList<>(this.behaviors);
        behaviors.removeIf(b -> b.getType() == behavior.getType());
        behaviors.add(behavior);
        return new ToolBehaviorsComponent(behaviors);
    }
}
