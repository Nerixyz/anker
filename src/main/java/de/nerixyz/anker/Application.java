package de.nerixyz.anker;

import de.nerixyz.anker.app.Lifecycle;
import de.nerixyz.anker.view.FxmlDispatch;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(FxmlDispatch.load("root"));
        stage.setTitle("Anker");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        Lifecycle.cleanupObjects();
    }
}