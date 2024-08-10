module com.martishin.graphsonmaps {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires jakarta.json;
    requires jdk.jsobject;
    requires javafx.web;
    requires com.dlsc.gmapsfx;

    exports com.martishin.graphsonmaps.application to javafx.graphics;
}
