package com.github.tilcob.game.input;

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

        device.press(Command.UP);
        assertEquals(List.of(Command.UP), stateA.pressedCommands);

        inputManager.setActiveState(StateB.class);
        assertEquals(List.of(Command.UP), stateA.releasedCommands);

        device.press(Command.UP);
        assertEquals(List.of(Command.UP), stateB.pressedCommands);
    }

    @Test
    void ignoresDuplicatePressesForSameCommand() {
        InputManager inputManager = new InputManager(new InputMultiplexer());
        MockInputDevice device = new MockInputDevice();
        inputManager.addDevice(device);

        StateA stateA = new StateA();
        inputManager.configureStates(StateA.class, stateA);

        device.press(Command.UP);
        device.press(Command.UP);

        assertEquals(List.of(Command.UP), stateA.pressedCommands);
    }

    @Test
    void ignoresReleaseBeforePress() {
        InputManager inputManager = new InputManager(new InputMultiplexer());
        MockInputDevice device = new MockInputDevice();
        inputManager.addDevice(device);

        StateA stateA = new StateA();
        inputManager.configureStates(StateA.class, stateA);

        device.release(Command.DOWN);

        assertTrue(stateA.releasedCommands.isEmpty());
    }

    private static class MockInputDevice implements InputDevice {
        private InputDeviceListener listener;

        @Override
        public void setListener(InputDeviceListener listener) {
            this.listener = listener;
        }

        void press(Command command) {
            listener.onCommandPressed(command);
        }

        void release(Command command) {
            listener.onCommandReleased(command);
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
