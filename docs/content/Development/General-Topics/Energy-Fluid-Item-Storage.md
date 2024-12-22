---
title: Item/Fluid/Energy Storage
---


# How to add an Item Inventory / Fluid Storage / Energy Container

!!! note

    In general, these containers should be created as `final` fields (that's what we need for the
    [SyncData](../SyncData/index.md) system).  
    Set their base arguments in the constructor (you can pass args for subclasses to modify).


## Implementations for Recipe Processing and adding Capabilities

You can create these containers via one of the following classes:

- `NotifiableItemStackHandler`
- `NotifiableFluidTank`
- `NotifiableEnergyContainer`

In general, you should prefer these classes over other implementations if possible, as they notify all listeners
of internal changes to improve performance.

**IO constructor parameters:**

- `handlerIO`: Whether the container is regarded as input or output during recipe processing
- `capabilityIO`: Whether the player can use hoppers, pipes, cables, etc. to interact with the storage


## General-Purpose implementations

If you don't need to use the storage for recipe processing and/or providing capabilities, you can just use one of the
following classes, as they are more lightweight:

- `ItemStackTransfer`
- `FluidStorage`


## Custom implementations

In some cases, you might need to create a custom implementation for either of these containers.  
To do so, use either of the following interfaces:

- `IItemTransfer`
- `IFluidTransfer`
- `IEnergyContainer`


## Specialized proxy implementations

In case you have special requirements to your containers, you may be able to use one of these implementations in
conjunction with one or more regular containers.  
They generally act as a proxy to the underlying container(s), while also handling these requirements.


### Proxying multiple containers

- `ItemTransferList`
- `FluidTransferList`
- `EnergyContainerList`


### IO-specific container proxies

For proxying multiple containers, limited to a specific IO direction.

- `IOItemTransferList`
- `IOFluidTransferList`  


### Rate-Limited proxies

!!! warning inline end "Not merged yet<br>_Branch: `mi-ender-link`_"

If you need to proxy any item or fluid container that needs to be rate limited for insertion and extraction, you can
use either of the following classes:

- `LimitingItemTransferProxy`
- `LimitingFluidTransferProxy`

The transfer limit passed as a constructor parameter will not renew automatically. Your container will therefore stop
transferring anything once this limit is reached.

If you want to make this a rate limit instead, you will have to schedule a task that regularly resets the transfer
limit to the maximum value for your task's interval:

??? example "Example Usage"

    ```java
    public class MyCover extends CoverBehavior {
        private LimitingFluidTransferProxy transferProxy;
        private ConditionalSubscriptionHandler rateLimitSubscription;
        
        public MyCover(IFluidTransfer myFluidTransfer) {
            super(/* ... */);
            
            transferProxy = new LimitingFluidTransferProxy(
                    myFluidTransfer,
                    0L // Initial limit of 0, will be updated regularly in isRateLimitRefreshActive()
            );
            rateLimitSubscription = new ConditionalSubscriptionHandler(
                    this,
                    this::resetTransferRateLimit, 
                    this::isRateLimitRefreshActive
            );
        }
        
        @Override
        public void onLoad() {
            super.onLoad();
            rateLimitSubscription.initialize(coverHolder.getLevel());
        }
        
        private void resetTransferRateLimit() {
            if (transferProxy == null)
                return;
    
            transferProxy.setRemainingTransfer(transferRate.getMilliBuckets() * 20);
        }
        
        private boolean isRateLimitRefreshActive() {
            // ...
        }
    }
    ```