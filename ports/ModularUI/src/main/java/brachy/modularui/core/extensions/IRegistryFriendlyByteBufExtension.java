package brachy.modularui.core.extensions;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface IRegistryFriendlyByteBufExtension {

    private RegistryFriendlyByteBuf self() {
        return (RegistryFriendlyByteBuf) this;
    }

    static RegistryFriendlyByteBuf createEmpty(RegistryAccess registryAccess) {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess, ConnectionType.NEOFORGE);
    }

    default RegistryFriendlyByteBuf mui$wrapByteBuf(ByteBuf byteBuf) {
        return new RegistryFriendlyByteBuf(byteBuf, self().registryAccess(), self().getConnectionType());
    }
}
