---
title: "Other Topics"
---


# Other Topics

This section contains other topics that aren't necessarily large enough to be grouped into their own categories.

- `.regressWhenWaiting(false)` - A property used when creating custom machines. Add this property to a custom machine
definition to make the machine pause if it gets stuck mid-recipe, rather than having its recipe progress tick backwards.
This is a very important property to set on any machine with per-tick outputs (such as custom generators), as without
it, these machines can potentially produce infinite outputs.