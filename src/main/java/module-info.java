module com.nsu.cse215l.redlolli.redlolli {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.nsu.cse215l.redlolli.redlolli to javafx.fxml;
    exports com.nsu.cse215l.redlolli.redlolli;
}