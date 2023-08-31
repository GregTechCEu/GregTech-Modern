package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkControllerData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderLinkCardBehavior implements IAddInformation {


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        readData(stack).ifPresent(data -> {
            BlockPos pos = data.pos().pos();
            var posDisplay = pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " +
                    "(" + data.pos().dimension().location().getPath() + ")";

            tooltipComponents.add(Component.translatable("metaitem.ender_link_card.linked"));
            tooltipComponents.add(Component.translatable("metaitem.ender_link_card.controller.pos", posDisplay));
            tooltipComponents.add(Component.translatable("metaitem.ender_link_card.controller.id", data.uuid()));
        });
    }


    public static Optional<EnderLinkControllerData> readData(ItemStack item) {
        if (!isEnderLinkCard(item))
            return Optional.empty();

        return Optional.ofNullable(item.getTag())
                .map(tag -> {
                    var uuid = tag.getUUID("controller");
                    var dimension = ResourceKey.create(
                            Registry.DIMENSION_REGISTRY,
                            ResourceLocation.of(tag.getString("dimension"), ':')
                    );
                    var position = new BlockPos(
                            tag.getInt("x"),
                            tag.getInt("y"),
                            tag.getInt("z")
                    );

                    return new EnderLinkControllerData(GlobalPos.of(dimension, position), uuid);
                });
    }

    public static void writeData(ItemStack item, @Nullable EnderLinkControllerData data) {
        if (!isEnderLinkCard(item))
            throw new IllegalArgumentException("The supplied item is not an ender link card.");

        var tag = item.getOrCreateTag();

        if (data == null) {
            tag.remove("controller");
            tag.remove("dimension");
            tag.remove("x");
            tag.remove("y");
            tag.remove("z");

            return;
        }

        tag.putUUID("controller", data.uuid());
        tag.putString("dimension", data.pos().dimension().location().toString());
        tag.putInt("x", data.pos().pos().getX());
        tag.putInt("y", data.pos().pos().getY());
        tag.putInt("z", data.pos().pos().getZ());
    }

    public static boolean isEnderLinkCard(ItemStack item) {
        return GTItems.ENDER_LINK_CARD.isIn(item);
    }
}
