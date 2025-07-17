package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

@Getter
public class ActionResult implements IManaged {

    public final static ActionResult SUCCESS = new ActionResult(true, null, null);
    public final static ActionResult FAIL_NO_REASON = new ActionResult(false, null, null);
    public final static ActionResult PASS_NO_CONTENTS = new ActionResult(true,
            Component.translatable("gtceu.recipe_logic.no_contents"), null);
    public final static ActionResult FAIL_NO_CAPABILITIES = new ActionResult(false,
            Component.translatable("gtceu.recipe_logic.no_capabilities"), null);

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ActionResult.class, null);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    private final boolean isSuccess;
    @Nullable
    private final Component reason;
    @Nullable
    private final RecipeCapability<?> capability;

    /**
     * @param isSuccess did the action succeed?
     * @param reason    if failed, the reason
     * @param capability which specific capability failed
     */
    public ActionResult(boolean isSuccess, @Nullable Component reason, @Nullable RecipeCapability<?> capability) {
        this.isSuccess = isSuccess;
        this.reason = reason;
        this.capability = capability;
    }

    public static ActionResult fail(@Nullable Component component, @Nullable RecipeCapability<?> capability) {
        return new ActionResult(false, component, capability);
    }

    public Component reason() {
        if (reason == null) return Component.empty();
        return reason;
    }

    public RecipeCapability<?> capability() {
        return capability;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {}
}
