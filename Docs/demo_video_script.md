# Kotlin++ Lite — Demonstration Video Script

**Course:** IS2205 Mobile Application Design and Development — Mini-Project  
**App:** Kotlin++ Lite (Modern Mobile Text Editor with Incremental Version Control)  
**Members:** Yasuru Sandeepa (Speaker 1) · Induwara Uthsara (Speaker 2)  
**Length:** 22–24 minutes (hard cap: 25 minutes)  
**Format:** Both members on camera for intro and closing. One member operates the phone/emulator while speaking; the other stays visible at the side of the frame.

---

## How to record

- Record landscape or portrait consistently. Landscape is easier for two people plus a phone.
- Keep the phone screen filling most of the frame. Faces can sit in a small corner or at the side.
- Speak slowly. Pause 1–2 seconds after each tap so the marker can see the result.
- If you overrun, cut the “architecture mention” lines first, then shorten Settings. Never skip Version History, Diff, Rollback, Crash Recovery, Kotlin highlighting, or Find & Replace — those are the spec’s signature items.
- Do **not** mention a third member. This video has two presenters only.

---

## Before you press record (prep, ~10 minutes)

Do this **off camera** so the live demo stays clean.

1. Install a **fresh** build of Kotlin++ Lite on one Android device or emulator. Uninstall any old copy first so Recent Files starts empty.
2. Grant file access when Android asks. Do not skip the permission dialog during the video — show it once, then confirm.
3. Have a second device (or the same Files app) ready with these three files already on storage, for the **Open File** moment:

**`notes.txt`**
```
Hello World
student
student
student
café — こんにちは — 你好
```

**`Student.kt`** (backup; you will type a similar file live)
```kotlin
class Student {
    private val name: String = "Yasuru"

    // Hello
    fun greet() {
        println("Hello")
    }
}
```

**`readme.md`**
```
# Kotlin++ Lite

A **mobile** editor for *Kotlin* and Markdown.

- Apple
- Orange

Inline code: `println("Hello")`

> Notes stay on this device.

[Project](https://github.com)
```

4. Know how you will **force-stop** the app for crash recovery:
   - Recents → swipe Kotlin++ Lite away, **or**
   - Settings → Apps → Kotlin++ Lite → Force stop.
   Do **not** tap Save before that demo.
5. Default auto-save interval is **10 seconds**. In the recovery scene you will change it to **5s** first.
6. Print this script or keep it on a laptop next to the camera. Speaker 1 reads the left column; Speaker 2 reads the right. The **SHOW** lines are what the operator taps.

---

## Runtime map

| Time | Scene | Speaker | Spec bucket |
|---|---|---|---|
| 0:00–2:00 | Introductions and contributions | Both | — |
| 2:00–3:20 | Launch, Home, App Drawer | Yasuru | Required UI |
| 3:20–7:10 | Editor engine (type, select, clipboard, undo) | Yasuru | Required editor |
| 7:10–9:20 | Line numbers, wrap, Find & Replace | Yasuru | Required editor |
| 9:20–12:20 | Files: New / Save / Open / Save As / UTF-8 / rename / delete / recent | Yasuru | Required files |
| 12:20–14:20 | Kotlin + Markdown highlighting | Yasuru | Required syntax |
| 14:20–14:40 | Handoff | Both | — |
| 14:40–16:50 | Settings + Crash Recovery | Induwara | Required recovery + extra settings |
| 16:50–20:20 | Versions, delta storage, Diff, Rollback | Induwara | Required version control |
| 20:20–22:50 | Additional / smart features | Induwara | Additional |
| 22:50–24:20 | Closing recap | Both | — |

---

# SCENE 0 — Introductions (0:00–2:00)

**SHOW:** Title card on screen, or the Home screen frozen in the background. Both faces visible. Do not tap the app yet.

### Speaker 1 — Yasuru

> Hello. We are presenting **Kotlin++ Lite**, our IS2205 mini-project: a native Android text editor for Kotlin and Markdown, with local files, crash recovery, and incremental version control.
>
> My name is **Yasuru Sandeepa**. I built the application foundation and the editor the user actually types in.
>
> My contribution covers:
> - Component 0 — project architecture, navigation, and the repository interfaces
> - Component 1 — the basic editor engine: buffer, cursor, selection, insert, delete, cut, copy, paste, undo and redo
> - Component 2 — line numbers, word wrap, find, find next and previous, replace, and replace all
> - Component 3 — the file system: new, open, save, save as, rename, delete, recent files, UTF-8, and read-only
> - Component 4 — the main UI: loading screen, Home, editor chrome, File Menu, Options Menu, App Drawer, and dialogs
> - Component 5 — the Kotlin lexer and syntax highlighter
> - Component 6 — the Markdown lexer and highlighting
>
> I will demonstrate all of those required editor, file, UI, and syntax features first.

