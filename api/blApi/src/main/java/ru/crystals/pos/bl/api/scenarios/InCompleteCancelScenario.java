package ru.crystals.pos.bl.api.scenarios;

import ru.crystals.pos.bl.api.listener.VoidListener;

public interface InCompleteCancelScenario<IN> extends Scenario {

    void start(IN arg, VoidListener onComplete, VoidListener onCancel);

}