package actr.env;
import javax.swing.JDialog;
import javax.swing.JPanel;

import actr.tasks.driving.Controls;

import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.*;
import java.sql.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

// import java.awt.Robot;
// import java.lang.Object;


/**
 * This class is used to create a custom dialog screen, i.e. a pop-up message. Customization is necessary
 * as the steering wheel must be used to press the focused button.
 * 
 * @author Milan de Mooij
 */

public class NDialog extends JDialog{
    
    public NDialog(Frame parent, String content_message, String title_message, Dimension center) {
        super(parent, title_message, false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel label = new JLabel(content_message);
        panel.add(label, gbc);

        JButton btnOk = new JButton("OK");
        gbc.gridx = 0;
        gbc.gridy = 1;
        
        panel.add(btnOk, gbc);
        
        // Get the size of the frame to center the dialog component (not completely accurate).
        Dimension size = parent.getSize();
        setLocation(size.width/2 - center.width,size.height/2 - center.height);
        // setLocation(size.width/2 -200,size.height/2 - 120);
        
        getContentPane().add(panel);
        pack();
        setVisible(true);
        //npk - close dialog box when button X on steering wheel is pressed
        closeDialog();                     
    }
    
    //npk
    public void closeDialog() {
        //instantiate controls
        Controls controls = new Controls();
        controls.startUp();
        try{
            Thread.sleep(2000);
        }catch(InterruptedException e){
            System.out.println("Das hat nicht funktioniert");
        }
        //stay in loop until the button is pressed
        while(!controls.buttonXpressed()){
     }

        dispose();
    }
}



