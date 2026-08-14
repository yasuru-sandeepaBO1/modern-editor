# Kotlin Editor — Design System
*Locked palette — these 8 colors only. No accent color, nothing else added.*

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


