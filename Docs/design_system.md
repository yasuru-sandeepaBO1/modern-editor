# Kotlin Editor — Design System
*Locked palette — these 8 colors only for all app chrome. No accent color, nothing else
added. Syntax highlighting inside the code area is the one scoped exception, documented
at the bottom of this file.*

## Fonts
- **JetBrains Mono** — code, line numbers
- **Inter** — all other UI text

## Colors — the only colors in this project

| Role | Hex |
|---|---|
| Screen / status bar background | `#31323f` |
| Header / top bar / toolbar background | `#353b47` |
| Editor content surface / gutter background / active tab | `#1e2430` |
| Line-number / gutter text | `#4c556b` |
| Inactive tab background | `#272b36` |
| Button surface / primary actions / active states | `#475163` |
| Button text / secondary UI text | `#c4c8d1` |
| Primary/emphasized text (titles, filenames, headings) | `#FFFFFF` |

## Rule

No color anywhere in the app — Compose code or `UIs/*.html` — may be outside this table.
There is no accent color in this project. `#475163` is reused as the surface for primary
actions and active states; it does not introduce a new hue.

Toggle switches keep their existing blue ON-state as the one previously-agreed exception.

---

## Syntax highlighting — code area only (Component 5)

Syntax highlighting needs hues that are clearly distinguishable from one another. The 8
colors above are eight near-identical dark blue-greys plus white, so keywords, strings
and comments would be indistinguishable if they were used. These colors are therefore
scoped strictly to **the text inside the editor's code area**.

Everything else — top bars, menus, drawer, dialogs, gutter, status bar, accessory
toolbar, and every other screen — still comes from the 8-color table above.

The values are the default IntelliJ / Android Studio **Darcula** scheme, i.e. what Kotlin
looks like in Android Studio itself.

| Token | Hex |
|---|---|
| Keywords and modifiers (`class`, `fun`, `val`, `private`) | `#CC7832` |
| String, raw string and character literals | `#6A8759` |
| Line and block comments | `#808080` |
| Annotations, incl. use-site target (`@Override`, `@file:JvmName`) | `#BBB529` |
| Numeric literals | `#6897BB` |
| Function declaration name (the `name` in `fun name(...)`) | `#FFC66D` |
| Base code text — identifiers, operators, punctuation | `#A9B7C6` |

Implemented in `ui/theme/SyntaxColors.kt`. Applied only to files recognised as Kotlin;
plain-text files keep `#FFFFFF` body text.


