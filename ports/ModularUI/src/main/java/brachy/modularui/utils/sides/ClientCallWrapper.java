package brachy.modularui.utils.sides;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Internal helper class acting as a safeguard for accessing client-only methods
 */
/* package-private */ final class ClientCallWrapper {

    private ClientCallWrapper() {}

    static RegistryAccess.Frozen getClientRegistries() {
        return Minecraft.getInstance().getConnection().registryAccess();
    }

    static RecipeManager getClientRecipeManager() {
        return Minecraft.getInstance().getConnection().getRecipeManager();
    }

    static PotionBrewing getClientPotionBrewing() {
        return Minecraft.getInstance().getConnection().potionBrewing();
    }
}
