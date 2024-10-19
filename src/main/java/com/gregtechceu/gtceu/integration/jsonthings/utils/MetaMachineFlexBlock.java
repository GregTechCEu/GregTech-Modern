package com.gregtechceu.gtceu.integration.jsonthings.utils;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.world.level.block.Block;

import com.google.common.collect.Maps;
import dev.gigaherz.jsonthings.things.IFlexBlock;
import dev.gigaherz.jsonthings.things.events.FlexEventHandler;
import dev.gigaherz.jsonthings.things.shapes.DynamicShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MetaMachineFlexBlock extends MetaMachineBlock implements IFlexBlock {

    private final Map<String, FlexEventHandler> eventHandlers = Maps.newHashMap();

    public MetaMachineFlexBlock(Properties properties, MachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public void setGeneralShape(@Nullable DynamicShape shape) {}

    @Override
    public void setCollisionShape(@Nullable DynamicShape shape) {}

    @Override
    public void setRaytraceShape(@Nullable DynamicShape shape) {}

    @Override
    public void setRenderShape(@Nullable DynamicShape shape) {}

    @Override
    public void addEventHandler(String eventName, FlexEventHandler eventHandler) {
        eventHandlers.put(eventName, eventHandler);
    }

    @Nullable
    @Override
    public FlexEventHandler getEventHandler(String eventName) {
        return eventHandlers.get(eventName);
    }

    @Override
    public Block self() {
        return super.self();
    }
}
