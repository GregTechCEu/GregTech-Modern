package com.gregtechceu.gtceu.common.item.spoil_actions;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilAction;
import com.gregtechceu.gtceu.api.item.spoilage.SpoilContext;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

/**
 * A spoil action which transforms 1x of the spoiling item into {@literal <item_multipler>} of the result item
 */
@Accessors(fluent = true)
public class TransformItemSpoilAction extends SpoilAction {

    // spotless:off
    public static final MapCodec<TransformItemSpoilAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryFixedCodec.create(Registries.ITEM).fieldOf("result_item").forGetter(TransformItemSpoilAction::resultItem),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("item_multiplier").forGetter(TransformItemSpoilAction::itemMultiplier)
    ).apply(instance, TransformItemSpoilAction::new));
    // spotless:on

    @Getter
    private final Holder<Item> resultItem;
    @Getter
    private final int itemMultiplier;

    public TransformItemSpoilAction(Holder<Item> resultItem, @Range(from = 0, to = Integer.MAX_VALUE) int itemMultiplier) {
        Validate.exclusiveBetween(0, Integer.MAX_VALUE, itemMultiplier, "Item multiplier must be greater than 0.");
        this.resultItem = resultItem;
        this.itemMultiplier = itemMultiplier;
    }

    public TransformItemSpoilAction(Holder<Item> resultItem) {
        this(resultItem, 1);
    }

    public TransformItemSpoilAction(Item resultItem, @Range(from = 0, to = Integer.MAX_VALUE) int itemMultiplier) {
        Validate.exclusiveBetween(0, Integer.MAX_VALUE, itemMultiplier, "Item multiplier must be greater than 0.");
        this.resultItem = resultItem.builtInRegistryHolder();
        this.itemMultiplier = itemMultiplier;
    }

    public TransformItemSpoilAction(Item resultItem) {
        this(resultItem, 1);
    }

    private ItemStack getResult(ItemStack stack) {
        return resultItem.value().getDefaultInstance().copyWithCount(stack.getCount() * itemMultiplier);
    }

    @Override
    public ItemStack getSpoilResult(ItemStack currentSpoilResult, ItemStack stack, SpoilContext spoilContext, boolean simulate) {
        return getResult(stack);
    }

    @Override
    public void appendTooltip(List<Component> tooltips, ItemStack stack) {
        ItemStack resultStack = getResult(stack);
        if (!resultStack.isEmpty()) tooltips.add(resultStack.getHoverName());
    }

    @Override
    public MapCodec<? extends SpoilAction> codec() {
        return CODEC;
    }
}
