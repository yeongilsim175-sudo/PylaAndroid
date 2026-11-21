# Farming stats overlay (4.1.1)

This branch replaces the original centered PYLA performance pill with five Korean game-overlay text lines in the top-right of the safe display region:

- 파밍 시간: HH:MM:SS — bot active time; pauses excluded
- 승리: N — confirmed victories in the current run
- 패배: N — confirmed defeats in the current run
- 획득 트로피: N — cumulative nonnegative trophy gain/loss from the existing TrophyObserver after each result; minimum 0
- 승률: P.P% — wins / (wins + losses), or 0.0% before results (draws excluded)

Counters reset at each new bot run, continue to display after Stop, and are updated from StageManager's confirmed end-game results. The draggable control bubble and its Start/Pause/Stop menu remain unchanged.

Note: the displayed trophy difference is the bot's locally calculated figure, **not** independently verified against Brawl Stars servers. The renderer draws white outlined text on a fully transparent window without the original pill background. The original performance-HUD helper classes remain in the tree for existing unit tests but are no longer instantiated by OverlayService.

Build with GitHub Actions `Android CI` and download the successful `android-debug-<run>-<attempt>` artifact, containing the debug APK. A debug build may use a different signing certificate than an installed original APK.
