/**
 * Flow command implementations.
 *
 * <p>Commands are imperative actions triggered by scripts (primarily Yarn).
 * Examples: giving an item, starting a quest step, opening UI, teleporting, etc.</p>
 *
 * <h2>Design</h2>
 * <ul>
 *   <li>Commands should be small and composable.</li>
 *   <li>Commands should validate arguments and throw a helpful error that includes source position.</li>
 *   <li>Commands should avoid heavy logic; delegate to systems/services where possible.</li>
 * </ul>
 *
 * <h2>Error reporting</h2>
 * <p>Commands should include {@code CommandCall.SourcePos} in exceptions so that authored
 * content can be debugged quickly.</p>
 */
package com.github.tilcob.game.flow.commands;
