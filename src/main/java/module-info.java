module de.nerixyz.anker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.apache.logging.log4j;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires static lombok;

    opens de.nerixyz.anker.controllers to javafx.fxml;
    opens de.nerixyz.anker.proto to com.fasterxml.jackson.databind;

    exports de.nerixyz.anker;
}