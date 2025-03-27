module com.drillup.drillup {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.microsoft.sqlserver.jdbc;
    requires org.apache.poi.poi;
    requires org.apache.logging.log4j;
    requires java.sql;

    opens com.drillup.drillup to javafx.fxml;
    exports com.drillup.drillup;
}