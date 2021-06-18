package main.java.Mercury;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;



public class Main extends Application {
    private Stage primaryStage;
    private MercuryController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Mercury Clicker");
        initRootLayout();
        controller.loadContent();
    }

    private void initRootLayout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/main/resources/view/fxml/mercury.fxml"));
            Parent parent = fxmlLoader.load();

            controller = fxmlLoader.getController();
            controller.setMainClass(this);
            Scene scene = new Scene(parent);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            //primaryStage.setMinWidth(600);
            //primaryStage.setMinHeight(300);

            primaryStage.show();
        } catch (Exception e)  {
            System.out.println("ошибка инициализации главной сцены");
            e.printStackTrace();
        }
    }
}
