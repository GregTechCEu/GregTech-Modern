package com.gregtechceu.gtceu.integration.create;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.cover.ComputerMonitorCover;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CreateIntegration {

    private CreateIntegration() {}

    public static void init() {
        GTCreateDisplaySources.init();
        GTCreateDisplayTargets.init();

        ComputerMonitorCover.addPlaceholder("redstone", CreateIntegration::processRedstonePlaceholder);
        ComputerMonitorCover.addPlaceholder("displayTarget", (cover, args) -> {
            if (!GTCEu.Mods.isCreateLoaded()) return GTStringUtils.literal("Create is not loaded!");
            if (args.size() != 1) return GTStringUtils.literal("Expected 1 argument");
            try {
                int i = GTStringUtils.toInt(args.get(0));
                if (i <= 0 || i > 100) GTStringUtils.literal("Line number must be from 1 to 100 (inclusive)");
                return new ArrayList<>(List.of(cover.getCreateDisplayTargetBuffer().get(i - 1)));
            } catch (NumberFormatException e) {
                return GTStringUtils.literal("Invalid line number '%s'".formatted(e.getMessage()));
            }
        });
    }

    private static int getRedstoneLinkPower(ComputerMonitorCover cover,
                                            Couple<RedstoneLinkNetworkHandler.Frequency> freq) {
        IRedstoneLinkable linkable = new IRedstoneLinkable() {

            @Override
            public int getTransmittedStrength() {
                return 0;
            }

            @Override
            public void setReceivedStrength(int power) {}

            @Override
            public boolean isListening() {
                return true;
            }

            @Override
            public boolean isAlive() {
                return true;
            }

            @Override
            public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
                return freq;
            }

            @Override
            public BlockPos getLocation() {
                return cover.coverHolder.getPos();
            }
        };
        Set<IRedstoneLinkable> network = Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(cover.coverHolder.getLevel(),
                linkable);
        int power = 0;
        for (IRedstoneLinkable i : network) {
            if (!i.isAlive()) continue;
            if (!RedstoneLinkNetworkHandler.withinRange(i, linkable)) continue;
            power = Math.max(power, i.getTransmittedStrength());
        }
        return power;
    }

    private static void setRedstoneLinkPower(ComputerMonitorCover cover,
                                             Couple<RedstoneLinkNetworkHandler.Frequency> freq, int power) {
        TemporaryRedstoneLinkTransmitter linkable = new TemporaryRedstoneLinkTransmitter(freq, power,
                cover.coverHolder.getPos(), cover.coverHolder.getLevel());
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(cover.coverHolder.getLevel(), linkable);
    }

    private static List<MutableComponent> processRedstonePlaceholder(ComputerMonitorCover cover,
                                                                     List<List<MutableComponent>> args) {
        if (args.isEmpty()) return GTStringUtils.literal("Expected at least 1 argument!");
        else if (GTStringUtils.equals(args.get(0), "get")) {
            if (args.size() < 2) return GTStringUtils.literal("Expected an argument after 'get'!");
            if (GTStringUtils.equals(args.get(1), "link")) {
                if (args.size() < 4)
                    return GTStringUtils.literal("Expected slot number and frequency slot number after 'link'!");
                try {
                    int slot = GTStringUtils.toInt(args.get(2));
                    int freq_slot = GTStringUtils.toInt(args.get(3));
                    if (slot < 1 || slot > 8) return GTStringUtils.literal("Expected slot index between 1 and 8");
                    ItemStack item = cover.itemStackHandler.getStackInSlot(slot - 1);
                    if (!GTCEu.Mods.isCreateLoaded()) return GTStringUtils.literal("Create is not loaded!");
                    if (item.is(AllItems.LINKED_CONTROLLER.get())) {
                        Couple<RedstoneLinkNetworkHandler.Frequency> freq = LinkedControllerItem.toFrequency(item,
                                freq_slot);
                        return GTStringUtils.literal(getRedstoneLinkPower(cover, freq));
                    } else return GTStringUtils.literal("Invalid redstone link controller!");
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid slot number '%s'".formatted(e.getMessage()));
                }
            } else {
                Direction direction = Direction.byName(GTStringUtils.componentsToString(args.get(1)));
                if (direction == null) return GTStringUtils.literal(
                        "2nd argument must be either 'link' or a valid direction (up,down,north,south,east,west)");
                return GTStringUtils.literal(cover.coverHolder.getLevel()
                        .getSignal(cover.coverHolder.getPos().relative(direction), direction));
            }
        } else if (GTStringUtils.equals(args.get(0), "set")) {
            if (args.size() < 2) return GTStringUtils.literal("Expected an argument after 'set'!");
            if (GTStringUtils.equals(args.get(1), "link")) {
                if (args.size() < 5)
                    return GTStringUtils.literal("Expected slot number, frequency slot number and power after 'link'!");
                try {
                    int slot = GTStringUtils.toInt(args.get(2));
                    int freq_slot = GTStringUtils.toInt(args.get(3));
                    int power = GTStringUtils.toInt(args.get(4));
                    if (power < 0 || power > 15)
                        return GTStringUtils.literal("Expected redstone power to be from 0 to 15");
                    if (slot < 1 || slot > 8) return GTStringUtils.literal("Expected slot index between 1 and 8");
                    ItemStack item = cover.itemStackHandler.getStackInSlot(slot - 1);
                    if (!GTCEu.Mods.isCreateLoaded()) return GTStringUtils.literal("Create is not loaded!");
                    if (item.is(AllItems.LINKED_CONTROLLER.get())) {
                        Couple<RedstoneLinkNetworkHandler.Frequency> freq = LinkedControllerItem.toFrequency(item,
                                freq_slot);
                        setRedstoneLinkPower(cover, freq, power);
                        return GTStringUtils.literal("");
                    } else return GTStringUtils.literal("Invalid redstone link controller!");
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid number '%s'".formatted(e.getMessage()));
                }
            } else {
                try {
                    int power = GTStringUtils.toInt(args.get(1));
                    if (power < 0 || power > 15)
                        return GTStringUtils.literal("Expected redstone power to be from 0 to 15");
                    cover.setRedstoneSignalOutput(power);
                    return GTStringUtils.literal("");
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid number '%s'".formatted(e.getMessage()));
                }
            }
        } else {
            return GTStringUtils.literal("1st argument must be either 'set' or 'get'");
        }
    }

    public static class TemporaryRedstoneLinkTransmitter implements IRedstoneLinkable {

        private static final ArrayList<TemporaryRedstoneLinkTransmitter> transmitters = new ArrayList<>();
        private final int power;
        private final Couple<RedstoneLinkNetworkHandler.Frequency> freq;
        private final BlockPos pos;
        private final Level level;
        private boolean alive;

        public TemporaryRedstoneLinkTransmitter(Couple<RedstoneLinkNetworkHandler.Frequency> frequency, int power,
                                                BlockPos pos, Level level) {
            this.power = power;
            this.freq = frequency;
            this.alive = true;
            this.pos = pos;
            this.level = level;
            transmitters.add(this);
        }

        @Override
        public int getTransmittedStrength() {
            return power;
        }

        @Override
        public void setReceivedStrength(int power) {}

        @Override
        public boolean isListening() {
            return false;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }

        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return freq;
        }

        @Override
        public BlockPos getLocation() {
            return pos;
        }

        public void destroy() {
            this.alive = false;
            Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(level, this);
        }

        public static void destroyAll() {
            while (!transmitters.isEmpty()) {
                transmitters.get(transmitters.size() - 1).destroy();
                transmitters.remove(transmitters.size() - 1);
            }
        }
    }
}
