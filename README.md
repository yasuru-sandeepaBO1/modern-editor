# Kotlin Mobile Text Editor

**IS2205: Mobile Application Design and Development — Mini-Project**
*Modern Mobile Text Editor with Incremental Version Control*

A native Android text editor for Kotlin and Markdown, with local file management, automatic crash recovery, and a delta-based (non-duplicating) version control system with diff and rollback support.

---

## Features

### Editor
- Text buffer with cursor, selection, insert/delete, copy/paste/cut
- Undo/redo — session-based edit history stack
- Line numbers and word wrapping
- Search and search & replace

### File Management
- New, Open, Save, Save As, Recent Files
- UTF-8 encoding support
- Read-only file lock

### Syntax Highlighting
- **Kotlin** — keywords, strings, comments, annotations
- **Markdown** — syntax highlighting, with an optional live preview panel

### Crash Recovery
- Automatic background caching of the active buffer (every ~10 seconds)
- Recovery prompt on relaunch after a crash or unexpected interruption

### Version Control
- Delta-based versioning — only diffs (patches) are stored, never full file copies
- Diff engine (`java-diff-utils`) for line-by-line comparison
- Rollback to any previous version

### Settings
- Font size, tab size, word wrap, line numbers, auto-recovery interval

---

## Tech Stack

- **Language:** Kotlin, native Android
- **Persistence:** Room (SQLite)
- **Diffing:** java-diff-utils
- **Settings storage:** Jetpack DataStore

---

## Project Structure

```
app/
│
├── ui/
│   ├── editor/        (EditorScreen, EditorViewModel)
│   ├── files/          (FilesScreen, FilesViewModel)
│   ├── history/        (HistoryScreen, DiffScreen, HistoryViewModel)
│   ├── settings/        (SettingsScreen, SettingsViewModel)
│   └── components/
│
├── editor/
│   ├── EditorEngine
│   ├── SyntaxHighlighter   → dispatches to KotlinLexer / MarkdownLexer
│   ├── KotlinLexer
│   ├── UndoRedoManager
│   └── SearchEngine
│
├── markdown/
│   ├── MarkdownLexer
│   ├── MarkdownParser
│   └── MarkdownRenderer     (optional preview)
│
├── file/
│   ├── FileManager
│   ├── FileReader
│   └── FileWriter
│
├── version/
│   ├── VersionManager
│   ├── DiffEngine            (wraps java-diff-utils)
│   ├── SnapshotManager
│   └── RollbackManager
│
├── recovery/
│   └── RecoveryManager
│
├── repository/
│   ├── FileRepository
│   ├── VersionRepository
│   └── SettingsRepository
│
├── database/
│   ├── AppDatabase
│   ├── FileDao
│   ├── VersionDao
│   ├── DeltaDao
│   └── entities/
│       ├── FileEntity
│       ├── VersionEntity
│       └── DeltaEntity
│
└── model/
    ├── EditorFile
    ├── Version
    ├── Patch          (persisted delta)
    └── DiffResult      (UI-facing diff for the rollback screen)
```

---

## Architecture

```
UI → ViewModel → Repository → File System / Room / DataStore
```

Screens never talk to a DAO or `FileManager` directly — everything routes through a repository layer (`FileRepository`, `VersionRepository`, `SettingsRepository`) that hides where the data actually lives. Undo/redo (in-memory, session-only) and version control (persistent, delta-based) are kept as fully separate systems.

The plan below is organized two ways: **the 17 levels** describe *what* the app is made of, each one a self-contained component built on top of the ones before it. **The build order** describes *when* to build each piece, broken into small, checkable phases.

---

## The 17 Levels

### Level 0 — Foundation & Architecture
*Foundation*
Set up everything invisible — the structure the other sixteen levels are built on top of — before a single editor feature exists.

