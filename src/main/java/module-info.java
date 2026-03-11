module com.nsu.cse215l.redlolli.redlolli {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.controlsfx.controls;

    opens com.nsu.cse215l.redlolli.redlolli to javafx.fxml;
    opens com.nsu.cse215l.redlolli.redlolli.ui to javafx.fxml;
    exports com.nsu.cse215l.redlolli.redlolli;
    exports com.nsu.cse215l.redlolli.redlolli.ui;
}