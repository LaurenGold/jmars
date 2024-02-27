package edu.asu.jmars.layer.stamp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.asu.jmars.Main;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.swing.ColorCombo;
import edu.asu.jmars.swing.ColorMapper.State;
import edu.asu.jmars.util.Util;

public class AddLayerDialog extends JDialog {
	public ColorCombo initialColor = new ColorCombo();
	public JTextField initialName = new JTextField();
	private boolean userHitOK = false;

	public AddLayerDialog(JPanel panel) {
		super(LManager.getLManager().getMainPanel().getAddLayerDialog(), "Add davinci stamp layer", true);

		buildDialog(panel, null);
	}
	
	/**
	 * 
	 * Returns a new StampAddLayerDialog instance based on the wrapper and whether this is being called from an
	 * add layer action or elsewhere. If being called from an add layer action (search or browse), 
	 * the parent will be the main JMARS AddLayerDialog. Otherwise, it will be the main JMARS JFrame.
	 *
	 * @param wrapper
	 * @param addLayerFlag true if being called from JMARS Add Layer Dialog. False if from a Focus Panel or elsewhere.
	 * @return new instance of Stamp AddLayerDialog
	 * @throws
	 *
	 */
	public static AddLayerDialog getStampLayerDialog(AddLayerWrapper wrapper, boolean addLayerFlag) {
	    if (addLayerFlag) {
	        return new AddLayerDialog(wrapper);
	    } else {
	        return new AddLayerDialog(Main.mainFrame, wrapper);
	    }
	}
	/**
	 ** Constructs a modal dialog for adding stamp layer
	 **/
	private AddLayerDialog(final AddLayerWrapper wrapper)
	{
		super(LManager.getLManager().getMainPanel().getAddLayerDialog(), "Add " + wrapper.getInstrument().replace("_"," ") + " stamp layer", true);

		JPanel queryPanel = wrapper.getContainer();
		buildDialog(queryPanel, wrapper);
	}
	private AddLayerDialog(JFrame parent, final AddLayerWrapper wrapper) {
	    super(parent, "Add " + wrapper.getInstrument().replace("_"," ") + " stamp layer", true);

        JPanel queryPanel = wrapper.getContainer();
        buildDialog(queryPanel, wrapper);
	}

	private void buildDialog(JPanel panel, final AddLayerWrapper wrapper) {		
		if (panel!=null) {
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panel, BorderLayout.CENTER);
		}

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel otherSettings = new JPanel();
		int hgap = 5;
		int vgap = 15;
		otherSettings.setLayout(new GridLayout(0, 2, hgap, vgap));
		
		otherSettings.add(new JLabel("Use stamp color:"));
		otherSettings.add(initialColor);
		otherSettings.add(new JLabel("Custom layer name:"));
		otherSettings.add(initialName);

		// Construct the "buttons" section of the container.
		JPanel buttons = new JPanel();
		buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		final JDialog dialog = this;

		//wrapper can be null for davinci layer, so check that first
		if (wrapper!=null && wrapper.supportsTemplates()) {
			JButton addFromTemplate = new JButton("Add from Template");
			addFromTemplate.addActionListener(new ActionListener() {
				QueryTemplateUI templateDialog = null;
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if (templateDialog == null) {
						templateDialog = new QueryTemplateUI(AddLayerDialog.this, wrapper);
					}
					templateDialog.setVisible(true);
					
					if (!templateDialog.isCancelled()) {
						userHitOK = true;
						dialog.setVisible(false);						
					}
					
				}
			});
			buttons.add(addFromTemplate);
		}

		JButton ok = new JButton("OK".toUpperCase());
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Do not allow submission before the server
				// has responded with options.
				if (wrapper!=null && wrapper.filter==null) {
					return;
				}
				userHitOK = true;
				dialog.setVisible(false);
			}
		});
		buttons.add(ok);

		JButton cancel = new JButton("Cancel".toUpperCase());
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}

		});

		buttons.add(cancel);  

		JButton help = new JButton("Help".toUpperCase());
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Util.launchBrowser("http://jmars.asu.edu/wiki/index.php/Instrument_Glossaries");
			}
		});

		buttons.add(help);

		bottomPanel.add(otherSettings, BorderLayout.CENTER);
		bottomPanel.add(buttons, BorderLayout.SOUTH);

		getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

		setLocation(LManager.getDisplayFrame().getLocation());
		
		pack();

		// Attempt to make sure the bottom of the window (with the submit/cancel buttons)
		// is visible on the screen.  Create an extra 60 pixel margin to deal for snazzy
		// toolbars that drop out of view but screw with the reported screen dimensions.
		int dif = screen.height - (getLocation().y + getSize().height + 60);

		if (dif<0) {
			setSize(getSize().width, getSize().height+dif);
		}
		
		toFront();
	}

	public boolean isCancelled()
	{
		return !userHitOK;
	}
	
	double colorMin=Double.NaN;
	double colorMax=Double.NaN;
	State colorState;
	String colorExpression="";
	String orderColumn="";
	boolean orderDirection=false;;
	
	
}
