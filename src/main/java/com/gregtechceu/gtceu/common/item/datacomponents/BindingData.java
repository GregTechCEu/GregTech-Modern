package com.gregtechceu.gtceu.common.item.datacomponents;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.EntityGetter;
import net.neoforged.neoforge.common.UsernameCache;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public record BindingData(int permissionLevel, UUID uuid) implements TooltipProvider {

    // spotless:off
    public static final Codec<BindingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("permission_level").forGetter(BindingData::permissionLevel),
            UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(BindingData::uuid)
    ).apply(instance, BindingData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BindingData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BindingData::permissionLevel,
            UUIDUtil.STREAM_CODEC, BindingData::uuid,
            BindingData::new
    );
    //spotless:on

    public @Nullable Component getBoundPlayerName(@Nullable EntityGetter level) {
        if (level != null) {
            Player player = level.getPlayerByUUID(this.uuid);
            if (player != null) {
                return player.getDisplayName();
            }
        } else {
            String lastUsername = UsernameCache.getLastKnownUsername(this.uuid);
            if (lastUsername != null) {
                return Component.literal(lastUsername);
            }
        }
        return null;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        Component displayName = getBoundPlayerName(context.level());
        if (displayName == null) {
            displayName = Component.translatable("gtceu.tooltip.player_name.unknown");
        }

        tooltipAdder.accept(Component.translatable("gtceu.tooltip.player_bind", displayName));
    }
}
