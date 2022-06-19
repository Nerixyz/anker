package de.nerixyz.anker.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class FxmlDispatch {
    private FxmlDispatch() {
    }

    public static <T> T load(@NonNull String name) throws IOException {
        return load(name, null);
    }

    public static <T> T load(@NonNull String name, Object params) throws IOException {
        return load(FxmlDispatch.class, name, params);
    }

    public static <T> T load(@NonNull Class<?> root, @NonNull String name) throws IOException {
        return load(root, name, null);
    }

    public static <T> T load(@NonNull Class<?> root, @NonNull String name, Object params) throws IOException {
        var loader = new FXMLLoader(root.getResource(name.endsWith(".fxml") ? name : name + "-view.fxml"));
        if (params != null) {
            loader.setControllerFactory(clazz -> {
                try {
                    return clazz.getConstructor(params.getClass()).newInstance(params);
                } catch (Exception e) {
                    throw new RuntimeException("Failed constructing controller", e);
                }
            });
        }
        return loader.load();
    }

    public static void goTo(@NonNull Node node, @NonNull String name) {
        goTo(node, name, null);
    }

    public static void goTo(@NonNull Node node, @NonNull String name, Object params) {
        try {
            if (node.getScene().getWindow() instanceof Stage stage) {
                stage.setScene(new Scene(load(name, params), node.getScene().getWidth(), node.getScene().getHeight()));
            } else {
                log.error("A node's window isn't a javafx.stage.Stage, it's a {}",
                          node.getScene().getWindow().getClass().getName());
            }
        } catch (Exception e) {
            log.error("Failed loading scene", e);
        }
    }

    public static void goTo(@NonNull ActionEvent evt, @NonNull String name) {
        goTo(evt, name, null);
    }

    public static void goTo(@NonNull ActionEvent evt, @NonNull String name, Object params) {
        if (evt.getSource() instanceof Node node)
            goTo(node, name, params);
    }
}
