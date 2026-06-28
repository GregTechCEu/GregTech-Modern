package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.GTCEu;

import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.registry.*;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This is a copy of KubeJS's {@link RegistryEventJS} with minor modifications, licensed LGPLv3. <a
 * href=
 * "https://github.com/KubeJS-Mods/KubeJS/blob/841690e742660596fbb17a480fd13f8638492123/src/main/java/dev/latvian/mods/kubejs/registry/RegistryKubeEvent.java">Source</a>
 *
 * @param <T> The type of object to register
 */
public class GTRegistryEventJS<T> extends StartupEventJS {

    private final RegistryInfo<T> registry;
    public final List<BuilderBase<? extends T>> created;

	public GTRegistryEventJS(RegistryInfo<T> r) {
		this.registry = r;
		this.created = new LinkedList<>();
	}

	@SuppressWarnings("unchecked")
    public BuilderBase<? extends T> create(String id, String typeName) {
		BuilderType<T> type = registry.types.get(typeName);

		if (type == null) {
			throw new IllegalArgumentException("Unknown type '" + typeName + "' for object '" + id + "'!");
		}

		BuilderBase<? extends T> builder = type.factory()
				.createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, GTCEu.appendIdString(id)));

		if (builder == null) {
			throw new IllegalArgumentException("Unknown type '" + typeName + "' for object '" + id + "'!");
		} else {
			registry.addBuilder(builder);
			created.add(builder);
		}

		return builder;
	}

	@SuppressWarnings("unchecked")
	public BuilderBase<? extends T> create(String id) {
		BuilderType<T> type = registry.getDefaultType();

		if (type == null) {
			throw new IllegalArgumentException("Registry for type '" + registry.key.location() + "' doesn't have any builders registered!");
		}

		BuilderBase<? extends T> builder = type.factory()
				.createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, GTCEu.appendIdString(id)));

		if (builder == null) {
			throw new IllegalArgumentException("Unknown type '" + type.type() + "' for object '" + id + "'!");
		} else {
			registry.addBuilder(builder);
			created.add(builder);
		}

		return builder;
	}

	@SuppressWarnings("unchecked")
	public CustomBuilderObject createCustom(String id, Supplier<Object> object) {
		if (object == null) {
			throw new IllegalArgumentException("Tried to register a null object with id: " + id);
		}
		ResourceLocation rl = UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, GTCEu.appendIdString(id));

		CustomBuilderObject builder = new CustomBuilderObject(rl, object, registry);
		registry.addBuilder(builder);
		created.add(builder);

		return builder;
	}
}
