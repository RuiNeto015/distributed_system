package others;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Text field with placeholder
 */
public class CustomTextField extends JTextField {
    private Font originalFont;
    private Color originalForeground;
    private Color placeholderForeground = new Color(160, 160, 160);
    private boolean textWrittenIn;

    /**
     * Class constructor
     *
     * @param columns
     */
    public CustomTextField(int columns) {
        super(columns);
    }

    /**
     * Function to set the font
     *
     * @param f the font to be applied
     */
    @Override
    public void setFont(Font f) {
        super.setFont(f);
        if (!isTextWrittenIn()) {
            originalFont = f;
        }
    }

    /**
     * Function to set the foreground
     *
     * @param fg the foreground
     */
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (!isTextWrittenIn()) {
            originalForeground = fg;
        }
    }

    /**
     * Getter for the placeholder foreground
     *
     * @return the placeholder foreground
     */
    public Color getPlaceholderForeground() {
        return placeholderForeground;
    }

    /**
     * Setter for the placeholder foreground
     *
     * @param placeholderForeground the placeholder foreground
     */
    public void setPlaceholderForeground(Color placeholderForeground) {
        this.placeholderForeground = placeholderForeground;
    }

    /**
     * Function that checks if the field has text input
     *
     * @return true if the field has text input otherwise false
     */
    public boolean isTextWrittenIn() {
        return textWrittenIn;
    }

    /**
     * Setter for textWrittenIn
     *
     * @param textWrittenIn the textWrittenIn
     */
    public void setTextWrittenIn(boolean textWrittenIn) {
        this.textWrittenIn = textWrittenIn;
    }

    /**
     * Setter for the placeholder
     *
     * @param text the placeholder
     */
    public void setPlaceholder(final String text) {

        this.customizeText(text);

        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                if (getText().trim().length() != 0) {
                    setFont(originalFont);
                    setForeground(originalForeground);
                    setTextWrittenIn(true);
                }

            }
        });

        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!isTextWrittenIn()) {
                    setText("");
                }

            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().trim().length() == 0) {
                    customizeText(text);
                }
            }

        });

    }

    private void customizeText(String text) {
        setText(text);
        setFont(new Font(getFont().getFamily(), Font.ITALIC, getFont().getSize()));
        setForeground(getPlaceholderForeground());
        setTextWrittenIn(false);
    }
}