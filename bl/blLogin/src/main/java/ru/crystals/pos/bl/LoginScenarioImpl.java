package ru.crystals.pos.bl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.crystals.pos.bl.api.layer.LayerScenario;
import ru.crystals.pos.bl.events.ShowPopupMessage;
import ru.crystals.pos.hw.events.listeners.BarcodeListener;
import ru.crystals.pos.hw.events.listeners.MSRListener;
import ru.crystals.pos.hw.events.listeners.MSRTracks;
import ru.crystals.pos.ui.UI;
import ru.crystals.pos.ui.UILayer;
import ru.crystals.pos.ui.forms.loading.LoginFormModel;
import ru.crystals.pos.ui.label.Label;
import ru.crystals.pos.user.LoginFailedException;
import ru.crystals.pos.user.User;
import ru.crystals.pos.user.UserAuthorisedEvent;
import ru.crystals.pos.user.UserModule;
import ru.crystals.pos.user.UserRight;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LoginScenarioImpl implements BarcodeListener, MSRListener, LayerScenario {

    private UI ui;
    private final UserModule userModule;
    private final ShowPopupMessage showPopupMessage;
    private final ScenarioManager scenarioManager;

    @Autowired
    private ApplicationEventPublisher publisher;

    private LoginFormModel model;

    public LoginScenarioImpl(UserModule userModule, ShowPopupMessage showPopupMessage, ScenarioManager scenarioManager) {
        this.userModule = userModule;
        this.showPopupMessage = showPopupMessage;
        this.scenarioManager = scenarioManager;
    }

    private LoginFormModel showLoginForm(String errorText) {
        LoginFormModel model = new LoginFormModel(getShiftText(),
            getInfoText(),
            Label.error(errorText),
            this::onPasswordEntered);
        ui.showForm(model);
        return model;
    }

    private void onPasswordEntered(String password) {
        try {
            User user = userModule.loginByPassword(password);
            startNextScenario(user);
        } catch (LoginFailedException e) {
            onLoginError(e);
        }
    }

    @Override
    public void onBarcode(String code) {
        try {
            User user = userModule.loginByBarcode(code);
            startNextScenario(user);
        } catch (LoginFailedException e) {
            onLoginError(e);
        }
    }

    @Override
    public void onMSR(MSRTracks msrTracks) {
        try {
            User user = userModule.loginByMSR(msrTracks);
            startNextScenario(user);
        } catch (LoginFailedException e) {
            onLoginError(e);
        }
    }

    private void onLoginError(LoginFailedException error) {
        showPopupMessage.shopPopup(error.getLocalizedMessage(), this::startPrivate);
    }

    private void startNextScenario(User user) {
        showLoginForm(user.getFirstName() + " ");
        if (user.hasRight(UserRight.SALE)) {
            scenarioManager.setLayer(UILayer.SALE);
        } else if (user.hasRight(UserRight.SHIFT)) {
            showLoginForm(user.getFirstName() + " еще не реализовано");
        } else if (user.hasRight(UserRight.CONFIGURATION)) {
            showLoginForm(user.getFirstName() + " еще не реализовано");
        }
    }

    private Label getInfoText() {
        String keys = Stream.of("Hot keys:",
            "F2 -> Barcode('X-002') (Кассир)",
            "F5 -> Barcode('12345')",
            "F8 -> TypedKey('8') + Barcode('00000008')",
            "F9 -> Payment('cash')",
            "F10 -> Payment('bank')",
            "F12 -> Barcode('XXXXX')",
            "NumPad+ -> SUBTOTAL",
            "ScreenSaver idle = 30 секунд",
            "Уберите товар с весов в продаже idle = 15 секунд").collect(Collectors.joining("<br>", "<html>", "</html>"));

        return Label.empty(keys);
    }

    private String getShiftText() {
        return "Открыта смена №00. Пароль кассира 2.";
    }

    @Override
    public UILayer getLayer() {
        return UILayer.LOGIN;
    }

    @Override
    public void start(UI ui) {
        this.ui = ui;
        startPrivate();
    }

    @Override
    public void onResume() {
        this.model.setLoginFailedText(Label.empty(""));
        this.model.modelChanged();
    }

    private void startPrivate() {
        this.model = showLoginForm("");
        UserAuthorisedEvent event = new UserAuthorisedEvent(null);
        publisher.publishEvent(event);
    }

}
