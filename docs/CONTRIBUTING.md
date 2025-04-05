# Contributor Info / Styleguide

For consistency, please follow the following styleguide when submitting PRs.


## Filenames & Basic Page Structure

The file structure and basic page layout must follow these conventions: 

- For each section, create a directory with a short, descriptive name in `Title Case`.
- File names should only contain `A-Z`, `a-z`, `0-9`, `-` and `_` (no spaces).  
- Directory names must use dashes (`-`) instead of spaces.
- If you want to add a short description of what a section is about, include an `index.md` in its directory.
- Every page of the docs must include a Markdown front matter block, containing _at least_ its title, followed by
  two empty lines.
- Every page must start with a level-1 heading. No other level-1 headings may be added.

Note that the page name/title must be as short and descriptive as possible, as it's displayed in the navigation area.  
You can, however, choose a longer version for the page _header_:

```markdown
---
title: My Page Name
---


# This is my Custom Page Header!

Page content here...
```

Contrary to filenames, both the title and header are **not** limited to specific characters. 


## Empty Lines

Please consider the following conventions for empty lines:

- At least two empty lines before each heading
- Exactly one line after each heading
- Separate each "element" of a page by an empty line inbetween
  (code blocks, lists, [admonitions](https://squidfunk.github.io/mkdocs-material/reference/admonitions/), etc.)


## Formatting

Please use the following formatting rules for standard markdown:

- Use double asterisks for bold text (`**bold**`) and underscores for italics (`_italics_`)
- Each code block must include a language whenever possible, so that syntax highlighting is applied correctly


## Index Pages

Only use index pages for a short description of what the current section is about.

Do not include any important documentation on index pages!  
You may link to certain relevant pages (either inside the current section, or in other sections) though. 


## Admonitions

Use [admonitions](https://squidfunk.github.io/mkdocs-material/reference/admonitions/) in the following situations:

- If you want to provide potentially useful, additional info about a feature, use an `info` admonition.
- For "see also" references, use an `info inline end` admonition.
- For tips / recommendations on _when_ or _how_ to use a feature, use he `tip` admonition type.
  - For general notes on _when_ or _how_ to use a specific feature (in development related docs), use the `note` type.
- For warning about possible bugs or unwanted behavior, use the `warning` type.
- If a feature may result in destructive actions in certain scenarios, use the `danger` type.
- For providing code examples, use the `example` type.  
  - **Always** provide a title for code examples, even if it's simply "Example usage".  
  - Use collapsible blocks (`??? example "my example title"` instead of `!!!`) for code examples where possible.  
    _In some situations this might not be as useful as using a non-collapsible block. Just fall back to `!!!` in these cases._
- For pages or sections that are not documented yet, use `!!! failure "Not yet documented"`
- For links, use the (custom) `link` type.

We suggest adding an empty line after the title for consistency, like in the following example.  
It is **strongly recommended** to include a custom, descriptive title for your admonition, if possible.


```markdown
!!! note "My descriptive title"
    
    This is my note's content
```


### Admonitions for experimental or not-yet-merged features

If a feature is either not yet merged or simply not stable yet, please add a title-only `warning inline end` before its
description, as shown below.

For additional info (e.g. which branch the feature is currently in), add a `<br>` tag in the title, followed by the
additional info in italics.  
If your additional information needs to be more detailed than that, consider using a collapsible admonition instead.

```markdown
!!! warning inline end "Not merged yet.<br>_Branch: `my-branch-name`_"

Paragraph containing the feature's description...
```

Here are some good examples you could use as the first line of your warning:

- Experimental
- Unstable
- Not yet implemented
- Not merged yet


## Annotations

Use [annotations](https://squidfunk.github.io/mkdocs-material/reference/annotations/) in the following situations:

- Explanatory comments in code examples (preferred over including these as an actual comment)
- Additional explanations to a specific aspect of your documented feature
- An explanation for a technical term used in your documentation

There must be an empty line before the first annotation or they won't work properly.

Also add an empty line between each annotation if at least one in the current block is longer than a single line.  
In that case, add two empty lines after the end of the annotation list.

Annotations must be numbered starting at 1 **for each block** of consecutive annotations.  
If they are separated by another element, you need to start at 1 again for the next block of annotations:


~~~markdown
```java
someVar.someMethod(); // (1)
return someVar; // (2)
```

1. annotation 1 for the first code block
2. annotation 2 for the first code block


```java
someVar.anotherMethod(); // (1)
```

1. annotation 1 for the second code block
~~~
