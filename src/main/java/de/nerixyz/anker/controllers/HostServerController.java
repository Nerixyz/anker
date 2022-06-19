package de.nerixyz.anker.controllers;

import de.nerixyz.anker.app.Validation;
import de.nerixyz.anker.controllers.params.StartChatParams;
import de.nerixyz.anker.net.Server;
import de.nerixyz.anker.view.FxmlDispatch;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class HostServerController {
    @FXML
    public TextField userNameField;
    @FXML
    public TextField portField;

    public void onHostClicked(ActionEvent actionEvent) {
        var port = Validation.parsePort(portField.getText());
        if (port == -1)
            return;
        if (!Validation.validateUsername(userNameField.getText()))
            return;

        FxmlDispatch.goTo(actionEvent, "chat", new StartChatParams(userNameField.getText(), new Server(port)));
    }
}
