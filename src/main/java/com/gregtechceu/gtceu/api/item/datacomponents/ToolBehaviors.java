package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.codec.StreamCodecUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public record ToolBehaviors(@Unmodifiable Map<ToolBehaviorType<?>, IToolBehavior<?>> behaviors) {

    public static final ToolBehaviors EMPTY = new ToolBehaviors(Map.of());
    // spotless:off
    public static final Codec<Map<ToolBehaviorType<?>, IToolBehavior<?>>> MAP_CODEC = Codec
            .dispatchedMap(GTRegistries.TOOL_BEHAVIORS.byNameCodec(), ToolBehaviorType::getCodec);
    public static final Codec<ToolBehaviors> CODEC = MAP_CODEC.xmap(ToolBehaviors::new, ToolBehaviors::behaviors);

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<ToolBehaviorType<?>, IToolBehavior<?>>> MAP_STREAM_CODEC = StreamCodecUtils.dispatchMap(
            HashMap::new,
            ByteBufCodecs.registry(GTRegistries.TOOL_BEHAVIOR_REGISTRY),
            type -> (StreamCodec<? super RegistryFriendlyByteBuf, IToolBehavior<?>>) type.getStreamCodec());
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolBehaviors> STREAM_CODEC = MAP_STREAM_CODEC
            .map(ToolBehaviors::new, ToolBehaviors::behaviors);
    // spotless:on

    public ToolBehaviors(List<IToolBehavior<?>> behaviors) {
        this(behaviors.stream().collect(Collectors.toMap(IToolBehavior::getType, Function.identity())));
    }

    public boolean hasBehavior(ToolBehaviorType<?> type) {
        return behaviors.containsKey(type);
    }

    public <T extends IToolBehavior<T>> T getBehavior(ToolBehaviorType<T> type) {
        return (T) this.behaviors.get(type);
    }

    public ToolBehaviors withBehavior(IToolBehavior<?> behavior) {
        Map<ToolBehaviorType<?>, IToolBehavior<?>> behaviors = new HashMap<>(this.behaviors);
        behaviors.put(behavior.getType(), behavior);
        return new ToolBehaviors(Collections.unmodifiableMap(behaviors));
    }
}
