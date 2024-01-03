package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import net.minecraft.core.registries.BuiltInRegistries;
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
    SWORD("sword", "swords", 3, -2.4F, false),
    PICKAXE("pickaxe", "pickaxes", 1, -2.8F, true),
    SHOVEL("shovel", "shovels", 1.5F, -3.0F, true),
    AXE("axe", "axes", 6.0F, -3.2F, true),
    HOE("hoe", "hoes", 0, -3.0F, true),

    MINING_HAMMER("mining_hammer", "mining_hammers", TagUtil.createBlockTag("mineable/pickaxe", true), 1.5F, -3.2F, GTCEu.id("item/tools/mining_hammer"), null, false, 3),

    SAW("saw", "saws", -1.0F, 1, GTSoundEntries.SAW_TOOL),
    HARD_HAMMER("hammer", "hammers", 1, -2.8F, GTSoundEntries.FORGE_HAMMER),
    SOFT_MALLET("mallet", "mallets", 0, -2.4F, GTSoundEntries.SOFT_MALLET_TOOL),
    WRENCH("wrench", "wrenches", 1, -2.8F, GTSoundEntries.WRENCH_TOOL),
    FILE("file", "files", 0, -2.4F, GTSoundEntries.FILE_TOOL),
    CROWBAR("crowbar", "crowbars", 2.0F, -2.4F, new ExistingSoundEntry(SoundEvents.ITEM_BREAK, SoundSource.BLOCKS)),
    SCREWDRIVER("screwdriver", "screwdrivers", -1.0F, 3.0F, GTSoundEntries.SCREWDRIVER_TOOL),
    MORTAR("mortar", "mortars", 0, -2.4F, GTSoundEntries.MORTAR_TOOL),
    WIRE_CUTTER("wire_cutter", "wire_cutters", -1.0F, -2.4F, GTSoundEntries.WIRECUTTER_TOOL),
    SCYTHE("scythe", "scythes", 5.0F, -3.0F),
//    SHEARS("shears", 1, 1, GTCEu.id("item/tools/handle_hammer"), GTCEu.id("item/tools/hammer")),
    KNIFE("knife", "knives", 1, 3.0F),
    BUTCHERY_KNIFE("butchery_knife", "butchery_knives", 1.5F, -1.3F),
//    GRAFTER("grafter", 1, 1, GTCEu.id("item/tools/handle_hammer"), GTCEu.id("item/tools/hammer")),
    PLUNGER("plunger", "plungers", 0, -2.4F, GTSoundEntries.PLUNGER_TOOL);

    public final String name;
    public final TagKey<Item> itemTag;
    public final TagKey<Block> harvestTag;
    public final float attackDamageModifier;
    public final float attackSpeedModifier;
    public final ResourceLocation modelLocation;
    @Nullable
    public final SoundEntry soundEntry;
    public final int durabilityMultiplier;

    GTToolType(String name, TagKey<Block> harvestTag, TagKey<Item> itemTag, float attackDamageModifier, float attackSpeedModifier, ResourceLocation modelLocation, SoundEntry soundEntry, int durabilityMultiplier) {
        this.name = name;
        this.itemTag = itemTag;
        this.harvestTag = harvestTag;
        this.attackDamageModifier = attackDamageModifier;
        this.attackSpeedModifier = attackSpeedModifier;
        this.modelLocation = modelLocation;
        this.soundEntry = soundEntry;
        this.durabilityMultiplier = durabilityMultiplier;
    }

    GTToolType(String name, String plural, TagKey<Block> harvestTag, float attackDamageModifier, float attackSpeedModifier, ResourceLocation modelLocation, SoundEntry soundEntry, boolean isVanilla, int durabilityMultiplier) {
        this(name, harvestTag, isVanilla ? TagUtil.createItemTag(plural, true) : TagUtil.createPlatformItemTag("tools/" + plural, plural), attackDamageModifier, attackSpeedModifier, modelLocation, soundEntry, durabilityMultiplier);
    }

    GTToolType(String name, String plural, float attackDamageModifier, float attackSpeedModifier, ResourceLocation modelLocation, SoundEntry soundEntry, boolean isVanilla) {
        this(name, plural, isVanilla ? TagUtil.createBlockTag("mineable/" + name, true) : TagUtil.createPlatformUnprefixedTag(BuiltInRegistries.BLOCK, "forge:mineable/" + name, "fabric:mineable/" + name), attackDamageModifier, attackSpeedModifier, modelLocation, soundEntry, isVanilla, 1);
    }

    GTToolType(String name, String plural, float attackDamageModifier, float attackSpeedModifier, SoundEntry soundEntry, boolean isVanilla) {
        this(name, plural, attackDamageModifier, attackSpeedModifier, GTCEu.id(String.format("item/tools/%s", name)), soundEntry, isVanilla);
    }

    GTToolType(String name, String plural, float attackDamageModifier, float attackSpeedModifier, SoundEntry soundEntry) {
        this(name, plural, attackDamageModifier, attackSpeedModifier, soundEntry, false);
    }

    GTToolType(String name, String plural, float attackDamageModifier, float attackSpeedModifier, boolean isVanilla) {
        this(name, plural, attackDamageModifier, attackSpeedModifier, null, isVanilla);
    }

    GTToolType(String name, String plural, float attackDamageModifier, float attackSpeedModifier) {
        this(name, plural, attackDamageModifier, attackSpeedModifier, false);
    }

    public boolean is(ItemStack itemStack) {
        return ToolHelper.is(itemStack, this);
    }

    public String getUnlocalizedName() {
        return "item.gtceu.tool." + name;
    }
}