### Speaker 2 — Induwara

> My name is **Induwara Uthsara**. I built the persistence, recovery, and version-control side of the app, plus settings and the extra editor features.
>
> My contribution covers:
> - Component 7 — crash recovery
> - Component 8 — Room database for files, versions, and deltas
> - Component 9 — the diff engine
> - Component 10 — incremental delta and patch storage — later versions store only the change, not a full copy of the file
> - Component 11 — version control foundation: snapshots, deltas, and metadata
> - Component 12 — history screen, reconstruction, and rollback with confirmation
> - Component 13 — the repository layer that hides storage from the UI
> - Component 14 — Settings, persisted with DataStore
> - Component 15 — optional features: auto-indent, bracket matching, auto-closing brackets, go to line, current-line highlight, full screen, hide toolbar, share, and Markdown preview
> - Component 16 — final testing and polish
>
> After Yasuru finishes the editor and files, I will demonstrate recovery, version history, diff, rollback, settings, and the additional features.
>
> The video stays under twenty-five minutes. We will label **required spec functions** first, then **additional features**.

---

# PART A — REQUIRED FEATURES (from IS-Mini-Pro-v1)

Yasuru operates the phone unless a SHOW line says otherwise.

---

## SCENE 1 — Launch, Home, App Drawer (2:00–3:20)

**Component:** 0 (navigation), 4 (UI)

**SHOW:** Cold-start the app from the launcher icon. Do not skip the splash.

### Speaker 1 — Yasuru

> We launch from a cold start. The spec requires a complete application, not an editor-only activity, so the first screen is our loading splash — **Kotlin++ Lite**, tagline “Code with gecko,” then the Home screen. We never drop the user straight into the editor.

**SHOW:** Hold on the splash for the full ~2 seconds. Home appears: logo, “Code Kotlin, anywhere.”, **New File**, **Open File**, empty **RECENT FILES** (“No recent files yet”).

> Home is the hub. New File and Open File are the two primary actions. Recent Files is empty on a fresh install — that empty state is intentional. There is no bottom tab bar. Navigation is hub-and-spoke: Home to Editor, Files List, or Settings.

**SHOW:** Tap the hamburger (top-left) to open the **App Drawer**. Slowly scroll: logo badge, **Settings**, **About**, **Privacy Policy**, **Help**, **Exit**.

> The App Drawer is reached from Home. Settings is one of two entries into the same Settings screen — Induwara will open Settings later. I will show About so you can see the product identity.

**SHOW:** Tap **About**. Hold on the dialog: Kotlin++ Lite, version 1.0.0, both names. Tap **Close**.

**SHOW:** Tap **Privacy Policy**. Scroll once. Say the next line, then Close.

> Privacy Policy states that files stay on-device, there is no telemetry, and recovery data never leaves the phone.

**SHOW:** Tap **Help**, show the short help text, Close. Tap outside the drawer (or Back) to dismiss it. Stay on Home.

> Exit would close the app, so we will not tap it during the demo.

---

## SCENE 2 — New File and the editor chrome (3:20–4:20)

**Component:** 3 (New File), 4 (Editor UI)

**SHOW:** On Home, tap **New File**.

### Speaker 1 — Yasuru

> New File opens a shared filename dialog. The same dialog component is reused for Save As and Rename. For New File only, we also pick a type: Kotlin, Markdown, or plain text.

**SHOW:** Name field → type `Student`. Tap **.kt / Kotlin**. Tap **Create**.

Editor opens. Point the camera at, in this order:

1. Top bar: hamburger, title `Student.kt`, search, save, undo, redo, overflow
2. Line-number gutter on the left
3. Code area
4. Accessory toolbar (`val`, `var`, `fun`, `if`, `else`, `for`, `while`, `return`, `class`, then symbols)
5. Status bar: `Line 1, Col 1` · `UTF-8` · `Kotlin`

> The editor is a single-file workspace. The title shows the current filename. The status bar reports cursor position, encoding, and language. UTF-8 is the encoding the spec requires, and it is always shown here.

