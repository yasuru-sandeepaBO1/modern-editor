# Kotlin Editor — Design System

Exact values. Do not substitute or approximate — use these hex codes directly in Compose
`Color(0xFF...)` definitions.

## Fonts
- **JetBrains Mono** — all code content, line numbers, filenames in lists
- **Inter** — all other UI text (labels, buttons, menus, dialogs)

## Colors

| Role | Hex | Notes |
|---|---|---|
| Screen background | `#191d26` | App-level base |
| Editor/content surface | `#1e2430` | Where code is typed |
| Elevated surface | `#232936` | Top bar, tabs, menus, cards, dialogs |
| Hover/pressed surface | `#2b3240` | Any pressed row inside a menu |
| Border/divider | `#363d4d` | Between menu groups, card borders |
| Gutter border | `#262c38` | Subtler line next to line numbers |
| Primary text | `#e5e7eb` (or white) | Menu labels, code text, filenames |
| Secondary/muted text | `#7b8598` – `#94a3b8` | Icon default color, placeholder text |
| Tertiary muted | `#4b5569` | Line-number gutter text specifically |
| Toolbar button surface | `#475163` | Accessory toolbar buttons |
| Toolbar button text | `#c4c8d1` | Accessory toolbar labels |
| **Accent** | `#857d39` | Primary actions and active states only |
| Accent hover/pressed | `#716a30` | ~15% darker than accent |

## Accent usage rule

The accent (`#857d39`) is used **sparingly** — restrained, not decorative:
- The blinking text cursor
- ON/active state of toggles (Word Wrap enabled, Read Only enabled)
- The "K" logo letter in the hero badge
- Icons (not fill/background) on the New File / Open File buttons
- The Kotlin-file-type badge in Recent Files lists (`KT`)

Everything else — Undo, Redo, Search, Save, the overflow icon, Markdown/Text file badges —
stays neutral gray, brightening to white on press. Do not apply the accent broadly; it
should read as one deliberate touch, not a UI theme color.