/**
 * Game "flow" layer.
 *
 * <p>This package contains the runtime glue that connects authored content (e.g. Yarn scripts)
 * with engine systems. It defines the central types used to execute scripted commands and
 * evaluate conditions consistently.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Defines the command call model (command name, arguments, source position).</li>
 *   <li>Provides shared runtime context passed into commands/functions.</li>
 *   <li>Acts as the boundary between content-driven logic and gameplay systems.</li>
 * </ul>
 */
package com.github.tilcob.game.flow;