---

## SCENE 3 — Basic editor engine (4:20–7:10)

**Component:** 1  
**Required:** type, cursor, insert, delete, selection, copy, paste, cut, multiline, undo, redo

**SHOW:** Tap the code area. Type exactly (use Enter for new lines):

```
Hello World
```

### Speaker 1 — Yasuru

> Typing goes into an in-memory text buffer. That is Component 1 — a working notepad before files or highlighting.

**SHOW:** Tap between `Hello` and `World`. Type ` Kotlin `. Result: `Hello Kotlin World`.

> Insert happens at the cursor, not at the end of the file.

**SHOW:** Backspace a few characters, then retype so the line is again `Hello World`.

> Delete removes the correct characters. The status bar line and column update as the cursor moves.

**SHOW:** Long-press `Hello` → Select → **Copy**. Tap after `World`, type a space, long-press → **Paste**. Line should read `Hello World Hello`.

> Copy and paste use the system clipboard.

**SHOW:** Select the second `Hello` → **Cut**. It disappears. Tap the end of the line → **Paste** it back.

> Cut removes the selection and still allows paste.

**SHOW:** Press Enter a few times and type:

```
Line two
Line three
Line four
```

> Multiline editing keeps lines separated. Gutter numbers should now show 1, 2, 3, 4, …

**SHOW:** Tap the **Undo** icon in the top bar several times. Then tap **Redo** several times.

> Undo and redo are a **session stack**. They are not version control. Undoing does not delete a saved version. Induwara will show the persistent history later. Multiple undos reverse in order; multiple redos restore in order.

**SHOW:** Leave a few lines of text in the buffer. Do not save yet.

---

## SCENE 4 — Line numbers, word wrap, Find & Replace (7:10–9:20)

**Component:** 2  
**Required:** line numbers, wrap, search, next/previous, replace, replace all  
**Note:** matching is plain text only — no regex.

**SHOW:** Type extra lines until at least 10 lines exist, or paste a block. Point at the gutter: 1 through 10.

### Speaker 1 — Yasuru

> Line numbers update when we add or delete lines. That is required editor navigation.

**SHOW:** On one line, type a very long sentence with no Enter, for example:  
`This is a very long line that should wrap across the screen instead of scrolling sideways forever when word wrap is on.`

> Word wrap is on by default. The long line wraps inside the view instead of running off the screen.

**SHOW:** Tap overflow (⋮) → **Word Wrap** so it shows **OFF**. The long line should now scroll horizontally. Toggle **Word Wrap** back **ON**. Dismiss the menu.

> Wrap can also be toggled from Settings. Same setting, two places.

**SHOW:** Replace the buffer content (select all → delete) and type:

```
student
student
student
teacher
```

Tap the **search** icon in the top bar. The Find & Replace bar opens.

> Find and Replace is a collapsible bar, not a separate screen. Matching is plain text, not regex.

**SHOW:** Find field → `student`. Counter should show `1/3`. Tap **down** (next), then **up** (previous). Matches highlight in the editor.

> Next and previous move between occurrences. The counter is `current / total`.

**SHOW:** Replace field → `learner`. Tap **Replace** once. Only the current `student` becomes `learner`. Counter updates.

> Replace changes one match.

**SHOW:** Tap **Replace All**. Remaining `student` matches become `learner`. `teacher` is unchanged.

> Replace All changes every match and leaves non-matching text alone.

**SHOW:** Find field → `zzzz`. Counter shows **No matches**. Close the find bar with X.

> A failed search is a clear empty result. The app does not crash.

---

## SCENE 5 — Save, UTF-8, Open, Save As, Recent Files, Rename, Delete, Read-only (9:20–12:20)

**Component:** 3, 4  
**Required:** New (already shown), Open, Save, Save As, Recent Files, UTF-8, Rename, Delete, Read-only

### 5a — Save + UTF-8

**SHOW:** Select all, delete, type:

```
Hello World
café
こんにちは
你好
```

Tap the **Save** (disk) icon in the top bar. If Android asks where to save, choose Downloads or Documents, keep the name `Student.kt`, confirm.

### Speaker 1 — Yasuru

> Save writes the buffer to device storage through the system document picker. The file is UTF-8, so Latin, Japanese, and Chinese characters round-trip correctly. We will prove that by reopening in a moment.

### 5b — File Menu

