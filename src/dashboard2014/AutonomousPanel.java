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
        CustomCheckbox oneBallCheesyShot = new CustomCheckbox("OneBallCheesyShot", false);

        CustomCheckbox lowGoal = new CustomCheckbox("LowGoal", false);

        CustomCheckbox twoBallDrag = new CustomCheckbox("TwoBallDrag", false);
        CustomCheckbox twoBallDragCheesyShot = new CustomCheckbox("TwoBallDragCheesyShot", false);

        CustomCheckbox noAuto = new CustomCheckbox("NoAuto", false);

        CustomCheckbox mobility = new CustomCheckbox("Mobility", false);
        CustomCheckbox mobilityDelayed = new CustomCheckbox("MobilityDelayed", false);


        radioGroup.add(oneBall);
        radioGroup.add(oneBallCheesyShot);

        radioGroup.add(lowGoal);

        radioGroup.add(twoBallDrag);
        radioGroup.add(twoBallDragCheesyShot);

        radioGroup.add(noAuto);

        radioGroup.add(mobility);
        radioGroup.add(mobilityDelayed);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(oneBall);
        add(oneBallCheesyShot);

        add(lowGoal);

        add(twoBallDrag);
        add(twoBallDragCheesyShot);

        add(noAuto);

        add(mobility);
        add(mobilityDelayed);

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
