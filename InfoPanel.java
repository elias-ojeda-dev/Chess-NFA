import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

public class InfoPanel extends JPanel{
    private final JLabel turnLabel = new JLabel("Turno: -");
    private final JLabel moveLabel = new JLabel("Movimiento: 0");
    private final JLabel modeLabel = new JLabel("Modo: -");
    private final JLabel winnerLabel = new JLabel("Ganador: En juego");

    public InfoPanel(){
        setLayout(new GridLayout(1, 4, 12, 0));
        setBackground(GameConfig.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        Font font = new Font("Consolas", Font.PLAIN, 14);
        Color textColor = Color.DARK_GRAY;
        turnLabel.setFont(font);
        turnLabel.setForeground(textColor);
        moveLabel.setFont(font);
        moveLabel.setForeground(textColor);
        modeLabel.setFont(font);
        modeLabel.setForeground(textColor);
        winnerLabel.setFont(font);
        winnerLabel.setForeground(textColor);

        add(turnLabel);
        add(moveLabel);
        add(modeLabel);
        add(winnerLabel);
    }

    public void updateStatus(String turn, int move, String mode, String winner){
        turnLabel.setText("Turno: " + turn);
        moveLabel.setText("Movimiento: " + move);
        modeLabel.setText("Modo: " + mode);
        winnerLabel.setText("Ganador: " + winner);
    }
}