**What we set up**
- Android project, Kotlin configuration, Gradle configuration, package structure, dependency management
- Application architecture split into UI, domain, repository, storage, editor, recovery, and version-control layers
- Core data models: `EditorFile`, `Version`, `Patch`, `DiffResult`
- Navigation between the Editor, Files, History, and Settings screens

**Repository layer**
Screens never talk to a DAO or a FileManager directly. Everything routes through a repository (`FileRepository`, `VersionRepository`, `SettingsRepository`) that hides whether the data actually came from the file system, Room, or DataStore. This is the seam that lets three people work on different layers at the same time without constant merge conflicts.

> Spec note: not a line item in the spec — an architectural decision made specifically to support the 3-person team split.

### Level 1 — Editor Engine
*Core System*
Get a working plain-text editor running before worrying about Kotlin, files, or anything else. This is the heart of the app.

**Components**
- Text editing surface and text buffer (the in-memory document content)
- Cursor and text selection
- Insert / delete, copy / paste / cut
- Undo / redo — a session-only memory stack tracking granular edits
- Line handling and line numbers
- Word wrapping
- Search and search & replace

> Spec note: undo/redo, word wrapping, and search & replace are explicitly required by the specification.

### Level 2 — File System
*Core System*
Let the editor read and write real files on the device instead of just holding text in memory.

**FileManager**
- New
- Open
- Recent Files
- Save
- Save As
- Rename
- Delete

> Spec note: Open, New, Recent Files, Save, Save As, and UTF-8 encoding support are explicitly required by the specification.

**Internal flow**
```
Opening:  User → File Picker → FileManager → FileReader → Editor Buffer
Saving:   Editor Buffer → FileManager → FileWriter → Device Storage
```

### Level 3 — User Interface
*Core System*
Build the visible rooms of the app around the editor engine.

**Screens & components**
- Editor screen: top app bar, file title, editor area, line numbers, toolbar, search bar
- Sidebar with recent files
- Menus and dialogs
- Status bar
- Settings screen

> Spec note: the specification recommends a single-active-file view with a sidebar for recent files — used as the baseline instead of a desktop-style multi-tab IDE.

### Level 4 — Kotlin Syntax Engine
*Core System*
Turn the plain-text editor into a Kotlin-aware one.

```
Raw Text → KotlinLexer → Tokens → SyntaxHighlighter → Styled Text → Editor
```

**Token types recognized**
- Keywords (`class`, `val`, `fun`, `private` …)
- Identifiers & types
- Strings
- Comments
- Annotations
- Function names

> Spec note: required scope is dynamic styling for keywords, strings, comments, and annotations, with keyword highlighting as the minimum bar.

### Level 5 — Markdown Engine
*Core System*
Support the second language the app is required to handle.

**Required**
- MarkdownLexer — Markdown syntax highlighting inside the editor

**Optional**
- MarkdownParser → MarkdownRenderer → toggleable live preview panel

> Spec note: the specification requires Markdown syntax highlighting; the preview panel is explicitly optional.

### Level 6 — Crash Recovery
*Core System*
Make sure a crash or an unexpected lifecycle interruption never costs the user their unsaved work.

**Flow**
- RecoveryManager periodically caches the active buffer to a temporary file (spec example: every 10 seconds)
- On relaunch, RecoveryManager checks for leftover recovery data
- If found, the user is prompted to restore it
- Recovery data is cleaned up once it's no longer needed

> Spec note: explicitly required — an automated background mechanism that periodically caches the active buffer.

### Level 7 — Version Control
*Core System*
Give the document a persistent, structured history — the signature feature of the project.

This is **not** the same system as undo/redo, and the two are kept completely separate:
- **Undo/Redo** — temporary, in-memory, session-only editing history
- **Version Control** — persistent, on-disk document history that survives closing the app

**Components**
- VersionManager
- SnapshotManager
- DiffEngine
- RollbackManager

### Level 8 — Incremental Delta System
*Core System*
Store version history without wasting storage by duplicating the whole file every time.

