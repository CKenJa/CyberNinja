package mod.ckenja.cyninja.core.action;

import java.util.function.BiFunction;

public enum ActionTickType {
    START_TO_END((ninjaAction, actionTick) -> {
        if (actionTick >= ninjaAction.getStartTick() && actionTick < ninjaAction.getEndTick()) {
            return TickState.STARTED;
        }
        if (actionTick < ninjaAction.getStartTick()) {
            return TickState.NOT_START;
        }
        return TickState.STOPPED;
    }),
    LOOP((ninjaAction, actionTick) -> TickState.STARTED),
    INSTANT((ninjaAction, actionTick) -> TickState.STOPPED);

    private final BiFunction<Action, Integer, TickState> function;

    ActionTickType(BiFunction<Action, Integer, TickState> function) {
        this.function = function;
    }

    TickState apply(Action action, int actionTick) {
        return function.apply(action ,actionTick);
    }
}
