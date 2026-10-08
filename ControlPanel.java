import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel{
    public interface ControlListener{
        void onStart(boolean automaticMode, String player1Input, String player2Input, int autoLength);
        void onStop();
    }

    private final JRadioButton manualRadioButton = new JRadioButton("Manual", true);
    private final JRadioButton automaticRadioButton = new JRadioButton("Automatico");
    private final JTextField player1InputField = new JTextField(12);
    private final JTextField player2InputField = new JTextField(12);
    private final JSpinner autoLengthSpinner = new JSpinner(new SpinnerNumberModel(5, 3, GameConfig.MAX_INPUT_LENGTH, 1));
    private final JButton startButton = new JButton("Iniciar");
    private final JButton stopButton = new JButton("Detener");
    private ControlListener listener;

    public ControlPanel(){
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Control"));
        buildLayout();
        wireEvents();
        updateEnabledState();
    }

    private void buildLayout(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        ButtonGroup group = new ButtonGroup();
        group.add(manualRadioButton);
        group.add(automaticRadioButton);
        add(new JLabel("Modo:"), gbc);
        gbc.gridx = 1;
        add(manualRadioButton, gbc);
        gbc.gridx = 2;
        add(automaticRadioButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Cadena J1:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(player1InputField, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Cadena J2:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(player2InputField, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("Long. auto:"), gbc);
        gbc.gridx = 1;
        add(autoLengthSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(startButton, gbc);
        gbc.gridx = 1;
        add(stopButton, gbc);
    }

    private void wireEvents(){
        ActionListener modeListener = e -> updateEnabledState();
        manualRadioButton.addActionListener(modeListener);
        automaticRadioButton.addActionListener(modeListener);

        startButton.addActionListener(e -> {
            if(listener != null){
                listener.onStart(automaticRadioButton.isSelected(), player1InputField.getText().trim(), player2InputField.getText().trim(),
                        (int) autoLengthSpinner.getValue());
            }
        });
        stopButton.addActionListener(e -> {
            if(listener != null){
                listener.onStop();
            }
        });
    }

    private void updateEnabledState(){
        boolean manualSelected = manualRadioButton.isSelected();
        player1InputField.setEnabled(manualSelected);
        player2InputField.setEnabled(manualSelected);
        autoLengthSpinner.setEnabled(!manualSelected);
    }

    public void setListener(ControlListener listener){
        this.listener = listener;
    }

    public void setControlsEnabled(boolean enabled){
        startButton.setEnabled(enabled);
        manualRadioButton.setEnabled(enabled);
        automaticRadioButton.setEnabled(enabled);
        player1InputField.setEnabled(enabled && manualRadioButton.isSelected());
        player2InputField.setEnabled(enabled && manualRadioButton.isSelected());
        autoLengthSpinner.setEnabled(enabled && automaticRadioButton.isSelected());
        stopButton.setEnabled(!enabled);
    }
}
