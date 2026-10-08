import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

public class LogPanel extends JPanel{
    private final JTextArea textArea;

    public LogPanel(){
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        scrollPane.setPreferredSize(new Dimension(360, GameConfig.WINDOW_SIZE));

        add(scrollPane, BorderLayout.CENTER);
    }

    public void append(String message){
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + System.lineSeparator());
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
