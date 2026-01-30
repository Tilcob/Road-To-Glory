/**
 * Yarn script runtime integration.
 *
 * <p>This package provides the runtime pieces required to execute Yarn scripts in the game:
 * loading compiled Yarn, running nodes, dispatching commands, and bridging to the flow layer.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Script execution lifecycle (start node, continue, stop).</li>
 *   <li>Dispatch of script commands into {@code com.github.tilcob.game.flow.commands}.</li>
 *   <li>Evaluation of conditions via the expression engine.</li>
 *   <li>Consistent error reporting using source positions.</li>
 * </ul>
 *
 * <h2>Debugging</h2>
 * <p>In debug mode, the runtime should favor strictness and better diagnostics to surface
 * authoring mistakes early.</p>
 */
package com.github.tilcob.game.yarn.script;
