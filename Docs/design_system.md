# Kotlin Editor — Design System
*Locked palette. These are the only colors in the project — nothing else.*

## Fonts
- **JetBrains Mono** — code, line numbers (unchanged from before; this reference's own
  fonts were Fira Code/Roboto Mono — not adopted, colors only per instruction)
- **Inter** — all other UI text

## Colors — exact source values

| Role | Hex |
|---|---|
| Screen / status bar background | `#31323f` |
| Header / top bar / toolbar background | `#353b47` |
| Editor content surface / gutter background / active tab | `#1e2430` |
| Line-number / gutter text | `#4c556b` |
| Inactive tab background | `#272b36` |
| Accessory toolbar button surface | `#475163` |
| Accessory toolbar button text | `#c4c8d1` |

## Required additions — not in the source file, filled transparently

| Role | Hex | Why |
|---|---|---|
| Primary/body text | `#FFFFFF` | None of the 7 source colors are light enough to read as body text on this dark background — this is a functional necessity, not a new color choice |
| Accent (active toggles, primary buttons, cursor) | `#857d39` | Carried over from earlier explicit decision — the source file has zero color for interactive states. Flag if this should change. |

## Rule

No color anywhere in the app — Compose code or `UIs/*.html` — may be outside this table.
Toggle switches keep their blue ON-state as a documented, permanent exception.# Kotlin Editor — Design System





