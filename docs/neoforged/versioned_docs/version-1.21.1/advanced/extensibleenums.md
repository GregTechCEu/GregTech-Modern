# Extensible Enums

Extensible Enums are an enhancement of specific Vanilla enums to allow new entries to be added. This is done by modifying the compiled bytecode of the enum at runtime to add the elements.

## `IExtensibleEnum`

All enums that can have new entries implement the `IExtensibleEnum` interface. This interface acts as a marker to allow the `RuntimeEnumExtender` launch plugin service to know what enums should be transformed.

:::warning
You should **not** be implementing this interface on your own enums. Use maps or registries instead depending on your usecase.  
Enums which are not patched to implement the interface cannot have the interface added to them via mixins or coremods due to the order the transformers run in.
:::

### Creating an Enum Entry

To create new enum entries, a JSON file needs to be created and referenced in the `neoforge.mods.toml` with the `enumExtensions` entry of a `[[mods]]` block. The specified path must be relative to the `resources` directory:
```toml
# In neoforge.mods.toml:
[[mods]]
## The file is relative to the output directory of the resources, or the root path inside the jar when compiled
## The 'resources' directory represents the root output directory of the resources
enumExtensions="META-INF/enumextensions.json"
```

The definition of the entry consists of the target enum's class name, the new field's name (must be prefixed with the mod ID), the descriptor of the constructor to use for constructing the entry and the parameters to be passed to said constructor.

```json5
{
    "entries": [
        {
            // The enum class the entry should be added to
            "enum": "net/minecraft/world/item/ItemDisplayContext",
            // The field name of the new entry, must be prefixed with the mod ID
            "name": "EXAMPLEMOD_STANDING",
            // The constructor to be used
            "constructor": "(ILjava/lang/String;Ljava/lang/String;)V",
            // Constant parameters provided directly.
            "parameters": [ -1, "examplemod:standing", null ]
        },
        {
            "enum": "net/minecraft/world/item/Rarity",
            "name": "EXAMPLEMOD_CUSTOM",
            "constructor": "(ILjava/lang/String;Ljava/util/function/UnaryOperator;)V",
            // The parameters to be used, provided as a reference to an EnumProxy<Rarity> field in the given class
            "parameters": {
                "class": "example/examplemod/MyEnumParams",
                "field": "CUSTOM_RARITY_ENUM_PROXY"
            }
        },
        {
            "enum": "net/minecraft/world/damagesource/DamageEffects",
            "name": "EXAMPLEMOD_TEST",
            "constructor": "(Ljava/lang/String;Ljava/util/function/Supplier;)V",
            // The parameters to be used, provided as a reference to a method in the given class
            "parameters": {
                "class": "example/examplemod/MyEnumParams",
                "method": "getTestDamageEffectsParameter"
            }
        }
    ]
}
```

```java
public class MyEnumParams {
    public static final EnumProxy<Rarity> CUSTOM_RARITY_ENUM_PROXY = new EnumProxy<>(
            Rarity.class, -1, "examplemod:custom", (UnaryOperator<Style>) style -> style.withItalic(true)
    );
    
    public static Object getTestDamageEffectsParameter(int idx, Class<?> type) {
        return type.cast(switch (idx) {
            case 0 -> "examplemod:test";
            case 1 -> (Supplier<SoundEvent>) () -> SoundEvents.DONKEY_ANGRY;
            default -> throw new IllegalArgumentException("Unexpected parameter index: " + idx);
        });
    }
}
```

#### Constructor

The constructor must be specified as a [method descriptor][jvmdescriptors] and must only contain the parameters visible in the source code, omitting the hidden constant name and ordinal parameters.  
If a constructor is marked with the `@ReservedConstructor` annotation, then it cannot be used for modded enum constants.

#### Parameters

The parameters can be specified in three ways with limitations depending on the parameter types:

- Inline in the JSON file as an array of constants (only allowed for primitive values, Strings and for passing null to any reference type)
- As a reference to a field of type `EnumProxy<TheEnum>` in a class from the mod (see `EnumProxy` example above)
    - The first parameter specifies the target enum and the subsequent parameters are the ones to be passed to the enum constructor
- As a reference to a method returning `Object`, where the return value is the parameter value to use. The method must have exactly two parameters of type `int` (index of the parameter) and `Class<?>` (expected type of the parameter)
    - The `Class<?>` object should be used to cast (`Class#cast()`) the return value in order to keep `ClassCastException`s in mod code.

:::warning
The fields and/or methods used as sources for parameter values should be in a separate class to avoid unintentionally loading mod classes too early.
:::

Certain parameters have additional rules:

- If the parameter is an int ID parameter related to a `@IndexedEnum` annotation on the enum, then it is ignored and replaced by the entry's ordinal. If said parameter is specified inline in the JSON, then it must be specified as `-1`, otherwise an exception is thrown.
- If the parameter is a String name parameter related to a `@NamedEnum` annotation on the enum, then it must be prefixed by the mod ID in the `namespace:path` format known from `ResourceLocation`s, otherwise an exception is thrown.

#### Retrieving the Generated Constant

The generated enum constant can be retrieved via `TheEnum.valueOf(String)`. If a field reference is used to provide the parameters, then the constant can also be retrieved from the `EnumProxy` object via `EnumProxy#getValue()`.

## Contributing to NeoForge

To add a new extensible enum to NeoForge, there are at least two required things to do:

- Make the enum implement `IExtensibleEnum` to mark that this enum should be transformed via the `RuntimeEnumExtender`.
- Add a `getExtensionInfo` method that returns `ExtensionInfo.nonExtended(TheEnum.class)`.

Further action is required depending on specific details about the enum:

- If the enum has an int ID parameter which should match the entry's ordinal, then the enum should be annotated with `@NumberedEnum` with the ID's parameter index as the annotation's value if it's not the first parameter
- If the enum has a String name parameter which is used for serialization and should therefore be namespaced, then the enum should be annotated with `@NamedEnum` with the name's parameter index as the annotation's value if it's not the first parameter
- If the enum is sent over the network, then it should be annotated with `@NetworkedEnum` with the annotation's parameter specifying in which direction the values may be sent (clientbound, serverbound or bidirectional)
    - Warning: networked enums will require additional steps once network checks for enums are implemented in NeoForge
- If the enum has constructors which are not usable by mods (i.e. because they require registry objects on an enum that may be initialized before modded registration runs), then they should be annotated with `@ReservedConstructor`

:::note
The `getExtensionInfo` method will be transformed at runtime to provide a dynamically generated `ExtensionInfo` if the enum actually had any entries added to it.
:::

```java
// This is an example, not an actual enum within Vanilla
public enum ExampleEnum implements net.neoforged.fml.common.asm.enumextension.IExtensibleEnum {
    // VALUE_1 represents the name parameter here
    VALUE_1(0, "value_1", false),
    VALUE_2(1, "value_2", true),
    VALUE_3(2, "value_3");

    ExampleEnum(int arg1, String arg2, boolean arg3) {
        // ...
    }

    ExampleEnum(int arg1, String arg2) {
        this(arg1, arg2, false);
    }

    public static net.neoforged.fml.common.asm.enumextension.ExtensionInfo getExtensionInfo() {
        return net.neoforged.fml.common.asm.enumextension.ExtensionInfo.nonExtended(ExampleEnum.class);
    }
}
```

[jvmdescriptors]: https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.3.2
