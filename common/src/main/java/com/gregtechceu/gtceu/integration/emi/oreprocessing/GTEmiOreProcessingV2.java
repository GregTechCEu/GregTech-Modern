package com.gregtechceu.gtceu.integration.emi.oreprocessing;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeTypeEmiCategory;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GTEmiOreProcessingV2 implements EmiRecipe {
    public static final EmiTexture BASE = texture("base");
    public static final EmiTexture CHEM = texture("chem");
    public static final EmiTexture SEP = texture("sep");
    public static final EmiTexture SIFT = texture("sift");
    public static final EmiTexture SMELT = texture("smelt");
    public static final int TEXTURE_WIDTH = 186;
    public static final int TEXTURE_HEIGHT = 174;
    public static final int WIDTH = 111 + 18 + 5 + 18 + 18;
    public static final int HEIGHT = 102 + 18 + 1 + 18 + 3 + 18;
    public static final NumberFormat PERCENT_FORMAT = DecimalFormat.getPercentInstance();
    public static final EmiIngredient STONE_DUSTS;
    protected static EmiIngredient cauldron;
    static {
        if (PERCENT_FORMAT instanceof DecimalFormat decimalFormat) {
            decimalFormat.setMaximumFractionDigits(4);
        }
        List<EmiIngredient> stoneDustStacks = new ArrayList<>();
        for (TagPrefix tagPrefix : TagPrefix.ORES.keySet()) {
            Material stoneDustMaterial = GTRegistries.MATERIALS.get(FormattingUtil.toLowerCaseUnder(tagPrefix.name));
            if (stoneDustMaterial != null) {
                stoneDustStacks.add(EmiStack.of(ChemicalHelper.get(TagPrefix.dust, stoneDustMaterial)));
            }
        }
        STONE_DUSTS = EmiIngredient.of(stoneDustStacks);
    }
    public static EmiTexture texture(String key) {
        int edge = 3;
        return texture(key, edge, edge, WIDTH, HEIGHT);
    }
    public static EmiTexture texture(String key, int u, int v, int width, int height) {
        return new EmiTexture(path(key), u, v, width, height, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
    public static ResourceLocation path(String key) {
        return GTCEu.id("textures/gui/arrows/oreby-" + key + ".png");
    }
    public static EmiIngredient getWorkstations(EmiRecipeCategory category) {
        return EmiIngredient.of(EmiApi.getRecipeManager().getWorkstations(category));
    }
    public static EmiIngredient getWorkstations(GTRecipeType recipeType) {
        return getWorkstations(GTRecipeTypeEmiCategory.CATEGORIES.apply(recipeType));
    }
    public static MutableComponent getTierBoostText(double boostPerTier) {
        return Component.translatable("gtceu.gui.content.tier_boost", PERCENT_FORMAT.format(boostPerTier));
    }
    public static EmiIngredient getCauldron() {
        if (cauldron == null) {
            cauldron = EmiIngredient.of(List.of(EmiStack.of(Items.CAULDRON), getWorkstations(GTRecipeTypes.ORE_WASHER_RECIPES)));
        }
        return cauldron;
    }
    public static void register(EmiRegistry registry) {
        registry.addDeferredRecipes(consumer -> {
            for (Material mat : GTRegistries.MATERIALS) {
                if (mat.hasProperty(PropertyKey.ORE)) {
                    registry.addRecipe(new GTEmiOreProcessingV2(mat));
                }
            }
        });
    }
    public final Material material;
    protected final ResourceLocation id;
    protected final List<EmiIngredient> inputs = new ArrayList<>();
    protected final List<EmiStack> outputs = new ArrayList<>();
    protected final List<EmiRecipeCategory> workstations = new ArrayList<>();
    public final EmiIngredient ore;
    public final EmiStack smelt;
    public final EmiStack crushed;
    public final EmiStack crushed2;
    public final EmiStack crushedPurified;
    public final EmiStack crushedRefined;
    public final EmiStack dustImpure;
    public final EmiStack dustPure;
    public final EmiStack dust;
    public final EmiStack dustGem;
    public final EmiStack bathFluid;
    public final EmiStack byProduct0dust;
    public final EmiStack byProduct0dustTiny;
    public final EmiStack byProduct0dustTiny3;
    public final EmiStack byProduct1;
    public final EmiStack byProduct1dustTiny;
    public final EmiStack byProduct1dustTiny3;
    public final EmiStack byProduct3;
    public final EmiStack seperatedBP0;
    public final EmiStack seperatedBP1;
    public final EmiStack gemExquisite;
    public final EmiStack gemFlawless;
    public final EmiStack gem;
    public final EmiStack gemFlawed;
    public final EmiStack gemChipped;
    public GTEmiOreProcessingV2(Material material) {
        this.material = material;
        id = GTCEu.id("/ore_processing/" + material.getName());
        workstations.add(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.MACERATOR_RECIPES));
        workstations.add(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.ORE_WASHER_RECIPES));
        workstations.add(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.CENTRIFUGE_RECIPES));
        workstations.add(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES));
        OreProperty oreProperty = material.getProperty(PropertyKey.ORE);
        //ore, stoneDusts
        List<ItemStack> oreStacks = new ArrayList<>();
        for (TagPrefix tagPrefix : TagPrefix.ORES.keySet()) {
            ItemStack oreStack = ChemicalHelper.get(tagPrefix, material);
            oreStacks.add(oreStack);
        }
        ore = EmiIngredient.of(oreStacks.stream().map(EmiStack::of).toList());
        inputs.add(ore);
        //crushed
        crushed = EmiStack.of(ChemicalHelper.get(TagPrefix.crushed, material));
        inputs.add(crushed);
        //crushed from ore
        crushed2 = crushed.copy().setAmount(2L * oreProperty.getOreMultiplier());
        inputs.add(crushed2);
        outputs.add(crushed2);
        //crushedPurified
        crushedPurified = EmiStack.of(ChemicalHelper.get(TagPrefix.crushedPurified, material));
        inputs.add(crushedPurified);
        outputs.add(crushedPurified);
        //crushedRefined
        crushedRefined = EmiStack.of(ChemicalHelper.get(TagPrefix.crushedRefined, material));
        inputs.add(crushedRefined);
        outputs.add(crushedRefined);
        //dustImpure
        dustImpure = EmiStack.of(ChemicalHelper.get(TagPrefix.dustImpure, material));
        inputs.add(dustImpure);
        outputs.add(dustImpure);
        //dustPure
        dustPure = EmiStack.of(ChemicalHelper.get(TagPrefix.dustPure, material));
        inputs.add(dustPure);
        outputs.add(dustPure);
        //dust
        dust = EmiStack.of(ChemicalHelper.get(TagPrefix.dust, material));
        outputs.add(dust);
        //byProducts
        List<Material> byProducts = oreProperty.getOreByProducts();
        Material bpMaterial0 = GTUtil.selectItemInList(0, material, byProducts, Material.class);
        Material bpMaterial1 = GTUtil.selectItemInList(1, material, byProducts, Material.class);
        Material bpMaterial3 = GTUtil.selectItemInList(3, material, byProducts, Material.class);
        ItemStack crushBPStack = ChemicalHelper.get(TagPrefix.gem, bpMaterial0);
        if (crushBPStack.isEmpty()) {
            crushBPStack = ChemicalHelper.get(TagPrefix.dust, bpMaterial0);
        }
        byProduct0dust = EmiStack.of(crushBPStack).setChance(0.14f);
        byProduct0dustTiny = EmiStack.of(ChemicalHelper.get(TagPrefix.dustTiny, bpMaterial0));
        outputs.add(byProduct0dustTiny);
        outputs.add(byProduct0dust);
        byProduct0dustTiny3 = byProduct0dustTiny.copy().setAmount(3);
        outputs.add(byProduct0dustTiny3);
        byProduct1 = EmiStack.of(ChemicalHelper.get(TagPrefix.dust, bpMaterial1)).setChance(0.14f);
        outputs.add(byProduct1);
        byProduct1dustTiny = EmiStack.of(ChemicalHelper.get(TagPrefix.dustTiny, bpMaterial1));
        outputs.add(byProduct1dustTiny);
        byProduct1dustTiny3 = byProduct1dustTiny.copy().setAmount(3);
        outputs.add(byProduct1dustTiny3);
        inputs.add(EmiStack.of(Fluids.WATER, FluidHelper.getBucket()));//washer second input
        //smelt
        Material smeltMaterial = Objects.requireNonNullElse(oreProperty.getDirectSmeltResult(), material);
        TagPrefix smeltTagPrefix;
        if (smeltMaterial.hasProperty(PropertyKey.INGOT)) {
            smeltTagPrefix = TagPrefix.ingot;
        } else if (smeltMaterial.hasProperty(PropertyKey.GEM)) {
            smeltTagPrefix = TagPrefix.gem;
        } else if (smeltMaterial.hasProperty(PropertyKey.DUST)) {
            smeltTagPrefix = TagPrefix.dust;
        } else {
            smeltTagPrefix = null;
        }
        if (smeltTagPrefix == null) {
            smelt = EmiStack.EMPTY;
        } else {
            smelt = EmiStack.of(ChemicalHelper.get(smeltTagPrefix, smeltMaterial));
            outputs.add(smelt);
            if (!material.hasProperty(PropertyKey.BLAST)) {
                workstations.add(VanillaEmiRecipeCategories.SMELTING);
            }
        }
        //bath BP
        Pair<Material, Integer> washedIn = oreProperty.getWashedIn();
        if (washedIn.getLeft() != null) {
            bathFluid = EmiStack.of(washedIn.getLeft().getFluid(), washedIn.getRight() * FluidHelper.getBucket() / 1000);
            inputs.add(bathFluid);
            byProduct3 = EmiStack.of(ChemicalHelper.get(TagPrefix.dust, bpMaterial3)).copy().setChance(0.7f);
            outputs.add(byProduct3);
            workstations.add(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.CHEMICAL_BATH_RECIPES));
        } else {
            byProduct3 = bathFluid = EmiStack.EMPTY;
        }
        //Separatied BP
        List<Material> separatedInto = oreProperty.getSeparatedInto();
        if (separatedInto != null && !separatedInto.isEmpty()) {
            seperatedBP0 = EmiStack.of(ChemicalHelper.get(TagPrefix.dustSmall, separatedInto.get(0))).setChance(0.4f);
            outputs.add(seperatedBP0);
            Material separatedInto1 = separatedInto.get(separatedInto.size() - 1);
            boolean isNugget = separatedInto1.getBlastTemperature() == 0 && separatedInto1.hasProperty(PropertyKey.INGOT);
            seperatedBP1 = EmiStack.of(ChemicalHelper.get(isNugget ? TagPrefix.nugget : TagPrefix.dustSmall, separatedInto1), isNugget ? 2 : 1).setChance(0.2f);
            outputs.add(seperatedBP1);
            workstations.add(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES));
        } else {
            seperatedBP0 = seperatedBP1 = EmiStack.EMPTY;
        }
        //sifter
        if (material.hasProperty(PropertyKey.GEM)) {
            gemExquisite = EmiStack.of(ChemicalHelper.get(TagPrefix.gemExquisite, material)).setChance(0.03f);
            outputs.add(gemExquisite);
            gemFlawless = EmiStack.of(ChemicalHelper.get(TagPrefix.gemFlawless, material)).setChance(0.1f);
            outputs.add(gemFlawless);
            gem = EmiStack.of(ChemicalHelper.get(TagPrefix.gem, material)).setChance(0.5f);
            dustGem = dust.copy().setChance(0.25f);
            gemFlawed = EmiStack.of(ChemicalHelper.get(TagPrefix.gemFlawed, material)).setChance(0.25f);
            outputs.add(gemFlawed);
            gemChipped = EmiStack.of(ChemicalHelper.get(TagPrefix.gemChipped, material)).setChance(0.35f);
            outputs.add(gemChipped);
            workstations.add(GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.SIFTER_RECIPES));
        } else {
            gemExquisite = gemFlawless = gem = dustGem = gemFlawed = gemChipped = EmiStack.EMPTY;
        }
    }
    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreProcessingEmiCategory.CATEGORY;
    }
    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }
    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }
    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }
    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }
    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }
    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BASE, 0, 0);
        widgets.addSlot(ore, 0, 0);
        //ore --macerator-> crushed
        widgets.addSlot(getWorkstations(GTRecipeTypes.MACERATOR_RECIPES), 0, 18 + (26 - 18) / 2).drawBack(false);
        widgets.addSlot(crushed2, 0, 18 + 26).recipeContext(this);
        Component boost085 = getTierBoostText(0.085);
        widgets.addSlot(byProduct0dust, 0, 18 * 2 + 26).recipeContext(this).appendTooltip(boost085);
        widgets.addSlot(STONE_DUSTS, 0, 18 * 3 + 26).recipeContext(this);
        //crushed --washer-> crushedPurified
        widgets.addSlot(getWorkstations(GTRecipeTypes.ORE_WASHER_RECIPES), 21, 22).drawBack(false);
        widgets.addSlot(EmiStack.of(Fluids.WATER, FluidHelper.getBucket()), 39, 22);
        widgets.addSlot(crushedPurified, 61, 22).recipeContext(this);
        widgets.addSlot(byProduct0dustTiny3, 61 + 18, 22).recipeContext(this);
        //crushed --macerator-> dustImpure
        widgets.addSlot(getWorkstations(GTRecipeTypes.MACERATOR_RECIPES), 20, 67).drawBack(false);
        widgets.addSlot(dustImpure, 20, 89).recipeContext(this);
        widgets.addSlot(byProduct0dust, 20, 89 + 18).recipeContext(this).appendTooltip(boost085);
        //dustImpure --centrifuge-> dust
        widgets.addSlot(getWorkstations(GTRecipeTypes.CENTRIFUGE_RECIPES), 47, 77).drawBack(false);
        widgets.addSlot(dust, 47, 77 + 18 + 3).recipeContext(this);
        widgets.addSlot(byProduct0dustTiny, 47, 77 + 18 + 3 + 18).recipeContext(this);
        //crushedPurified --macerator-> dustPure
        widgets.addSlot(getWorkstations(GTRecipeTypes.MACERATOR_RECIPES), 111, 45).drawBack(false);
        widgets.addSlot(dustPure, 111 + 18 + 5, 45).recipeContext(this);
        widgets.addSlot(byProduct1, 111 + 18 + 5 + 18, 45).recipeContext(this).appendTooltip(boost085);
        //dustPure --centrifuge-> dust
        widgets.addSlot(getWorkstations(GTRecipeTypes.CENTRIFUGE_RECIPES), 131, 66).drawBack(false);
        widgets.addSlot(dust, 131, 66 + 18 + 4).recipeContext(this);
        widgets.addSlot(byProduct1dustTiny, 131, 66 + 18 + 4 + 18).recipeContext(this);
        //crushedPurified --thermal_centrifuge-> crushedRefined
        widgets.addSlot(getWorkstations(GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES), 94, 67).drawBack(false);
        widgets.addSlot(crushedRefined, 94, 67 + 18 + 4).recipeContext(this);
        widgets.addSlot(byProduct1dustTiny3, 94, 67 + 18 + 4 + 18).recipeContext(this);
        //crushedRefined --macerator-> dust
        widgets.addSlot(getWorkstations(GTRecipeTypes.MACERATOR_RECIPES), 68, 77).drawBack(false);
        widgets.addSlot(dust, 68, 77 + 18 + 3).recipeContext(this);
        widgets.addSlot(byProduct1, 68, 77 + 18 + 3 + 18).recipeContext(this).appendTooltip(boost085);
        //crushed --cauldron-> crushedPurified
        widgets.addSlot(crushed2.copy().setAmount(1), 0, 102);
        widgets.addSlot(getCauldron(), 0, 102 + 1 + 18).drawBack(false);
        int yLastLine = 102 + 18 + 1 + 18 + 3;
        widgets.addSlot(crushedPurified, 0, yLastLine).recipeContext(this);
        //dustImpure --cauldron-> dust
        widgets.addSlot(dustImpure, 18 + 2, yLastLine);
        widgets.addSlot(getCauldron(), 18 + 2 + 18 + 1, yLastLine).drawBack(false);
        widgets.addSlot(dust, 18 + 2 + 18 + 1 + 18 + 3, yLastLine).recipeContext(this);
        //dustPure --cauldron-> dust
        widgets.addSlot(dustPure, 81, yLastLine);
        widgets.addSlot(getCauldron(), 81 + 18 + 1, yLastLine).drawBack(false);
        widgets.addSlot(dust, 81 + 18 + 1 + 18 + 3, yLastLine).recipeContext(this);
        //ore --furnace-> smelt
        if (!smelt.isEmpty() && !material.hasProperty(PropertyKey.BLAST)) {
            widgets.addTexture(SMELT, 0, 0);//widgets.addTexture(SMELT_ARROW, SLOT.width, (SLOT.height - SMELT_ARROW.height) / 2);
            EmiRecipeCategory category =/* material.hasProperty(PropertyKey.BLAST) ? GTRecipeTypeEmiCategory.CATEGORIES.apply(GTRecipeTypes.BLAST_RECIPES) :*/ VanillaEmiRecipeCategories.SMELTING;
            widgets.addSlot(getWorkstations(category), 18 + (25 - 18) / 2, 0).drawBack(false);
            widgets.addSlot(smelt, 18 + 25, 0).recipeContext(this);
        }
        //crushed --bath-> crushedPurified
        if (!bathFluid.isEmpty()) {
            widgets.addTexture(CHEM, 0, 0);
            widgets.addSlot(getWorkstations(GTRecipeTypes.CHEMICAL_BATH_RECIPES), 21, 45).drawBack(false);
            widgets.addSlot(bathFluid, 39, 45);
            widgets.addSlot(crushedPurified, 61, 45).recipeContext(this);
            widgets.addSlot(byProduct3, 79, 45).recipeContext(this).appendTooltip(getTierBoostText(0.058));
        }
        //dustPure --electromagnetic_separator-> dust
        if (!seperatedBP0.isEmpty()) {
            widgets.addTexture(SEP, 0, 0);
            int xSep = 131 + 18 + 3;
            widgets.addSlot(getWorkstations(GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES), xSep, 66).drawBack(false);
            widgets.addSlot(dust, xSep, 66 + 18 + 4).recipeContext(this);
            widgets.addSlot(seperatedBP0, xSep, 66 + 18 + 4 + 18).recipeContext(this);
            widgets.addSlot(seperatedBP1, xSep, 66 + 18 + 4 + 18 * 2).recipeContext(this);
        }
        //crushedPurified --sifter-> gem
        if (!gem.isEmpty()) {
            widgets.addTexture(SIFT, 0, 0);
            widgets.addSlot(getWorkstations(GTRecipeTypes.SIFTER_RECIPES), 98, 20).drawBack(false);
            int xGem = 98 + 18;
            widgets.addSlot(gemExquisite, xGem, 0).recipeContext(this).appendTooltip(getTierBoostText(0.01));
            widgets.addSlot(gemFlawless, xGem + 18, 0).recipeContext(this).appendTooltip(getTierBoostText(0.015f));
            widgets.addSlot(gem, xGem + 18 * 2, 0).recipeContext(this).appendTooltip(getTierBoostText(0.075f));
            widgets.addSlot(dustGem, xGem, 18).recipeContext(this).appendTooltip(getTierBoostText(0.05f));
            widgets.addSlot(gemFlawed, xGem + 18, 18).recipeContext(this).appendTooltip(getTierBoostText(0.03f));
            widgets.addSlot(gemChipped, xGem + 18 * 2, 18).recipeContext(this).appendTooltip(getTierBoostText(0.04f));
        }
    }
    @Override
    public boolean hideCraftable() {
        return true;
    }
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
    @Override
    public List<EmiIngredient> getCatalysts() {
        List<EmiIngredient> list = new ArrayList<>(getCauldron().getEmiStacks());
        for (EmiRecipeCategory workstation : workstations) {
            list.addAll(getWorkstations(workstation).getEmiStacks());
        }
        return list;
    }
}
