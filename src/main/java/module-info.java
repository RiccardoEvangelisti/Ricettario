module ricettario {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
	requires java.sql;
	requires org.controlsfx.controls;
	requires com.jfoenix;
	requires java.desktop;
	requires org.apache.derby.commons;
	requires org.apache.derby.engine;

    opens com.myproject.ricettario.application to javafx.fxml;
    exports com.myproject.ricettario.application;
}