```
Instead of:  Version 1 → full file,  Version 2 → full file,  Version 3 → full file …
We do:       Original File → Patch 1 → Patch 2 → Patch 3 → Patch 4 …
```

> Spec note: explicitly required — subsequent versions store diff/patch information rather than duplicating the complete file.

### Level 9 — Diff Engine
*Core System*
Calculate exactly what changed between two versions of the text.

**Two related, but different, objects**
- `Patch` — the actual delta used for storage and reconstructing a version
- `DiffResult` — the structure used to render the comparison in the History UI

> Spec note: the specification allows using an existing open-source utility such as java-diff-utils instead of writing a diff algorithm from scratch.

### Level 10 — Rollback System
*Core System*
Let the user travel backward through a document's history and restore an earlier state.

```
Version 2 selected → reconstruct content from stored patches → load into Editor Buffer
```

> Spec note: required — inspect previous snapshots, view a basic structural/line-by-line diff, and restore the file to any previous version state.

### Level 11 — Database
*Core System*
Persist file, version, and delta metadata so history survives app restarts.

**Tables**
- `Files` — id, name, path, language, createdAt, modifiedAt, readOnly
- `Versions` — id, fileId, versionNumber, timestamp, description
- `Deltas` — id, versionId, previousVersionId, patch

> Spec note: the specification explicitly permits SQLite through Room for persisting versioning information.

### Level 12 — Repository Layer
*Core System*
Decouple the rest of the app from where data actually lives — the plumbing between the rooms and the foundation.

```
UI → ViewModel → Repository → File System / Room / DataStore
```

**Components**
- `FileRepository` — file-related operations
- `VersionRepository` — version history
- `SettingsRepository` — persisted settings

### Level 13 — Settings
*Smart Feature*
Let the user customize how the editor looks and behaves.

- Font size
- Tab size
- Word wrap
- Line numbers
- Syntax highlighting
- Auto-recovery interval

```
Settings UI → SettingsViewModel → SettingsRepository → DataStore
```

> Spec note: DataStore as the persistence mechanism is an architectural choice — the specification doesn't prescribe one.

### Level 14 — Read-Only Mode
*Smart Feature*
A small but explicitly required safety feature.

```
Read-only flag enabled → typing / edit attempts on that file are blocked
```

> Spec note: the ability to lock files as read-only is explicitly listed in the specification.

### Level 15 — Optional / Smart Features
*Smart Feature*
Polish layered on top of a working required feature set — build these only once everything above is solid.

**Editor improvements**
- Auto indentation
- Bracket matching
- Auto-closing brackets
- Go to line
- Current-line highlighting
- Code folding
- File statistics

**Kotlin improvements**
- Deeper token recognition
- Automated code formatting

**Markdown improvements**
- Markdown preview panel

**UI improvements**
- Themes
- Custom fonts & sizes
- Animations
- Better dialogs & empty states
- File icons

> Spec note: automated Kotlin formatting and the Markdown preview are both explicitly optional in the specification.

### Level 16 — Testing & Polish
*Testing & Delivery*
Prove it actually works, then package it for submission.

**Testing areas**
- Editor
- File system
- Syntax highlighting
- Recovery
- Version control
- Diff
- Rollback
- Database
- UI

**Hardening**
- Error handling
- Performance
- Large files
- Corrupted recovery files
- App restart behavior
- Storage failures
- Version-reconstruction correctness

**Submission deliverables**
- Source code and a shared APK build link, hosted on GitHub
- A technical report covering storage, preview, comparison, delta tracking, and syntax highlighting
- A demonstration video (≤ 25 minutes) with all three members presenting and stating their individual contribution

---

## Complete Build Order

This is the sequence to actually build in — each phase has a single checkable goal, so you always know whether it's done before moving to the next one.

> **One change from the original phase order:** Database (originally Phase 9) and Diff Engine (originally Phase 10) are now built before Version Control (originally Phase 8). Creating a snapshot needs a diff engine to generate the patch and a database to store it — so Version Control can't actually be finished until both exist. Everything else keeps its original order.

