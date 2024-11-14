package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.entity.*;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.MobCategory;

import com.tterrag.registrate.util.entry.EntityEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTEntityTypes {

    public static final EntityEntry<DynamiteEntity> DYNAMITE = REGISTRATE
            .<DynamiteEntity>entity("dynamite", DynamiteEntity::new, MobCategory.MISC)
            .lang("Dynamite")
            .properties(builder -> builder.sized(0.25F, 0.25F).fireImmune().clientTrackingRange(4).updateInterval(10))
            .tag(EntityTypeTags.IMPACT_PROJECTILES)
            .register();

    public static final EntityEntry<PowderbarrelEntity> POWDERBARREL = REGISTRATE
            .<PowderbarrelEntity>entity("powderbarrel", PowderbarrelEntity::new, MobCategory.MISC)
            .lang("Powderbarrel")
            .properties(builder -> builder.sized(0.98F, 0.98F).fireImmune().clientTrackingRange(10).updateInterval(10))
            .register();

    public static final EntityEntry<IndustrialTNTEntity> INDUSTRIAL_TNT = REGISTRATE
            .<IndustrialTNTEntity>entity("industrial_tnt", IndustrialTNTEntity::new, MobCategory.MISC)
            .lang("Industrial TNT")
            .properties(builder -> builder.sized(0.98F, 0.98F).fireImmune().clientTrackingRange(10).updateInterval(10))
            .register();

    public static final EntityEntry<GTBoat> BOAT = REGISTRATE
            .<GTBoat>entity("boat", GTBoat::new, MobCategory.MISC)
            .lang("Boat")
            .properties(builder -> builder.sized(1.375f, 0.5625f).clientTrackingRange(10))
            .register();

    public static final EntityEntry<GTChestBoat> CHEST_BOAT = REGISTRATE
            .<GTChestBoat>entity("chest_boat", GTChestBoat::new, MobCategory.MISC)
            .lang("Chest Boat")
            .properties(builder -> builder.sized(1.375f, 0.5625f).clientTrackingRange(10))
            .register();

    public static void init() {}
}
