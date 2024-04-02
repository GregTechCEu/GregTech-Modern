package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.*;
import com.gregtechceu.gtceu.utils.IngredientEquality;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.crafting.NBTIngredient;

import java.util.Collection;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote ItemRecipeCapability
 */
public class ItemRecipeCapability extends RecipeCapability<Ingredient> {

    public final static ItemRecipeCapability CAP = new ItemRecipeCapability();

    protected ItemRecipeCapability() {
        super("item", 0xFFD96106, SerializerIngredient.INSTANCE);
    }

    @Override
    public Ingredient copyInner(Ingredient content) {
        return SizedIngredient.copy(content);
    }

    @Override
    public Ingredient copyWithModifier(Ingredient content, ContentModifier modifier) {
        return content instanceof SizedIngredient sizedIngredient ? SizedIngredient.create(sizedIngredient.getInner(), modifier.apply(sizedIngredient.getAmount()).intValue()) : SizedIngredient.create(content, modifier.apply(1).intValue());
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object obj) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (obj instanceof Ingredient ingredient) {

            // all kinds of special cases
            if (ingredient instanceof NBTIngredient nbt && nbt.isStrict()) {
                ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
            } if (ingredient instanceof IntCircuitIngredient circuit) {
                ingredients.addAll(MapItemStackNBTIngredient.from(circuit));
            } else if (ingredient instanceof SizedIngredient sized) {
                if (sized.getInner() instanceof NBTIngredient nbt && nbt.isStrict()) {
                    ingredients.addAll(MapItemStackNBTIngredient.from(nbt));
                } else if (sized.getInner() instanceof IntersectionIngredient intersection) {
                    ingredients.add(new MapIntersectionIngredient(intersection));
                } else {
                    for (Ingredient.Value value : sized.getInner().getValues()) {
                        if (value instanceof Ingredient.TagValue tagValue) {
                            ingredients.add(new MapItemTagIngredient(tagValue.tag()));
                        } else {
                            Collection<ItemStack> stacks = value.getItems();
                            for (ItemStack stack : stacks) {
                                ingredients.add(new MapItemStackIngredient(stack, sized.getInner()));
                            }
                        }
                    }
                }
            } else if (ingredient instanceof IntersectionIngredient intersection) {
                ingredients.add(new MapIntersectionIngredient(intersection));
            } else if (ingredient instanceof CompoundIngredient compound) {
                for (Ingredient inner : compound.getChildren()) {
                    ingredients.addAll(convertToMapIngredient(inner));
                }
            } else {
                for (Ingredient.Value value : ingredient.getValues()) {
                    if (value instanceof Ingredient.TagValue tagValue) {
                        ingredients.add(new MapItemTagIngredient(tagValue.tag()));
                    } else {
                        Collection<ItemStack> stacks = value.getItems();
                        for (ItemStack stack : stacks) {
                            ingredients.add(new MapItemStackIngredient(stack, ingredient));
                        }
                    }
                }
            }
        } else if (obj instanceof ItemStack stack) {
            ingredients.add(new MapItemStackIngredient(stack));

            stack.getTags().forEach(tag -> ingredients.add(new MapItemTagIngredient(tag)));
            if (stack.hasTag()) {
                ingredients.add(new MapItemStackNBTIngredient(stack, NBTIngredient.of(true, stack)));
            }
            TagPrefix prefix = ChemicalHelper.getPrefix(stack.getItem());
            if (prefix != null && TagPrefix.ORES.containsKey(prefix)) {
                Material material = ChemicalHelper.getMaterial(stack.getItem()).material();
                ingredients.add(new MapIntersectionIngredient((IntersectionIngredient) IntersectionIngredient.of(Ingredient.of(prefix.getItemTags(material)[0]), Ingredient.of(prefix.getItemParentTags()[0]))));
            }
        }
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        List<Object> list = new ObjectArrayList<>(ingredients.size());
        for (Object item : ingredients) {
            if (item instanceof Ingredient ingredient) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof Ingredient ingredient1) {
                        if (IngredientEquality.ingredientEquals(ingredient, ingredient1)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof ItemStack stack) {
                        if (ingredient.test(stack)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                if (ingredient instanceof IntCircuitIngredient) {
                    list.add(0, ingredient);
                } else if (ingredient instanceof SizedIngredient sized && sized.getInner() instanceof IntCircuitIngredient) {
                    list.add(0, ingredient);
                } else {
                    list.add(ingredient);
                }
            } else if (item instanceof ItemStack stack) {
                boolean isEqual = false;
                for (Object obj : list) {
                    if (obj instanceof Ingredient ingredient) {
                        if (ingredient.test(stack)) {
                            isEqual = true;
                            break;
                        }
                    } else if (obj instanceof ItemStack stack1) {
                        if (ItemStack.isSameItem(stack, stack1)) {
                            isEqual = true;
                            break;
                        }
                    }
                }
                if (isEqual) continue;
                list.add(stack);
            }
        }
        return list;
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

}
