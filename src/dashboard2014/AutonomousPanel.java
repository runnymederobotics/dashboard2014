package dashboard2014;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class AutonomousPanel extends JPanel {

    Dashboard2014 dashboard;
    ButtonGroup radioGroup;

    public AutonomousPanel(Dashboard2014 dashboard) {
        this.dashboard = dashboard;

        radioGroup = new ButtonGroup();

        CustomCheckbox oneBall = new CustomCheckbox("OneBall", false);
        CustomCheckbox twoBallDrag = new CustomCheckbox("TwoBallDrag", false);

        radioGroup.add(oneBall);
        radioGroup.add(twoBallDrag);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(oneBall);
        add(twoBallDrag);
    }

    class CustomCheckbox extends JCheckBox implements ItemListener {

        public CustomCheckbox() {
            super();
            init();
        }

        public CustomCheckbox(String text, boolean defaultState) {
            super(text, defaultState);
            init();
        }

        private void init() {
            addItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            if (!dashboard.connected()) {
                radioGroup.clearSelection();
            } else if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                dashboard.sendAutonomousMode(getText());
            }
        }
    }

    public void clear() {
        radioGroup.clearSelection();
    }
}
