package com.github.tilcob.game.yarn.script;

public sealed interface ScriptEvent
    permits ScriptEvent.Text,
    ScriptEvent.Command,
    ScriptEvent.IfStart,
    ScriptEvent.Else,
    ScriptEvent.EndIf {

    record Text(String text) implements ScriptEvent {}
    record Command(String raw) implements ScriptEvent {}
    record IfStart(String condition) implements ScriptEvent {}
    record Else() implements ScriptEvent {}
    record EndIf() implements ScriptEvent {}
}
