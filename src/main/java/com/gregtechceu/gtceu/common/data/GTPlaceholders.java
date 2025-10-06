package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.placeholder.*;
import com.gregtechceu.gtceu.api.placeholder.exceptions.*;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.monitor.AdvancedMonitorPartMachine;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

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
        int cnt = 0;
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack fluidStack = fluidHandler.getFluidInTank(i);
            String fluidId = Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid())).toString();
            if (id == null || fluidId.equals(id)) cnt += fluidStack.getAmount();
        }
        return cnt;
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
                if (args.isEmpty()) return MultiLineComponent.literal(countItems((ItemFilter) null, itemHandler));
                if (args.size() == 1) return MultiLineComponent
                        .literal(countItems(GTStringUtils.componentsToString(args.get(0)), itemHandler));
                if (GTStringUtils.equals(args.get(0), "filter")) {
                    int slot = PlaceholderUtils.toInt(args.get(1));
                    PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                    try {
                        if (ctx.itemStackHandler() == null)
                            throw new NotSupportedException();
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
                            .literal(countFluids(GTStringUtils.componentsToString(args.get(0)), fluidHandler));
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
                ChatFormatting color = ChatFormatting.getByName(GTStringUtils.componentsToString(args.get(0)));
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
                        args.get(0).stream().map(c -> c.withStyle(ChatFormatting.UNDERLINE)).toList());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("strike") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                return new MultiLineComponent(
                        args.get(0).stream().map(c -> c.withStyle(ChatFormatting.STRIKETHROUGH)).toList());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("obf") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                return new MultiLineComponent(
                        args.get(0).stream().map(c -> c.withStyle(ChatFormatting.OBFUSCATED)).toList());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("random") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 2);
                return MultiLineComponent.literal(GTValues.RNG.nextIntBetweenInclusive(
                        PlaceholderUtils.toInt(args.get(0)), PlaceholderUtils.toInt(args.get(1))));
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
                if (ctx.cover() instanceof IPlaceholderInfoProviderCover cover)
                    return MultiLineComponent.literal(cover.getTicksSincePlaced());
                throw new NotSupportedException();
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
                if (GTStringUtils.equals(args.get(0), "get")) {
                    Direction direction = Direction.byName(GTStringUtils.componentsToString(args.get(1)));
                    if (direction == null)
                        throw new InvalidArgsException();
                    return MultiLineComponent.literal(ctx.level()
                            .getSignal(ctx.pos().relative(direction), direction));
                } else if (GTStringUtils.equals(args.get(1), "set")) {
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
                int i = PlaceholderUtils.toInt(args.get(0));
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
                String arg1 = GTStringUtils.componentsToString(args.get(0));
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
                    PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                    if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                    ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                    int capacity = -1;
                    if (stack.getItem() instanceof ComponentItem componentItem) {
                        for (IItemComponent component : componentItem.getComponents()) {
                            if (component instanceof IDataItem dataComponent) {
                                capacity = dataComponent.getCapacity();
                                break;
                            }
                        }
                    }
                    if (capacity == -1) throw new MissingItemException("any data item", slot);
                    PlaceholderUtils.checkRange("index", 0, capacity - 1, PlaceholderUtils.toInt(args.get(2)));
                    ListTag data = stack.getOrCreateTag().getList("computer_monitor_cover_data", Tag.TAG_STRING);
                    while (data.size() <= PlaceholderUtils.toInt(args.get(2))) data.add(StringTag.valueOf(""));
                    int p = stack.getOrCreateTag().getInt("computer_monitor_cover_p");
                    if (GTStringUtils.equals(args.get(2), "")) args.set(2, MultiLineComponent.literal(p));
                    if (GTStringUtils.equals(args.get(0), "get"))
                        return MultiLineComponent
                                .literal(data.getString(PlaceholderUtils.toInt(args.get(2)) % capacity))
                                .setIgnoreSpaces(true);
                    else if (args.get(0).equalsString("set")) {
                        data.set(PlaceholderUtils.toInt(args.get(2)) % capacity,
                                StringTag.valueOf(args.get(3).toString()));
                        stack.getOrCreateTag().put("computer_monitor_cover_data", data);
                        return MultiLineComponent.empty();
                    } else if (args.get(0).equalsString("setp")) {
                        stack.getOrCreateTag().putInt("computer_monitor_cover_p",
                                PlaceholderUtils.toInt(args.get(3)) % capacity);
                        return MultiLineComponent.empty();
                    } else if (args.get(0).equalsString("inc")) {
                        stack.getOrCreateTag().putInt("computer_monitor_cover_p", (p + 1) % capacity);
                        return MultiLineComponent.empty();
                    } else if (args.get(0).equalsString("dec")) {
                        stack.getOrCreateTag().putInt("computer_monitor_cover_p", p == 0 ? capacity - 1 : p - 1);
                        return MultiLineComponent.empty();
                    } else throw new InvalidArgsException();
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
                int slot = GTStringUtils.toInt(args.get(0));
                PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                Tag tag = ctx.itemStackHandler().getStackInSlot(slot - 1).getOrCreateTag();
                for (int i = 1; i < args.size() - 1; i++) {
                    if (!(tag instanceof CompoundTag compoundTag)) return MultiLineComponent.empty();
                    tag = compoundTag.get(args.get(i).toString());
                }
                return tag == null ? MultiLineComponent.empty() : MultiLineComponent.literal(tag.toString());
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("toChars") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                if (args.get(0).isEmpty()) return MultiLineComponent.empty();
                StringBuilder out = new StringBuilder();
                for (char c : GTStringUtils.componentsToString(args.get(0)).toCharArray()) out.append(c).append(' ');
                return MultiLineComponent.literal(out.substring(0, out.length() - 2));
            }
        });
        PlaceholderHandler.addPlaceholder(new Placeholder("toAscii") {

            @Override
            public MultiLineComponent apply(PlaceholderContext ctx,
                                            List<MultiLineComponent> args) throws PlaceholderException {
                PlaceholderUtils.checkArgs(args, 1);
                String arg = args.get(0).toString();
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
                int l = PlaceholderUtils.toInt(args.get(0));
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
                double a = PlaceholderUtils.toDouble(args.get(0));
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
                int slot = PlaceholderUtils.toInt(args.get(0));
                PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                if (ctx.itemStackHandler() == null) throw new NotSupportedException();
                ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                int capacity = -1;
                if (stack.getItem() instanceof ComponentItem componentItem) {
                    for (IItemComponent component : componentItem.getComponents()) {
                        if (component instanceof IDataItem dataComponent) {
                            capacity = dataComponent.getCapacity();
                            break;
                        }
                    }
                }
                if (capacity == -1) throw new MissingItemException("any data item", slot);
                ListTag tag = stack.getOrCreateTag().getList("computer_monitor_cover_data", Tag.TAG_STRING);
                int operationsLeft = 5000;
                int p = 0, start = 0, cnt = 0;
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
                }
                getData(ctx).putBoolean("completed", true);
                int num = 0;
                for (int i = start; i < code.length(); i++) {
                    if (operationsLeft <= 0) {
                        getData(ctx).putBoolean("completed", false);
                        getData(ctx).putInt("pointer", p);
                        getData(ctx).putInt("index", i);
                        break;
                    }
                    while (tag.size() <= p) tag.add(StringTag.valueOf("0"));
                    if (tag.getString(p).isEmpty()) tag.set(i, StringTag.valueOf("0"));
                    switch (code.charAt(i)) {
                        case '+' -> tag.set(p,
                                StringTag.valueOf(String.valueOf((Integer.parseInt(tag.getString(p)) + num) % 256)));
                        case '-' -> {
                            int tmp = Integer.parseInt(tag.getString(p)) - num;
                            if (tmp < 0) tmp = (256 - ((-tmp) % 256)) % 256;
                            tag.set(p, StringTag.valueOf(String.valueOf(tmp)));
                        }
                        case '>' -> p += num;
                        case '<' -> p -= num;
                        case '[' -> loops.push(i);
                        case ']' -> {
                            if (Integer.parseInt(tag.getString(p)) == 0) loops.pop();
                            else i = loops.peek();
                        }
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
                int slot = PlaceholderUtils.toInt(args.get(0));
                PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                if (!stack.getOrCreateTag().contains("boundPlayerPermLevel"))
                    throw new MissingItemException("any data item bound to player", slot);
                int perm = stack.getOrCreateTag().getInt("boundPlayerPermLevel");
                Component displayName = Component.Serializer
                        .fromJson(stack.getOrCreateTag().getString("boundPlayerName"));
                if (displayName == null) displayName = Component.literal("Placeholder processor");
                if (ctx.level() instanceof ServerLevel serverLevel) {
                    MinecraftServer server = serverLevel.getServer();
                    MultiLineComponent output = MultiLineComponent.empty();
                    UUID playerUUID = null;
                    try {
                        playerUUID = UUID.fromString(stack.getOrCreateTag().getString("boundPlayerUUID"));
                    } catch (RuntimeException ignored) {}
                    ServerPlayer player = playerUUID == null ? null : server.getPlayerList().getPlayer(playerUUID);
                    CommandSource customSource = new CommandSource() {

                        @Override
                        public void sendSystemMessage(@NotNull Component message) {
                            output.append(List.of(message));
                            output.appendNewline();
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
                            perm,
                            displayName.getString(),
                            displayName,
                            server,
                            player);
                    server.getCommands().performPrefixedCommand(source, args.get(1).toString());
                    return output;
                } else throw new NotSupportedException();
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
                long n = PlaceholderUtils.toLong(args.get(0));
                Map<Long, String> suffixes = Map.of(
                        1L, "",
                        1000L, "K",
                        1000000L, "M",
                        1000000000L, "B",
                        1000000000000L, "T");
                long max = 1;
                for (Long i : suffixes.keySet()) {
                    if (n >= i && max < i) max = i;
                }
                return MultiLineComponent.literal("%.2f%s".formatted(((double) n) / max, suffixes.get(max)));
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
                    PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                    ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                    if (stack.getOrCreateTag().contains("boundPlayerUUID"))
                        owner = UUID.fromString(stack.getOrCreateTag().getString("boundPlayerUUID"));
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
                            PlaceholderUtils.checkRange("slot index", 1, 8, slot);
                            ItemStack stack = ctx.itemStackHandler().getStackInSlot(slot - 1);
                            UUID uuid;
                            if (stack.getOrCreateTag().contains("enderRedstoneLinkTransmitterUUID")) {
                                uuid = stack.getOrCreateTag().getUUID("enderRedstoneLinkTransmitterUUID");
                            } else {
                                uuid = UUID.randomUUID();
                                stack.getOrCreateTag().putUUID("enderRedstoneLinkTransmitterUUID", uuid);
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
    }
}
