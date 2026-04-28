# Contributing to the Documentation

This is a non-exhaustive guideline for making contributions to the [NeoForged Documentation][docs] repository. Contributions can be made by forking and cloning the repository and then added via a pull request, or PR, on the [GitHub][docs].

You can run the website locally using [npm]. It is recommended to use a Node Version Manager like [nvm] (Mac, Linux) or [nvs] (Windows) to setup and install npm and Node. From there, you can run the following commands:

```bash
nvm use # or nvs use on Windows
npm install
npm run start
```

## Principles

This documentation is a guide to help a modder understand and implement a given concept from Minecraft or NeoForged.

This documentation is **not** meant as a tutorial, allowing a modder to copy-paste the examples. If you are looking for a tutorial, there are plenty of videos and pages, which are not linked here, that you can use and follow along with.

This documentation is also **not** meant as documentation for a class. Providing a description of an element is unavoidable when writing a guide; however, if you would like to document a class, you should contribute to [Parchment for Minecraft][parchment] or [NeoForge for NeoForged][neo].

Finally, this documentation is **not** meant to explain Java concepts. This documentation is intended for people who already have a solid basis in Java. If a Java concept needs to be explained to better understand the concept (such as JVM Descriptors for Access Transformers), a link should be provided to the original resource. Otherwise, if you are unfamiliar with Java, there are plenty of online resources to learn from:

- [JetBrains Academy][jetbrains]
- [Codeacademy][codeacademy]
- [University of Helsinki][helsinki]
- [Oracle][oracle]
- [Introduction to Programming using Java by David J. Eck][eck]

## Concepts

Each page should guide a modder on a particular concept. If the concept is too large in scope, the concept should be split into separate sub-concepts, each within its own page. For example, if you are writing a cookbook, there can be a page for each recipe, rather than a single page containing all the recipes.

When describing a concept, you should first introduce what the concept is, where it is used in Minecraft, why it should be used, and how to use it. Each section within a concept should have a header. A section can also be broken into sub-sections if necessary. For example, each recipe within a cookbook can have a sub-section for ingredients and the recipe itself.

If you need to refer to other concepts, the relevant page should be linked along with a summary and/or some example to understand the application.

## Examples

Code examples should generally be pseudocode-like objects meant to enhance the understanding of a modder. For this documentation, pseudocode-like refers to code blocks written in the structure and syntax of the desired language with comments used as placeholders for specific logic that the modder may choose to implement themselves. The code blocks do not necessarily need to be compilable, but each line should have valid syntax and structure of the desired language.

When implementing a method, it is usually specific to the desired goal a modder is trying to achieve. As a guide, this documentation aims to be somewhat agnostic to a modder's specific goal, instead covering the general use case.

Let's say we are using a method called `#applyDiscount` to take some value off the current price. Not everyone will implement the same logic within the method. So, the pseudocode can leave a comment mentioning what to do instead:

```java
// In some class

public float applyDiscount(float price) {
    float newPrice = price;
    // Apply discount to newPrice
    // ...
    return newPrice;
}
```

:::tip
If the pseudocode is not explanatory enough to understand the concept, then a full code example can be used instead. A full code example should supply dummy values and explain what they represent.
:::

## Minor and Patch Changes

If a change occurs between a minor or patch versions of NeoForge, then relevant changes in the documentation should be split into separate sections or put into tabs. This maintains the accuracy of the information depending on the version the modder is currently developing for.

````md
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs defaultValue="latest">
<!-- Value represents the last supported version of this method. -->
<!-- Label represents the version range to display to the reader. -->
<TabItem value="latest" label="Latest">

<!-- Markdown here -->
```java
public void latestMethod() {
    // ...
}
```

</TabItem>
<!-- There must be an empty line before and after the Markdown text in a tab. -->
<TabItem value="20.2.67" label="[20.2.35,20.2.67]"> 

<!-- Markdown here -->
```java
public void previousMethod() {
    // ...
}
```

</TabItem>
<TabItem value="20.2.34" label="[20.2.0,20.2.34]">

<!-- Markdown here -->
```java
public void firstMethod() {
    // ...
}
```

</TabItem>
</Tabs>
````

Output:

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs defaultValue="latest">
<TabItem value="latest" label="Latest">

<!-- Markdown here -->
```java
public void latestMethod() {
    // ...
}
```

</TabItem>
<TabItem value="20.2.67" label="[20.2.35,20.2.67]"> 

<!-- Markdown here -->
```java
public void previousMethod() {
    // ...
}
```

</TabItem>
<TabItem value="20.2.34" label="[20.2.0,20.2.34]">

<!-- Markdown here -->
```java
public void firstMethod() {
    // ...
}
```

</TabItem>
</Tabs>

## Style Guide

This documentation uses [Docusaurus][docusaurus], which internally uses [MDX][mdx], to generate the pages. You can find more detailed information about available features on their pages. This style guide will be more focused towards common features and formatting we use in the Markdown files.

### Front Matter

Front matter defines metadata fields which can affect how the page is rendered. This is denoted using `---`, similar to a code block. The only front matter that should usually be defined is `sidebar_position`, which determines where the page should be rendered on the sidebar.

There are other metadata fields like `title` and `description`, but those are typically parsed from the page itself.

```md
<!-- Second page in the sidebar -->
---
sidebar_position: 2
---
```

:::note
If another page defines the sidebar positions that a new page should be placed, use a floating point number. The positions will be converted to integers in a formatting PR.

