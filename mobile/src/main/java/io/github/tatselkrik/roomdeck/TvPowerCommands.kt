package io.github.tatselkrik.roomdeck

import io.github.tatselkrik.roomdeck.remote.AndroidTvKey

// Bedroom testing confirmed that TCL handles the standard POWER key while
// ignoring the explicit SLEEP key. Keep both Off entry points on this command.
internal val TV_POWER_OFF_KEY = AndroidTvKey.Power
