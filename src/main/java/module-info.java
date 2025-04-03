module org.aktr0s.Luminova {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires animated.gif.lib;


    opens org.aktr0s.Luminova to javafx.fxml;
    exports org.aktr0s.Luminova;
}