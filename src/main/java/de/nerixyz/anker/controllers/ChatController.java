package de.nerixyz.anker.controllers;

import de.nerixyz.anker.app.Lifecycle;
import de.nerixyz.anker.controllers.params.StartChatParams;
import de.nerixyz.anker.net.NetworkConnection;
import de.nerixyz.anker.proto.*;
import de.nerixyz.anker.view.FxmlDispatch;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.NonNull;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class ChatController implements Initializable {
    private final @NonNull String userName;
    private final @NonNull NetworkConnection connection;
    private final AtomicReference<String> peerName = new AtomicReference<>("Peer");
    @FXML
    public ListView<String> chatMessagesBox;
    @FXML
    public TextField chatInputBox;
    private ObservableList<String> chatMessages;

    public ChatController(@NonNull StartChatParams params) {
        userName = params.username();
        connection = params.connection();
    }

    public void onChatInputKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() != KeyCode.ENTER || keyEvent.isShiftDown())
            return;
        if (chatInputBox.getText().isEmpty())
            return;
        connection.sendMessage(new ChatMessage(chatInputBox.getText()));
        addMessage(userName, chatInputBox.getText());
        chatInputBox.setText("");
    }

    @Override
    public void initialize(URL _location, ResourceBundle _resources) {
        connection.setListener(new NetworkListener(this));
        connection.startConnection();
        chatMessages = chatMessagesBox.getItems();
        Lifecycle.trackObjectToClose(new WeakReference<>(connection));
    }

    private void addMessage(@NonNull String sender, @NonNull String message) {
        addRawMessage("[" + sender + "]: " + message);
    }

    private void addRawMessage(@NonNull String message) {
        chatMessages.add(message);
        chatMessagesBox.scrollTo(chatMessages.size());
    }

    private void goTo(@NonNull String view) {
        Lifecycle.untrackObjectToClose(connection);
        FxmlDispatch.goTo(chatMessagesBox, view);
    }

    private record NetworkListener(@NonNull ChatController self) implements NetworkConnection.Listener {
        @Override
        public void onConnection() {
            self.connection.sendMessage(new ChangeNameMessage(self.userName));
            Platform.runLater(() -> self.addRawMessage("Connected."));
        }

        @Override
        public void onMessage(@NonNull GameMessage message) {
            if (message instanceof ChatMessage chat) {
                Platform.runLater(() -> self.addMessage(self.peerName.get(), chat.getMessage()));
            } else if (message instanceof ChangeNameMessage changeNameMessage) {
                self.peerName.set(changeNameMessage.getName());
            }
        }

        @Override
        public void onClosed() {
            Platform.runLater(() -> self.goTo("root"));
        }

        @Override
        public void onSingleConnectionClosed() {
            Platform.runLater(() -> self.addRawMessage(self.peerName.get() + " disconnected."));
        }
    }
}
