package com.gregtechceu.gtceu.api.recipe;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRecipe
 */
@SuppressWarnings("ALL")
public class GTRecipe implements net.minecraft.world.item.crafting.Recipe<Container> {
    public final GTRecipeType recipeType;
    public final ResourceLocation id;
    public final Map<RecipeCapability<?>, List<Content>> inputs;
    public final Map<RecipeCapability<?>, List<Content>> outputs;
    public final Map<RecipeCapability<?>, List<Content>> tickInputs;
    public final Map<RecipeCapability<?>, List<Content>> tickOutputs;
    public final List<RecipeCondition> conditions;
    public CompoundTag data;
    public int duration;
    @Getter
    public boolean isFuel;

    public GTRecipe(GTRecipeType recipeType, ResourceLocation id, Map<RecipeCapability<?>, List<Content>> inputs, Map<RecipeCapability<?>, List<Content>> outputs, Map<RecipeCapability<?>, List<Content>> tickInputs, Map<RecipeCapability<?>, List<Content>> tickOutputs, List<RecipeCondition> conditions, CompoundTag data, int duration, boolean isFuel) {
        this.recipeType = recipeType;
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;
        this.conditions = conditions;
        this.data = data;
        this.duration = duration;
        this.isFuel = isFuel;
    }

    public GTRecipe copy() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        GTRecipeSerializer.SERIALIZER.toNetwork(buf, this);
        return GTRecipeSerializer.SERIALIZER.fromNetwork(id, buf);
    }

    public GTRecipe copy(ContentModifier modifier) {
        var copied = copy();
        modifyContents(copied.inputs, modifier);
        modifyContents(copied.outputs, modifier);
        modifyContents(copied.tickInputs, modifier);
        modifyContents(copied.tickOutputs, modifier);
        copied.duration = modifier.apply(this.duration).intValue();
        return copied;
    }

    public static void modifyContents(Map<RecipeCapability<?>, List<Content>> contents, ContentModifier modifier) {
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            var cap = entry.getKey();
            for (Content content : entry.getValue()) {
                content.content = cap.copyContent(content.content, modifier);
            }
        }
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTRecipeSerializer.SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return recipeType;
    }

    @Override
    public boolean matches(@NotNull Container pContainer, @NotNull Level pLevel) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container pContainer) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
    
    ///////////////////////////////////////////////////////////////
    // **********************internal logic********************* //
    ///////////////////////////////////////////////////////////////

    public List<Content> getInputContents(RecipeCapability<?> capability) {
        return inputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getOutputContents(RecipeCapability<?> capability) {
        return outputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickInputContents(RecipeCapability<?> capability) {
        return tickInputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickOutputContents(RecipeCapability<?> capability) {
        return tickOutputs.getOrDefault(capability, Collections.emptyList());
    }

    public boolean matchRecipe(IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies()) return false;
        if (!matchRecipe(IO.IN, holder, inputs)) return false;
        if (!matchRecipe(IO.OUT, holder, outputs)) return false;
        return true;
    }

    public boolean matchTickRecipe(IRecipeCapabilityHolder holder) {
        if (hasTick()) {
            if (!holder.hasProxies()) return false;
            if (!matchRecipe(IO.IN, holder, tickInputs)) return false;
            if (!matchRecipe(IO.OUT, holder, tickOutputs)) return false;
        }
        return true;
    }

    public boolean matchRecipe(IO io, IRecipeCapabilityHolder holder, Map<RecipeCapability<?>, List<Content>> contents) {
        Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies = holder.getCapabilitiesProxy();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            Set<IRecipeHandler<?>> used = new HashSet<>();
            List content = new ArrayList<>();
            Map<String, List> contentSlot = new HashMap<>();
            for (Content cont : entry.getValue()) {
                if (cont.slotName == null) {
                    content.add(cont.content);
                } else {
                    contentSlot.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                }
            }
            RecipeCapability<?> capability = entry.getKey();
            content = content.stream().map(capability::copyContent).toList();
            if (content.isEmpty() && contentSlot.isEmpty()) continue;
            if (content.isEmpty()) content = null;
            if (capabilityProxies.contains(io, capability)) {
                for (IRecipeHandler<?> proxy : capabilityProxies.get(io, capability)) { // search same io type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    if (content != null) {
                        content = proxy.searchingRecipe(io, this, content, null);
                    }
                    if (proxy.getSlotNames() != null) {
                        Iterator<String> iterator = contentSlot.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (proxy.getSlotNames().contains(key)) {
                                List<?> left = proxy.searchingRecipe(io, this, contentSlot.get(key), key);
                                if (left == null) iterator.remove();
                            }
                        }
                    }
                    if (content == null && contentSlot.isEmpty()) break;
                }
            }
            if (content == null && contentSlot.isEmpty()) continue;
            if (capabilityProxies.contains(IO.BOTH, capability)) {
                for (IRecipeHandler<?> proxy : capabilityProxies.get(IO.BOTH, capability)) { // search both type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    if (content != null) {
                        content = proxy.searchingRecipe(io, this, content, null);
                    }
                    if (proxy.getSlotNames() != null) {
                        Iterator<String> iterator = contentSlot.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (proxy.getSlotNames().contains(key)) {
                                List<?> left = proxy.searchingRecipe(io, this, contentSlot.get(key), key);
                                if (left == null) iterator.remove();
                            }
                        }
                    }
                    if (content == null && contentSlot.isEmpty()) break;
                }
            }
            if (content != null || !contentSlot.isEmpty()) return false;
        }
        return true;
    }

    public boolean handleTickRecipeIO(IO io, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, io == IO.IN ? tickInputs : tickOutputs);
    }

    public boolean handleRecipeIO(IO io, IRecipeCapabilityHolder holder) {
        if (!holder.hasProxies() || io == IO.BOTH) return false;
        return handleRecipe(io, holder, io == IO.IN ? inputs : outputs);
    }

    public boolean handleRecipe(IO io, IRecipeCapabilityHolder holder, Map<RecipeCapability<?>, List<Content>> contents) {
        Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilityProxies = holder.getCapabilitiesProxy();
        for (Map.Entry<RecipeCapability<?>, List<Content>> entry : contents.entrySet()) {
            Set<IRecipeHandler<?>> used = new HashSet<>();
            List content = new ArrayList<>();
            Map<String, List> contentSlot = new HashMap<>();
            for (Content cont : entry.getValue()) {
                if (cont.chance == 1 || GTValues.RNG.nextFloat() < cont.chance) { // chance input
                    if (cont.slotName == null) {
                        content.add(cont.content);
                    } else {
                        contentSlot.computeIfAbsent(cont.slotName, s -> new ArrayList<>()).add(cont.content);
                    }
                }
            }
            RecipeCapability<?> capability = entry.getKey();
            content = content.stream().map(capability::copyContent).toList();
            if (content.isEmpty() && contentSlot.isEmpty()) continue;
            if (content.isEmpty()) content = null;
            if (capabilityProxies.contains(io, capability)) {
                for (IRecipeHandler<?> proxy : capabilityProxies.get(io, capability)) { // search same io type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    if (content != null) {
                        content = proxy.handleRecipe(io, this, content, null);
                    }
                    if (proxy.getSlotNames() != null) {
                        Iterator<String> iterator = contentSlot.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (proxy.getSlotNames().contains(key)) {
                                List<?> left = proxy.handleRecipe(io, this, contentSlot.get(key), key);
                                if (left == null) iterator.remove();
                            }
                        }
                    }
                    if (content == null && contentSlot.isEmpty()) break;
                }
            }
            if (content == null && contentSlot.isEmpty()) continue;
            if (capabilityProxies.contains(IO.BOTH, capability)) {
                for (IRecipeHandler<?> proxy : capabilityProxies.get(IO.BOTH, capability)) { // search both type
                    if (used.contains(proxy)) continue;
                    used.add(proxy);
                    if (content != null) {
                        content = proxy.handleRecipe(io, this, content, null);
                    }
                    if (proxy.getSlotNames() != null) {
                        Iterator<String> iterator = contentSlot.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (proxy.getSlotNames().contains(key)) {
                                List<?> left = proxy.handleRecipe(io, this, contentSlot.get(key), key);
                                if (left == null) iterator.remove();
                            }
                        }
                    }
                    if (content == null && contentSlot.isEmpty()) break;
                }
            }
            if (content != null || !contentSlot.isEmpty()) {
                GTCEu.LOGGER.warn("io error while handling a recipe {} outputs. holder: {}", id, holder);
                return false;
            }
        }
        return true;
    }

    public boolean hasTick() {
        return !tickInputs.isEmpty() || !tickOutputs.isEmpty();
    }

    public void preWorking(IRecipeCapabilityHolder holder) {
        handlePre(inputs, holder, IO.IN);
        handlePre(outputs, holder, IO.OUT);
    }

    public void postWorking(IRecipeCapabilityHolder holder) {
        handlePost(inputs, holder, IO.IN);
        handlePost(outputs, holder, IO.OUT);
    }

    public void handlePre(Map<RecipeCapability<?>, List<Content>> contents, IRecipeCapabilityHolder holder, IO io) {
        contents.forEach(((capability, tuples) -> {
            if (holder.getCapabilitiesProxy().contains(io, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(io, capability)) {
                    capabilityProxy.preWorking(holder, io, this);
                }
            } else if (holder.getCapabilitiesProxy().contains(IO.BOTH, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(IO.BOTH, capability)) {
                    capabilityProxy.preWorking(holder, io, this);
                }
            }
        }));
    }

    public void handlePost(Map<RecipeCapability<?>, List<Content>> contents, IRecipeCapabilityHolder holder, IO io) {
        contents.forEach(((capability, tuples) -> {
            if (holder.getCapabilitiesProxy().contains(io, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(io, capability)) {
                    capabilityProxy.postWorking(holder, io, this);
                }
            } else if (holder.getCapabilitiesProxy().contains(IO.BOTH, capability)) {
                for (IRecipeHandler<?> capabilityProxy : holder.getCapabilitiesProxy().get(IO.BOTH, capability)) {
                    capabilityProxy.postWorking(holder, io, this);
                }
            }
        }));
    }

    public boolean checkConditions(@Nonnull RecipeLogic recipeLogic) {
        if (conditions.isEmpty()) return true;
        Map<String, List<RecipeCondition>> or = new HashMap<>();
        for (RecipeCondition condition : conditions) {
            if (condition.isOr()) {
                or.computeIfAbsent(condition.getType(), type -> new ArrayList<>()).add(condition);
            } else if (condition.test(this, recipeLogic) == condition.isReverse()) {
                return false;
            }
        }
        for (List<RecipeCondition> conditions : or.values()) {
            if (conditions.stream().allMatch(condition -> condition.test(this, recipeLogic) == condition.isReverse())) {
                return false;
            }
        }
        return true;
    }
}
