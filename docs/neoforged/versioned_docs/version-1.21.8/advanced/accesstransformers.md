import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Access Transformers

Access Transformers (ATs for short) allow for widening the visibility and modifying the `final` flags of classes, methods, and fields. They allow modders to access and modify otherwise inaccessible members in classes outside their control.

The [specification document][specs] can be viewed on the NeoForged GitHub.

## Adding ATs

Adding an Access Transformer to your mod project is as simple as adding a single line into your `build.gradle`:

Access Transformers need to be declared in `build.gradle`. AT files can be specified anywhere as long as they are copied to the `resources` output directory on compilation.

<Tabs defaultValue="mdg">
<TabItem value="mdg" label="ModDevGradle">

No need to do anything here by default!

</TabItem>
<TabItem value="ng" label="NeoGradle">

```gradle
// In build.gradle:
minecraft {
    accessTransformers {
        file 'src/main/resources/META-INF/accesstransformer.cfg'
    }
}
```

</TabItem>
</Tabs>

By default, NeoForge will search for `META-INF/accesstransformer.cfg`. If the `build.gradle` specifies access transformers in any other location, then their location needs to be defined within `neoforge.mods.toml`:

```toml
# In neoforge.mods.toml:
[[accessTransformers]]
## The file is relative to the output directory of the resources, or the root path inside the jar when compiled
## The 'resources' directory represents the root output directory of the resources
file="META-INF/accesstransformer.cfg"
```

Additionally, multiple AT files can be specified and will be applied in order. This can be useful for larger mods with multiple packages.

<Tabs defaultValue="mdg">
<TabItem value="mdg" label="ModDevGradle">

```gradle
// In build.gradle:
neoForge {
    // ModDevGradle already tries to include 'src/main/resources/META-INF/accesstransformer.cfg' by default
    accessTransformers.from 'src/additions/resources/accesstransformer_additions.cfg'
}
```

</TabItem>
<TabItem value="ng" label="NeoGradle">

```gradle
// In build.gradle:
minecraft {
    accessTransformers {
        file 'src/main/resources/META-INF/accesstransformer.cfg'
        file 'src/additions/resources/accesstransformer_additions.cfg'
    }
}
```

</TabItem>
</Tabs>

```toml
# In neoforge.mods.toml
[[accessTransformers]]
file="accesstransformer_main.cfg"

[[accessTransformers]]
file="accesstransformer_additions.cfg"
```

After adding or modifying any Access Transformer, the Gradle project must be refreshed for the transformations to take effect.

## The Access Transformer Specification

### Comments

All text after a `#` until the end of the line will be treated as a comment and will not be parsed.

### Access Modifiers

Access modifiers specify to what new member visibility the given target will be transformed to. In decreasing order of visibility:

- `public` - visible to all classes inside and outside its package
- `protected` - visible only to classes inside the package and subclasses
- `default` - visible only to classes inside the package
- `private` - visible only to inside the class

A special modifier `+f` and `-f` can be appended to the aforementioned modifiers to either add or remove respectively the `final` modifier, which prevents subclassing, method overriding, or field modification when applied.

:::danger
Directives only modify the method they directly reference; any overriding methods will not be access-transformed. It is advised to ensure transformed methods do not have non-transformed overrides that restrict the visibility, which will result in the JVM throwing an error.

Examples of methods that can be safely transformed are `final` methods (or methods in `final` classes), and `static` methods. `private` methods are generally safe as well; however, they could cause unintentional overrides in any subtypes, so some additional manual validation should be performed.
:::

### Targets and Directives

#### Classes

To target classes:

```
<access modifier> <fully qualified class name>
```

Inner classes are denoted by combining the fully qualified name of the outer class and the name of the inner class with a `$` as separator.

#### Fields

To target fields:

```
<access modifier> <fully qualified class name> <field name>
```

#### Methods

Targeting methods require a special syntax to denote the method parameters and return type:

```
<access modifier> <fully qualified class name> <method name>(<parameter types>)<return type>
```

##### Specifying Types

Also called "descriptors": see the [Java Virtual Machine Specification, SE 21, sections 4.3.2 and 4.3.3][jvmdescriptors] for more technical details.

- `B` - `byte`, a signed byte
- `C` - `char`, a Unicode character code point in UTF-16
- `D` - `double`, a double-precision floating-point value
- `F` - `float`, a single-precision floating-point value
- `I` - `integer`, a 32-bit integer
- `J` - `long`, a 64-bit integer
- `S` - `short`, a signed short
- `Z` - `boolean`, a `true` or `false` value
- `[` - references one dimension of an array
    - Example: `[[S` refers to `short[][]`
- `L<class name>;` - references a reference type
    - Example: `Ljava/lang/String;` refers to `java.lang.String` reference type _(note the use of slashes instead of periods)_
- `(` - references a method descriptor, parameters should be supplied here or nothing if no parameters are present
    - Example: `<method>(I)Z` refers to a method that requires an integer argument and returns a boolean
- `V` - indicates a method returns no value, can only be used at the end of a method descriptor
    - Example: `<method>()V` refers to a method that has no arguments and returns nothing

### Examples

```
# Makes public the ByteArrayToKeyFunction interface in Crypt
public net.minecraft.util.Crypt$ByteArrayToKeyFunction

# Makes protected and removes the final modifier from 'random' in MinecraftServer
protected-f net.minecraft.server.MinecraftServer random

# Makes public the 'makeExecutor' method in Util,
# accepting a String and returns a TracingExecutor
public net.minecraft.Util makeExecutor(Ljava/lang/String;)Lnet/minecraft/TracingExecutor;

# Makes public the 'leastMostToIntArray' method in UUIDUtil,
# accepting two longs and returning an int[]
public net.minecraft.core.UUIDUtil leastMostToIntArray(JJ)[I
```

[specs]: https://github.com/NeoForged/AccessTransformers/blob/main/FMLAT.md
[jvmdescriptors]: https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.3.2
