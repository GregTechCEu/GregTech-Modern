package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.common.item.tool.behavior.*;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author Screret
 * @date 2023/2/23
 * @implNote GTToolType
 */
public class GTToolType {
    @Getter
    private static final Map<String, GTToolType> types = new HashMap<>();

    public static final GTToolType SWORD = GTToolType.builder("sword")
            .toolTag(TagUtil.createItemTag("swords", true))
            .harvestTag(TagUtil.createBlockTag("mineable/sword"))
            .toolStats(b -> b.attacking().attackDamage(3.0F).attackSpeed(-2.4F))
            .constructor(GTSwordItem::create)
            .build();
    public static final GTToolType PICKAXE = GTToolType.builder("pickaxe")
            .toolTag(TagUtil.createItemTag("pickaxes", true))
            .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", true))
            .toolStats(b -> b.blockBreaking().attackDamage(1.0F).attackSpeed(-2.8F)/*.behaviors(TorchPlaceBehavior.INSTANCE)*/)
            .build();
    public static final GTToolType SHOVEL = GTToolType.builder("shovel")
            .toolTag(TagUtil.createItemTag("shovels", true))
            .harvestTag(TagUtil.createBlockTag("mineable/shovel", true))
            .toolStats(b -> b.blockBreaking().attackDamage(1.5F).attackSpeed(-3.0F).behaviors(GrassPathBehavior.INSTANCE))
            .constructor(GTShovelItem::create)
            .build();
    public static final GTToolType AXE = GTToolType.builder("axe")
            .toolTag(TagUtil.createItemTag("axes", true))
            .harvestTag(TagUtil.createBlockTag("mineable/axe", true))
            .toolStats(b -> b.blockBreaking()
                    .attackDamage(5.0F).attackSpeed(-3.2F).baseEfficiency(2.0F)
                    .behaviors(DisableShieldBehavior.INSTANCE, TreeFellingBehavior.INSTANCE, LogStripBehavior.INSTANCE, ScrapeBehavior.INSTANCE, WaxOffBehavior.INSTANCE))
            .constructor(GTAxeItem::create)
            .build();
    public static final GTToolType HOE = GTToolType.builder("hoe")
            .toolTag(TagUtil.createItemTag("hoes", true))
            .harvestTag(TagUtil.createBlockTag("mineable/hoe", true))
            .toolStats(b -> b.cannotAttack().attackSpeed(-1.0F).behaviors(HoeGroundBehavior.INSTANCE))
            .constructor(GTHoeItem::create)
            .build();

