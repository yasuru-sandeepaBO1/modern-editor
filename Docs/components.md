# Kotlin Editor — Finalized Components, Tasks & Test Cases
*UI-aligned revision — reflects the finalized Stitch screens*

## What changed in this revision

- **Component 0** — navigation graph finalized: hub-and-spoke, no bottom tab bar
- **Component 2** — Search & Replace confirmed as dedicated buttons; plain-text matching only, no regex
- **Component 3** — Delete now requires a confirmation dialog; New File/Save As/Rename share one dialog component; Open uses Android's native file picker, not a custom browser
- **Component 4** — fully rewritten to match the finalized screens (Loading, Home, App Drawer, Editor + File Menu + Options Menu, Files List, dialogs) — replaces the old generic "Sidebar" task
- **Component 9** — Diff screen's empty state (identical versions) made explicit
- **Component 11** — new test case: only a file's very first version is ever a SNAPSHOT
- **Component 12** — Rollback now requires a confirmation step before it executes
- **Component 14** — Settings sections and controls finalized (live slider, dark theme only, simple on/off highlighting toggle)
- **Component 15** — added Full Screen, Hide Toolbar, and Share (confirmed final UI elements with no other home); File Statistics stays deferred

## Finalized screen & menu inventory

**Screens:** Loading → First Impression (Home) → Editor → Files List → Version History → Diff Comparison, plus Settings (reachable from two places).
**Overlays (not nav routes):** File Menu, Options Menu, App Drawer, Find & Replace bar, New File/Save As/Rename dialog, Delete confirmation, Rollback confirmation, Recovery dialog.
**No bottom tab bar anywhere.**

---

## COMPONENT 0 — FOUNDATION / PROJECT ARCHITECTURE

**Tasks in component**
- Create Android project
- Configure Kotlin
- Configure Gradle
- Establish package structure
- Establish UI/domain/storage/editor architecture
- Establish repository interfaces
- Establish basic data models — EditorFile, Version, Patch, DiffResult
- Establish navigation structure — **(finalized)** hub-and-spoke, no bottom tab bar: Loading → First Impression (Home) → {Editor, Files List, Settings via App Drawer}. Editor → Version History → Diff Comparison. File Menu, Options Menu, App Drawer, and every dialog (New File/Save As/Rename, Delete confirm, Rollback confirm, Recovery) are overlays/modals, not separate nav routes.
- Establish dependency strategy
- Create basic application shell

The final package structure is the destination; we don't implement every class yet.

**Test cases**

**TC0.1 — Build**
Open project. Build the application.
Expected: Build succeeds with no errors.

**TC0.2 — Launch**
Run the app.
Expected: app launches without crashing.

**TC0.3 — Navigation skeleton**
Navigate between the currently implemented placeholder screens.
Expected: navigation works.

**TC0.4 — Package structure**
Inspect Android Studio project.
Expected: planned packages exist where appropriate.

**TC0.5 — Clean build**
Clean project. Rebuild.
Expected: successful build.

✅ **Component passes when** the project is structurally ready and runs without errors.

---

## COMPONENT 1 — BASIC EDITOR ENGINE

This is our first real functional component. The roadmap correctly puts the editor before syntax highlighting, versioning, and databases.

**Tasks in component**
- Text editing surface
- Text buffer
- Cursor
- Cursor movement
- Text selection
- Insert text
- Delete text
- Cut
- Copy
- Paste
- Multiline editing
- Undo
- Redo
- Line handling

**Test cases**

**TC1.1 — Type text**
Tap editor. Type "Hello World".
Expected: text appears correctly.

**TC1.2 — Cursor**
Tap different locations in the text.
Expected: cursor moves to the selected location.

**TC1.3 — Insert**
Place cursor between two words. Type something.
Expected: text is inserted at cursor.

**TC1.4 — Delete**
Delete characters.
Expected: correct characters disappear.

**TC1.5 — Selection**
Select part of the text.
Expected: selected text is visually highlighted.

**TC1.6 — Copy/paste**
Select text → Copy. Move cursor → Paste.
Expected: copied text appears.

**TC1.7 — Cut**
Select text → Cut.
Expected: text disappears and can be pasted elsewhere.

**TC1.8 — Multiline**
Enter several lines.
Expected: lines remain correctly separated.

**TC1.9 — Undo**
Type something. Undo.
Expected: latest edit disappears.

**TC1.10 — Redo**
Undo an edit. Redo.
Expected: edit returns.

