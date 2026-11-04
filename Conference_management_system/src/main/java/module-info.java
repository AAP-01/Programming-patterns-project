module vanier.conference_management_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens vanier.conference_management_system to javafx.fxml;
    exports vanier.conference_management_system;
}