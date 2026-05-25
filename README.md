# SynKeys

A fork of [HeliBoard](https://github.com/Helium314/HeliBoard) that adds synonym suggestions to the keyboard suggestion strip, powered by the [Open English WordNet](https://en-word.net).

## What's Different

The suggestion strip shows two types of suggestions simultaneously, color-coded:
- **Spelling corrections** — the standard HeliBoard autocorrect candidates
- **Synonym suggestions** — synonyms of the current or last committed word, sourced from WordNet

Synonyms also appear in the next-word prediction slot (after pressing space), replacing the default next-word predictions with synonyms of the word you just typed.

## How It Works

### New files
- **`SynonymProvider.kt`** — loads a pre-built SQLite synonym database from app assets and exposes a `getSynonyms(word)` method
- **`synonyms.db`** — a flattened SQLite database derived from the Open English WordNet 2024 release, bundled in `app/src/main/assets/`

### Modified files
- **`SuggestedWords.java`** — added `KIND_SYNONYM` suggestion type
- **`Suggest.kt`** — instantiates `SynonymProvider` and injects synonym suggestions into the suggestion pipeline after spelling corrections are assembled
- **`InputLogic.java`** — passes `context` to `Suggest` constructor so `SynonymProvider` can load the database
- **`SuggestionStripView.kt`** — color-codes synonym suggestions differently from spelling corrections

## Building

### Prerequisites
- Android Studio (Hedgehog or newer)
- JDK 17+ (use Android Studio's embedded JDK)

### Build from Android Studio
1. Open the project: File → Open → select the cloned folder
2. Let Gradle sync finish
3. Hit the ▶ Run button to build and deploy to a connected device or emulator

### Build from terminal
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/`.

### Install via adb
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell ime enable helium314.keyboard.debug/helium314.keyboard.latin.LatinIME
adb shell ime set helium314.keyboard.debug/helium314.keyboard.latin.LatinIME
```

## Rebuilding the Synonym Database

The bundled `synonyms.db` is derived from the Open English WordNet. To rebuild it from a fresh OEWN release:

1. Download the latest `oewn-YYYY.db` from [x-englishwordnet/sqlite](https://github.com/x-englishwordnet/sqlite)
2. Run the flattening script:

```bash
sqlite3 oewn-2024.db <<'EOF'
ATTACH DATABASE 'synonyms.db' AS out;

CREATE TABLE out.synonyms AS
SELECT w1.word AS word,
       GROUP_CONCAT(w2.word, '|') AS synonyms
FROM words w1
JOIN senses s1 ON w1.wordid = s1.wordid
JOIN senses s2 ON s1.synsetid = s2.synsetid AND s2.wordid != s1.wordid
JOIN words w2 ON s2.wordid = w2.wordid
GROUP BY w1.word;

CREATE INDEX out.idx_word ON synonyms(word);
EOF
```

3. Copy `synonyms.db` to `app/src/main/assets/synonyms.db`
4. Rebuild the app

## Data Source

Synonym data is from the [Open English WordNet 2024](https://en-word.net), a community-maintained update to the original Princeton WordNet. Licensed under the [Princeton WordNet License](https://wordnet.princeton.edu/license-and-commercial-use) (BSD-style, free for use).

## Based On

[HeliBoard](https://github.com/Helium314/HeliBoard) — an open source Android keyboard based on AOSP LatinIME.
Licensed under Apache 2.0 and GPL-3.0.