**TC1.11 — Multiple undo**
Make 5 different edits. Undo repeatedly.
Expected: edits reverse in correct order.

**TC1.12 — Multiple redo**
Undo several edits. Redo repeatedly.
Expected: edits return in correct order.

✅ **Component passes when** you can use it like a basic Notepad without files or syntax highlighting.

---

## COMPONENT 2 — EDITOR NAVIGATION & SEARCH

Search/replace and word wrapping are explicitly part of the required editor functionality. **(finalized)** The Find & Replace bar has dedicated Replace and Replace All buttons, not just input fields — and matching is plain text only, no regex mode.

**Tasks in component**
- Line handling
- Line numbers
- Word wrapping
- Search
- Find next
- Find previous
- Search & replace
- Replace all

**Test cases**

**TC2.1 — Line numbers**
Type 10 lines.
Expected: numbers 1–10 appear correctly.

**TC2.2 — Line creation**
Add/delete lines.
Expected: numbering updates correctly.

**TC2.3 — Word wrapping**
Enter a very long line. Enable wrapping.
Expected: text wraps instead of extending indefinitely.

**TC2.4 — Search**
Type repeated word "student". Search for "student".
Expected: matching occurrences are found.

**TC2.5 — Next/previous**
Search for repeated text. Tap next/previous.
Expected: selection moves between matches.

**TC2.6 — Replace**
Tap Replace on one occurrence.
Expected: only that occurrence changes.

**TC2.7 — Replace all**
Tap Replace All.
Expected: every matching occurrence changes.

**TC2.8 — No result**
Search for nonexistent text.
Expected: clear "not found" behavior; no crash.

---

## COMPONENT 3 — FILE SYSTEM

The roadmap's file component explicitly covers these operations.

**Tasks in component**
- New file — via Home's "New File" button or Editor's File Menu; opens a dialog with a filename field and a .kt / .md / .txt type selector
- Open file — via Home's "Open File" button or Editor's File Menu "Open"; uses Android's native system file picker (Storage Access Framework), not a custom in-app browser
- File reader
- Save — Editor top bar icon and File Menu item
- Save As — dialog, pre-filled with the current filename
- UTF-8 handling
- Recent files — Home screen preview list + full Files List screen
- Rename — dialog, pre-filled with the current filename; available from Editor's Options Menu and the Files List screen
- Delete — available from the Files List screen; **requires a confirmation dialog** ("Delete [filename]? This cannot be undone.") before removal
- File lifecycle management
- Read-only state — toggle in Editor's Options Menu, plus a "Read-only by Default" setting for newly created files

**Test cases**

**TC3.1 — New**
Tap New File.
Expected: dialog appears for filename + type; confirming opens an empty editor of that type.

**TC3.2 — Type + Save**
Create text. Save.
Expected: file is created.

**TC3.3 — Open**
Open saved file.
Expected: correct contents appear.

**TC3.4 — Save changes**
Open file. Modify it. Save. Close/reopen.
Expected: modifications remain.

**TC3.5 — Save As**
Open file → Save As → new filename.
Expected: new file is created.

**TC3.6 — Original file**
After Save As, inspect original.
Expected: original remains unchanged unless intentionally modified.

**TC3.7 — UTF-8**
Save text containing characters such as: é, こんにちは, 你好. Reopen.
Expected: characters remain correct.

**TC3.8 — Recent files**
Open several files. Return to Home, then open the Files List.
Expected: recently used files appear in both places.

**TC3.9 — Rename**
Rename a file via the dialog.
Expected: new name appears and file remains accessible.

**TC3.10 — Delete**
Delete a file from the Files List screen; confirm the deletion in the dialog that appears.
Expected: file disappears and cannot be reopened through the app.

**TC3.11 — Read-only**
Mark file read-only. Try typing.
Expected: modification is blocked.

---

## COMPONENT 4 — MAIN USER INTERFACE
*(fully rewritten to match the finalized Stitch UI)*

