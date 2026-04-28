package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElementHelper;

public class MEPatternBufferProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof MEPatternBufferPartMachine) {
            CompoundTag serverData = blockAccessor.getServerData();
            if (!serverData.getBooleanOr("formed", false)) return;

            iTooltip.add(Component.translatable("gtceu.top.proxies_bound", serverData.getIntOr("proxies", 0))
                    .withStyle(TooltipHelper.RAINBOW_HSL_SLOW));
            readBufferTag(iTooltip, serverData);
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MEPatternBufferPartMachine buffer) {
            if (!buffer.isFormed()) {
                compoundTag.putBoolean("formed", false);
                return;
            }
            compoundTag.putBoolean("formed", true);
            compoundTag.putInt("proxies", buffer.getProxies().size());
            writeBufferTag(compoundTag, buffer);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTJadeIds.toResourceLocation("me_pattern_buffer");
    }

    public static void writeBufferTag(CompoundTag compoundTag, MEPatternBufferPartMachine buffer) {
        var merged = buffer.mergeInternalSlots();
        var items = merged.items();
        var fluids = merged.fluids();

        HolderLookup.Provider provider = buffer.getLevel().registryAccess();
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        ListTag itemsTag = new ListTag();
        for (var entry : items.object2LongEntrySet()) {
            var ct = (CompoundTag) ItemStack.CODEC.encodeStart(ops, entry.getKey()).getOrThrow();
            ct.putLong("real", entry.getLongValue());
            itemsTag.add(ct);
        }
        if (!itemsTag.isEmpty()) compoundTag.put("items", itemsTag);

        ListTag fluidsTag = new ListTag();
        for (var entry : fluids.object2LongEntrySet()) {
            var ct = (CompoundTag) FluidStack.CODEC.encodeStart(ops, entry.getKey()).getOrThrow();
            ct.putLong("real", entry.getLongValue());
            fluidsTag.add(ct);
        }
        if (!fluidsTag.isEmpty()) compoundTag.put("fluids", fluidsTag);
    }

    public static void readBufferTag(ITooltip iTooltip, CompoundTag serverData) {
        IElementHelper helper = IElementHelper.get();

        HolderLookup.Provider provider = Minecraft.getInstance().level.registryAccess();
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        ListTag itemsTag = serverData.getListOrEmpty("items");
        for (Tag t : itemsTag) {
            if (!(t instanceof CompoundTag ct)) continue;
            var stack = ItemStack.CODEC.parse(ops, ct).result();
            var count = ct.getLongOr("real", 0);
            if (stack.isPresent() && !stack.get().isEmpty() && count > 0) {
                iTooltip.add(helper.smallItem(stack.get()));
                Component text = Component.literal(" ")
                        .append(Component.literal(FormattingUtil.formatNumbers(count))
                                .withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal("× ").withStyle(ChatFormatting.WHITE))
                        .append(stack.get().getHoverName().copy().withStyle(ChatFormatting.GOLD));
                iTooltip.append(text);
            }
        }
        ListTag fluidsTag = serverData.getListOrEmpty("fluids");
        for (Tag t : fluidsTag) {
            if (!(t instanceof CompoundTag ct)) continue;
            var stack = FluidStack.CODEC.parse(ops, ct).result();
            var amount = ct.getLongOr("real", 0);
            if (stack.isPresent() && !stack.get().isEmpty() && amount > 0) {
                iTooltip.add(GTElementHelper.smallFluid(JadeFluidObject.of(stack.get().getFluid())));
                Component text = Component.literal(" ")
                        .append(Component.literal(FormattingUtil.formatBuckets(amount)))
                        .withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
                        .append(stack.get().getHoverName().copy().withStyle(ChatFormatting.DARK_AQUA));
                iTooltip.append(text);
            }
        }
    }
}
