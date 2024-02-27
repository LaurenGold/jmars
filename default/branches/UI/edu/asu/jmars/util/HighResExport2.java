package edu.asu.jmars.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import edu.asu.jmars.Main;
import edu.asu.jmars.ProjObj;
import edu.asu.jmars.ZoomManager;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.layer.Layer.LView3D;
import edu.asu.jmars.swing.ColorMapOp;
import edu.asu.jmars.swing.ImportantMessagePanel;
import edu.asu.jmars.ui.looknfeel.ThemeFont;

@SuppressWarnings("serial")
public class HighResExport2 {
	private static JComboBox<Integer> ppdBx;
	private static JCheckBox scaleChk;
	private static JButton fileBtn;
	private static JFileChooser chooser;
	private static JLabel fileLbl;
	private static String fileStr;
	private static JButton okayBtn;
	private static JButton cancelBtn;
	private static JFrame frame;
	
	private static Font plainFont;
	private static int pad = 2;
	private static Insets in = new Insets(pad, pad, pad, pad);	
	private static Rectangle2D wRect;
	
	private static HighResExport2 hre;
	private boolean success = true;
	
	public HighResExport2(){
		buildDialog();
	}
	
	public static HighResExport2 getInstance(){
		if(hre == null){
			hre = new HighResExport2();
		}
		return hre;
	}
	
	public void showDialog(Rectangle2D worldRect){
		buildDialog();
		
		frame.setLocationRelativeTo(Main.mainFrame);
		frame.setVisible(true);
		
		wRect = worldRect;
	}
	
