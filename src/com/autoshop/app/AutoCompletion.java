package com.autoshop.app;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class AutoCompletion {

    public static void enable(JComboBox<String> comboBox) {
        final List<String> originalItems = new ArrayList<>();
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            originalItems.add(comboBox.getItemAt(i));
        }

        final JTextField textEditor = (JTextField) comboBox.getEditor().getEditorComponent();

        // Listener for Typing (Filtering)
        textEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (isNavigationKey(e)) return;

                SwingUtilities.invokeLater(() -> {
                    String currentText = textEditor.getText();
                    List<String> filteredItems = new ArrayList<>();

                    // Filter Logic
                    for (String item : originalItems) {
                        if (item.toLowerCase().contains(currentText.toLowerCase())) {
                            filteredItems.add(item);
                        }
                    }

                    // Update Model
                    DefaultComboBoxModel<String> newModel = new DefaultComboBoxModel<>();
                    for (String item : filteredItems) {
                        newModel.addElement(item);
                    }
                    comboBox.setModel(newModel);
                    textEditor.setText(currentText);

                    if (filteredItems.size() > 0) {
                        comboBox.showPopup();
                    } else {
                        comboBox.hidePopup();
                    }
                });
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // If the list is filtered and has items, pick the first one
                    if (comboBox.getItemCount() > 0) {
                        Object topItem = comboBox.getItemAt(0);
                        textEditor.setText(topItem.toString());
                        comboBox.setSelectedItem(topItem);
                        comboBox.hidePopup();
                    }
                }
            }
        });
    }

    private static boolean isNavigationKey(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_ENTER ||
                e.getKeyCode() == KeyEvent.VK_UP ||
                e.getKeyCode() == KeyEvent.VK_DOWN ||
                e.getKeyCode() == KeyEvent.VK_LEFT ||
                e.getKeyCode() == KeyEvent.VK_RIGHT;
    }
}