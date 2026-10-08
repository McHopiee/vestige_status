# RP Status (Fabric, Minecraft 26.2)

Players type `/status` to open a menu and pick their roleplay statuses. Each one that's on shows up as a colored square next to their name in the TAB player list.

| Status | Square | Notes |
|---|---|---|
| In Character | green | only one of IC / OOC at a time |
| Out of Character | yellow | |
| Do Not Disturb | red | |
| Open to Interactions | aqua | only one of Open / Closed at a time |
| Closed to Interactions | orange | |
| Recording | pink | |
| Streaming | purple | |

Every status can be on or off. Statuses are saved, so they stay after relogging or a restart.

## Install

1. Put `rp-status-1.0.0.jar` in your server's `mods` folder, next to Fabric API and TAB.
   (The menu library and Placeholder API are bundled inside the jar, so there's nothing else to install.)
2. Start the server once.
3. Make sure `tablist-name-formatting` is `enabled: true` in `config/tab/config.yml` (it is by default).
4. In `config/tab/groups.yml`, add `%rpstatus:squares%` to the front of the `tabprefix`:

   ```yaml
   _DEFAULT_:
     tabprefix: "%rpstatus:squares%"
   ```

   If your groups already have a prefix (like a rank), put the placeholder in front of it in each group, e.g.
   `tabprefix: "%rpstatus:squares%&7[Member] "`.
   You can also do it in-game: `/tab group _DEFAULT_ tabprefix "%rpstatus:squares%"`

5. Run `/tab reload`. Squares update within about a second of a player changing their status.

## Commands

- `/status` opens the menu (anyone can use it)
- `/status clear` turns all of your statuses off

## Placeholders

- `%rpstatus:squares%` gives the colored squares plus a space, or nothing if no status is on
- `%rpstatus:names%` gives the names as text, e.g. `In Character, Streaming`

You can use these anywhere TAB takes text, such as nametags above heads or the scoreboard.

## Changing colors or the symbol

Edit `config/rpstatus/config.json` and restart the server:

```json
{
  "symbol": "■",
  "space_after_squares": true,
  "colors": {
    "in_character": "#55FF55",
    "do_not_disturb": "#FF5555"
  }
}
```

## Building the jar (Windows)

1. Install **Java 25 (JDK)** if you don't have it, e.g. Eclipse Temurin 25 from adoptium.net. Tick "Set JAVA_HOME" during install.
2. Unzip this folder somewhere, open it in File Explorer, click the address bar, type `cmd` and press Enter.
3. Run: `gradlew.bat build`
   The first build downloads Minecraft and Fabric, so it takes a few minutes.
4. The mod is `build\libs\rp-status-1.0.0.jar`. (Ignore any `-sources.jar` if one appears.)

On Mac/Linux it's `./gradlew build`.
