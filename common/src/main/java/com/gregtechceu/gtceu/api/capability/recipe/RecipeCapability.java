package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * Used to detect whether a machine has a certain capability.
 */
public class RecipeCapability<T> {

    public final String name;
    public final int color;
    public final IContentSerializer<T> serializer;

    protected RecipeCapability(String name, int color, IContentSerializer<T> serializer) {
        this.name = name;
        this.color = color;
        this.serializer = serializer;
    }

    /**
     * deep copy of this content. recipe need it for searching and such things
     */
    public T copyInner(T content) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        serializer.toNetwork(buf, content);
        return serializer.fromNetwork(buf);
    }

    /**
     * deep copy and modify the size attribute for those Content that have the size attribute.
     */
    public T copyWithModifier(T content, ContentModifier modifier){
        return copyInner(content);
    }

    @SuppressWarnings("unchecked")
    public final T copyContent(Object content) {
        return copyInner((T) content);
    }

    @SuppressWarnings("unchecked")
    public final T copyContent(Object content, ContentModifier modifier) {
        return copyWithModifier((T) content, modifier);
    }

    /**
     * used for recipe builder via KubeJs.
     */
    public T of(Object o) {
        return serializer.of(o);
    }

    public String slotName(IO io) {
        return "%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT));
    }

    public String slotName(IO io, int index) {
        return "%s_%s_%s".formatted(name, io.name().toLowerCase(Locale.ROOT), index);
    }

    public Component getTraslateComponent() {
        return Component.translatable("recipe.capability.%s.name".formatted(name));
    }

     //TODO
    public double calculateAmount(List<T> left) {
        return 1;
    }
}
