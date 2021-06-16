package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Controller {
    @FXML
    Button btnSendMsg;
    @FXML
    TextArea msgArea;
    @FXML
    TextArea mainField;

    StringBuilder msg = new StringBuilder();

    @FXML
    public void initialize() {
        msgArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                btnSendMsg.setDisable(observable.getValue().isEmpty());
            }
        });
    }

    public void sendMsg() {
        msg.append("Вы: ").append(msgArea.getText()).append("\n");
        msgArea.clear();
        mainField.setText(msg.toString());

    }public void btnClick(ActionEvent actionEvent) {
        sendMsg();
    }

    public void enterSend(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
            sendMsg();
        }
    }

}
