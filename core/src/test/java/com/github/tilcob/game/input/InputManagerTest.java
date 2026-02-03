package com.github.tilcob.game.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputManagerTest {
    @Test
    void routesCommandsAndReleasesOnStateChange() {
        InputManager inputManager = new InputManager(new InputMultiplexer());
        MockInputDevice device = new MockInputDevice();
        inputManager.addDevice(device);

        StateA stateA = new StateA();
        StateB stateB = new StateB();
        inputManager.configureStates(StateA.class, stateA, stateB);

        device.press();
        assertEquals(List.of(Command.UP), stateA.pressedCommands);

        inputManager.setActiveState(StateB.class);
        assertEquals(List.of(Command.UP), stateA.releasedCommands);

        device.press();
        assertEquals(List.of(Command.UP), stateB.pressedCommands);
    }

    @Test
    void ignoresDuplicatePressesForSameCommand() {
        InputManager inputManager = new InputManager(new InputMultiplexer());
        MockInputDevice device = new MockInputDevice();
        inputManager.addDevice(device);

        StateA stateA = new StateA();
        inputManager.configureStates(StateA.class, stateA);

        device.press();
        device.press();

        assertEquals(List.of(Command.UP), stateA.pressedCommands);
    }

    @Test
    void listensForSingleKeyPress() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        InputManager inputManager = new InputManager(multiplexer);
        List<Integer> captured = new ArrayList<>();

        inputManager.listenForNextKey(captured::add);
        multiplexer.keyDown(Input.Keys.F);
        multiplexer.keyDown(Input.Keys.G);

        assertEquals(List.of(Input.Keys.F), captured);
    }

    @Test
    void ignoresReleaseBeforePress() {
        InputManager inputManager = new InputManager(new InputMultiplexer());
        MockInputDevice device = new MockInputDevice();
        inputManager.addDevice(device);

        StateA stateA = new StateA();
        inputManager.configureStates(StateA.class, stateA);

        device.release();

        assertTrue(stateA.releasedCommands.isEmpty());
    }

    private static class MockInputDevice implements InputDevice {
        private InputDeviceListener listener;

        @Override
        public void setListener(InputDeviceListener listener) {
            this.listener = listener;
        }

        void press() {
            listener.onCommandPressed(Command.UP);
        }

        void release() {
            listener.onCommandReleased(Command.DOWN);
        }
    }

    private static class RecordingControllerState implements ControllerState {
        final List<Command> pressedCommands = new ArrayList<>();
        final List<Command> releasedCommands = new ArrayList<>();

        @Override
        public void keyDown(Command command) {
            pressedCommands.add(command);
        }

        @Override
        public void keyUp(Command command) {
            releasedCommands.add(command);
        }
    }

    private static class StateA extends RecordingControllerState {}

    private static class StateB extends RecordingControllerState {}
}
