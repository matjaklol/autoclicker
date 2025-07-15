module org.openjfx.customfx {
    requires javafx.controls;
    requires javafx.fxml;
	requires javafx.base;
	requires com.github.kwhat.jnativehook;
	requires java.desktop;

    opens org.openjfx.customfx to javafx.fxml;
    exports org.openjfx.customfx;
}
