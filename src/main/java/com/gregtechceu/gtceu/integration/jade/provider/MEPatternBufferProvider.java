package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElementHelper;

public class MEPatternBufferProvider extends MachineInfoProvider<MEPatternBufferPartMachine, CompoundTag> {

    public MEPatternBufferProvider() {
        super(GTCEu.id("me_pattern_buffer"), MEPatternBufferPartMachine.class);
    }

    @Override
    protected CompoundTag write(MEPatternBufferPartMachine buffer) {
        var tag = new CompoundTag();
        if (!buffer.isFormed()) {
            tag.putBoolean("formed", false);
            return tag;
        }
        tag.putBoolean("formed", true);
        tag.putInt("proxies", buffer.getProxies().size());
        writeBufferTag(tag, buffer);
        return tag;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!data.getBoolean("formed")) return;

        tooltip.add(Component.translatable("gtceu.top.proxies_bound", data.getInt("proxies"))
                .withStyle(TooltipHelper.RAINBOW_HSL_SLOW));
        readBufferTag(tooltip, data);
    }

    public static void writeBufferTag(CompoundTag compoundTag, MEPatternBufferPartMachine buffer) {
        var merged = buffer.mergeInternalSlots();
        var items = merged.items();
        var fluids = merged.fluids();

        HolderLookup.Provider provider = buffer.getLevel().registryAccess();
        ListTag itemsTag = new ListTag();
        for (var entry : items.object2LongEntrySet()) {
            var ct = (CompoundTag) entry.getKey().save(provider);
            ct.putLong("real", entry.getLongValue());
            itemsTag.add(ct);
        }
        if (!itemsTag.isEmpty()) compoundTag.put("items", itemsTag);

        ListTag fluidsTag = new ListTag();
        for (var entry : fluids.object2LongEntrySet()) {
            var ct = (CompoundTag) entry.getKey().save(provider);
            ct.putLong("real", entry.getLongValue());
            fluidsTag.add(ct);
        }
        if (!fluidsTag.isEmpty()) compoundTag.put("fluids", fluidsTag);
    }

    public static void readBufferTag(ITooltip iTooltip, CompoundTag serverData) {
        IElementHelper helper = IElementHelper.get();

        HolderLookup.Provider provider = Minecraft.getInstance().level.registryAccess();
        ListTag itemsTag = serverData.getList("items", Tag.TAG_COMPOUND);
        for (Tag t : itemsTag) {
            if (!(t instanceof CompoundTag ct)) continue;
            var stack = ItemStack.parse(provider, ct);
            var count = ct.getLong("real");
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
        ListTag fluidsTag = serverData.getList("fluids", Tag.TAG_COMPOUND);
        for (Tag t : fluidsTag) {
            if (!(t instanceof CompoundTag ct)) continue;
            var stack = FluidStack.parse(provider, ct);
            var amount = ct.getLong("real");
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
