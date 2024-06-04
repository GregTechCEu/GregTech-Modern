package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviorsComponent;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsComponent;

public class ToolModeSwitchBehavior implements IToolBehavior<ToolModeSwitchBehavior> {

    public static final ToolModeSwitchBehavior INSTANCE = new ToolModeSwitchBehavior(ModeType.BOTH);

    public static final Codec<ToolModeSwitchBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ModeType.CODEC.lenientOptionalFieldOf("mode_type", ModeType.BOTH)
                    .forGetter(val -> val.modeType))
            .apply(instance, ToolModeSwitchBehavior::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolModeSwitchBehavior> STREAM_CODEC = StreamCodec
            .composite(
                    ModeType.STREAM_CODEC, ToolModeSwitchBehavior::getModeType,
                    ToolModeSwitchBehavior::new);

    @Getter
    private final ToolModeSwitchBehavior.ModeType modeType;

    protected ToolModeSwitchBehavior(ToolModeSwitchBehavior.ModeType type) {
        this.modeType = type;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level world, @NotNull Player player,
                                                                        @NotNull InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            ToolModeSwitchBehavior.ModeType type = ModeType.values()[(this.modeType.ordinal() + 1) %
                    ModeType.values().length];
            itemStack.update(GTDataComponents.TOOL_BEHAVIORS, ToolBehaviorsComponent.EMPTY,
                    behavior -> behavior.withBehavior(new ToolModeSwitchBehavior(type)));

            player.displayClientMessage(Component.translatable("metaitem.machine_configuration.mode", type.getName()),
                    true);
            return InteractionResultHolder.success(itemStack.copy());
        }

        return IToolBehavior.super.onItemRightClick(world, player, hand);
    }

    @Override
    public ToolBehaviorType<ToolModeSwitchBehavior> getType() {
        return GTToolBehaviors.MODE_SWITCH;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        var component = getBehaviorsComponent(stack);
        ToolModeSwitchBehavior behavior = component.getBehavior(GTToolBehaviors.MODE_SWITCH);
        tooltip.add(Component.translatable("metaitem.machine_configuration.mode",
                (behavior != null ? behavior.modeType : ModeType.BOTH).getName()));
    }

    public enum ModeType implements StringRepresentable {

        ITEM("item", Component.translatable("gtceu.mode.item")),
        FLUID("fluid", Component.translatable("gtceu.mode.fluid")),
        BOTH("both", Component.translatable("gtceu.mode.both"));

        public static final Codec<ModeType> CODEC = StringRepresentable.fromEnum(ModeType::values);
        public static final StreamCodec<ByteBuf, ModeType> STREAM_CODEC = ByteBufCodecs.BYTE
                .map(aByte -> ModeType.values()[aByte], val -> (byte) val.ordinal());

        @Getter
        private final String id;
        @Getter
        private final Component name;

        ModeType(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }
}
