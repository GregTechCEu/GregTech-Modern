package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @see gregtech.common.ToolEventHandlers#onHarvestDrops(BlockEvent.HarvestDropsEvent)
 */
public class HarvestIceBehavior implements IToolBehavior<HarvestIceBehavior> {

    public static final HarvestIceBehavior INSTANCE = new HarvestIceBehavior();
    public static final MapCodec<HarvestIceBehavior> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, HarvestIceBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected HarvestIceBehavior() {/**/}

    // ice harvesting is handled in an event elsewhere

    @Override
    public ToolBehaviorType<HarvestIceBehavior> getType() {
        return GTToolBehaviors.HARVEST_ICE;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.silk_ice"));
    }
}
