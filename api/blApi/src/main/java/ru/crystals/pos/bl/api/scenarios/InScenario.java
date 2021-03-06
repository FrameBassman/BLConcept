package ru.crystals.pos.bl.api.scenarios;

import ru.crystals.pos.ui.UI;

/**
 * Интерфейс сценария с аргументом
 * @param <IN> тип аргумента
 */
public interface InScenario<IN> extends Scenario {

    void start(UI ui, IN inArg);

}