**SHOW:** Tap the editor hamburger. File Menu slides in from the left. Point at every row: Open, New File, Open Folder (greyed out), Save, Save As, Rename, Settings.

> The File Menu is an overlay, not a new route. **Open Folder** is in the design for consistency but is intentionally disabled — folder browsing is out of scope. Everything else is wired.

**SHOW:** Tap outside to close. Do not tap Open Folder.

### 5c — Save As (original stays unchanged)

**SHOW:** Change the first line to `Hello Kotlin++`. Tap hamburger → **Save As**. Dialog is pre-filled with the current name. Change it to `StudentCopy.kt`. Confirm and pick a location.

> Save As creates a **new** file. The original `Student.kt` on disk still has the old first line unless we saved over it. That is the spec’s “original file remains unchanged” case.

### 5d — Back to Home, Recent Files, Open

**SHOW:** Tap the editor Back (or system Back). If **Unsaved changes** appears, tap **Save**. Land on Home.

Home Recent Files should now list `Student.kt` / `StudentCopy.kt`.

> Recent Files appear on Home as a preview of the last files we used. Tapping an entry opens that file in the editor.

**SHOW:** Tap `Student.kt` on Home. Contents should include the UTF-8 characters. Point at `café`, `こんにちは`, `你好`.

> Reopening proves UTF-8 persistence.

**SHOW:** Back to Home. Tap **Open File**. Android’s **system file picker** appears (not a custom browser). Open `notes.txt` from storage.

> Open uses the Storage Access Framework, as specified — we did not build an in-app file browser.

### 5e — Files List: Rename and Delete

**SHOW:** Back to Home. Tap **VIEW ALL**. Full **Recent Files** list. Point at Rename and Delete on a row.

> The Files List is the full recent-files screen. Rename and Delete live here. Delete always asks for confirmation.

**SHOW:** On `StudentCopy.kt` (or `notes.txt` if you prefer to keep the Kotlin file), tap **Rename**. Dialog is pre-filled. Change to `StudentCopy2.kt`. Confirm. The list updates.

> Rename keeps the file accessible under the new name. The editor title updates if that file is open.

**SHOW:** Tap **Delete** on the renamed copy. Dialog: `Delete [filename]? This cannot be undone.` Tap **Cancel** first.

> Cancel leaves the file. The spec requires an explicit confirm step.

**SHOW:** Delete again → tap the confirm button. The file disappears from the list.

> After confirm, the file is gone from the app’s recent list and cannot be reopened from here.

**SHOW:** Tap Back to Home, then open `Student.kt` again from Recent Files.

### 5f — Read-only

**SHOW:** Overflow (⋮) → toggle **Read Only** to **ON**. Status bar shows **READ-ONLY** badge. Try to type — nothing should be inserted.

### Speaker 1 — Yasuru

> Read-only is a required safety lock. Typing is blocked. The badge in the status bar makes the state visible.

**SHOW:** Toggle **Read Only** back **OFF**. Typing works again.

---

## SCENE 6 — Kotlin syntax highlighting (12:20–13:30)

**Component:** 5  
**Required:** keywords, strings, comments, annotations, identifiers not mis-tagged, highlighting updates while editing

**SHOW:** Select all in `Student.kt`, delete, then either paste or type using the accessory toolbar for keywords:

```kotlin
@Deprecated
class Student {
    private val name: String = "Yasuru"

    // Hello
    fun greet() {
        println("Hello")
    }
}
```

Tap accessory chips `class`, `val`, `fun` rather than typing those words, so the toolbar is demonstrated.

### Speaker 1 — Yasuru

> This is the required Kotlin pipeline: raw text, lexer, tokens, highlighter, styled editor. Keywords such as `class`, `private`, `val`, and `fun` use keyword colour. Strings such as `"Yasuru"` and `"Hello"` use string colour. `// Hello` is a comment. `@Deprecated` is an annotation. Identifiers — `Student`, `name`, `greet` — stay as normal identifiers, not keywords.

**SHOW:** Point at each token type as you name it. Then insert a character inside `greet` and delete it.

> Highlighting recalculates as we edit. The accessory toolbar is also a manual test surface for keyword recognition.

---

## SCENE 7 — Markdown highlighting (13:30–14:20)

**Component:** 6  
**Required:** Markdown highlighting and `.md` recognition. Preview is optional — Induwara will show preview in Part B.

**SHOW:** Back to Home (save if prompted). Tap **New File**. Name `notes`. Type **.md / Markdown**. Create.

Type:

```
# Hello

**bold text**
*italic text*

- Apple
- Orange

Inline: `println()`
```

### Speaker 1 — Yasuru

> Opening a `.md` file activates the Markdown highlighter. Headings, bold markers, italic markers, list markers, and inline code are recognized. Preview is **not** required by the spec; it is an additional feature Induwara will show later. Status bar language should read **Markdown**.

**SHOW:** Point at heading colour, bold/italic rendering, list markers, and inline code. Save the file.

---

## SCENE 8 — Handoff (14:20–14:40)

**SHOW:** Stay on the Markdown editor, or go to Home. Both faces visible for a few seconds.

### Speaker 1 — Yasuru

> That completes the required editor, file, UI, Kotlin, and Markdown features — Components 0 through 6. I now hand over to Induwara for recovery, the database, version control, diff, rollback, settings, and the additional features.

### Speaker 2 — Induwara

> Thank you. I will start with Settings and crash recovery, then the signature feature of the project: incremental versions, diff, and rollback.

---

## SCENE 9 — Settings (14:40–15:50)

**Component:** 14 (supporting; persisted customization)  
**SHOW:** Home hamburger → **Settings**. Scroll the whole screen once before changing anything.

### Speaker 2 — Induwara

> Settings is one screen, reached from the Home drawer and from the editor Options Menu. Values persist in DataStore, so they survive an app restart.
>
> Three sections:
> - **Editor Preferences** — font size slider with a live pixel value, tab size 2 / 4 / 8, word wrap, line numbers, highlight current line
> - **Appearance** — syntax highlighting on or off. The chrome is dark-only; we did not add a second theme
> - **System & Recovery** — auto-save interval 5 seconds, 10 seconds, 30 seconds, or 1 minute, which feeds crash recovery; and read-only by default for newly created files

**SHOW:** Drag **Font Size** and say the number out loud. Change **Tab Size** to 4 spaces if it is not already. Leave wrap and line numbers **ON**. Leave **Highlight Current Line** **ON**. Leave **Syntax Highlighting** **ON**.

**SHOW:** Set **Auto-save Interval** to **5s**. Leave **Read-only by Default** off.

> I set recovery to five seconds so the next demo is short. We will come back and prove font size on the editor in a moment.

**SHOW:** Back. Open `Student.kt` from Home. Point at the larger (or changed) font. Point at the current-line highlight behind the cursor.

> Font size and current-line highlight apply immediately in the editor.

---

## SCENE 10 — Crash recovery (15:50–16:50)

**Component:** 7  
**Required:** periodic cache, detect after interruption, Restore / Discard, no prompt after a normal save, cleanup

**SHOW:** In `Student.kt`, go to the bottom and type a unique unsaved line:

```
RECOVERY_DEMO_LINE
```

Do **not** tap Save. Wait at least 6 seconds (interval is 5s). Say the next line while waiting.

### Speaker 2 — Induwara

> RecoveryManager copies the active buffer to app-private storage on the auto-save interval. That is the spec’s background cache. I have not saved the file. After five seconds the recovery record exists. I will now force-stop the app, which is the crash or unexpected interruption case.

**SHOW:** Force-stop (Recents swipe away, or Force stop). Relaunch from the launcher.

Splash → Home → **Crash Recovery** dialog: “Unsaved changes were found for Student.kt. Would you like to restore them?” Two buttons only: **Restore** and **Discard**. No X. You must choose.

> The dialog has no close button. The user must Restore or Discard.

**SHOW:** Tap **Restore**. Editor opens with `RECOVERY_DEMO_LINE` present.

> Restore puts the unsaved buffer back.

**SHOW:** Now demonstrate Discard on a second pass, quickly: type `DISCARD_ME`, wait 6 seconds, force-stop, relaunch, tap **Discard**. That extra line must **not** come back.

> Discard throws the recovery data away. After either choice, a second restart does not show a stale prompt — recovery is cleaned up. If we Save normally, the next launch also stays silent.

**SHOW:** Open `Student.kt`, tap Save once, so the next scenes start from a saved file.

---

## SCENE 11 — Version control, delta storage, history (16:50–18:40)

**Components:** 8, 10, 11, 12 (create + list)  
**Required:** persistent versions, first version is SNAPSHOT, later versions are DELTA, undo is independent, metadata

**SHOW:** Editor still on `Student.kt`. Make sure the Kotlin sample (class Student…) is the content. Overflow (⋮) → **Version History**.