### Phase 0 — Architecture
*Goal: empty but correctly structured application.*
- Create Android project
- Establish project configuration
- Establish package structure
- Establish architecture
- Establish navigation
- Establish data models
- Establish dependency strategy
- Establish repository interfaces

### Phase 1 — Basic Editor
*Goal: a working plain-text editor.*
- Editor screen
- Text buffer
- Text input
- Cursor
- Selection
- Copy / paste / cut
- Insert / delete
- Undo
- Redo
- Multiline editing

### Phase 2 — Editor Features
*Goal: a genuinely usable text editor.*
- Line handling
- Line numbers
- Word wrapping
- Search
- Search next / previous
- Replace
- Replace all

### Phase 3 — File System
*Goal: the editor can work with real files.*
- New file
- Open file
- File reader
- Save
- Save As
- UTF-8 handling
- Recent files
- File management
- Read-only flag

### Phase 4 — User Interface
*Goal: the app looks and behaves like a real editor.*
- Main editor UI
- Top app bar
- Sidebar
- Recent files UI
- Menus
- Dialogs
- Status information
- Settings screen

### Phase 5 — Kotlin Highlighting
*Goal: .kt files look like Kotlin code.*
- KotlinLexer
- Token model
- Keyword recognition
- String recognition
- Comment recognition
- Annotation recognition
- SyntaxHighlighter
- Connect highlighting to editor

### Phase 6 — Markdown
*Goal: required Markdown editing works; optional preview can be added afterward.*
- MarkdownLexer
- Markdown highlighting
- Language detection / toggle
- MarkdownParser (optional)
- MarkdownRenderer (optional)
- Markdown preview (optional)

### Phase 7 — Crash Recovery
*Goal: app crashes → user doesn't lose recent work.*
- RecoveryManager
- Temporary recovery storage
- Periodic buffer caching
- Recovery detection
- Recovery dialog / UI
- Restore recovered buffer
- Cleanup recovery data

### Phase 8 — Database *(reordered)*
*Goal: persistent storage exists for files, versions, and deltas.*
- Room setup
- FileEntity
- VersionEntity
- DeltaEntity
- FileDao
- VersionDao
- DeltaDao
- AppDatabase
- Connect repositories

### Phase 9 — Diff Engine *(reordered)*
*Goal: the app can calculate and display exactly what changed between two pieces of text.*
- DiffEngine (wraps java-diff-utils)
- Patch generation
- DiffResult model
- Line-by-line comparison
- Diff UI

### Phase 10 — Version Control *(reordered)*
*Goal: we can create non-duplicating versions.*
- Version model
- VersionManager
- SnapshotManager
- Create snapshots
- Version metadata
- Delta generation (via Phase 9's DiffEngine)
- Patch storage (via Phase 8's DeltaDao)

### Phase 11 — Rollback
*Goal: user can inspect and restore previous versions.*
- History screen
- Version list
- Version selection
- Version reconstruction
- Restore
- Rollback UI

### Phase 12 — Settings
*Goal: user customization persists across sessions.*
- Settings model
- SettingsRepository
- DataStore
- Font size
- Tab size
- Theme
- Word wrap setting
- Editor preferences

### Phase 13 — Smart Features
*Goal: polish — build only if time permits, after everything required works.*
- Auto indentation
- Bracket matching
- Auto-closing brackets
- Go to line
- Code folding
- Current-line highlighting
- Kotlin formatter
- Markdown preview
- Advanced themes
- Additional editor customization

### Phase 14 — Final Polish
*Goal: ship a stable, gradeable deliverable.*
- Error handling
- Performance optimization
- Large-file testing
- Crash testing
- Storage-failure testing
- Version-reconstruction testing
- UI testing
- Documentation
- APK generation
- GitHub / source submission
- Demonstration-video preparation