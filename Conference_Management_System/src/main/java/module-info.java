module very.cool.project.conference_management_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens very.cool.project.conference_management_system to javafx.fxml;
    exports very.cool.project.conference_management_system;
}