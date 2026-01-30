/**
 * Yarn expression engine.
 *
 * <p>This package implements lexing, parsing, diagnostics, and evaluation of expressions used by Yarn
 * conditions and computed values.</p>
 *
 * <h2>Strict mode</h2>
 * <p>Strict mode is typically coupled to debug logging:</p>
 * <ul>
 *   <li>Debug enabled → strict comparisons</li>
 *   <li>Debug disabled → non-strict (limited fallback) comparisons</li>
 * </ul>
 *
 * <p>Regardless of strict mode:</p>
 * <ul>
 *   <li><b>Boolean ↔ Number</b> comparisons/conversions are never allowed.</li>
 * </ul>
 *
 * <h2>Diagnostics</h2>
 * <p>When debug logging is enabled, additional expression diagnostics may be emitted
 * (e.g. token dump / AST dump / evaluation trace).</p>
 */
package com.github.tilcob.game.yarn.expression;