Empty state: “No versions yet. Tap save to create one.”

### Speaker 2 — Induwara

> Version History is persistent document history. It is not undo. Undo dies when the session ends. Versions survive closing the app because they live in Room — files, versions, and deltas tables — behind the VersionRepository. The UI never talks to a DAO directly. That is Component 13.

**SHOW:** Tap the **Save** (disk) icon in the Version History top bar. Label: `Initial snapshot`. Tap **Save**.

Card appears: **v1** badge **SNAPSHOT**, timestamp, label `Initial snapshot`, buttons **View Diff** and **Restore**.

> The first version of a file is always a **SNAPSHOT** — a full copy of the text. A file has exactly one snapshot. Every version after that is a **DELTA**: we store only the patch, not another full file. That is the spec’s incremental, non-duplicating version control.

**SHOW:** Back to the editor. Change `"Yasuru"` to `"Induwara"`. Add a line `    fun id(): String = name`. Tap overflow → Version History → Save Version. Label: `Renamed student`. Save.

Card **v2** badge **DELTA**.

> v2 is labeled DELTA. The database stores a patch from v1 to v2 generated by the diff engine.

**SHOW:** Back to editor. Change `Hello` in the println to `Welcome`. Version History → Save Version. Label: `Greeting change`. Save.

Now three cards: v1 SNAPSHOT, v2 DELTA, v3 DELTA.

> Three distinguishable versions, each with number, type, time, and optional label. If I undo a few keystrokes in the editor, these cards stay. Undo cannot delete a version.

---

## SCENE 12 — Diff comparison (18:40–19:30)

**Component:** 9, 12  
**Required:** line-by-line added / removed / modified, empty state when identical, dual line numbers

**SHOW:** On the **v1** card, tap **View Diff**.

Header: `Comparing v1 → current` and filename. Dual old/new line-number columns. Removed lines tinted one way, added lines the other, `+` / `−` markers. Scroll the diff.

### Speaker 2 — Induwara

> Diff Comparison uses the same engine that builds patches. You see a line-by-line view: old line number, new line number, and tinted add or remove rows. A modified line appears as a removed line plus an added line. Multiple changes in one file all show in one scrollable list.
>
> `Patch` is what we store. `DiffResult` is what this screen draws. They are related but not the same object.

**SHOW:** Point at one removed line and one added line. Point at the bottom button **Rollback to v1**. Do **not** tap it yet.

> Rollback is available from this screen as well as from the version card. It always confirms first.

**SHOW:** Back to Version History. If time allows: open View Diff on **v3** (should be very close to current). If the buffer still matches v3, you may see **“No differences between vX and current”**.

> Identical versions do not draw a fake diff. They show a clear empty state.

---

## SCENE 13 — Rollback with confirmation (19:30–20:20)

**Component:** 12  
**Required:** restore exact older content, confirm dialog, editing works after rollback

**SHOW:** On **v2**, tap **Restore**. Dialog: `Roll back to v2? Any unsaved changes in the current file will be lost.` Buttons **Cancel** / **Roll Back**.

### Speaker 2 — Induwara

> Rollback reconstructs that version by applying the snapshot plus any patches up to the target. Confirmation is required. Cancel leaves the editor untouched.

**SHOW:** Tap **Cancel**. Nothing changes. Tap **Restore** on v2 again → **Roll Back**.

Editor returns with v2 content: name is `"Induwara"`, greeting still `Hello` (not `Welcome`).

> The buffer is exactly v2. Reconstruction is not a guess — it is snapshot plus deltas.

**SHOW:** Type a new comment `// after rollback`. It inserts normally.

> After rollback, editing continues as usual. A later save or a new version can be created on top of the restored text. Closing and reopening the app keeps the Room history.

**SHOW:** Back to Home.

---

# PART B — ADDITIONAL FEATURES (not required by the spec)

Induwara still operates. Keep this block brisk — this is where videos go over 25 minutes.

---

## SCENE 14 — Smart editor extras (20:20–22:00)

**Component:** 15, plus Options Menu extras from Component 4/15

**SHOW:** Open `Student.kt`. Place the cursor at the end of a line that ends with `{`. Press Enter.

### Speaker 2 — Induwara

> **Auto-indent** copies the previous line’s indent and adds a tab-size step after an opening brace. Tab size came from Settings.

**SHOW:** Type `{` in the code. The closing `}` appears automatically. Move the cursor onto `{` — both braces highlight.

