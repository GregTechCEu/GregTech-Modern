---
title: Modular Items
---

# Modular Items

### IModularItem

`IModularItem` is a capability that can be attached to item stacks.
Its recommended implementation is `ModularItemStack`, which can be attached to a stack
in an `AttachCapabilitiesEvent<ItemStack>` listener, or attached as an item component
to a `ComponentItem` using `ModularItemComponent`. Note that if it is attached not as
a component, module-related tooltips won't be displayed, and `onUse` and related methods won't work.

!!! example "Attaching a `ModularItemStack` using an event"
    ```java
    @Mod.EventBusSubscriber(modid = "your_mod_id", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ExampleEventListener {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
            ItemStack stack = event.getObject();
            if (stack.is(Items.DIAMOND_CHESTPLATE)) {
                int slots = 5, maxTier = GTValues.HV;
                List<ItemModuleSlot> defaultSlots = new ArrayList<>();
                for (int i = 0; i < slots; i++) defaultSlots.add(GTItemModules.TIERED_SLOTS[maxTier]);
                // ModularItemStack accepts a Function<ItemStack, List<ItemModuleSlot>> in the constructor
                // The stack provided to that function is the same stack as here, so we can ignore it
                event.addCapability(GTCEu.id("modular"), new ModularItemStack(stack, ignored -> defaultSlots));
            }
        }
    }
    ```

!!! example "Attaching a `ModularItemComponent` to a `ComponentItem`"
    ```java
    public void attachModularComponent() {
        // fertilizer will now have 3 MV module slots
        GTItems.FERTILIZER.get().attachComponents(new ModularItemComponent(3, GTValues.MV));
    }
    ```

### ItemModuleSlot

An `ItemModuleSlot` is basically a `Predicate<ItemModule>` (a function that accepts an `ItemModule` and returns a `boolean`) with some other methods.
It has 2 abstract methods for your subclasses to implement:

- `acceptsModule(ItemModule)` -> `boolean`
    returns whether the provided module can be put into this slot
- `getDisplayName()` -> `Component`
    returns the text to be displayed as part of the tooltip
- `getSlotTexture()` -> `IDrawable` (returns `null` by default)
    returns the texture to use for this slot in the equipment foundry UI, `null` means to use the default

!!! example "Tiered slot implementation"
    ```java
    public class TieredItemModuleSlot extends ItemModuleSlot {
    
        @Getter
        private final int tier;
    
        public TieredItemModuleSlot(ResourceLocation id, int tier) {
            super(id);
            this.tier = tier;
        }
    
        @Override
        public boolean acceptsModule(ItemModule module) {
            return !(module instanceof ITieredItemModule tieredModule) || tieredModule.getTier() <= getTier();
        }
    
        @Override
        public Component getDisplayName() {
            return Component.translatable("metaarmor.tooltip.modifier_slot.tiered", GTValues.VNF[getTier()]);
        }
    }
    ```

### ItemModule

`ItemModule` is an NBT-serializable object that can be attached to modular items.<br>
`AppliedItemModule` is an object that contains info about the module itself, the item
it is applied to, and the module's NBT.

??? info "List of all overridable methods in `ItemModule`"
    - `getInfo()` -> `Component` (override required)<br>
      returns the info about this module to display in XEI
    - `onAttach(AppliedItemModule)` -> `void`<br>
      called when this module is attached to a modular item
    - `onRemove(AppliedItemModule)` -> `void`<br>
      called when this module is removed from a modular item
    - `onEquip(LivingEntity, AppliedItemModule)` -> `void`<br>
      called when the armor piece this module is attached to is equipped
    - `onUnequip(LivingEntity, AppliedItemModule)` -> `void`<br>
      called when the armor piece this module is attached to is unequipped
    - `onArmorTick(LivingEntity, AppliedItemModule)` -> `void`<br>
      called each tick while the armor piece this module is attached to is equipped
    - `onInventoryTick(Player, AppliedItemModule)` -> `void`<br>
      called each tick while the item this module is attached to is in a player's inventory
    - `appendHoverText(Level, TooltipFlag, List<Component>, AppliedItemModule)` -> `void`<br>
      called to insert lines into the modular item's tooltip
    - `useEnergyInInventory(LivingEntity, AppliedItemModule)` -> `boolean` (default `true`)<br>
      returns whether to consume energy while the item is in a player's inventory, if `false`, the item
      consumes energy only in armor slots
    - `energyUsagePerTick(LivingEntity, AppliedItemModule)` -> `long` (default `0`)<br>
      returns the amount of energy to consume this tick (called every tick)
    - `changeDamage(LivingEntity, AppliedItemModule, float damage, DamageSource)`-> `float` (returns `damage` by default)<br>
      returns the amount of damage to actually inflict, called each time the entity wearing the armor piece is damaged
    - `canRemove(AppliedItemModule)` -> `boolean` (default `true`)<br>
      returns whether this module can be removed from the item in an equipment foundry
    - `isPPE(AppliedItemModule)` -> `boolean` (default `false`)<br>
      returns whether armor pieces with this module should be considered personal protection equipment (hazmat)
    - `canApplyTo(ItemStack)` -> `boolean`<br>
      returns whether this module can be attached to the provided stack, by default returns whether a module of this type
      is already attached to the stack (blocking more than one module of the same type)
    - `isEnabled(AppliedItemModule)` -> `boolean` (default `true`)<br>
      returns whether this module is enabled (all other methods, such as `onInventoryTick` won't be calld if it's disabled)
    - `setEnabled(AppliedItemModule, boolean)` -> `void`<br>
      by default, sets the return value of `isEnabled` by modifying this module's NBT; this method is not required to actually enable/disable this module
    - `use(AppliedItemModule, Level, Player, InteractionHand)` -> `InteractionResultHolder<ItemStack>`<br>
      called when the item this module is attached to is used
    - `useOn(AppliedItemModule, UseOnContext)` -> `InteractionResult`<br>
      called when the item this module is attached to is used on a block

!!! example "Example of an energy shield module"
    ```java
    public class EnergyShieldItemModule extends TieredItemModule {
    
        public EnergyShieldItemModule(ResourceLocation id, int tier) {
            super(id, tier);
        }
    
        @Override
        public Component getInfo() {
            return Component.literal("Makes you invulnerable and consumes 1A " + GTValues.VNF[getTier()]);
        }
        
        @Override
        public long energyUsagePerTick(LivingEntity entity, AppliedItemModule module) {
            return GTValues.V[getTier()];
        }
    
        @Override
        public float changeDamage(LivingEntity entity, AppliedItemModule module, float amount, DamageSource source) {
            return 0;
        }
    
        @Override
        public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                    AppliedItemModule module) {
            super.appendHoverText(level, isAdvanced, tooltips, module);
            tooltips.add(Component.literal("Energy shield (" + GTValues.VNF[getTier()] + ")"));
        }
    }
    ```