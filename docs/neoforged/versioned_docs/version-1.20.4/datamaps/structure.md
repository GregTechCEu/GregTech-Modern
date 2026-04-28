# JSON Structure
For the purposes of this page, we will use a data map which is an object with two float keys: `amount` and `chance` as an example. The codec for that object can be found [here](./index.md#registration).

## Location
Data maps are loaded from a JSON file located at `mapNamespace/data_maps/registryNamespace/registryPath/mapPath.json`, where:
- `mapNamespace` is the namespace of the ID of the data map
- `mapPath` is the path of the ID of the data map
- `registryNamespace` is the namespace of the ID of the registry
- `registryPath` is the path of the ID of the registry

:::note
The registry namespace is ommited if it is `minecraft`.
:::

Examples:
- For a data map named `mymod:drop_healing` for the `minecraft:item` registry (as in the example), the path will be `mymod/data_maps/item/drop_healing.json`.
- For a data map named `somemod:somemap` for the `minecraft:block` registry, the path will be `somemod/data_maps/block/somemap.json`.
- For a data map named `example:stuff` for the `somemod:custom` registry, the path will be `example/data_maps/somemod/custom/stuff.json`.

## Global `replace` field
The JSON file has an optional, global `replace` field, which is similar to tags, and when `true` will remove all previously attached values of that data map. This is useful for datapacks that want to completely change the entire data map.

## Loading conditions
Data map files support [loading conditions](../resources/server/conditional) both at root-level and at entry-level through a `neoforge:conditions` array.

## Adding values
Values can be attached to objects using the `values` map. Each key will represent either the ID of an individual registry entry to attach the value to, or a tag key, preceeded by `#`. If it is a tag, the same value will be attached to all entries in that tag.  
The key will be the object to attach.

```js
{
    "values": {
        // Attach a value to the carrot item
        "minecraft:carrot": {
            "amount": 12,
            "chance": 1
        },
        // Attach a value to all items in the logs tag
        "#minecraft:logs": {
            "amount": 1,
            "chance": 0.1
        }
    }
}
```

:::info
The above structure will invoke mergers in the case of [advanced data maps](./index.md#advanced-data-maps). If you do not want to invoke the merger for a specific object, then you will have to use a structure similar to this one:
```js
{
    "values": {
        // Overwrite the value of the carrot item
        "minecraft:carrot": {
            // highlight-next-line
            "replace": true,
            // The new value will be under a value sub-object
            "value": {
                "amount": 12,
                "chance": 1
            }
        }
    }
}
```
:::

## Removing values

A JSON file can also remove values previously attached to objects, through the use of the `remove` array:
```js
{
    // Remove the value attached to apples and potatoes
    "remove": ["minecraft:apple", "minecraft:potato"]
}
```
The array contains a list of registry entry IDs or tags to remove the value from.

:::warning
Removals happen after the values in the current JSON file have been attached, so you can use the removal feature to remove a value attached to an object through a tag:
```js
{
    "values": {
        "#minecraft:logs": 12
    },
    // Remove the value from the acacia log, so that all logs but acacia have the value 12 attached to them
    "remove": ["minecraft:acacia_log"]
}
```
:::

:::info
In the case of [advanced data maps](./index.md#advanced-data-maps) that provide a custom remover, the arguments of the remover can be provided by transforming the `remove` array into a map.  
Let's assume that the remover object is serialized as a string and removes the value with a given key for a `Map`-based data map:
```js
{
    "remove": {
        // The remover will be deserialized from the value (`somekey1` in this case)
        // and applied to the value attached to the carrot item
        "minecraft:carrot": "somekey1"
    }
}
```
:::