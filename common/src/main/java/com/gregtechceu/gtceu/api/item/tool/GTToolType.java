package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote GTToolType
 */
public enum GTToolType {
//    SWORD("sword", 3, -2.4F),
//    PICKAXE("pickaxe", 1, -2.8F),
//    SHOVEL("shovel", 1.5F, -3.0F),
//    AXE("axe", 6.0F, -3.2F),
//    HOE("hoe", 0, -3.0F),
    SAW("saw", 1, 1, GTSoundEntries.SAW_TOOL),
    HARD_HAMMER("hammer", 1, 1, GTSoundEntries.FORGE_HAMMER),
    SOFT_MALLET("mallet", 1, 1, GTSoundEntries.SOFT_MALLET_TOOL),
    WRENCH("wrench", 1, 1, GTSoundEntries.WRENCH_TOOL),
    FILE("file", 1, 1, GTSoundEntries.FILE_TOOL),
    CROWBAR("crowbar", 1, 1, new ExistingSoundEntry(SoundEvents.ITEM_BREAK, SoundSource.BLOCKS)),
    SCREWDRIVER("screwdriver", 1, 1, GTSoundEntries.SCREWDRIVER_TOOL),
    MORTAR("mortar", 1, 1, GTSoundEntries.MORTAR_TOOL),
    WIRE_CUTTER("wire_cutter", 1, 1, GTSoundEntries.WIRECUTTER_TOOL),
    SCYTHE("scythe", 1, 1),
//    SHEARS("shears", 1, 1, GTCEu.id("item/tools/handle_hammer"), GTCEu.id("item/tools/hammer")),
    KNIFE("knife", 1, 1),
    BUTCHERY_KNIFE("butchery_knife", 1, 1),
//    GRAFTER("grafter", 1, 1, GTCEu.id("item/tools/handle_hammer"), GTCEu.id("item/tools/hammer")),
    PLUNGER("plunger", 1, 1, GTSoundEntries.PLUNGER_TOOL);
    
    public final String name;
    public final TagKey<Item> itemTag;
    public final TagKey<Block> harvestTag;
    public final float attackDamageModifier;
    public final float attackSpeedModifier;
    public final ResourceLocation modelLocation;
    @Nullable
    public final SoundEntry soundEntry;

    GTToolType(String name, TagKey<Block> harvestTag, TagKey<Item> itemTag, float attackDamageModifier, float attackSpeedModifier, ResourceLocation modelLocation, SoundEntry soundEntry) {
        this.name = name;
        this.itemTag = itemTag;
        this.harvestTag = harvestTag;
        this.attackDamageModifier = attackDamageModifier;
        this.attackSpeedModifier = attackSpeedModifier;
        this.modelLocation = modelLocation;
        this.soundEntry = soundEntry;
    }

    GTToolType(String name, TagKey<Block> harvestTag, float attackDamageModifier, float attackSpeedModifier, ResourceLocation modelLocation, SoundEntry soundEntry) {
        this(name, harvestTag, TagUtil.createItemTag("tool/" + name), attackDamageModifier, attackSpeedModifier, modelLocation, soundEntry);
    }

    GTToolType(String name, float attackDamageModifier, float attackSpeedModifier, ResourceLocation modelLocation, SoundEntry soundEntry) {
        this(name, TagUtil.createBlockTag("mineable/" + name), attackDamageModifier, attackSpeedModifier, modelLocation, soundEntry);
    }

    GTToolType(String name, float attackDamageModifier, float attackSpeedModifier, SoundEntry soundEntry) {
        this(name, attackDamageModifier, attackSpeedModifier, GTCEu.id(String.format("item/tools/%s", name)), soundEntry);
    }

    GTToolType(String name, float attackDamageModifier, float attackSpeedModifier) {
        this(name, attackDamageModifier, attackSpeedModifier, GTCEu.id(String.format("item/tools/%s", name)), null);
    }

    public boolean is(ItemStack itemStack) {
        return ToolHelper.is(itemStack, this);
    }

    public String getUnlocalizedName() {
        return "tool_type." + name;
    }
}
