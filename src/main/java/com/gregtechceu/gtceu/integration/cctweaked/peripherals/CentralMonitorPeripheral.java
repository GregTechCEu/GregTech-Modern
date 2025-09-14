package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.capability.ICentralMonitor;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.common.item.modules.ImageModuleBehaviour;
import com.gregtechceu.gtceu.common.item.modules.TextModuleBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.world.item.ItemStack;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import org.jetbrains.annotations.Nullable;

public class CentralMonitorPeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return "gtceu:central_monitor";
    }

    @LuaFunction
    public static MethodResult getGroups(ICentralMonitor centralMonitor) {
        return MethodResult.of(centralMonitor.getMonitorGroups().stream().map(LuaMonitorGroup::new).toList());
    }

    public static class LuaMonitorGroup {

        private final MonitorGroup group;

        public LuaMonitorGroup(MonitorGroup group) {
            this.group = group;
        }

        @LuaFunction
        public String getName() {
            return group.getName();
        }

        @LuaFunction
        public LuaMonitorModule getModule() {
            return new LuaMonitorModule(group.getItemStackHandler().getStackInSlot(0));
        }

        // TODO item transfer (setModule, etc.)
    }

    public static class LuaMonitorModule {

        private final ItemStack stack;

        public LuaMonitorModule(ItemStack stack) {
            this.stack = stack;
        }

        private @Nullable IMonitorModuleItem getModuleItem() {
            if (stack.getItem() instanceof ComponentItem componentItem) {
                for (IItemComponent component : componentItem.getComponents()) {
                    if (component instanceof IMonitorModuleItem moduleItem) {
                        return moduleItem;
                    }
                }
            }
            return null;
        }

        @LuaFunction
        public String getType() {
            if (stack.isEmpty()) return "none";
            IMonitorModuleItem moduleItem = getModuleItem();
            if (moduleItem != null) return moduleItem.getType();
            return "invalid";
        }

        @LuaFunction
        public MethodResult getCurrentText() {
            if (getModuleItem() instanceof TextModuleBehaviour textModule) {
                return MethodResult.of(textModule.getText(stack).toString());
            } else return MethodResult.of();
        }

        @LuaFunction
        public void setPlaceholderText(String text) {
            if (getModuleItem() instanceof TextModuleBehaviour textModule) {
                textModule.setPlaceholderText(stack, text);
            }
        }

        @LuaFunction
        public MethodResult getScale() {
            if (getModuleItem() instanceof TextModuleBehaviour textModule) {
                return MethodResult.of(textModule.getScale(stack));
            } else return MethodResult.of();
        }

        @LuaFunction
        public void setScale(double scale) {
            if (getModuleItem() instanceof TextModuleBehaviour textModule) {
                textModule.setScale(stack, scale);
            }
        }

        @LuaFunction
        public MethodResult getImageUrl() {
            if (getModuleItem() instanceof ImageModuleBehaviour imageModule) {
                return MethodResult.of(imageModule.getUrl(stack));
            } else return MethodResult.of();
        }

        @LuaFunction
        public void setImageUrl(String url) {
            if (getModuleItem() instanceof ImageModuleBehaviour imageModule) {
                imageModule.setUrl(stack, url);
            }
        }
    }
}
