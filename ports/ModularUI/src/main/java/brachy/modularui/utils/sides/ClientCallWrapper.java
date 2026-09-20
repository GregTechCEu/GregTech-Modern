package brachy.modularui.utils.sides;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.block.entity.FuelValues;

/**
 * Internal helper class acting as a safeguard for accessing client-only methods
 */
/* package-private */ final class ClientCallWrapper {

    private ClientCallWrapper() {}

    static RegistryAccess.Frozen getClientRegistries() {
        return Minecraft.getInstance().getConnection().registryAccess();
    }

    static RecipeAccess getClientRecipeAccess() {
        return Minecraft.getInstance().getConnection().recipes();
    }

    static FuelValues getClientFuelValues() { return Minecraft.getInstance().getConnection().fuelValues(); }

    static PotionBrewing getClientPotionBrewing() {
        return Minecraft.getInstance().getConnection().potionBrewing();
    }
}
