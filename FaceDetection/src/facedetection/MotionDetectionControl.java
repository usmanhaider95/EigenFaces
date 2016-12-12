package facedetection;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.media.Control;

public class MotionDetectionControl implements Control, ActionListener, ChangeListener  {

  Component component;
  JButton button;
  JSlider threshold;
  MotionDetectionEffect motion;


  public MotionDetectionControl(MotionDetectionEffect motion) {

    this.motion = motion;

  }

  public Component getControlComponent () {

    if (component == null) {

      button = new JButton("Debug");
      button.addActionListener(this);

      button.setToolTipText("Click to turn debugging mode on/off");

      threshold = new JSlider(JSlider.HORIZONTAL,
                               0,
                               motion.THRESHOLD_MAX,
                               motion.THRESHOLD_INIT);

      threshold.setMajorTickSpacing(motion.THRESHOLD_INC);
      threshold.setPaintLabels(true);
      threshold.addChangeListener(this);

      Panel componentPanel = new Panel();
      componentPanel.setLayout(new BorderLayout());
      componentPanel.add("East", button);
      componentPanel.add("West", threshold);
      componentPanel.invalidate();
      component = componentPanel;
    }
    return component;
  }

  public void actionPerformed(ActionEvent e) {

    Object o = e.getSource();

    if (o == button) {
      if (motion.debug == false)
         motion.debug = true;
      else motion.debug = false;

    }

  }
  public void stateChanged(ChangeEvent e) {

    Object o = e.getSource();
    if (o == threshold) {
      motion.blob_threshold = threshold.getValue()*1000;
    }
  }
}
