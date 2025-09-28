# Electricity
The vast majority of GregTech Modern machines run on Electricity, also known as EU ("energy units"). Electricity and
Electric machines share several common safety and behavior rules.

## General concepts of EU
* EU is produced by [**Generators**](./Generators.md) every tick
* [**Electric Machines**](./Machines.md) consume EU every tick while operating
* Batteries and Battery Buffers act as EU [**Storage**](./Energy-Storage.md)
* [**Cables**](./Cables.md) transport EU between generators, storage, and machines

Batteries and Machines store EU in an internal buffer, but all EU transportation is done using **Voltage** and **Amperage**.

* Voltage (V) is the power tier of a device, and the size of an energy "packet" which is emitted by Generators and received
by Machines.
* Cables and Machines have a voltage tier (LV, MV, HV . . .), which is the maximum voltage they can safely carry or receive. 
Carrying or receiving unsafe Voltages can be highly destructive. Each successive voltage tier is 4x the voltage of the
previous. (LV = 32V, MV = 128V, HV = 512V . . .)
* Amperage (A) is how many Voltage packets are being carried at the same time in parallel.
* Voltage x Amperage results in **EU/t**. EU/t x Time results in **Total EU**.

