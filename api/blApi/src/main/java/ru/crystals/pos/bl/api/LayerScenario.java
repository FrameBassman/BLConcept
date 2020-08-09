package ru.crystals.pos.bl.api;

import ru.crystals.pos.ui.UILayer;

public interface LayerScenario extends Scenario {

    UILayer getLayer();

    /**
     * Запуск сценария
     */
    void start();

    /**
     * при приостановлении
     */
    default void onSuspend() {};

    /**
     * при возобновлении
     */
    void onActivate();

}