**Tasks in component**
- Loading screen
- First Impression (Home) screen — hero block, New File button, Open File button, Recent Files preview list, "View All" link, empty state for zero recent files
- App Drawer (opens from Home's hamburger icon) — logo badge, Settings, About, Privacy Policy, Contact Us, Exit
- Editor screen — top app bar (hamburger, file title, search, save, undo, redo, overflow), code area, line-number gutter, accessory toolbar (Kotlin symbol + keyword tokens), status bar (line/col, encoding, language, read-only badge)
- File Menu (opens from Editor's hamburger icon) — Open, New File, Open Folder*, Save, Save As, Rename, Settings
- Options Menu (opens from Editor's overflow icon) — Word Wrap, Full Screen, Undo, Redo, Read Only, Hide Toolbar, Go to Line, Share, Rename, Version History, Settings
- Find & Replace bar (collapsible, from Editor's search icon) — Find field, Replace field, Find next/previous, Replace, Replace All
- Files List screen (full Recent Files, reached via Home's "View All") — Rename and Delete actions per file
- Dialogs: New File (with type selector), Save As, Rename (one shared modal component), Delete confirmation

*\*Open Folder is present in the menu for design consistency but is not wired to any folder-browsing functionality — out of scope.*

**Test cases**

**TC4.1 — App launch**
Launch app.
Expected: Loading screen appears briefly, then the First Impression (Home) screen appears — not directly the Editor.

**TC4.2 — Open into Editor**
From Home, tap New File or an existing Recent Files entry.
Expected: Editor screen opens with the correct file loaded (or a blank buffer for New File), top bar showing the correct filename.

**TC4.3 — File title updates**
Open different files in sequence.
Expected: the filename shown in the Editor's top bar updates correctly each time.

**TC4.4 — Toolbar actions**
Tap every icon in the Editor's top bar and every button in the accessory toolbar.
Expected: each performs its intended action — Undo/Redo/Save/Search work; Kotlin token buttons insert the correct symbol/keyword at the cursor.

**TC4.5 — Recent Files (Home + Files List)**
Open several files, return to Home.
Expected: Home's Recent Files preview shows the most recent files; tapping "View All" opens the full Files List showing all of them.

**TC4.6 — File selection**
From Home or the Files List screen, select a file.
Expected: the selected file opens in the Editor.

**TC4.7 — Menus**
Open the File Menu, Options Menu, and App Drawer in turn.
Expected: each opens with the correct items, and every item performs its intended action or opens its intended screen/dialog.

**TC4.8 — Dialogs**
Trigger New File, Save As, Rename, and Delete.
Expected: each dialog opens with correct fields/pre-filled values; Cancel and confirm behave correctly. Delete specifically requires an explicit confirm step before the file is removed.

**TC4.9 — Back navigation**
Navigate into a screen or open a dialog/menu, then press Back.
Expected: correct previous screen/state appears; an open menu or dialog closes on Back rather than leaving the screen entirely.

✅ **Component passes when** the full flow (Loading → Home → Editor → History → Diff, plus every menu and dialog) works exactly as designed, matching the finalized Stitch screens.

---

## COMPONENT 5 — KOTLIN SYNTAX ENGINE

The roadmap's Kotlin pipeline is: Raw Text → KotlinLexer → Tokens → SyntaxHighlighter → Styled Text → Editor. The accessory toolbar's keyword tokens (`val`, `var`, `fun`, `if`, `else`, `for`, `while`, `return`, `class`) double as a quick manual test surface for keyword recognition.

**Tasks in component**
- Kotlin lexer
- Token recognition
- Kotlin keywords
- Strings
- Comments
- Annotations
- Syntax highlighter
- Connect highlighting to editor

**Test cases**

Create a test file containing:

```kotlin
class Student {
    private val name: String = "Yasuru"

    // Hello
    fun greet() {
        println("Hello")
    }
}
```

**TC5.1 — Keywords**
Check class, private, val, fun.
Expected: keyword styling appears.

**TC5.2 — Strings**
Check "Yasuru" and "Hello".
Expected: strings have correct styling.

**TC5.3 — Comments**
Add // comment.
Expected: comment styling appears.

**TC5.4 — Annotation**
Add @Override or another annotation.
Expected: annotation is recognized/styled.

**TC5.5 — Normal identifiers**
Check Student, name, greet.
Expected: identifiers aren't incorrectly classified as keywords.

**TC5.6 — Editing highlighted text**
Add/delete characters inside highlighted code.
Expected: highlighting updates.

**TC5.7 — Large code block**
Paste a reasonably large Kotlin file.
Expected: no crash and acceptable responsiveness.

---

## COMPONENT 6 — MARKDOWN ENGINE

Markdown highlighting is required; preview is optional.

**Tasks in component**

Required:
- Markdown lexer
- Markdown syntax highlighting
- Markdown file recognition

Optional:
- Markdown parser
- Markdown renderer
- Preview

**Test cases**

**TC6.1 — Heading**
`# Hello`
Expected: heading syntax is highlighted.

**TC6.2 — Bold**
`**Hello**`
Expected: bold syntax is recognized.

**TC6.3 — Italic**
`*Hello*`
Expected: italic syntax is recognized.

**TC6.4 — Lists**
`- Apple` / `- Orange`
Expected: list syntax is recognized.

**TC6.5 — Code**
`` `println()` ``
Expected: inline code is recognized.

**TC6.6 — Markdown file**
Open .md file.
Expected: Markdown highlighting activates.

**TC6.7 — Preview (if implemented)**
Open Markdown preview.
Expected: rendered output corresponds to source.

---

## COMPONENT 7 — CRASH RECOVERY

The intended flow is buffer → RecoveryManager → temporary recovery data → startup detection → restore. **(finalized)** Recovery dialog is a two-button modal (Discard / Restore) with no close/X button — the user must choose one.

**Tasks in component**
- RecoveryManager
- Temporary recovery storage
- Periodic caching
- Recovery detection
- Recovery dialog
- Restore recovered content
- Recovery cleanup

**Test cases**

**TC7.1 — Recovery creation**
Open/create a file. Type significant text. Wait for recovery interval.
Expected: recovery data is created.

**TC7.2 — Simulated interruption**
Enter text. Force-close the app/process in an appropriate test environment. Reopen.
Expected: recovery is detected.

**TC7.3 — Restore**
Choose Restore.
Expected: previous unsaved content returns.

**TC7.4 — Discard**
Trigger recovery. Choose Discard.
Expected: recovered content is not restored.

**TC7.5 — Normal save**
Save file normally. Restart app.
Expected: no unnecessary recovery prompt for already-saved content.

**TC7.6 — Recovery cleanup**
Restore/discard recovery. Restart again.
Expected: stale recovery data doesn't repeatedly appear.

---

## COMPONENT 8 — DATABASE / PERSISTENCE *(reordered — was Component 10)*

Moved ahead of Version Control and Delta/Patch because both of those need somewhere to actually store data before their own tests can pass.

**Tasks in component**
- Room/SQLite setup
- AppDatabase
- FileEntity
- VersionEntity
- DeltaEntity
- FileDao
- VersionDao
- DeltaDao
- Connect persistence to repositories

**Test cases**

**TC8.1 — Database creation**
Launch app.
Expected: database initializes without crash.

**TC8.2 — File persistence**
Save/open file. Restart app.
Expected: metadata remains.

**TC8.3 — Version persistence**
Create versions. Restart.
Expected: versions remain.

**TC8.4 — Delta persistence**
Create multiple changed versions. Restart.
Expected: versions can still be reconstructed.

**TC8.5 — Delete**
Delete file.
Expected: associated records behave according to the designed lifecycle rules.

**TC8.6 — Repeated restart**
Create data → force close → reopen several times.
Expected: database remains consistent.

---

## COMPONENT 9 — DIFF ENGINE *(reordered — was Component 11)*

Moved ahead of Delta/Patch because generating a patch requires a diff engine to exist first. **(finalized)** Diff Comparison screen uses dual old/new line-number columns and subtle tinted (not solid) add/remove backgrounds.

**Tasks in component**
- DiffEngine
- Generate differences
- Patch integration
- DiffResult
- Line-by-line comparison
- Diff visualization — dual old/new line-number columns, tinted add/remove line colors, empty state ("No differences between vX and vY") when two versions are identical

**Test cases**

**TC9.1 — Added line**
V1 has 3 lines. V2 has 4.
Expected: added line is shown.

**TC9.2 — Removed line**
Delete a line.
Expected: deletion is shown.

**TC9.3 — Modified line**
Change one line.
Expected: modification is shown (as a removed/added pair).

**TC9.4 — Multiple changes**
Add, delete and modify lines.
Expected: all differences are displayed.

**TC9.5 — Identical versions**
Compare identical versions.
Expected: no line-by-line diff is shown; the screen instead displays a clear "No differences" empty state.

**TC9.6 — Long file**
Compare larger versions.
Expected: diff content scrolls independently; screen remains usable.

---

## COMPONENT 10 — INCREMENTAL DELTA / PATCH SYSTEM *(reordered — was Component 9)*

Now buildable, since Database (Component 8) and Diff Engine (Component 9) both already exist.

**Tasks in component**
- Patch model
- Delta generation
- Compare previous/current versions
- Store only changes where appropriate
- Reconstruct later versions
- Connect to VersionManager

**Test cases**

**TC10.1 — Initial version**
Create first version.
Expected: initial snapshot is stored correctly.

**TC10.2 — Small change**
Change one line. Create second version.
Expected: delta represents that change.

**TC10.3 — Multiple changes**
Change several lines. Create version.
Expected: all changes are represented.

**TC10.4 — Reconstruction**
Create V1 → V2 → V3. Reconstruct V2.
Expected: exact V2 contents return.

**TC10.5 — No unnecessary duplication**
Create versions with small changes. Verify persistence behavior.
Expected: later versions use delta/patch information rather than blindly duplicating the entire file.

**TC10.6 — Chain reconstruction**
Create 5+ versions. Reconstruct the oldest, middle and newest versions.
Expected: all contents are correct.

---

## COMPONENT 11 — VERSION CONTROL FOUNDATION *(reordered — was Component 8)*

Important: Do NOT confuse this with Undo/Redo. Undo/Redo is session history; version control is persistent document history.

**Tasks in component**
- Version model
- VersionManager
- SnapshotManager
- Snapshot creation
- Version metadata
- Version numbering/naming
- Connect version lifecycle to the editor

**Test cases**

**TC11.1 — Create version**
Open file. Make a meaningful change. Create version.
Expected: version is recorded.

**TC11.2 — Multiple versions**
Create Version 1. Modify. Create Version 2. Modify. Create Version 3.
Expected: all three are distinguishable.

**TC11.3 — Version metadata**
Inspect history.
Expected: correct version information is shown.

**TC11.4 — Undo independence**
Make an edit. Undo. Check version history.
Expected: Undo does not behave as persistent version deletion.

**TC11.5 — Persistence**
Close/reopen app.
Expected: saved version history remains.

**TC11.6 — Snapshot vs. delta labeling** *(new)*
Create several versions of a file, including across separate app sessions.
Expected: only the very first version ever created for that file is labeled SNAPSHOT; every version after it, no matter how much later, is labeled DELTA. A file can only ever have exactly one SNAPSHOT.

---

## COMPONENT 12 — HISTORY & ROLLBACK

The intended flow is version selection → reconstruction from patches → editor buffer. **(finalized)** Rollback requires an explicit confirmation step before it executes.

**Tasks in component**
- History screen
- Version list
- Version selection
- Version reconstruction
- Diff screen integration
- Restore
- RollbackManager
- Rollback confirmation dialog — "Roll back to vX? Any unsaved changes in the current file will be lost." with Cancel / Roll Back buttons; triggered from the Diff Comparison screen's Rollback button

**Test cases**

**TC12.1 — History**
Create 3 versions. Open History.
Expected: all versions appear.

**TC12.2 — Select version**
Tap Version 2.
Expected: Version 2 details/content are shown.

**TC12.3 — View diff**
Select two versions. Open diff.
Expected: correct differences appear.

**TC12.4 — Restore old version**
Select Version 1. Restore, confirming the dialog.
Expected: editor contains exact Version 1 content.

**TC12.5 — Restore middle version**
Restore Version 2 from V1 → V2 → V3.
Expected: exact V2 content returns.

**TC12.6 — Restore latest**
Restore latest version.
Expected: current content matches latest version.

**TC12.7 — Post-rollback behavior**
Restore old version. Make a new edit.
Expected: editing works normally afterward.

**TC12.8 — Persistence**
Roll back. Close/reopen.
Expected: resulting state/history remains consistent.

**TC12.9 — Rollback confirmation** *(new)*
Tap "Rollback to vX" on the Diff Comparison screen.
Expected: a confirmation dialog appears before anything changes. Tapping Cancel leaves the editor untouched; tapping Roll Back performs the restore as in TC12.4–12.6.

---

## COMPONENT 13 — REPOSITORY LAYER

Architecture: UI → ViewModel → Repository → Storage.

Note: because this layer was required by Component 0's architecture, Components 3 and 8–12 have already been exercising it indirectly. Treat these as confirmation/regression tests, not first-time tests.

**Tasks in component**
- FileRepository
- VersionRepository
- SettingsRepository
- Connect ViewModels/business logic to repositories
- Hide storage implementation from UI

**Test cases**

**TC13.1 — File operation**
Create/save/open through UI.
Expected: works without UI directly depending on storage implementation.

**TC13.2 — Version operation**
Create/view versions.
Expected: history works through VersionRepository.

**TC13.3 — Restart**
Restart app.
Expected: repository retrieves persisted data correctly.

**TC13.4 — Error handling**
Attempt invalid/missing file operation.
Expected: user gets a controlled error instead of app crash.

---

## COMPONENT 14 — SETTINGS
*(sections and controls finalized)*

Reached from two places: Home's App Drawer, and Editor's Options Menu — both open the same screen.

**Tasks in component**
- Settings screen
- Settings model
- SettingsRepository
- DataStore persistence
- Section: Editor Preferences — Font Size (interactive slider, live value display), Tab Size (dropdown), Word Wrap (toggle), Line Numbers (toggle), Highlight Current Line (toggle)
- Section: Appearance — Syntax Highlighting (simple on/off toggle); app chrome is dark-only
- Section: System & Recovery — Auto-save Interval (dropdown: 5s/10s/30s/1m — feeds Component 7's caching interval), Read-only by Default (toggle — sets the default for newly created files, ties to Component 3)

**Test cases**

**TC14.1 — Font size**
Drag the Font Size slider. Return to editor.
Expected: font size changes live, matching the slider's value.

**TC14.2 — Persistence**
Change font size. Close/reopen app.
Expected: setting remains.

**TC14.3 — Word wrap**
Toggle word wrap.
Expected: editor behavior changes.

**TC14.4 — Line numbers**
Toggle line numbers.
Expected: line numbers appear/disappear.

**TC14.6 — Syntax highlighting**
Disable/enable highlighting.
Expected: highlighting responds accordingly.

**TC14.7 — Multiple settings**
Change several settings. Restart app.
Expected: all settings persist.

---

## COMPONENT 15 — OPTIONAL / SMART FEATURES

Only after all required functionality is stable.

**Tasks in component**
- Auto indentation
- Bracket matching
- Auto-closing brackets
- Go to line
- Current-line highlighting
- Code folding
- Advanced Kotlin highlighting
- Kotlin formatting
- Markdown preview
- Themes
- Custom fonts
- Better dialogs
- Better empty states
- File statistics — not present in the current Options Menu design; kept here as deferred/optional
- Full Screen mode — hides system chrome for a distraction-free view *(new)*
- Hide Toolbar — hides the accessory toolbar *(new)*
- Share — native Android share sheet for the current file's content *(new)*

**Test cases**

For each optional feature:

**TC15.X.1 — Normal operation**
Use feature normally.
Expected: correct result.

**TC15.X.2 — Boundary case**
Use feature with empty/minimal input.
Expected: no crash.

**TC15.X.3 — Interaction**
Use feature while editing/saving/searching.
Expected: doesn't break existing functionality.

**TC15.X.4 — Persistence**
If the feature has a setting, restart app.
Expected: setting remains.

**TC15.X.5 — Regression**
Test basic typing, save, open, undo/redo after adding feature.
Expected: existing functionality still works.

---

## COMPONENT 16 — FINAL TESTING & POLISH

This becomes our full regression test.

**Tasks in component**
- Editor testing
- File testing
- Syntax testing
- Recovery testing
- Version testing
- Diff testing
- Rollback testing
- Database testing
- UI testing — verify final build matches the finalized Stitch screens exactly (colors, menu contents, dialog behavior)
- Error handling
- Performance
- Large files
- Corrupted recovery data
- App restart
- Storage failures
- Version reconstruction
- UI polish
- APK build

**Test cases**

**TC16.1 — Fresh installation**
Install fresh APK. Launch.
Expected: app works.

**TC16.2 — Complete editor workflow**
New → type → edit → undo → redo → save.
Expected: everything works.

**TC16.3 — File workflow**
Save → close → reopen → modify → Save As.
Expected: correct files/content.

**TC16.4 — Syntax workflow**
Open .kt. Edit.
Expected: highlighting remains correct.

**TC16.5 — Markdown workflow**
Open .md. Edit.
Expected: Markdown highlighting works.

**TC16.6 — Recovery workflow**
Type → interrupt app → reopen → recover.
Expected: recovery works.

**TC16.7 — Version workflow**
Create multiple versions.
Expected: history persists.

**TC16.8 — Diff workflow**
Compare versions.
Expected: correct changes.

**TC16.9 — Rollback workflow**
Restore an old version, confirming the dialog.
Expected: exact content restored.

**TC16.10 — Settings workflow**
Change settings → restart.
Expected: settings persist.

**TC16.11 — Large file**
Open a reasonably large text/code file.
Expected: no crash and acceptable responsiveness.

**TC16.12 — Full regression**
Run the major workflows again after all optional features.
Expected: no previously working feature is broken.