```md
<!-- sidebar_position 1 and 2 are defined, but this page should go in-between them -->
---
sidebar_position: 1.5
---
```
:::

#### Categories

Categories are folders within the documentation. They inherit titles and positional data from `index.md`. If an `index.md` is not within a subfolder, a `_category_.json` file should be created, specifying the `label` representing the name of the section, and `position` representing where on the sidebar it should go.

```json5
{
    // Name of the category to display
    "label": "Example Title",

    // This will be rendered as the third element on the sidebar at the current level.
    "position": 3
}
```

### Titles

Titles are defined using up to six hashtags (`#`) to define each section. Titles should capitalize everything but unimportant words.

```md
<!-- There should only be one title with a single '#'. -->
# Guide For Contributing to This Documentation

### Building and Testing Your Mod
```

### Diction

Spelling, grammar, and syntax should follow those in American English. Avoid using contractions in sentences; use two separate words ('is not' instead of 'isn't'). Additionally, avoid using pronouns (e.g. I, me, you) when possible, unless you need to directly refer to the reader. Demonstratives (e.g. this, that, its) should be used sparingly to avoid confusing the reader. Prefer using the actual object or noun being referred to.

### Paragraphs

Paragraphs should be a continuous block, separated by a newline. Paragraphs should **not** have each sentence be on a new line.

```md
This is my first paragraph. See how the next sentence is on the same line? You can use word wrapping in your editor to stop the line from going off the screen.

This is my next paragraph. It is separated by a new line.
```

### Indentation

When indenting lines, use four spaces instead of tabs. Most markdown features require four spaces to recognize indentation, so it allows consistency across the document.

```md
- Hello World
    - Four Spaces In
```

### Importance

Emphasizing words should be done using **bold** or _italics_. Please use two asterisks (`**`) for bold and an underscore (`_`) for italics to make the separation in Markdown more distinct.

```md
This is a **bolded** word.

This is an _italicized_ word.
```

### Lists

Unordered lists should always use `-` for every level while ordered lists should always start with `1.`.

```md
- This is an unordered list.
- This is a second element.
    - This is a subelement.

1. This is an ordered list.
1. This is a second element.
    1. This is a subelement.
```

### Code References

When referencing elements outside of code blocks, they should be surrounded with backticks (`` ` ``). Classes should use their simple name. Methods and fields should specify the class name followed by a `#`. If the class name is implied, the method or field can simply be prefixed with `#`. Inner classes should specify the name of the outer class followed by a `.`.

```md
<!-- Classes -->
`MyClass`
`MyClass.InnerClass`

<!-- Method and Fields -->
`MyClass#foo`
`MyClass.InnerClass#bar`
`#SOME_CONSTANT`
```

Code blocks should specify the language after the triple backtick (`` ``` ``). When writing a JSON block, the JSON5 (`json5`) syntax highlighter should be used to allow comments.

````md
<!-- Java Code Block -->
```java
public void run() {
    //...
}
```

<!-- JSON Code Block -->
```json5
{
    // Comments are allowed here
    "text": "Hiya"
}
```
````

#### Events

Events in code blocks should use the `EventBusSubscriber` format: public static method with a `SubscribeEvent` annotation. The annotation must be followed by a comment indicating the bus the event will be registered to. The comment should also specify if the event handler should be registered on a given side.

```java
@SubscribeEvent // on the <bus name> event bus [only on the physical <side name>]
public static void methodName(EventName event) {
    // Code goes here
}
```

:::note
The events page is exempted as it describes the different methods of event handler registration.
:::

### Links

All links should use brackets (`[]`) to refer to a link specified on the bottom of the markdown page. The second pair of brackets can be omitted if the name between the first pair of brackets is used.

```md
<!-- Somewhere on a page -->
There are [two] different types of [link references][linkref].

<!-- At the bottom of a page -->
[two]: https://linkrefwithoutref.donotclick
[linkref]: https://linkref.donotclick
```

### Admonitions

Admonitions can be specified on the page using three colons (`:::`) and by specifying its type. Admonition formatting can be found on the [Docusaurus wiki][admonition].

```md
:::note
I'm within an admonition!
:::
```

### Diagrams

Diagrams can be created using the [Mermaid library][mermaid].

When creating a diagram, a convention is used to make `abstract` classes red and non-`abstract` classes blue.

````md
<!-- Class1 is abstract, Class2 is not -->
```mermaid
graph LR;
    Class1-->Class2;
    
    class Class1 red;
    class Class2 blue;
```
````

[docs]: https://github.com/neoforged/Documentation

[npm]: https://www.npmjs.com/
[nvm]: https://github.com/nvm-sh/nvm
[nvs]: https://github.com/jasongin/nvs

[parchment]: https://github.com/ParchmentMC/Parchment
[neo]: https://github.com/neoforged/NeoForge/

[jetbrains]: https://www.jetbrains.com/academy/
[codeacademy]: https://www.codecademy.com/learn/learn-java
[helsinki]: https://java-programming.mooc.fi/
[oracle]: https://docs.oracle.com/javase/tutorial/
[eck]: http://math.hws.edu/javanotes/

[docusaurus]: https://docusaurus.io/docs/markdown-features
[mermaid]: https://mermaid.js.org/intro/
[mdx]: https://mdxjs.com/guides/

[admonition]: https://docusaurus.io/docs/markdown-features/admonitions