	private void buildDialog(){
		frame = new JFrame();
		frame.setAlwaysOnTop(true);
		frame.setTitle("High Resolution JPEG Export");
		
		JPanel mainPnl = new JPanel(new BorderLayout());
		mainPnl.setBorder(new EmptyBorder(10, 10, 5, 10));
		mainPnl.setBackground(Util.lightBlue);
		
		plainFont = ThemeFont.getRegular();
		
		JLabel resLbl = new JLabel("What resolution do you wish to export at?");
		JLabel ppdLbl = new JLabel("PPD:");
		ppdLbl.setFont(plainFont);
		
		ZoomManager zmgr = Main.testDriver.mainWindow.getZoomManager();
		Vector<Integer> ppds = new Vector<Integer>(Arrays.asList(zmgr.getExportZoomFactors()));
		ppdBx = new JComboBox<Integer>(ppds);
		ppdBx.setSelectedItem(zmgr.getZoomPPD());
		
		scaleChk = new JCheckBox("Scale shape widths and labels as resolution increases");
		scaleChk.setFont(plainFont);
		scaleChk.setSelected(true);
		
		JLabel locationLbl = new JLabel("Where do you want the file to go?");
		
		//set up the directory chooser
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		chooser.setDialogTitle("Choose Output File");
		JPEGFilter filter = new JPEGFilter();
		chooser.setFileFilter(filter);
		
		fileBtn = new JButton(fileAct);
		fileLbl = new JLabel(" ");

		ImportantMessagePanel messagePnl = new ImportantMessagePanel("Clicking OK will close this window and run the export in the background.  A dialog will pop up when it has finished.");
		
		okayBtn = new JButton(okayAct);
		okayBtn.setEnabled(false);
		cancelBtn = new JButton(cancelAct);
		
		JPanel innerPnl = new JPanel(new GridBagLayout());
		JPanel southPnl = new JPanel(new GridBagLayout());
		southPnl.setBackground(Util.lightBlue);
		
		int row = 0;
		innerPnl.add(resLbl, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		innerPnl.add(ppdLbl, new GridBagConstraints(0, row, 1, 1, 0.5, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		innerPnl.add(ppdBx, new GridBagConstraints(1, row++, 1, 1, 0.5, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
		innerPnl.add(scaleChk, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		innerPnl.add(locationLbl, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		innerPnl.add(fileBtn, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		innerPnl.add(fileLbl, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		
		row = 0;
		southPnl.add(messagePnl, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), pad, pad));

		southPnl.add(okayBtn, new GridBagConstraints(0, row, 1, 1, 0.5, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		southPnl.add(cancelBtn, new GridBagConstraints(1, row++, 1, 1, 0.5, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
		
		mainPnl.add(innerPnl, BorderLayout.CENTER);
		mainPnl.add(southPnl, BorderLayout.SOUTH);
		
		mainPnl.setPreferredSize(new Dimension(385, 290));
		
		frame.setContentPane(mainPnl);
		frame.pack();
		frame.setResizable(false);
	}

	private AbstractAction fileAct = new AbstractAction("Select File...".toUpperCase()) {
		public void actionPerformed(ActionEvent e) {
			int val = chooser.showDialog(frame, "Ok".toUpperCase());
			if(val == JFileChooser.APPROVE_OPTION){
				fileStr = chooser.getSelectedFile().getPath();
				//check to see if user added extension, add it if they didn't
				if (!fileStr.contains(".jpg")){
					fileStr += ".jpg";
				}
			}
			//update label
			fileLbl.setText(fileStr);
			
			//enable the okay button if a valid file name is set  
			// and at least one source selected
			if(fileStr!=null && fileStr.length()>0 && fileStr.contains("jpg")){
				okayBtn.setEnabled(true);
			}
			
		}
	};
	
	private AbstractAction okayAct = new AbstractAction("OK".toUpperCase()) {
		public void actionPerformed(ActionEvent e) {
			//check world rect is not null
			if(wRect != null){
				
				frame.setVisible(false);
				//get projection and ppd
				final ProjObj po = Main.testDriver.mainWindow.getProj().getProjection();
				final int ppd = (Integer)ppdBx.getSelectedItem();
				
			    final BufferedImage finalImage = GraphicsEnvironment
					    .getLocalGraphicsEnvironment()
					    .getDefaultScreenDevice()
					    .getDefaultConfiguration()
					    .createCompatibleImage((int)(wRect.getWidth()*ppd), (int)(wRect.getHeight()*ppd), Transparency.OPAQUE);
					    
				//get the graphics object that will be drawn into by each layer
				final Graphics2D g2 = (Graphics2D)finalImage.getGraphics();
				
				
				//get all the layers (and use their lview3ds to get the data)
				final ArrayList<LView> views = new ArrayList<LView>(LManager.getLManager().viewList);
				
				//kick off a new thread, because the map layer will not run from the awt thread
				
				//by doing this there is no reason to keep the dialog around,
				// since it is all run in the background...but the user doesn't have
				// any kind of progress bar or know that work is being done...
				Thread manager = new Thread(new Runnable() {
					public void run() {
						
						//color map op is needed to convert images based on
						// the alpha value of their lview
						ColorMapOp cmo = new ColorMapOp(null);
						
						// they are already in order from bottom to top
						for(LView lview : views){
							//if not visible in main view, don't export
							if(!lview.isVisible()){
								continue;
							}
							//get the lview3d
							LView3D view3d = lview.getLView3D();
							System.err.println("Getting "+lview.getName()+" data for Hi Res Export");
							
							int scaleFactor = getScaleFactor();
							BufferedImage bi = view3d.createDataImage(wRect, po, ppd, scaleFactor);
							
							//draw the layer data on the final image
							// with the proper alpha value
							if(bi != null){
								g2.drawImage(bi, cmo.forAlpha(lview.getAlpha()), 0, 0);
							}
						}
						
						//write out the image
						try {
							ImageIO.write(finalImage, "JPEG", new File(fileStr));
						} catch (IOException e1) {
							success = false;
							e1.printStackTrace();
							System.err.println("Export failed.  Please see log for more information.");
						}
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if(success){
									clearData();
									JOptionPane.showMessageDialog(frame, "Export Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
								}else{
									clearData();
									JOptionPane.showMessageDialog(frame, "Export Failed, see log.", "Failure", JOptionPane.INFORMATION_MESSAGE);
									frame.setVisible(false);
								}
							}
						});
					}
				});
				
				manager.start();
				
			}
		}
	};
	
	/**
	 * @return If the scale checkbox is not selected, the returned int will be 1.
	 * Otherwise returns the value the labels should be scaled by, based
	 * on the current zoom level and the selected export zoom level.
	 */
	private int getScaleFactor(){
		int result = 1;
		
		if(scaleChk.isSelected()){
			result = ((Integer)ppdBx.getSelectedItem())/Main.testDriver.mainWindow.getZoomManager().getZoomPPD();
		}
		
		return result;
	}
	
	
	private AbstractAction cancelAct = new AbstractAction("Cancel".toUpperCase()) {
		public void actionPerformed(ActionEvent e) {
			clearData();
			//hide this dialog
			frame.setVisible(false);
		}
	};
	
	private void clearData(){
		//clear the world rectangle
		wRect = null;
		//clear the file string
		fileStr = " ";
		fileLbl.setText(fileStr);
		//disable okay
		okayBtn.setEnabled(false);
	}
}
