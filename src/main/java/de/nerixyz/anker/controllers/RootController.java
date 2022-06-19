package de.nerixyz.anker.controllers;

import de.nerixyz.anker.view.FxmlDispatch;
import javafx.event.ActionEvent;

public class RootController {
    public void onJoinClicked(ActionEvent actionEvent) {
        FxmlDispatch.goTo(actionEvent, "join-chat");
    }

    public void onHostClicked(ActionEvent actionEvent) {
        FxmlDispatch.goTo(actionEvent, "host-server");
    }
}
