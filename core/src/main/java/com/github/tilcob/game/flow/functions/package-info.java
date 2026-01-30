/**
 * Flow functions callable from scripts.
 *
 * <p>Functions are pure(-ish) helpers that return a value and can be used inside expressions,
 * conditions, or computed arguments. They should be deterministic and side-effect free whenever
 * possible.</p>
 *
 * <h2>Typical usage</h2>
 * <ul>
 *   <li>Condition checks: e.g. inventory, quest state, flags.</li>
 *   <li>Simple transforms: string/number helpers (if allowed by your scripting rules).</li>
 * </ul>
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>Prefer read-only access to game state.</li>
 *   <li>Return types should be stable and well documented.</li>
 *   <li>On errors, include {@code CommandCall.SourcePos} for script diagnostics.</li>
 * </ul>
 */
package com.github.tilcob.game.flow.functions;
