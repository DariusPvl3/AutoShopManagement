package com.autoshop.app.util;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.*;
import java.util.List;

public class AutoCompletion extends KeyAdapter {
    private final JComboBox<String> comboBox;
    private final JTextComponent editor;
    private final DefaultComboBoxModel<String> model;
    private final AutoCompleteProvider provider;
    private boolean isProcessing = false;

    public interface AutoCompleteProvider {
        List<String> getSuggestions(String text);
    }

    public AutoCompletion(JComboBox<String> comboBox, AutoCompleteProvider provider) {
        this.comboBox = comboBox;
        this.provider = provider;
        this.model = (DefaultComboBoxModel<String>) comboBox.getModel();
        this.editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        this.editor.addKeyListener(this);
    }

    // Static helper for simple String arrays (like your Brand list)
    public static void enable(JComboBox<String> comboBox) {
        // Create a simple provider that filters the existing items in the box
        String[] originalItems = new String[comboBox.getItemCount()];
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            originalItems[i] = comboBox.getItemAt(i);
        }

        enable(comboBox, text -> {
            java.util.List<String> list = new java.util.ArrayList<>();
            for (String item : originalItems) {
                if (item.toLowerCase().startsWith(text.toLowerCase())) {
                    list.add(item);
                }
            }
            return list;
        });
    }

    public static void enable(JComboBox<String> comboBox, AutoCompleteProvider provider) {
        new AutoCompletion(comboBox, provider);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Skip navigation keys
        if (isNavigationKey(e.getKeyCode())) return;

        // Run AFTER the event is processed to fix "even letter" bug
        SwingUtilities.invokeLater(() -> {
            if (isProcessing) return;
            isProcessing = true;
            try {
                String text = editor.getText();
                // If empty, usually we don't show popup or we show all.
                // Let's hide if empty to be clean.
                if (text.isEmpty()) {
                    comboBox.hidePopup();
                    return;
                }

                List<String> suggestions = provider.getSuggestions(text);

                if (!suggestions.isEmpty()) {
                    model.removeAllElements();
                    for (String s : suggestions) model.addElement(s);

                    // Essential: restore the typed text because removing elements clears it
                    editor.setText(text);
                    comboBox.showPopup();
                } else {
                    comboBox.hidePopup();
                }
            } finally {
                isProcessing = false;
            }
        });
    }

    private boolean isNavigationKey(int keyCode) {
        return keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE ||
                keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT;
    }
}