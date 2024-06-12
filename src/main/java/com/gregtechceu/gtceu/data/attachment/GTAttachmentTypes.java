package com.gregtechceu.gtceu.data.attachment;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class GTAttachmentTypes {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, GTCEu.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MedicalConditionTracker>> MEDICAL_CONDITION_TRACKER = ATTACHMENT_TYPES
            .register("hazard_tracker", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Player player) {
                    return new MedicalConditionTracker(player);
                }
                return null;
            }).build());

    public static void init(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