> **Auto-closing brackets** pair `{ }`, `( )`, `[ ]`, quotes and backticks. **Bracket matching** highlights the opener and closer around the caret.

**SHOW:** Overflow → **Go to line...** type `3` → keyboard Go / action. Cursor jumps to line 3. Close the menu if it is still open.

> **Go to line** jumps by number and clamps to the file’s range. An empty or invalid number does nothing harmful.

**SHOW:** Point at the highlighted current line (already on from Settings).

> **Current-line highlighting** is a Settings toggle. We already enabled it.

**SHOW:** Overflow → **Hide Toolbar** ON. Accessory keyword row disappears. Toggle OFF. Toolbar returns.

> **Hide Toolbar** is extra chrome control, not in the spec’s required editor list.

**SHOW:** Overflow → **Full Screen** ON. System status/navigation chrome hides. Toggle OFF.

> **Full Screen** is a distraction-free extra.

**SHOW:** Overflow → **Share**. Native Android share sheet appears. Cancel without sending.

> **Share** hands the current buffer to the system share sheet. We do not upload anywhere.

**SHOW:** Overflow — point at **Preview**. It should be hidden on a `.kt` file.

> Markdown **Preview** only appears for `.md` files.

**SHOW:** Home → open `notes.md` → overflow → **Preview**. Rendered headings, bold, italic, lists. Back.

> Preview is explicitly **optional** in the spec. We implemented it as an overlay. Highlighting was the required Markdown feature; this is the extra.

---

## SCENE 15 — Remaining Options Menu and polish (22:00–22:50)

**SHOW:** Still in editor, overflow, slowly pan the full menu so every item is visible: Word Wrap, Full Screen, Hide Toolbar, Read Only, Undo, Redo, Go to line, Share, Preview (if md), Rename, Version History, Settings.

### Speaker 2 — Induwara

> Every Options Menu action is live except things we already marked out of scope. Rename from here uses the same dialog as the Files List. Settings from here is the same screen as the Home drawer.
>
> Architecture, briefly: UI to ViewModel to Repository to file system, Room, or DataStore. That split let Yasuru own the editor and files while I owned recovery and versions without fighting over storage code.
>
> We did **not** ship code folding, automatic Kotlin formatting, or file statistics. Those stayed deferred. Everything else in Components 0 through 16 that is in this build, you have now seen.

---

# SCENE 16 — Closing (22:50–24:20)

**SHOW:** Home screen. Both faces on camera. Stop tapping.

### Speaker 1 — Yasuru

> To recap the **required** functions from the mini-project specification:
> - a working editor with cursor, selection, insert, delete, cut, copy, paste, multiline, undo and redo
> - line numbers, word wrap, find, replace, and replace all
> - new, open, save, save as, recent files, UTF-8, rename, delete with confirmation, and read-only
> - Kotlin highlighting for keywords, strings, comments, and annotations
> - Markdown highlighting
> - a complete UI: splash, Home, editor, menus, and dialogs

### Speaker 2 — Induwara

> And the remaining **required** core:
> - automatic crash recovery with Restore or Discard
> - Room persistence
> - incremental delta versioning — one snapshot, then patches only
> - line-by-line diff
> - rollback to any previous version with confirmation
>
> **Additional** features we also shipped: settings with live font size and recovery interval, auto-indent, bracket matching and auto-close, go to line, current-line highlight, full screen, hide toolbar, share, Markdown preview, About, Privacy Policy, and Help.

### Speaker 1 — Yasuru

> I, Yasuru Sandeepa, implemented Components 0 to 6 — architecture, editor, search, files, UI, Kotlin, and Markdown.

### Speaker 2 — Induwara

> I, Induwara Uthsara, implemented Components 7 to 16 — recovery, database, diff, deltas, versions, history, rollback, repositories, settings, optional features, and final polish.

### Both

> Thank you. This was Kotlin++ Lite.

**SHOW:** End on the splash or Home logo for 3 seconds. Stop recording.

---

# If you are over time — cut in this order

Keep these no matter what: intro names + contribution, launch, type + undo, find/replace, save/open/UTF-8, Kotlin highlight, recovery dialog, v1 SNAPSHOT / v2 DELTA, one diff, one rollback, closing names.

Cut first if needed:

