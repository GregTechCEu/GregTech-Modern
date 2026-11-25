package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.spoilage.BlockSpoilTicker;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import org.jetbrains.annotations.ApiStatus;

public class GTAttachmentTypes {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, GTCEu.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MedicalConditionTracker>> MEDICAL_CONDITION_TRACKER = ATTACHMENT_TYPES
            .register("hazard_tracker", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Player player) {
                    return new MedicalConditionTracker(player);
                }
                return null;
            }).build());

    @ApiStatus.Internal
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LevelChunkTicks<Block>>> SPOILAGE_CHUNK_TICKS = ATTACHMENT_TYPES
            .register("spoilage_chunk_ticks", () -> AttachmentType.builder(() -> new LevelChunkTicks<Block>())
                    .serialize(BlockSpoilTicker.ChunkTickSerialiser.INSTANCE).build());

    public static void init(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
