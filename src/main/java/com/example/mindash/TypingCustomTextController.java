package com.example.mindash;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TypingCustomTextController {

    @FXML
    private TextArea customTextInputArea;
    @FXML
    private Button applyButton;
    @FXML
    private Label errorMessageLabel; // To display validation messages

    private String customText; // To hold the validated custom text

    /**
     * Returns the custom text entered by the user.
     * @return The custom text string.
     */
    public String getCustomText() {
        return customText;
    }

    /**
     * Pre-fills the text area with existing custom text, if any.
     * This is useful if the user opens the window multiple times.
     * @param existingText The text to display initially.
     */
    public void setExistingText(String existingText) {
        if (existingText != null && !existingText.isEmpty()) {
            customTextInputArea.setText(existingText);
        }
    }

    /**
     * Handles the action when the "Apply Text" button is clicked.
     * Validates the input and closes the window if valid.
     */
    @FXML
    private void applyText() {
        String input = customTextInputArea.getText().trim();

        if (input.isEmpty()) {
            errorMessageLabel.setText("Custom text cannot be empty!");
            customText = null;
        } else if (input.length() < 20) {
            errorMessageLabel.setText("Custom text too short! Minimum 20 characters recommended.");
            customText = null;
        } else if (input.length() > 250) {
            errorMessageLabel.setText("Custom text too long! Maximum 250 characters allowed.");
            customText = null;
        }
        else {
            customText = input; // Store the validated text
            // Close the window
            Stage stage = (Stage) applyButton.getScene().getWindow();
            stage.close();
        }
    }
}
