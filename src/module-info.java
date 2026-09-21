module FoundationsF26 {
	requires javafx.controls;
	requires java.sql;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
	opens passwordEvaluationTestbedMain to javafx.graphics, javafx.fxml;
}
