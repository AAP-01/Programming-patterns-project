module vanier.conference_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;
    requires org.xerial.sqlitejdbc;

    opens vanier.conference_management_system to javafx.fxml;
    exports vanier.conference_management_system;
    exports vanier.conference_management_system.database;
    exports vanier.conference_management_system.model;
    exports vanier.conference_management_system.dao;
}