1. About / Privacy / Help dialogs (mention only)
2. Save As original-file explanation
3. Rename + Delete cancel-then-confirm (do confirm only)
4. Font-size return-to-editor proof
5. Full Screen, Hide Toolbar, Share
6. Discard pass of recovery (keep Restore only)
7. Identical-versions empty diff

---

# Coverage checklist (tick while editing the video)

## Required by the specification (IS-Mini-Pro-v1 / README spec notes)

| Feature | Scene | Done |
|---|---|---|
| App launches; hub-and-spoke navigation | 1 | ☐ |
| Loading → Home, not straight to editor | 1 | ☐ |
| Text buffer, type, cursor, insert, delete | 3 | ☐ |
| Selection, copy, paste, cut | 3 | ☐ |
| Multiline | 3 | ☐ |
| Undo / redo (session, not versions) | 3 | ☐ |
| Line numbers | 4 | ☐ |
| Word wrap | 4 | ☐ |
| Find, next, previous | 4 | ☐ |
| Replace, replace all, no-match | 4 | ☐ |
| New File + type selector | 2 | ☐ |
| Save | 5a | ☐ |
| Open (system picker) | 5d | ☐ |
| Save As; original unchanged | 5c | ☐ |
| UTF-8 round-trip | 5a, 5d | ☐ |
| Recent Files (Home + View All) | 5d, 5e | ☐ |
| Rename | 5e | ☐ |
| Delete + confirmation | 5e | ☐ |
| Read-only blocks typing | 5f | ☐ |
| Kotlin keywords, strings, comments, annotations | 6 | ☐ |
| Markdown highlighting | 7 | ☐ |
| Crash recovery cache + dialog | 10 | ☐ |
| Restore / Discard | 10 | ☐ |
| Room persistence (stated + versions survive) | 11 | ☐ |
| First version SNAPSHOT, later DELTA | 11 | ☐ |
| Diff line-by-line + identical empty state | 12 | ☐ |
| Rollback + confirmation | 13 | ☐ |
| Repository layer (stated) | 11 | ☐ |

## Additional features

| Feature | Scene | Done |
|---|---|---|
| App Drawer: About, Privacy, Help, Exit (Exit not tapped) | 1 | ☐ |
| Settings: font, tab, wrap, line numbers, current line, highlighting, interval, read-only default | 9 | ☐ |
| Auto-indent | 14 | ☐ |
| Auto-closing brackets | 14 | ☐ |
| Bracket matching highlight | 14 | ☐ |
| Go to line | 14 | ☐ |
| Current-line highlighting | 9, 14 | ☐ |
| Hide Toolbar | 14 | ☐ |
| Full Screen | 14 | ☐ |
| Share sheet | 14 | ☐ |
| Markdown preview | 14 | ☐ |
| Accessory toolbar keyword/symbol insert | 6 | ☐ |
| Unsaved-changes dialog (if it appears on Back) | 5d | ☐ |
| File Menu inventory, Open Folder disabled | 5b | ☐ |
| Options Menu inventory | 15 | ☐ |
| Status bar: line/col, UTF-8, language, READ-ONLY | 2, 5f | ☐ |

## Components.md map

| Component | Who speaks | Scene |
|---|---|---|
| 0 Foundation | Yasuru | 0, 1 |
| 1 Basic editor | Yasuru | 3 |
| 2 Navigation & search | Yasuru | 4 |
| 3 File system | Yasuru | 2, 5 |
| 4 Main UI | Yasuru | 1, 2, 5, 15 |
| 5 Kotlin syntax | Yasuru | 6 |
| 6 Markdown engine | Yasuru | 7 (+ Induwara preview in 14) |
| 7 Crash recovery | Induwara | 10 |
| 8 Database | Induwara | 11 (explained) |
| 9 Diff engine | Induwara | 12 |
| 10 Delta / patch | Induwara | 11 |
| 11 Version foundation | Induwara | 11 |
| 12 History & rollback | Induwara | 11–13 |
| 13 Repository layer | Induwara | 11 (explained) |
| 14 Settings | Induwara | 9 |
| 15 Optional / smart | Induwara | 14–15 |
| 16 Testing & polish | Induwara | 15 (stated) |

---

# Sample spoken “contribution” lines (if you freeze)

**Yasuru (short version):**  
“I am Yasuru. I designed the architecture and built the editor, search, file system, main screens, Kotlin highlighting, and Markdown highlighting.”

**Induwara (short version):**  
“I am Induwara. I built crash recovery, the Room database, incremental versions, diff, rollback, settings, and the extra editor features.”
