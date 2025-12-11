package com.autoshop.app.util;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class AutoCompletion {

    // Method 1: For Dynamic Database Data (Name, Phone, Plate)
    // No changes needed here because 'provider.fetchSuggestions' runs fresh on every keystroke.
    public static void enable(JComboBox<String> comboBox, SuggestionProvider provider) {
        final List<String> originalItems = new ArrayList<>();
        for(int i=0;i<comboBox.getItemCount();i++)
            originalItems.add(comboBox.getItemAt(i));
        final JTextField textEditor = (JTextField) comboBox.getEditor().getEditorComponent();

        textEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(isNavigationKey(e)) return;
                SwingUtilities.invokeLater(() -> {
                    String currentText = textEditor.getText();
                    List<String> filteredItems = provider.fetchSuggestions(currentText);

                    DefaultComboBoxModel<String> newModel = new DefaultComboBoxModel<>();
                    for (String item : filteredItems)
                        newModel.addElement(item);
                    comboBox.setModel(newModel);
                    textEditor.setText(currentText);
                    if (!filteredItems.isEmpty())
                        comboBox.showPopup();
                    else
                        comboBox.hidePopup();
                });
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleEnterKey(e, comboBox, textEditor);
            }
        });
    }

    // Method 2: For Fixed Lists (Brand) - THE BUG WAS HERE
    public static void enable(JComboBox<String> comboBox) {
        // 1. Capture the full original list
        final List<String> originalItems = new ArrayList<>();
        for (int i = 0; i < comboBox.getItemCount(); i++)
            originalItems.add(comboBox.getItemAt(i));

        final JTextField textEditor = (JTextField) comboBox.getEditor().getEditorComponent();

        // 2. Typing Logic (Filtering)
        textEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (isNavigationKey(e)) return;

                SwingUtilities.invokeLater(() -> {
                    String currentText = textEditor.getText();
                    List<String> filteredItems = new ArrayList<>();

                    // Filter from the ORIGINAL list
                    for (String item : originalItems)
                        if (item.toLowerCase().contains(currentText.toLowerCase()))
                            filteredItems.add(item);

                    DefaultComboBoxModel<String> newModel = new DefaultComboBoxModel<>();
                    for (String item : filteredItems)
                        newModel.addElement(item);
                    comboBox.setModel(newModel);
                    textEditor.setText(currentText);
                    if (!filteredItems.isEmpty())
                        comboBox.showPopup();
                    else
                        comboBox.hidePopup();
                });
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleEnterKey(e, comboBox, textEditor);
            }
        });

        // 3. THE FIX: Restore full list when popup closes
        // This ensures that next time you click the arrow, you see everything.
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                restoreFullList();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                restoreFullList();
            }

            private void restoreFullList() {
                SwingUtilities.invokeLater(() -> {
                    String currentText = textEditor.getText();

                    // Rebuild the full model
                    DefaultComboBoxModel<String> fullModel = new DefaultComboBoxModel<>();
                    for (String item : originalItems) {
                        fullModel.addElement(item);
                    }

                    comboBox.setModel(fullModel);
                    // Restore the text/selection so the user doesn't lose their work
                    comboBox.setSelectedItem(currentText);
                    textEditor.setText(currentText);
                });
            }
        });
    }

    // --- SHARED HELPERS ---
    private static void handleEnterKey(KeyEvent e, JComboBox<String> comboBox, JTextField textEditor) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (comboBox.getItemCount() > 0) {
                int selectionIndex = comboBox.getSelectedIndex();
                int targetIndex = (selectionIndex == -1) ? 0 : selectionIndex;

                Object selectedItem = comboBox.getItemAt(targetIndex);
                if (selectedItem != null) {
                    textEditor.setText(selectedItem.toString());
                    comboBox.setSelectedItem(selectedItem);
                    comboBox.hidePopup();
                }
            }
        }
    }

    private static boolean isNavigationKey(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_ENTER ||
                e.getKeyCode() == KeyEvent.VK_UP ||
                e.getKeyCode() == KeyEvent.VK_DOWN ||
                e.getKeyCode() == KeyEvent.VK_LEFT ||
                e.getKeyCode() == KeyEvent.VK_RIGHT;
    }

    public interface SuggestionProvider {
        List<String> fetchSuggestions(String inputText);
    }
}