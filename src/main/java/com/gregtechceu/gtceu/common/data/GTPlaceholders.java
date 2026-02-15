package com.gregtechceu.gtceu.data.placeholder;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.placeholder.*;
import com.gregtechceu.gtceu.api.placeholder.exceptions.*;
import com.gregtechceu.gtceu.client.renderer.placeholder.ModulePlaceholderRenderer;
import com.gregtechceu.gtceu.client.renderer.placeholder.QuadPlaceholderRenderer;
import com.gregtechceu.gtceu.client.renderer.placeholder.RectPlaceholderRenderer;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.item.datacomponents.BindingData;
import com.gregtechceu.gtceu.common.item.datacomponents.DataItem;
import com.gregtechceu.gtceu.common.item.datacomponents.FormatStringList;
import com.gregtechceu.gtceu.common.item.modules.ImageModuleBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.monitor.AdvancedMonitorPartMachine;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import com.tterrag.registrate.util.RegistrateDistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GTPlaceholders {

    public static int countItems(String id, @Nullable IItemHandler itemHandler) {
        if (itemHandler == null) return 0;
        int cnt = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemStack = itemHandler.getStackInSlot(i);
            String itemId = "%s:%s".formatted(itemStack.getItem().getCreatorModId(itemStack),
                    itemStack.getItem().toString());
            if (itemId.equals(id)) cnt += itemStack.getCount();
        }
        return cnt;
    }

    public static int countFluids(@Nullable String id, @Nullable IFluidHandler fluidHandler) {
        if (fluidHandler == null) return 0;
        int count = 0;
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack fluidStack = fluidHandler.getFluidInTank(i);
            String fluidId = Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluidStack.getFluid())).toString();

            if (id == null || fluidId.equals(id)) {
                count += fluidStack.getAmount();
            }
        }
        return count;
    }

    public static int countItems(@Nullable ItemFilter filter, @Nullable IItemHandler itemHandler) {
        if (itemHandler == null)
            return -1;
        int cnt = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (filter == null || filter.test(itemHandler.getStackInSlot(i)))
                cnt += itemHandler.getStackInSlot(i).getCount();
        }
        return cnt;
    }

    public static void initPlaceholders() {
        RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> GTPlaceholders::initRenderers);
        PlaceholderHandler.addPlaceholder(new Placeholder("energy") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                if (ctx.level().getBlockEntity(ctx.pos()) instanceof IMachineBlockEntity machineBE) {
                    if (machineBE.getMetaMachine() instanceof IEnergyInfoProvider energyInfoProvider) {
                        return MultiLineComponent.literal(energyInfoProvider.getEnergyInfo().stored().longValue());
                    }
                }
                IEnergyContainer energy = GTCapabilityHelper.getEnergyContainer(ctx.level(), ctx.pos(), ctx.side());
                return MultiLineComponent.literal(energy != null ? energy.getEnergyStored() : 0);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("energyCapacity") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                if (ctx.level().getBlockEntity(ctx.pos()) instanceof IMachineBlockEntity machineBE) {
                    if (machineBE.getMetaMachine() instanceof IEnergyInfoProvider energyInfoProvider) {
                        return MultiLineComponent.literal(energyInfoProvider.getEnergyInfo().capacity().longValue());
                    }
                }
                IEnergyContainer energy = GTCapabilityHelper.getEnergyContainer(ctx.level(), ctx.pos(), ctx.side());
                return MultiLineComponent.literal(energy != null ? energy.getEnergyCapacity() : 0);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("calc") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) {
                List<String> stringArgs = new ArrayList<>();
                args.forEach((components) -> stringArgs.add(GTStringUtils.componentsToString(components)));
                return MultiLineComponent.literal(GTStringUtils.calc(stringArgs));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("itemCount") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IItemHandler itemHandler = GTCapabilityHelper.getItemHandler(ctx.level(), ctx.pos(), ctx.side());
                if (args.isEmpty()) {
                    return MultiLineComponent.literal(countItems((ItemFilter) null, itemHandler));
                }
                if (args.size() == 1) {
                    return MultiLineComponent.literal(countItems(
                            GTStringUtils.componentsToString(args.getFirst()), itemHandler));
                }
                if (GTStringUtils.equals(args.getFirst(), "filter")) {
                    int slot = PlaceholderUtils.toInt(args.get(1));
                    if (ctx.itemStackHandler() == null)
                        throw new NotSupportedException();
                    PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                    try {
                        return MultiLineComponent.literal(countItems(
                                ItemFilter.loadFilter(ctx.itemStackHandler().getStackInSlot(slot - 1)), itemHandler));
                    } catch (NullPointerException e) {
                        throw new MissingItemException("filter", slot);
                    }
                }
                throw new InvalidArgsException();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("fluidCount") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                IFluidHandler fluidHandler = GTCapabilityHelper.getFluidHandler(ctx.level(), ctx.pos(), ctx.side());
                if (args.isEmpty()) return MultiLineComponent.literal(countFluids(null, fluidHandler));
                if (args.size() == 1)
                    return MultiLineComponent
                            .literal(countFluids(GTStringUtils.componentsToString(args.getFirst()), fluidHandler));
                PlaceholderUtils.checkArgs(args, 1);
                return MultiLineComponent.empty(); // unreachable
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("if") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2, true);
                try {
                    if (GTStringUtils.toDouble(args.get(0)) != 0) {
                        return new MultiLineComponent(args.get(1)).setIgnoreSpaces(true);
                    } else if (args.size() > 2) {
                        return new MultiLineComponent(args.get(2)).setIgnoreSpaces(true);
                    } else return MultiLineComponent.empty();
                } catch (NumberFormatException e) {
                    return args.get(1);
                }
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("color") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2);
                ChatFormatting color = ChatFormatting.getByName(GTStringUtils.componentsToString(args.getFirst()));
                if (color == null) throw new InvalidArgsException();
                return new MultiLineComponent(args.get(1).stream().map(c -> c.withStyle(color)).toList());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("underline") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                return new MultiLineComponent(
                        args.getFirst().stream().map(c -> c.withStyle(ChatFormatting.UNDERLINE)).toList());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("strike") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                return new MultiLineComponent(
                        args.getFirst().stream().map(c -> c.withStyle(ChatFormatting.STRIKETHROUGH)).toList());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("obf") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                return new MultiLineComponent(
                        args.getFirst().stream().map(c -> c.withStyle(ChatFormatting.OBFUSCATED)).toList());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("random") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2);
                return MultiLineComponent.literal(GTValues.RNG.nextIntBetweenInclusive(
                        PlaceholderUtils.toInt(args.getFirst()), PlaceholderUtils.toInt(args.get(1))));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("repeat") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2);
                int count = PlaceholderUtils.toInt(args.get(0));
                PlaceholderUtils.checkRange("n", 0, 50000, count);
                MultiLineComponent out = MultiLineComponent.empty();
                for (int i = 0; i < count; i++) out.append(args.get(1));
                return out.setIgnoreSpaces(true);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("block") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                return MultiLineComponent.literal("█");
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("tick") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                return MultiLineComponent.literal(ctx.level().getGameTime());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("select") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1, true);
                int i = PlaceholderUtils.toInt(args.get(0));
                PlaceholderUtils.checkArgs(args, i + 1, true);
                return new MultiLineComponent(args.get(i + 1)).setIgnoreSpaces(true);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("redstone") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2, true);
                if (GTStringUtils.equals(args.getFirst(), "get")) {
                    Direction direction = Direction.byName(GTStringUtils.componentsToString(args.get(1)));
                    if (direction == null)
                        throw new InvalidArgsException();
                    return MultiLineComponent.literal(ctx.level()
                            .getSignal(ctx.pos().relative(direction), direction));
                } else if (GTStringUtils.equals(args.get(0), "set")) {
                    int power = PlaceholderUtils.toInt(args.get(1));
                    PlaceholderUtils.checkRange("redstone power", 0, 15, power);
                    if (ctx.cover() == null) throw new NotSupportedException();
                    ctx.cover().setRedstoneSignalOutput(power);
                    return MultiLineComponent.empty();
                }
                throw new InvalidArgsException();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("previousText") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                int i = PlaceholderUtils.toInt(args.getFirst());
                if (ctx.previousText() == null) throw new NotSupportedException();
                PlaceholderUtils.checkRange("line", 1, ctx.previousText().size(), i);
                return MultiLineComponent.of(ctx.previousText().get(i - 1)).setIgnoreSpaces(true);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("progress") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                IWorkable workable = GTCapabilityHelper.getWorkable(ctx.level(),
                        ctx.pos(), ctx.side());
                if (workable == null) throw new NotSupportedException();
                return MultiLineComponent.literal(workable.getProgress());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("maxProgress") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                IWorkable workable = GTCapabilityHelper.getWorkable(ctx.level(),
                        ctx.pos(), ctx.side());
                if (workable == null) throw new NotSupportedException();
                return MultiLineComponent.literal(workable.getMaxProgress());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("maintenance") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                IMaintenanceMachine maintenance = GTCapabilityHelper.getMaintenanceMachine(ctx.level(),
                        ctx.pos(), ctx.side());
                if (maintenance == null) throw new NotSupportedException();
                return MultiLineComponent.literal(maintenance.hasMaintenanceProblems() ? 1 : 0);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("active") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                IWorkable workable = GTCapabilityHelper.getWorkable(ctx.level(),
                        ctx.pos(), ctx.side());
                if (workable == null) throw new NotSupportedException();
                return MultiLineComponent.literal(workable.isActive() ? 1 : 0);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("voltage") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                if (ctx.level().getBlockEntity(ctx.pos()) instanceof CableBlockEntity cable) {
                    return MultiLineComponent.literal(cable.getAverageVoltage());
                }
                throw new NotSupportedException();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("amperage") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 0);
                if (ctx.level().getBlockEntity(ctx.pos()) instanceof CableBlockEntity cable) {
                    return MultiLineComponent.literal(cable.getAverageAmperage());
                }
                throw new NotSupportedException();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("count") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1, true);
                String arg1 = GTStringUtils.componentsToString(args.getFirst());
                int cnt = -1;
                for (List<MutableComponent> arg : args) {
                    if (GTStringUtils.equals(arg, arg1)) cnt++;
                }
                return MultiLineComponent.literal(cnt);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("data") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2, true);
                try {
                    int slot = PlaceholderUtils.toInt(args.get(1));
                    if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                    PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                    ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                    DataItem component = stack.get(GTDataComponents.DATA_ITEM);
                    if (component == null) {
                        throw new MissingItemException("any data item", slot);
                    }
                    int capacity = component.capacity();

                    PlaceholderUtils.checkRange("index", 0, capacity - 1, PlaceholderUtils.toInt(args.get(2)));
                    // <<<<<<< HEAD:src/main/java/com/gregtechceu/gtceu/data/placeholder/GTPlaceholders.java

                    FormatStringList immutableData = stack.get(GTDataComponents.COMPUTER_MONITOR_DATA);
                    if (immutableData == null) {
                        throw new MissingItemException("any data item", slot);
                    }
                    FormatStringList.Mutable monitorData = immutableData.mutable();
                    while (monitorData.size() <= PlaceholderUtils.toInt(args.get(2))) {
                        monitorData.add("");
                    }

                    int p = stack.getOrDefault(GTDataComponents.COMPUTER_MONITOR_P, 0);
                    if (GTStringUtils.equals(args.get(2), "")) {
                        args.set(2, MultiLineComponent.literal(p));
                    }
                    if (GTStringUtils.equals(args.getFirst(), "get")) {
                        return MultiLineComponent.literal(
                                monitorData.get(PlaceholderUtils.toInt(args.get(2)) % capacity)).setIgnoreSpaces(true);
                    } else if (args.getFirst().equalsString("set")) {
                        monitorData.set(PlaceholderUtils.toInt(args.get(2)) % capacity, args.get(3).toString());
                        stack.set(GTDataComponents.COMPUTER_MONITOR_DATA, monitorData.toImmutable());
                        return MultiLineComponent.empty();
                    } else if (args.getFirst().equalsString("setp")) {
                        stack.set(GTDataComponents.COMPUTER_MONITOR_P, PlaceholderUtils.toInt(args.get(3)) % capacity);
                        return MultiLineComponent.empty();
                    } else if (args.getFirst().equalsString("inc")) {
                        stack.set(GTDataComponents.COMPUTER_MONITOR_P, (p + 1) % capacity);
                        return MultiLineComponent.empty();
                    } else if (args.getFirst().equalsString("dec")) {
                        stack.set(GTDataComponents.COMPUTER_MONITOR_P, p == 0 ? capacity - 1 : p - 1);
                        return MultiLineComponent.empty();
                    } else {
                        throw new InvalidArgsException();
                    }
                } catch (IndexOutOfBoundsException e) {
                    throw new InvalidArgsException();
                }
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("combine") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) {
                MultiLineComponent out = MultiLineComponent.empty();
                for (int i = 0; i < args.size(); i++) {
                    out.append(args.get(i));
                    if (i != args.size() - 1) out.append(" ");
                }
                return out.setIgnoreSpaces(true);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("nbt") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1, true);
                int slot = GTStringUtils.toInt(args.getFirst());
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                return MultiLineComponent
                        .literal(ctx.itemStackHandler().getStackInSlot(slot - 1).getComponents().toString());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("toChars") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                if (args.getFirst().isEmpty()) return MultiLineComponent.empty();
                StringBuilder out = new StringBuilder();
                for (char c : GTStringUtils.componentsToString(args.getFirst()).toCharArray())
                    out.append(c).append(' ');
                return MultiLineComponent.literal(out.substring(0, out.length() - 2));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("toAscii") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                String arg = args.getFirst().toString();
                if (arg.length() != 1) throw new InvalidArgsException();
                return MultiLineComponent.literal((int) arg.toCharArray()[0]);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("fromAscii") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                return MultiLineComponent.literal((char) PlaceholderUtils.toInt(args.get(0))).setIgnoreSpaces(true);
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("subList") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2, true);
                int l = PlaceholderUtils.toInt(args.getFirst());
                int r = PlaceholderUtils.toInt(args.get(1));
                PlaceholderUtils.checkRange("start index", 0, args.size(), l);
                PlaceholderUtils.checkRange("end index", 0, args.size(), r);
                MultiLineComponent out = MultiLineComponent.empty();
                for (int i = l; i < r - 1; i++) out.append(args.get(i)).append(' ');
                out.append(args.get(r - 1));
                out.setIgnoreSpaces(true);
                return out;
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("cmp") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 3);
                double a = PlaceholderUtils.toDouble(args.getFirst());
                double b = PlaceholderUtils.toDouble(args.get(2));
                return switch (args.get(1).toString()) {
                    case ">" -> MultiLineComponent.literal(a > b ? 1 : 0);
                    case "<" -> MultiLineComponent.literal(a < b ? 1 : 0);
                    case ">=" -> MultiLineComponent.literal(a >= b ? 1 : 0);
                    case "<=" -> MultiLineComponent.literal(a <= b ? 1 : 0);
                    case "==" -> MultiLineComponent.literal(a == b ? 1 : 0);
                    case "!=" -> MultiLineComponent.literal(a != b ? 1 : 0);
                    default -> throw new InvalidArgsException();
                };
            }
        });

        PlaceholderHandler.addPlaceholder(new Placeholder("bf") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2);
                int slot = PlaceholderUtils.toInt(args.getFirst());
                PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();

                ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                FormatStringList immutableData = stack.get(GTDataComponents.COMPUTER_MONITOR_DATA);
                if (immutableData == null) {
                    throw new MissingItemException("any data item", slot);
                }
                FormatStringList.Mutable monitorData = immutableData.mutable();

                int operationsLeft = 5000;
                int p = 0, start = 0, cnt = 0, num = 0;
                String rawCode = args.get(1).toString().replaceAll("[^+\\-><\\[\\]]", "");
                StringBuilder codeBuilder = new StringBuilder();
                // optimize BF code ("[----[+++]-<<-]" -> "[4-[3+]1-2<1-]")
                @Nullable
                Character cur = null;
                for (char i : rawCode.toCharArray()) {
                    if (cur != null) {
                        if (cur == i) cnt++;
                        else {
                            codeBuilder.append(cnt).append(cur);
                            cur = null;
                            cnt = 0;
                        }
                    }
                    if (cur == null) {
                        if (List.of('+', '-', '<', '>').contains(i)) {
                            cur = i;
                            cnt = 1;
                        } else codeBuilder.append(i);
                    }
                }
                if (cur != null) codeBuilder.append(cnt).append(cur);
                String code = codeBuilder.toString();
                Stack<Integer> loops = new Stack<>();
                if (!getData(ctx).getBoolean("completed")) {
                    p = getData(ctx).getInt("pointer");
                    start = getData(ctx).getInt("index");
                    num = getData(ctx).getInt("num");
                }
                getData(ctx).putBoolean("completed", true);
                for (int i = 0; i < code.length(); i++) {
                    if (operationsLeft <= 0) {
                        getData(ctx).putBoolean("completed", false);
                        getData(ctx).putInt("pointer", p);
                        getData(ctx).putInt("index", i);
                        getData(ctx).putInt("num", num);
                        break;
                    }
                    while (monitorData.size() <= p) {
                        monitorData.add("0");
                    }
                    if (monitorData.get(p).isEmpty()) {
                        monitorData.set(i, "0");
                    }
                    try {
                        switch (code.charAt(i)) {
                            case '+' -> monitorData.set(p,
                                    String.valueOf((Integer.parseInt(monitorData.get(p)) + num) % 256));
                            case '-' -> {
                                int tmp = Integer.parseInt(monitorData.get(p)) - num;
                                if (tmp < 0) tmp = (256 - ((-tmp) % 256)) % 256;
                                monitorData.set(p, String.valueOf(tmp));
                            }
                            case '>' -> p += num;
                            case '<' -> p -= num;
                            case '[' -> loops.push(i);
                            case ']' -> {
                                if (Integer.parseInt(monitorData.get(p)) == 0) {
                                    loops.pop();
                                } else {
                                    i = loops.peek();
                                }
                            }
                            default -> throw new PlaceholderException(Component
                                    .translatable("gtceu.computer_monitor_cover.error.bf_invalid", i).getString());
                        }
                    } catch (Exception e) {

                    }
                    if (Character.isDigit(code.charAt(i))) {
                        num = num * 10 + code.charAt(i) - '0';
                    } else num = 0;
                    operationsLeft--;
                }
                return MultiLineComponent.empty();
            }
        });

        PlaceholderHandler.addPlaceholder(new Placeholder("cmd") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2);
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                int slot = PlaceholderUtils.toInt(args.getFirst());
                PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);

                BindingData bindingData = stack.get(GTDataComponents.BINDING_DATA);
                if (bindingData == null) {
                    throw new MissingItemException("any data item bound to player", slot);
                }

                Component displayName = bindingData.getBoundPlayerName(ctx.level());
                if (displayName == null) {
                    displayName = Component.translatable("gtceu.tooltip.player_name.placeholder_processor");
                }

                if (!(ctx.level() instanceof ServerLevel serverLevel)) {
                    throw new NotSupportedException();
                }
                MinecraftServer server = serverLevel.getServer();
                Player player = ctx.level().getPlayerByUUID(bindingData.uuid());

                MultiLineComponent output = MultiLineComponent.empty();
                CommandSource customSource = new CommandSource() {

                    @Override
                    public void sendSystemMessage(@NotNull Component message) {
                        output.append(List.of(message)).appendNewline();
                    }

                    @Override
                    public boolean acceptsSuccess() {
                        return true;
                    }

                    @Override
                    public boolean acceptsFailure() {
                        return true;
                    }

                    @Override
                    public boolean shouldInformAdmins() {
                        return false;
                    }
                };

                CommandSourceStack source = new CommandSourceStack(
                        customSource,
                        ctx.pos() == null ? Vec3.ZERO : ctx.pos().getCenter(),
                        Vec2.ZERO,
                        serverLevel,
                        bindingData.permissionLevel(),
                        displayName.getString(),
                        displayName,
                        server,
                        player);
                server.getCommands().performPrefixedCommand(source, args.get(1).toString());
                return output;
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("tm") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) {
                return MultiLineComponent.literal("™");
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("formatInt") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                long n = PlaceholderUtils.toLong(args.getFirst());
                Map<Long, String> suffixes = Map.of(
                        1L, "",
                        1000L, "K",
                        1000000L, "M",
                        1000000000L, "B",
                        1000000000000L, "T");
                long max = 1;
                for (long i : suffixes.keySet()) {
                    if (n >= i && max < i) max = i;
                }
                return MultiLineComponent.literal(String.format(Locale.ROOT, "%.2f%s",
                        ((double) n) / max, suffixes.get(max)));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("click") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                if (!(MetaMachine.getMachine(ctx.level(), ctx.pos()) instanceof AdvancedMonitorPartMachine monitor))
                    throw new NotSupportedException();
                monitor.resetClicked();
                if (args.isEmpty()) return MultiLineComponent.literal(monitor.isClicked() ? 1 : 0);
                PlaceholderUtils.checkArgs(args, 1);
                if (args.get(0).equalsString("x")) return MultiLineComponent.literal(monitor.getClickPosX());
                if (args.get(0).equalsString("y")) return MultiLineComponent.literal(monitor.getClickPosY());
                throw new InvalidArgsException();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("ender") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2, true);
                String type = args.get(0).toString();
                String channel = args.get(1).toString();
                UUID owner = null;
                if (args.size() > 2 && !args.get(2).toString().isEmpty()) {
                    if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                    int slot = PlaceholderUtils.toInt(args.get(2));
                    PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                    ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                    // if (stack.getOrCreateTag().contains("boundPlayerUUID"))
                    // owner = UUID.fromString(stack.getOrCreateTag().getString("boundPlayerUUID"));
                    if (stack.has(GTDataComponents.DATA_BOUND_PLAYER))
                        owner = stack.get(GTDataComponents.DATA_BOUND_PLAYER).uuid();
                }
                VirtualEnderRegistry ender = VirtualEnderRegistry.getInstance();
                switch (type) {
                    case "redstone" -> {
                        channel = "ERLink#" + channel;
                        if (!ender.hasEntry(owner, EntryTypes.ENDER_REDSTONE, channel))
                            return MultiLineComponent.literal(0);
                        if (args.size() > 4) {
                            if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                            int slot = PlaceholderUtils.toInt(args.get(3));
                            PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                            ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                            UUID uuid;
                            if (stack.has(GTDataComponents.ENDER_REDSTONE_LINK_TRANSMITTER_UUID)) {
                                uuid = stack.get(GTDataComponents.ENDER_REDSTONE_LINK_TRANSMITTER_UUID);
                            } else {
                                uuid = UUID.randomUUID();
                                stack.set(GTDataComponents.ENDER_REDSTONE_LINK_TRANSMITTER_UUID, uuid);
                            }
                            int power = PlaceholderUtils.toInt(args.get(4));
                            PlaceholderUtils.checkRange("redstone signal", 0, 15, power);
                            ender.getEntry(owner, EntryTypes.ENDER_REDSTONE, channel).setSignal(uuid, power);
                            return MultiLineComponent.empty();
                        }
                        return MultiLineComponent
                                .literal(ender.getEntry(owner, EntryTypes.ENDER_REDSTONE, channel).getSignal());
                    }
                    case "item" -> {
                        channel = "EILink#" + channel;
                        if (!ender.hasEntry(owner, EntryTypes.ENDER_ITEM, channel))
                            return MultiLineComponent.literal(0);
                        IItemHandler items = ender.getEntry(owner, EntryTypes.ENDER_ITEM, channel).getHandler();
                        int count = 0;
                        for (int i = 0; i < items.getSlots(); i++) count += items.getStackInSlot(i).getCount();
                        return MultiLineComponent.literal(count);
                    }
                    case "itemId" -> {
                        channel = "EILink#" + channel;
                        if (!ender.hasEntry(owner, EntryTypes.ENDER_ITEM, channel))
                            return MultiLineComponent.literal(ItemStack.EMPTY.toString());
                        IItemHandler items = ender.getEntry(owner, EntryTypes.ENDER_ITEM, channel).getHandler();
                        if (items.getSlots() == 0) return MultiLineComponent.literal(ItemStack.EMPTY.toString());
                        return MultiLineComponent.literal(items.getStackInSlot(0).toString());
                    }
                    case "itemPull" -> {
                        channel = "EILink#" + channel;
                        if (!ender.hasEntry(owner, EntryTypes.ENDER_ITEM, channel))
                            return MultiLineComponent.empty();
                        IItemHandler items = ender.getEntry(owner, EntryTypes.ENDER_ITEM, channel).getHandler();
                        if (ctx.itemStackHandler() != null)
                            GTTransferUtils.transferItemsFiltered(items, ctx.itemStackHandler(), stack -> true, 1);
                        else throw new NotSupportedException();
                        return MultiLineComponent.empty();
                    }
                    case "itemPush" -> {
                        channel = "EILink#" + channel;
                        if (!ender.hasEntry(owner, EntryTypes.ENDER_ITEM, channel))
                            return MultiLineComponent.empty();
                        IItemHandler items = ender.getEntry(owner, EntryTypes.ENDER_ITEM, channel).getHandler();
                        if (ctx.itemStackHandler() != null)
                            GTTransferUtils.transferItemsFiltered(ctx.itemStackHandler(), items, stack -> true, 1);
                        else throw new NotSupportedException();
                        return MultiLineComponent.empty();
                    }
                    case "fluid" -> {
                        channel = "EFLink#" + channel;
                        if (!ender.hasEntry(owner, EntryTypes.ENDER_FLUID, channel))
                            return MultiLineComponent.literal(0);
                        return MultiLineComponent.literal(
                                ender.getEntry(owner, EntryTypes.ENDER_FLUID, channel).getFluidTank().getFluidAmount());
                    }
                    default -> throw new InvalidArgsException();
                }
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("eval") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                return PlaceholderHandler.processPlaceholders(args.get(0).toString(), ctx);
            }
        });

        PlaceholderHandler.addPlaceholder(new Placeholder("module") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 3);
                int slot = PlaceholderUtils.toInt(args.get(0));
                double x = PlaceholderUtils.toDouble(args.get(1));
                double y = PlaceholderUtils.toDouble(args.get(2));
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot);
                if (stack.getItem() instanceof IComponentItem componentItem) {
                    for (IItemComponent component : componentItem.getComponents()) {
                        if (component instanceof IMonitorModuleItem module) module.tickInPlaceholder(stack, ctx);
                    }
                }
                return MultiLineComponent.empty().addGraphics(new GraphicsComponent(
                        x, y, x, y,
                        "module",
                        (CompoundTag) stack.save(ctx.level().registryAccess())));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("setImage") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2);
                int slot = PlaceholderUtils.toInt(args.get(0));
                String url = args.get(1).toString();
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot);
                if (stack.getItem() instanceof IComponentItem componentItem) {
                    for (IItemComponent component : componentItem.getComponents()) {
                        if (component instanceof ImageModuleBehaviour module) {
                            module.setUrl(stack, url);
                        }
                    }
                }
                return MultiLineComponent.empty();
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("rect") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 5);
                double x = PlaceholderUtils.toDouble(args.get(0));
                double y = PlaceholderUtils.toDouble(args.get(1));
                double width = PlaceholderUtils.toDouble(args.get(2));
                double height = PlaceholderUtils.toDouble(args.get(3));
                if (x < 0) x = 0;
                if (y < 0) y = 0;
                CompoundTag renderData = new CompoundTag();
                renderData.putDouble("x", x);
                renderData.putDouble("y", y);
                renderData.putDouble("width", width);
                renderData.putDouble("height", height);
                renderData.putInt("color", 0xFF000000 | PlaceholderUtils.toInt(args.get(4)));
                return MultiLineComponent.empty().addGraphics(new GraphicsComponent(
                        x, y, x + width, y + height,
                        "rect",
                        renderData));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("quad") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 12);
                CompoundTag renderData = new CompoundTag();
                float x1 = PlaceholderUtils.toFloat(args.get(0));
                float y1 = PlaceholderUtils.toFloat(args.get(1));
                float x2 = PlaceholderUtils.toFloat(args.get(2));
                float y2 = PlaceholderUtils.toFloat(args.get(3));
                float x3 = PlaceholderUtils.toFloat(args.get(4));
                float y3 = PlaceholderUtils.toFloat(args.get(5));
                float x4 = PlaceholderUtils.toFloat(args.get(6));
                float y4 = PlaceholderUtils.toFloat(args.get(7));
                renderData.putFloat("x1", 0);
                renderData.putFloat("y1", 0);
                renderData.putFloat("x2", x2 - x1);
                renderData.putFloat("y2", y2 - y1);
                renderData.putFloat("x3", x3 - x1);
                renderData.putFloat("y3", y3 - y1);
                renderData.putFloat("x4", x4 - x1);
                renderData.putFloat("y4", y4 - y1);
                renderData.putInt("color1", 0xFF000000 + PlaceholderUtils.toInt(args.get(8)));
                renderData.putInt("color2", 0xFF000000 + PlaceholderUtils.toInt(args.get(9)));
                renderData.putInt("color3", 0xFF000000 + PlaceholderUtils.toInt(args.get(10)));
                renderData.putInt("color4", 0xFF000000 + PlaceholderUtils.toInt(args.get(11)));
                return MultiLineComponent.empty().addGraphics(new GraphicsComponent(
                        GTMath.min(x1, x2, x3, x4), GTMath.min(y1, y2, y3, y4), GTMath.max(x1, x2, x3, x4),
                        GTMath.max(y1, y2, y3, y4),
                        "quad",
                        renderData));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("item") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                int slot = PlaceholderUtils.toInt(args.get(0));
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                PlaceholderUtils.checkRange("slot index", 1, ctx.itemStackHandler().getSlots(), slot);
                return MultiLineComponent.literal(ctx.itemStackHandler().getStackInSlot(slot - 1).toString());
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void initRenderers() {
        PlaceholderHandler.addRenderer("module", new ModulePlaceholderRenderer());
        PlaceholderHandler.addRenderer("rect", new RectPlaceholderRenderer());
        PlaceholderHandler.addRenderer("quad", new QuadPlaceholderRenderer());
        PlaceholderHandler.addPlaceholder(new Placeholder("blockNbt") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) {
                BlockEntity blockEntity = ctx.level().getBlockEntity(ctx.pos());
                if (blockEntity == null) return MultiLineComponent.empty();
                Tag tag = blockEntity.saveWithFullMetadata(ctx.level().registryAccess());
                if (tag instanceof CompoundTag compoundTag && compoundTag.contains("cover")) {
                    CompoundTag coverTag = compoundTag.getCompound("cover");
                    if (coverTag.contains(ctx.side().getName())) {
                        CompoundTag cover = coverTag.getCompound(ctx.side().getName()).getCompound("payload")
                                .getCompound("d");
                        cover.putString("text", "[REMOVED]");
                    }
                }
                for (MultiLineComponent arg : args) {
                    if (!(tag instanceof CompoundTag compoundTag)) return MultiLineComponent.empty();
                    tag = compoundTag.get(arg.toString());
                }
                return tag == null ? MultiLineComponent.empty() : MultiLineComponent.literal(tag.toString());
            }
        });
    }
}
