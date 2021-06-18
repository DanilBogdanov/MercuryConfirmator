package main.java.Mercury;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.Mercury.model.Company;
import main.java.Mercury.model.MercuryParser;

import java.util.Date;


public class MercuryController {
    private Main mainClass;
    @FXML
    TextField textFieldWeight;
    @FXML
    DatePicker datePicker;
    @FXML
    ToggleGroup toggleGroup;
    @FXML
    Button buttonStart;
    @FXML
    Button buttonStop;
    @FXML
    Button buttonUpdate;
    @FXML
    RadioButton radioButton1;
    @FXML
    RadioButton radioButton2;
    @FXML
    RadioButton radioButton3;
    @FXML
    RadioButton radioButton4;
    @FXML
    RadioButton radioButton5;
    @FXML
    RadioButton radioButton6;
    @FXML
    CheckBox checkBox;

    private final MercuryParser mainMercuryParser =  new MercuryParser(true);
    private Thread secondThread;

    public void setMainClass(Main mainClass) {
        this.mainClass = mainClass;
    }

    public void initialize() {
        updateRadioButtons();
    }

    public void loadContent() {
        Thread threadLoadCount = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mainMercuryParser) {
                    mainMercuryParser.updateCountOfCompany();
                }
                Platform.runLater(() -> updateRadioButtons());
            }
        });
        threadLoadCount.setDaemon(true);
        threadLoadCount.start();

    }

    private void updateRadioButtons() {
        radioButton1.setText(Company.BAGIRA_CHAIKA.getName());
        radioButton2.setText(Company.BAGIRA_KLAMAS.getName());
        radioButton3.setText(Company.BAGIRA_OG.getName());
        radioButton4.setText(Company.BAGIRA_MERCURY.getName());
        radioButton5.setText(Company.BAGIRA_PERVOM.getName());
        radioButton6.setText(Company.BAGIRA_PUSHKINA.getName());
    }

    private void sendMessage(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();

    }

    @FXML
    private void buttonStart() {
        Date maxDate;
        int maxWeight;

        if (datePicker.getValue() == null) {
            sendMessage("Выберите дату");
            return;
        }
        maxDate = java.sql.Date.valueOf(datePicker.getValue());

        if (textFieldWeight.getText().equals("")) {
            sendMessage("Не указан максимальный вес.");
            return;
        }
        try {
            maxWeight = Integer.parseInt(textFieldWeight.getText());
        } catch (Exception e) {
            sendMessage("Некоректный вес");
            return;
        }


        final Company company;
        RadioButton radioButtonSelected = (RadioButton) toggleGroup.getSelectedToggle();
        switch (radioButtonSelected.getId()) {
            case "radioButton1" : {
                company = Company.BAGIRA_CHAIKA;
                break;
            }
            case "radioButton2" : {
                company = Company.BAGIRA_KLAMAS;
                break;
            }
            case "radioButton3" : {
                company = Company.BAGIRA_OG;
                break;
            }
            case "radioButton4" : {
                company = Company.BAGIRA_MERCURY;
                break;
            }
            case "radioButton5" : {
                company = Company.BAGIRA_PERVOM;
                break;
            }
            case "radioButton6" : {
                company = Company.BAGIRA_PUSHKINA;
                break;
            }
            default: {
                company = null;
                sendMessage("Не выбрана компания");
                break;
            }
        }

        boolean visible = !checkBox.isSelected();

        secondThread = new Thread() {
            @Override
            public void run() {
                new MercuryParser(false).mainLoop(maxWeight, maxDate, visible, company);

            }
        };
        secondThread.start();
    }

    @FXML
    private void buttonStop() {
        if (secondThread != null) {
            secondThread.interrupt();
        }
        MercuryParser.isInterrupted = true;
    }

    @FXML
    private void buttonUpdate() {
        loadContent();
    }
}
