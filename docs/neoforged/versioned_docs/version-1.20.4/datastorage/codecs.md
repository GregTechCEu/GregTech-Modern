# Codecs

Codecs are a serialization tool from Mojang's [DataFixerUpper] used to describe how objects can be transformed between different formats, such as `JsonElement`s for JSON and `Tag`s for NBT.

## Using Codecs

Codecs are primarily used to encode, or serialize, Java objects to some data format type and decode, or deserialize, formatted data objects back to its associated Java type. This is typically accomplished using `Codec#encodeStart` and `Codec#parse`, respectively.

### DynamicOps

To determine what intermediate file format to encode and decode to, both `#encodeStart` and `#parse` require a `DynamicOps` instance to define the data within that format.

The [DataFixerUpper] library contains `JsonOps` to codec JSON data stored in [`Gson`'s][gson] `JsonElement` instances. `JsonOps` supports two versions of `JsonElement` serialization: `JsonOps#INSTANCE` which defines a standard JSON file, and `JsonOps#COMPRESSED` which allows data to be compressed into a single string.

```java
// Let exampleCodec represent a Codec<ExampleJavaObject>
// Let exampleObject be a ExampleJavaObject
// Let exampleJson be a JsonElement

// Encode Java object to regular JsonElement
exampleCodec.encodeStart(JsonOps.INSTANCE, exampleObject);

// Encode Java object to compressed JsonElement
exampleCodec.encodeStart(JsonOps.COMPRESSED, exampleObject);

// Decode JsonElement into Java object
// Assume JsonElement was parsed normally
exampleCodec.parse(JsonOps.INSTANCE, exampleJson);
```

Minecraft also provides `NbtOps` to codec NBT data stored in `Tag` instances. This can be referenced using `NbtOps#INSTANCE`.

```java
// Let exampleCodec represent a Codec<ExampleJavaObject>
// Let exampleObject be a ExampleJavaObject
// Let exampleNbt be a Tag

// Encode Java object to Tag
exampleCodec.encodeStart(JsonOps.INSTANCE, exampleObject);

// Decode Tag into Java object
exampleCodec.parse(JsonOps.INSTANCE, exampleNbt);
```

#### Format Conversion

`DynamicOps` can also be used separately to convert between two different encoded formats. This can be done using `#convertTo` and supplying the `DynamicOps` format and the encoded object to convert.

```java
// Convert Tag to JsonElement
// Let exampleTag be a Tag
JsonElement convertedJson = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, exampleTag);
```

### DataResult

Encoded or decoded data using codecs return a `DataResult` which holds the converted instance or some error data depending on whether the conversion was successful. When the conversion is successful, the `Optional` supplied by `#result` will contain the successfully converted object. If the conversion fails, the `Optional` supplied by `#error` will contain the `PartialResult`, which holds the error message and a partially converted object depending on the codec.

Additionally, there are many methods on `DataResult` that can be used to transform the result or error into the desired format. For example, `#resultOrPartial` will return an `Optional` containing the result on success, and the partially converted object on failure. The method takes in a string consumer to determine how to report the error message if present.

```java
// Let exampleCodec represent a Codec<ExampleJavaObject>
// Let exampleJson be a JsonElement

// Decode JsonElement into Java object
DataResult<ExampleJavaObject> result = exampleCodec.parse(JsonOps.INSTANCE, exampleJson);

result
  // Get result or partial on error, report error message
  .resultOrPartial(errorMessage -> /* Do something with error message */)
  // If result or partial is present, do something
  .ifPresent(decodedObject -> /* Do something with decoded object */);
```

## Existing Codecs

### Primitives

The `Codec` class contains static instances of codecs for certain defined primitives.

Codec         | Java Type
:---:         | :---
`BOOL`        | `Boolean`
`BYTE`        | `Byte`
`SHORT`       | `Short`
`INT`         | `Integer`
`LONG`        | `Long`
`FLOAT`       | `Float`
`DOUBLE`      | `Double`
`STRING`      | `String`
`BYTE_BUFFER` | `ByteBuffer`
`INT_STREAM`  | `IntStream`
`LONG_STREAM` | `LongStream`
`PASSTHROUGH` | `Dynamic<?>`\*
`EMPTY`       | `Unit`\*\*

\* `Dynamic` is an object which holds a value encoded in a supported `DynamicOps` format. These are typically used to convert encoded object formats into other encoded object formats.

\*\* `Unit` is an object used to represent `null` objects.

### Vanilla and Forge

Minecraft and NeoForge define many codecs for objects that are frequently encoded and decoded. Some examples include `ResourceLocation#CODEC` for `ResourceLocation`s, `ExtraCodecs#INSTANT_ISO8601` for `Instant`s in the `DateTimeFormatter#ISO_INSTANT` format, and `CompoundTag#CODEC` for `CompoundTag`s.

:::caution
`CompoundTag`s cannot decode lists of numbers from JSON using `JsonOps`. `JsonOps`, when converting, sets a number to its most narrow type. `ListTag`s force a specific type for its data, so numbers with different types (e.g. `64` would be `byte`, `384` would be `short`) will throw an error on conversion.
:::

Vanilla and NeoForge registries also have codecs for the type of object the registry contains (e.g. `Registry#BLOCK` or `ForgeRegistries#BLOCKS` have a `Codec<Block>`). `Registry#byNameCodec` and `IForgeRegistry#getCodec` will encode the registry object to their registry name, or an integer identifier if compressed. Vanilla registries also have a `Registry#holderByNameCodec` which encodes to a registry name and decodes to the registry object wrapped in a `Holder`.

## Creating Codecs

Codecs can be created for encoding and decoding any object. For understanding purposes, the equivalent encoded JSON will be shown.

### Records

Codecs can define objects through the use of records. Each record codec defines any object with explicit named fields. There are many ways to create a record codec, but the simplest is via `RecordCodecBuilder#create`.

`RecordCodecBuilder#create` takes in a function which defines an `Instance` and returns an application (`App`) of the object. A correlation can be drawn to creating a class *instance* and the constructors used to *apply* the class to the constructed object.

```java
// Some object to create a codec for
public class SomeObject {

  public SomeObject(String s, int i, boolean b) { /* ... */ }

  public String s() { /* ... */ }

  public int i() { /* ... */ }

  public boolean b() { /* ... */ }
}
```

#### Fields

An `Instance` can define up to 16 fields using `#group`. Each field must be an application defining the instance the object is being made for and the type of the object. The simplest way to meet this requirement is by taking a `Codec`, setting the name of the field to decode from, and setting the getter used to encode the field.

A field can be created from a `Codec` using `#fieldOf`, if the field is required, or `#optionalFieldOf`, if the field is wrapped in an `Optional` or defaulted. Either method requires a string containing the name of the field in the encoded object. The getter used to encode the field can then be set using `#forGetter`, taking in a function which given the object, returns the field data.

From there, the resulting product can be applied via `#apply` to define how the instance should construct the object for the application. For ease of convenience, the grouped fields should be listed in the same order they appear in the constructor such that the function can simply be a constructor method reference.

```java
public static final Codec<SomeObject> RECORD_CODEC = RecordCodecBuilder.create(instance -> // Given an instance
  instance.group( // Define the fields within the instance
    Codec.STRING.fieldOf("s").forGetter(SomeObject::s), // String
    Codec.INT.optionalFieldOf("i", 0).forGetter(SomeObject::i), // Integer, defaults to 0 if field not present
    Codec.BOOL.fieldOf("b").forGetter(SomeObject::b) // Boolean
  ).apply(instance, SomeObject::new) // Define how to create the object
);
```

```js
// Encoded SomeObject
{
  "s": "value",
  "i": 5,
  "b": false
}

// Another encoded SomeObject
{
  "s": "value2",
  // i is omitted, defaults to 0
  "b": true
}
```

### Transformers

Codecs can be transformed into equivalent, or partially equivalent, representations through mapping methods. Each mapping method takes in two functions: one to transform the current type into the new type, and one to transform the new type back to the current type. This is done through the `#xmap` function.

```java
// A class
public class ClassA {

  public ClassB toB() { /* ... */ }
}

// Another equivalent class
public class ClassB {

  public ClassA toA() { /* ... */ }
}

// Assume there is some codec A_CODEC
public static final Codec<ClassB> B_CODEC = A_CODEC.xmap(ClassA::toB, ClassB::toA);
```

If a type is partially equivalent, meaning that there are some restrictions during conversion, there are mapping functions which return a `DataResult` which can be used to return an error state whenever an exception or invalid state is reached.

Is A Fully Equivalent to B | Is B Fully Equivalent to A | Transform Method
:---:                      | :---:                      | :---
Yes                        | Yes                        | `#xmap`
Yes                        | No                         | `#flatComapMap`
No                         | Yes                        | `#comapFlatMap`
No                         | No                         | `#flatXMap`

```java
// Given an string codec to convert to a integer
// Not all strings can become integers (A is not fully equivalent to B)
// All integers can become strings (B is fully equivalent to A)
public static final Codec<Integer> INT_CODEC = Codec.STRING.comapFlatMap(
  s -> { // Return data result containing error on failure
    try {
      return DataResult.success(Integer.valueOf(s));
    } catch (NumberFormatException e) {
      return DataResult.error(s + " is not an integer.");
    }
  },
  Integer::toString // Regular function
);
```

```js
// Will return 5
"5"

// Will error, not an integer
"value"
```

#### Range Codecs

Range codecs are an implementation of `#flatXMap` which returns an error `DataResult` if the value is not inclusively between the set minimum and maximum. The value is still provided as a partial result if outside the bounds. There are implementations for integers, floats, and doubles via `#intRange`, `#floatRange`, and `#doubleRange` respectively.

```java
public static final Codec<Integer> RANGE_CODEC = Codec.intRange(0, 4); 
```

```js
// Will be valid, inside [0, 4]
4

// Will error, outside [0, 4]
5
```

### Defaults

If the result of encoding or decoding fails, a default value can be supplied instead via `Codec#orElse` or `Codec#orElseGet`.

```java
public static final Codec<Integer> DEFAULT_CODEC = Codec.INT.orElse(0); // Can also be a supplied value via #orElseGet
```

```js
// Not an integer, defaults to 0
"value"
```

### Unit

A codec which supplies an in-code value and encodes to nothing can be represented using `Codec#unit`. This is useful if a codec uses a non-encodable entry within the data object.

```java
public static final Codec<IForgeRegistry<Block>> UNIT_CODEC = Codec.unit(
  () -> ForgeRegistries.BLOCKS // Can also be a raw value
);
```

```js
// Nothing here, will return block registry codec
```

### List

A codec for a list of objects can be generated from an object codec via `Codec#listOf`.

```java
// BlockPos#CODEC is a Codec<BlockPos>
public static final Codec<List<BlockPos>> LIST_CODEC = BlockPos.CODEC.listOf();
```

```js
// Encoded List<BlockPos>
[
  [1, 2, 3], // BlockPos(1, 2, 3)
  [4, 5, 6], // BlockPos(4, 5, 6)
  [7, 8, 9]  // BlockPos(7, 8, 9)
]
```

List objects decoded using a list codec are stored in an **immutable** list. If a mutable list is needed, a [transformer] should be applied to the list codec.

### Map

A codec for a map of keys and value objects can be generated from two codecs via `Codec#unboundedMap`. Unbounded maps can specify any string-based or string-transformed value to be a key.

```java
// BlockPos#CODEC is a Codec<BlockPos>
public static final Codec<Map<String, BlockPos>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, BlockPos.CODEC);
```

```js
// Encoded Map<String, BlockPos>
{
  "key1": [1, 2, 3], // key1 -> BlockPos(1, 2, 3)
  "key2": [4, 5, 6], // key2 -> BlockPos(4, 5, 6)
  "key3": [7, 8, 9]  // key3 -> BlockPos(7, 8, 9)
}
```

Map objects decoded using a unbounded map codec are stored in an **immutable** map. If a mutable map is needed, a [transformer] should be applied to the map codec.

:::caution
Unbounded maps only support keys that encode/decode to/from strings. A key-value [pair] list codec can be used to get around this restriction.
:::

### Pair

A codec for pairs of objects can be generated from two codecs via `Codec#pair`.

A pair codec decodes objects by first decoding the left object in the pair, then taking the remaining part of the encoded object and decodes the right object from that. As such, the codecs must either express something about the encoded object after decoding (such as [records]), or they have to be augmented into a `MapCodec` and transformed into a regular codec via `#codec`. This can typically done by making the codec a [field] of some object.

```java
public static final Codec<Pair<Integer, String>> PAIR_CODEC = Codec.pair(
  Codec.INT.fieldOf("left").codec(),
  Codec.STRING.fieldOf("right").codec()
);
```

```js
// Encoded Pair<Integer, String>
{
  "left": 5,       // fieldOf looks up 'left' key for left object
  "right": "value" // fieldOf looks up 'right' key for right object
}
```

:::tip
A map codec with a non-string key can be encoded/decoded using a list of key-value pairs applied with a [transformer].
:::

### Either

A codec for two different methods of encoding/decoding some object data can be generated from two codecs via `Codec#either`.

An either codec attempts to decode the object using the first codec. If it fails, it attempts to decode using the second codec. If that also fails, then the `DataResult` will only contain the error from the second codec failure.

```java
public static final Codec<Either<Integer, String>> EITHER_CODEC = Codec.either(
  Codec.INT,
  Codec.STRING
);
```

```js
// Encoded Either$Left<Integer, String>
5

// Encoded Either$Right<Integer, String>
"value"
```

:::tip
This can be used in conjunction with a [transformer] to get a specific object from two different methods of encoding.
:::

### Dispatch

Codecs can have subcodecs which can decode a particular object based upon some specified type via `Codec#dispatch`. This is typically used in registries which contain codecs, such as rule tests or block placers.

A dispatch codec first attempts to get the encoded type from some string key (usually `type`). From there, the type is decoded, calling a getter for the specific codec used to decode the actual object. If the `DynamicOps` used to decode the object compresses its maps, or the object codec itself is not augmented into a `MapCodec` (such as records or fielded primitives), then the object needs to be stored within a `value` key. Otherwise, the object is decoded at the same level as the rest of the data.

```java
// Define our object
public abstract class ExampleObject {

  // Define the method used to specify the object type for encoding
  public abstract Codec<? extends ExampleObject> type();
}

// Create simple object which stores a string
public class StringObject extends ExampleObject {

  public StringObject(String s) { /* ... */ }

  public String s() { /* ... */ }

  public Codec<? extends ExampleObject> type() {
    // A registered registry object
    // "string":
    //   Codec.STRING.xmap(StringObject::new, StringObject::s)
    return STRING_OBJECT_CODEC.get();
  }
}

// Create complex object which stores a string and integer
public class ComplexObject extends ExampleObject {

  public ComplexObject(String s, int i) { /* ... */ }

  public String s() { /* ... */ }

  public int i() { /* ... */ }

  public Codec<? extends ExampleObject> type() {
    // A registered registry object
    // "complex":
    //   RecordCodecBuilder.create(instance ->
    //     instance.group(
    //       Codec.STRING.fieldOf("s").forGetter(ComplexObject::s),
    //       Codec.INT.fieldOf("i").forGetter(ComplexObject::i)
    //     ).apply(instance, ComplexObject::new)
    //   )
    return COMPLEX_OBJECT_CODEC.get();
  }
}

// Assume there is an IForgeRegistry<Codec<? extends ExampleObject>> DISPATCH
public static final Codec<ExampleObject> = DISPATCH.getCodec() // Gets Codec<Codec<? extends ExampleObject>>
  .dispatch(
    ExampleObject::type, // Get the codec from the specific object
    Function.identity() // Get the codec from the registry
  );
```

```js
// Simple object
{
  "type": "string", // For StringObject
  "value": "value" // Codec type is not augmented from MapCodec, needs field
}

// Complex object
{
  "type": "complex", // For ComplexObject

  // Codec type is augmented from MapCodec, can be inlined
  "s": "value",
  "i": 0
}
```

[DataFixerUpper]: https://github.com/Mojang/DataFixerUpper
[gson]: https://github.com/google/gson
[transformer]: #transformer-codecs
[pair]: #pair
[records]: #records
[field]: #fields