    public static final GTToolType MINING_HAMMER = GTToolType.builder("mining_hammer")
            .toolTag(TagUtil.createItemTag("tools/mining_hammers", false))
            .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", true))
            .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                    .efficiencyMultiplier(0.4F).attackDamage(1.5F).attackSpeed(-3.2F)
                    .durabilityMultiplier(3.0F)
                    /*.behaviors(TorchPlaceBehavior.INSTANCE)*/)
            .build();
    public static final GTToolType SPADE = GTToolType.builder("spade")
            .toolTag(TagUtil.createItemTag("tools/spades", false))
            .harvestTag(TagUtil.createBlockTag("mineable/shovel", true))
            .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                    .efficiencyMultiplier(0.4F).attackDamage(1.5F).attackSpeed(-3.2F)
                    .durabilityMultiplier(3.0F)
                    .behaviors(GrassPathBehavior.INSTANCE))
            .build();
    public static final GTToolType SCYTHE = GTToolType.builder("scythe")
            .toolTag(TagUtil.createItemTag("tools/scythes", false))
            .harvestTag(TagUtil.createBlockTag("mineable/hoe", true))
            .toolStats(b -> b.blockBreaking().attacking()
                    .attackDamage(5.0F).attackSpeed(-3.0F).durabilityMultiplier(3.0F)
                    .aoe(2, 2, 2)
                    .behaviors(HoeGroundBehavior.INSTANCE, HarvestCropsBehavior.INSTANCE)
                    .canApplyEnchantment(EnchantmentCategory.DIGGER))
            .constructor(GTHoeItem::create)
            .build();

    public static final GTToolType SAW = GTToolType.builder("saw")
            .toolTag(TagUtil.createItemTag("tools/saws", false))
            .harvestTag(TagUtil.createBlockTag("mineable/saw", false))
            .toolStats(b -> b.crafting().damagePerCraftingAction(2)
                    .attackDamage(-1.0F).attackSpeed(-2.6F)
                    .behaviors(HarvestIceBehavior.INSTANCE))
            .sound(GTSoundEntries.SAW_TOOL)
            .symbol('s')
            .build();
    public static final GTToolType HARD_HAMMER = GTToolType.builder("hammer")
            .toolTag(TagUtil.createItemTag("tools/hammers", false))
            .harvestTag(TagUtil.createBlockTag("mineable/hammer", false))
            .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", true))
            .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(2)
                    .attackDamage(1.0F).attackSpeed(-2.8F)
                    .behaviors(new EntityDamageBehavior(2.0F, IronGolem.class)))
            .sound(GTSoundEntries.FORGE_HAMMER)
            .symbol('h')
            .build();
    public static final GTToolType SOFT_MALLET = GTToolType.builder("mallet")
            .toolTag(TagUtil.createItemTag("tools/mallets", false))
            .toolStats(b -> b.crafting().cannotAttack().attackSpeed(-2.4F))
            .sound(GTSoundEntries.SOFT_MALLET_TOOL)
            .symbol('r')
            .build();
    public static final GTToolType WRENCH = GTToolType.builder("wrench")
            .toolTag(TagUtil.createItemTag("tools/wrenches", false))
            .toolTag(TagUtil.createItemTag("tools/wrench", false))
            .harvestTag(TagUtil.createBlockTag("mineable/wrench", false))
            .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                    .attackDamage(1.0F).attackSpeed(-2.8F)
                    .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, IronGolem.class)))
            .sound(GTSoundEntries.WRENCH_TOOL)
            .symbol('w')
            .build();
    public static final GTToolType FILE = GTToolType.builder("file")
            .toolTag(TagUtil.createItemTag("tools/files", false))
            .toolStats(b -> b.crafting().damagePerCraftingAction(4)
                    .cannotAttack().attackSpeed(-2.4F))
            .sound(GTSoundEntries.FILE_TOOL)
            .symbol('f')
            .build();
    public static final GTToolType CROWBAR = GTToolType.builder("crowbar")
            .toolTag(TagUtil.createItemTag("tools/crowbars", false))
            .harvestTag(TagUtil.createBlockTag("mineable/crowbar", false))
            .toolStats(b -> b.blockBreaking().crafting()
                    .attackDamage(2.0F).attackSpeed(-2.4F)
                    .sneakBypassUse().behaviors(RotateRailBehavior.INSTANCE))
            .sound(new ExistingSoundEntry(SoundEvents.ITEM_BREAK, SoundSource.BLOCKS))
            .symbol('c')
            .build();
    public static final GTToolType SCREWDRIVER = GTToolType.builder("screwdriver")
            .toolTag(TagUtil.createItemTag("tools/screwdrivers", false))
            .toolStats(b -> b.crafting().damagePerCraftingAction(4).sneakBypassUse()
                    .attackDamage(-1.0F).attackSpeed(3.0F)
                    .behaviors(new EntityDamageBehavior(3.0F, Spider.class)))
            .sound(GTSoundEntries.SCREWDRIVER_TOOL)
            .symbol('d')
            .build();
    public static final GTToolType MORTAR = GTToolType.builder("mortar")
            .toolTag(TagUtil.createItemTag("tools/mortars", false))
            .toolStats(b -> b.crafting().damagePerCraftingAction(2).cannotAttack().attackSpeed(-2.4F))
            .sound(GTSoundEntries.MORTAR_TOOL)
            .symbol('m')
            .build();
    public static final GTToolType WIRE_CUTTER = GTToolType.builder("wire_cutter")
            .toolTag(TagUtil.createItemTag("tools/wire_cutters", false))
            .harvestTag(TagUtil.createBlockTag("mineable/wire_cutter", false))
            .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                    .damagePerCraftingAction(4).attackDamage(-1.0F).attackSpeed(-2.4F))
            .sound(GTSoundEntries.WIRECUTTER_TOOL)
            .symbol('x')
            .build();
    public static final GTToolType KNIFE = GTToolType.builder("knife")
            .toolTag(TagUtil.createItemTag("tools/knives", false))
            .harvestTag(TagUtil.createBlockTag("mineable/knife", false))
            .toolStats(b -> b.crafting().attacking().attackSpeed(3.0F))
            .constructor(GTSwordItem::create)
            .symbol('k')
            .build();
    public static final GTToolType BUTCHERY_KNIFE = GTToolType.builder("butchery_knife")
            .toolTag(TagUtil.createItemTag("tools/butchery_knives", false))
            .toolStats(b -> b.attacking().attackDamage(1.5F).attackSpeed(-1.3F).defaultEnchantment(Enchantments.MOB_LOOTING, 3))
            .constructor(GTSwordItem::create)
            .build();
    //public static GTToolType GRAFTER = new GTToolType("grafter", 1, 1, GTCEu.id("item/tools/handle_hammer"), GTCEu.id("item/tools/hammer"));
    public static final GTToolType PLUNGER = GTToolType.builder("plunger")
            .toolTag(TagUtil.createItemTag("tools/plungers", false))
            .toolStats(b -> b.cannotAttack().attackSpeed(-2.4F).sneakBypassUse()
                    .behaviors(PlungerBehavior.INSTANCE))
            .sound(GTSoundEntries.PLUNGER_TOOL)
            .build();
    public static final GTToolType SHEARS = GTToolType.builder("shears")
            .toolTag(TagUtil.createItemTag("tools/shears", false))
            .harvestTag(TagUtil.createBlockTag("mineable/shears", false))
            .toolStats(b -> b)
            .build();
    public static final GTToolType DRILL_LV = GTToolType.builder("lv_drill")
        .toolTag(TagUtil.createItemTag("tools/drills", false))
        .toolTag(TagUtil.createItemTag("pickaxes", true))
        .toolTag(TagUtil.createItemTag("shovels", true))
        .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", false))
        .harvestTag(TagUtil.createBlockTag("mineable/shovel", false))
        .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(3.0F)
                .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                /*.behaviors(TorchPlaceBehavior.INSTANCE)*/)
        .sound(GTSoundEntries.DRILL_TOOL, true)
        .electric(GTValues.LV)
        .build();
    public static final GTToolType DRILL_MV = GTToolType.builder("mv_drill")
        .toolTag(TagUtil.createItemTag("tools/drills", false))
        .toolTag(TagUtil.createItemTag("pickaxes", true))
        .toolTag(TagUtil.createItemTag("shovels", true))
        .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", false))
        .harvestTag(TagUtil.createBlockTag("mineable/shovel", false))
        .toolStats(b -> b.blockBreaking().aoe(1, 1, 2)
                .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(4.0F)
                .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_MV)
                /*.behaviors(TorchPlaceBehavior.INSTANCE)*/)
        .sound(GTSoundEntries.DRILL_TOOL, true)
        .electric(GTValues.MV)
        .build();
    public static final GTToolType DRILL_HV = GTToolType.builder("hv_drill")
        .toolTag(TagUtil.createItemTag("tools/drills", false))
        .toolTag(TagUtil.createItemTag("pickaxes", true))
        .toolTag(TagUtil.createItemTag("shovels", true))
        .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", false))
        .harvestTag(TagUtil.createBlockTag("mineable/shovel", false))
        .toolStats(b -> b.blockBreaking().aoe(2, 2, 4)
                .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(5.0F)
                .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV)
                /*.behaviors(TorchPlaceBehavior.INSTANCE)*/)
        .sound(GTSoundEntries.DRILL_TOOL, true)
        .electric(GTValues.HV)
        .build();
    public static final GTToolType DRILL_EV = GTToolType.builder("ev_drill")
        .toolTag(TagUtil.createItemTag("tools/drills", false))
        .toolTag(TagUtil.createItemTag("pickaxes", true))
        .toolTag(TagUtil.createItemTag("shovels", true))
        .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", false))
        .harvestTag(TagUtil.createBlockTag("mineable/shovel", false))
        .toolStats(b -> b.blockBreaking().aoe(3, 3, 6)
                .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(6.0F)
                .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_EV)
                /*.behaviors(TorchPlaceBehavior.INSTANCE)*/)
        .sound(GTSoundEntries.DRILL_TOOL, true)
        .electric(GTValues.EV)
        .build();
    public static final GTToolType DRILL_IV = GTToolType.builder("iv_drill")
        .toolTag(TagUtil.createItemTag("tools/drills", false))
        .toolTag(TagUtil.createItemTag("pickaxes", true))
        .toolTag(TagUtil.createItemTag("shovels", true))
        .harvestTag(TagUtil.createBlockTag("mineable/pickaxe", false))
        .harvestTag(TagUtil.createBlockTag("mineable/shovel", false))
        .toolStats(b -> b.blockBreaking().aoe(4, 4, 8)
                .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(7.0F)
                .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV)
                /*.behaviors(TorchPlaceBehavior.INSTANCE)*/)
        .sound(GTSoundEntries.DRILL_TOOL, true)
        .electric(GTValues.IV)
        .build();
    public static final GTToolType CHAINSAW_LV = GTToolType.builder("lv_chainsaw")
        .toolTag(TagUtil.createItemTag("axes", true))
        .toolTag(TagUtil.createItemTag("tools/chainsaws", false))
        .harvestTag(TagUtil.createBlockTag("mineable/axe", false))
        .toolStats(b -> b.blockBreaking()
            .efficiencyMultiplier(2.0F)
            .attackDamage(5.0F).attackSpeed(-3.2F)
            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
            .behaviors(HarvestIceBehavior.INSTANCE, DisableShieldBehavior.INSTANCE, TreeFellingBehavior.INSTANCE))
        .sound(GTSoundEntries.CHAINSAW_TOOL, true)
        .electric(GTValues.LV)
        .build();
    public static final GTToolType WRENCH_LV = GTToolType.builder("lv_wrench")
        .toolTag(TagUtil.createItemTag("tools/wrenches", false))
        .toolTag(TagUtil.createItemTag("tools/wrench", false))
        .harvestTag(TagUtil.createBlockTag("mineable/wrench", false))
        .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
            .efficiencyMultiplier(2.0F)
            .attackDamage(1.0F).attackSpeed(-2.8F)
            .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, IronGolem.class))
            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
        .sound(GTSoundEntries.WRENCH_TOOL, true)
        .electric(GTValues.LV)
        .build();
    public static final GTToolType WRENCH_HV = GTToolType.builder("hv_wrench")
        .toolTag(TagUtil.createItemTag("tools/wrenches", false))
        .toolTag(TagUtil.createItemTag("tools/wrench", false))
        .harvestTag(TagUtil.createBlockTag("mineable/wrench", false))
        .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
            .efficiencyMultiplier(3.0F)
            .attackDamage(1.0F).attackSpeed(-2.8F)
            .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, IronGolem.class))
            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV))
        .sound(GTSoundEntries.WRENCH_TOOL, true)
        .electric(GTValues.HV)
        .build();
    public static final GTToolType WRENCH_IV = GTToolType.builder("iv_wrench")
        .toolTag(TagUtil.createItemTag("tools/wrenches", false))
        .toolTag(TagUtil.createItemTag("tools/wrench", false))
        .harvestTag(TagUtil.createBlockTag("mineable/wrench", false))
        .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
            .efficiencyMultiplier(4.0F)
            .attackDamage(1.0F).attackSpeed(-2.8F)
            .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, IronGolem.class))
            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV))
        .sound(GTSoundEntries.WRENCH_TOOL, true)
        .electric(GTValues.IV)
        .build();
    public static final GTToolType BUZZSAW = GTToolType.builder("buzzsaw")
        .toolTag(TagUtil.createItemTag("tools/saws", false))
        .toolTag(TagUtil.createItemTag("tools/buzzsaws", false))
        .harvestTag(TagUtil.createBlockTag("mineable/wrench", false))
        .toolStats(b -> b.crafting().attackDamage(1.5F).attackSpeed(-3.2F)
            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
        .sound(GTSoundEntries.CHAINSAW_TOOL, true)
        .electric(GTValues.IV)
        .build();
    public static final GTToolType SCREWDRIVER_LV = GTToolType.builder("lv_screwdriver")
        .toolTag(TagUtil.createItemTag("tools/screwdrivers", false))
        .toolStats(b -> b.crafting().sneakBypassUse()
            .attackDamage(-1.0F).attackSpeed(3.0F)
            .behaviors(new EntityDamageBehavior(3.0F, Spider.class))
            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
        .sound(GTSoundEntries.CHAINSAW_TOOL)
        .electric(GTValues.IV)
        .build();


    public final String name;
    // at least one has to be set. first one MUST be the main tag.
    public final List<TagKey<Item>> itemTags;
    public final List<TagKey<Block>> harvestTags;
    public final ResourceLocation modelLocation;
    @Nullable
    public final SoundEntry soundEntry;
    public final boolean playSoundOnBlockDestroy;
    public final Character symbol;

    public final IGTToolDefinition toolDefinition;
    public final ToolConstructor constructor;
    public final int electricTier;

    public GTToolType(String name, Character symbol, IGTToolDefinition toolDefinition, ToolConstructor constructor, List<TagKey<Block>> harvestTags, List<TagKey<Item>> itemTags, ResourceLocation modelLocation, @Nullable SoundEntry soundEntry, boolean playSoundOnBlockDestroy, int electricTier) {
        this.name = name;
        this.symbol = symbol;
        this.toolDefinition = toolDefinition;
        this.constructor = constructor;
        this.itemTags = itemTags;
        this.harvestTags = harvestTags;
        this.modelLocation = modelLocation;
        this.soundEntry = soundEntry;
        this.playSoundOnBlockDestroy = playSoundOnBlockDestroy;
        this.electricTier = electricTier;

        types.put(name, this);
    }

    public boolean is(ItemStack itemStack) {
        return ToolHelper.is(itemStack, this);
    }

    public String getUnlocalizedName() {
        return "item.gtceu.tool." + name;
    }

    @FunctionalInterface
    public interface ToolConstructor {
        IGTTool apply(GTToolType type, MaterialToolTier tier, Material material, IGTToolDefinition definition, Item.Properties properties);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Accessors(fluent = true, chain = true)
    public static class Builder {
        private final String name;

        private final List<TagKey<Item>> itemTags = new ArrayList<>();
        private final List<TagKey<Block>> harvestTags = new ArrayList<>();

        @Setter
        private IGTToolDefinition toolStats;
        @Setter
        private int tier = -1;
        @Setter
        private Character symbol = null;
        @Setter
        private ToolConstructor constructor = GTToolItem::create;
        @Setter
        private ResourceLocation modelLocation;
        private SoundEntry sound;
        private boolean playSoundOnBlockDestroy;

        public Builder(String name) {
            this.name = name;
            this.modelLocation = GTCEu.id("item/tools/" + name);
        }

        @SafeVarargs
        public final Builder toolTag(TagKey<Item>... tags) {
            itemTags.addAll(Arrays.stream(tags).toList());
            return this;
        }

        @SafeVarargs
        public final Builder harvestTag(TagKey<Block>... tags) {
            harvestTags.addAll(Arrays.stream(tags).toList());
            return this;
        }

        @Tolerate
        public Builder toolStats(UnaryOperator<ToolDefinitionBuilder> builder) {
            this.toolStats = builder.apply(new ToolDefinitionBuilder()).build();
            return this;
        }

        public Builder sound(SoundEntry sound) {
            return this.sound(sound, false);
        }

        public Builder sound(SoundEntry sound, boolean playSoundOnBlockDestroy) {
            this.sound = sound;
            this.playSoundOnBlockDestroy = playSoundOnBlockDestroy;
            return this;
        }

        public Builder electric(int tier) {
            return tier(tier);
        }

        private GTToolType get() {
            return new GTToolType(name,
                    symbol,
                    toolStats,
                    constructor,
                    harvestTags,
                    itemTags,
                    modelLocation,
                    sound,
                    playSoundOnBlockDestroy,
                    tier
            );
        }

        public GTToolType build() {
            if (this.symbol == null) {
                return get();
            }
            GTToolType existing = ToolHelper.getToolFromSymbol(this.symbol);
            if (existing != null) {
                throw new IllegalArgumentException(
                        String.format("Symbol %s has been taken by %s already!", symbol, existing));
            }
            GTToolType supplied = get();
            ToolHelper.registerToolSymbol(this.symbol, supplied);
            return supplied;
        }
    }
}
