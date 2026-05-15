package controllers;

import Entites.User;
import Services.UserCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class affichageuser {

    @FXML
    private TableView<User> tv_users;

    @FXML
    private TableColumn<User, Integer> col_id;

    @FXML
    private TableColumn<User, String> col_username;

    @FXML
    private TableColumn<User, String> col_email;

    @FXML
    private TableColumn<User, String> col_firstName;

    @FXML
    private TableColumn<User, String> col_lastName;

    @FXML
    private TableColumn<User, String> col_role;

    @FXML
    private TableColumn<User, String> col_status;

    @FXML
    public void initialize() {

        col_id.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        col_username.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );

        col_email.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        // CORRECTION ICI
        col_firstName.setCellValueFactory(
                new PropertyValueFactory<>("first_name")
        );

        col_lastName.setCellValueFactory(
                new PropertyValueFactory<>("last_name")
        );

        col_role.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );

        col_status.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        loadUsers();
    }

    private void loadUsers() {

        UserCRUD UserCRUD = new UserCRUD();

        try {

            ObservableList<User> users =
                    FXCollections.observableArrayList(
                            UserCRUD.afficher()
                    );

            tv_users.setItems(users);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


