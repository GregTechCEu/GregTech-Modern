package com.gregtechceu.gtceu.api.gui.compass;

import com.gregtechceu.gtceu.api.gui.compass.component.*;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/9/3
 * @implNote LayoutComponentManager
 */
public final class LayoutComponentManager {
    private final static Map<String, Class<? extends ILayoutComponent>> COMPONENTS = new HashMap<>();

    public static void registerComponent(String name, Class<? extends ILayoutComponent> clazz) {
        COMPONENTS.put(name, clazz);
    }

    @Nullable
    public static ILayoutComponent createComponent(String name, Element element) {
        Class<? extends ILayoutComponent> clazz = COMPONENTS.get(name);
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.newInstance().fromXml(element);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        registerComponent("text", TextBoxComponent.class);
        registerComponent("image", ImageComponent.class);
        for (HeaderComponent.Header header : HeaderComponent.Header.values()) {
            registerComponent(header.name(), HeaderComponent.class);
            registerComponent(header.name(), HeaderComponent.class);
            registerComponent(header.name(), HeaderComponent.class);
        }
        registerComponent("br", BlankComponent.class);
        registerComponent("recipe", RecipeComponent.class);
        registerComponent("scene", SceneComponent.class);
        registerComponent("ingredient", IngredientComponent.class);
    }

}
