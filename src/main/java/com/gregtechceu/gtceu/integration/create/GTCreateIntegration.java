package com.gregtechceu.gtceu.integration.create;

import com.gregtechceu.gtceu.api.placeholder.*;
import com.gregtechceu.gtceu.api.placeholder.exceptions.InvalidArgsException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.MissingItemException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.NotSupportedException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class GTCreateIntegration {

    private GTCreateIntegration() {}

    public static void init() {
        GTCreateDisplaySources.init();
        GTCreateDisplayTargets.init();

        PlaceholderHandler.addPlaceholder(new Placeholder("redstone", 1) {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                return processRedstonePlaceholder(ctx, args);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("displayTarget") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                if (!(ctx.cover() instanceof IPlaceholderInfoProviderCover cover)) throw new NotSupportedException();
                int i = PlaceholderUtils.toInt(args.get(0));
                PlaceholderUtils.checkRange("line number", 1, 100, i);
                return MultiLineComponent.of(cover.getCreateDisplayTargetBuffer().get(i - 1));
            }
        });
    }

    private static int getRedstoneLinkPower(PlaceholderContext ctx,
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
                return ctx.pos();
            }
        };
        Set<IRedstoneLinkable> network = Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(ctx.level(),
                linkable);
        int power = 0;
        for (IRedstoneLinkable i : network) {
            if (!i.isAlive()) continue;
            if (!RedstoneLinkNetworkHandler.withinRange(i, linkable)) continue;
            power = Math.max(power, i.getTransmittedStrength());
        }
        return power;
    }

    private static void setRedstoneLinkPower(PlaceholderContext ctx,
                                             Couple<RedstoneLinkNetworkHandler.Frequency> freq, int power) {
        TemporaryRedstoneLinkTransmitter linkable = new TemporaryRedstoneLinkTransmitter(freq, power,
                ctx.pos(), ctx.level());
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(ctx.level(), linkable);
    }

    private static MultiLineComponent processRedstonePlaceholder(PlaceholderContext ctx,
                                                                 List<MultiLineComponent> args) throws PlaceholderException {
        PlaceholderUtils.checkArgs(args, 1, true);
        if (GTStringUtils.equals(args.get(0), "get")) {
            PlaceholderUtils.checkArgs(args, 2, true);
            if (GTStringUtils.equals(args.get(1), "link")) {
                PlaceholderUtils.checkArgs(args, 4);
                int slot = PlaceholderUtils.toInt(args.get(2));
                int freq_slot = PlaceholderUtils.toInt(args.get(3));
                PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                ItemStack item = ctx.itemStackHandler().getStackInSlot(slot - 1);
                if (item.is(AllItems.LINKED_CONTROLLER.get())) {
                    Couple<RedstoneLinkNetworkHandler.Frequency> freq = LinkedControllerItem.toFrequency(item,
                            freq_slot);
                    return MultiLineComponent.literal(getRedstoneLinkPower(ctx, freq));
                } else throw new MissingItemException("redstone link", slot);
            } else {
                Direction direction = Direction.byName(args.get(1).toString());
                if (direction == null)
                    throw new InvalidArgsException();
                return MultiLineComponent.literal(ctx.level().getSignal(ctx.pos().relative(direction), direction));
            }
        } else if (GTStringUtils.equals(args.get(0), "set")) {
            PlaceholderUtils.checkArgs(args, 2, true);
            if (GTStringUtils.equals(args.get(1), "link")) {
                PlaceholderUtils.checkArgs(args, 5);
                int slot = PlaceholderUtils.toInt(args.get(2));
                int freq_slot = PlaceholderUtils.toInt(args.get(3));
                int power = PlaceholderUtils.toInt(args.get(4));
                PlaceholderUtils.checkRange("redstone power", 0, 15, power);
                PlaceholderUtils.checkRange("slot", 1, 8, slot);
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                ItemStack item = ctx.itemStackHandler().getStackInSlot(slot - 1);
                if (item.is(AllItems.LINKED_CONTROLLER.get())) {
                    Couple<RedstoneLinkNetworkHandler.Frequency> freq = LinkedControllerItem.toFrequency(item,
                            freq_slot);
                    setRedstoneLinkPower(ctx, freq, power);
                    return MultiLineComponent.empty();
                } else throw new MissingItemException("redstone link", slot);
            } else {
                int power = PlaceholderUtils.toInt(args.get(1));
                PlaceholderUtils.checkRange("redstone power", 0, 15, power);
                if (ctx.cover() == null) throw new NotSupportedException();
                ctx.cover().setRedstoneSignalOutput(power);
                return MultiLineComponent.empty();
            }
        } else {
            throw new InvalidArgsException();
        }
    }

    public static <
            T extends DisplaySource> SimpleBuilder<DisplaySource, T, GTRegistrate> displaySource(GTRegistrate registrate,
                                                                                                 String name,
                                                                                                 Supplier<T> supplier) {
        return registrate.entry(name, callback -> new SimpleBuilder<>(
                registrate, registrate, name, callback, CreateRegistries.DISPLAY_SOURCE, supplier)
                .byBlock(DisplaySource.BY_BLOCK)
                .byBlockEntity(DisplaySource.BY_BLOCK_ENTITY));
    }

    public static <
            T extends DisplayTarget> SimpleBuilder<DisplayTarget, T, GTRegistrate> displayTarget(GTRegistrate registrate,
                                                                                                 String name,
                                                                                                 Supplier<T> supplier) {
        return registrate.entry(name, callback -> new SimpleBuilder<>(
                registrate, registrate, name, callback, CreateRegistries.DISPLAY_TARGET, supplier)
                .byBlock(DisplayTarget.BY_BLOCK)
                .byBlockEntity(DisplayTarget.BY_BLOCK_ENTITY));
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
