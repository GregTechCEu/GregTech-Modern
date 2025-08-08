---
title: "Other Topics"
---


# Other Topics

This section contains other topics that aren't necessarily large enough to be grouped into their own categories.

- `.regressWhenWaiting(false)` - A property used when creating Custom Machines. Add this property to a Custom Machine
definition to make the machine Pause if it gets stuck mid-recipe, rather than having its recipe progress tick backwards.
This is a very important property to set on any machine with per-tick outputs (such as custom Generators), as without
it, these machines can potentially produce infinite outputs.