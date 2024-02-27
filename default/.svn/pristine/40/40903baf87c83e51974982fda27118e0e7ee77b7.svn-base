package edu.asu.jmars.layer.nomenclature;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import edu.asu.jmars.Main;
import edu.asu.jmars.graphics.SpatialGraphics2D;
import edu.asu.jmars.layer.FocusPanel;
import edu.asu.jmars.layer.InvestigateData;
import edu.asu.jmars.layer.LViewSettings;
import edu.asu.jmars.layer.Layer;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.layer.nomenclature.NomenclatureLView.NomenclatureSettings;
import edu.asu.jmars.swing.ColorButton;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.ThemeFont;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeText;
import edu.asu.jmars.util.Config;
import edu.asu.jmars.util.DebugLog;
import edu.asu.jmars.util.Util;
import edu.asu.jmars.viz3d.ThreeDManager;
import edu.emory.mathcs.backport.java.util.Arrays;

/* Class for Nomenclature layerview */
public class NomenclatureLView extends Layer.LView {
	public static final String NOMENCLATURE_DIR = "nomenclature/";// @since change bodies
	private static final DecimalFormat f = new DecimalFormat("0.###");
	private static DebugLog log = DebugLog.instance();
	List<MarsFeature> landmarks = new ArrayList<MarsFeature>();
	private Set<String> landmarkTypes = new HashSet<String>();
	private Map<MarsFeature, Rectangle2D> labelLocations = new ConcurrentHashMap<MarsFeature, Rectangle2D>();
	private Font labelFont = null;
	private NomenclatureLayer myLayer;
	TreeSet<String> sortedList = new TreeSet<String>();
	TreeSet<String> sortedListDetails = new TreeSet<String>();
	private static String SEARCH_PROMPT = "search for landmark...";

	public NomenclatureLView() {
		super(null);
	}

	public NomenclatureLView(NomenclatureLayer parent, NomenclatureLView3D lview3d) {
		super(parent, lview3d);
		myLayer = parent;

		if (lview3d != null) {
			lview3d.setLView(this);
		}

		/**
		 * Read in the file of landmarks on mars - a file was chosen instead of a db
		 * table since the data is fairly static and it will make it a hell of a lot
		 * easier for the student version - which is supposed to operate without network
		 * connectivity.
		 * 
		 * The format of the file is comma separated and should contain the following
		 * fields:
		 * 
		 * lanmark type, name, latitude(N), longitude (W), diameter (km),
		 * origin/description
		 * 
		 * File is in east planetocentric. Stored internally as west.
		 */

		try {
			// @since change bodies
			File nomenclatureFile = NomenclatureLView.getFeaturesFile();
			FileInputStream fis = new FileInputStream(nomenclatureFile);
			InputStreamReader isr = new InputStreamReader(fis, "utf-8");
			BufferedReader in = new BufferedReader(isr);
			// end change bodies
			String lineIn = in.readLine();
			while (lineIn != null && lineIn.compareToIgnoreCase("STOP") != 0) {
				MarsFeature mf = new MarsFeature();

				try {
					StringTokenizer tok = new StringTokenizer(lineIn, "\t");

					mf.landmarkType = tok.nextToken().trim();
					mf.name = tok.nextToken().trim();
					mf.latitude = Double.parseDouble(tok.nextToken());
					// USER east lon => JMARS west lon
					mf.longitude = (360 - Double.parseDouble(tok.nextToken())) % 360;
					mf.diameter = Double.parseDouble(tok.nextToken());
					mf.origin = tok.nextToken().trim();

					landmarks.add(mf);
					landmarkTypes.add(mf.landmarkType);

				} catch (NoSuchElementException ne) {
					// ignore
				}

				lineIn = in.readLine();
			}

			in.close();
		} catch (Exception e) {
			log.aprintln(e);
		}

		// define default items to show
		if (settings.showLandmarkTypes.size() == 0) {
			// @since change bodies
			String defaultLandmarkType = Config.get(Util.getProductBodyPrefix() + Config.CONFIG_DEFAULT_LANDMARK_TYPE);
			if (defaultLandmarkType != null && !"".equals(defaultLandmarkType)) {
				settings.showLandmarkTypes.add(defaultLandmarkType);
			} else {
				if (landmarks != null && landmarks.size() > 0) {
					settings.showLandmarkTypes.add(landmarks.get(0).landmarkType);
				}
			}
			// end change bodies
		}
	}

	public List<MarsFeature> getLandmarks() {
		return landmarks;
	}

	public List<MarsFeature> getShownLandmarks() {
		return landmarks.stream()
				.filter(landmark -> settings.showLandmarkTypes.contains(landmark.landmarkType))
				.collect(Collectors.toList());
	}

	/**
	 * @since change bodies
	 */
	public static File getFeaturesFile() {
		// check if there is a new version of the nomenclature for this body
		// with this check, we can eliminate the features.txt files from the jar file if
		// we choose - KJR 1/4/12
		String featureFileName = Main.getBody().toLowerCase() + "_features.txt";
		String remoteUrl = Config.get(Config.CONTENT_SERVER) + NOMENCLATURE_DIR + featureFileName;
		return Util.getCachedFile(remoteUrl, true);
	}

	/**
	 * This is good - build the focus panel on demand instead of during the view's
	 * constructor. That way, the saved session variables get propogated properly.
	 */
	public FocusPanel getFocusPanel() {
		if (focusPanel == null) {
			focusPanel = new FocusPanel(this, true);
			JScrollPane nomAdjustments = new JScrollPane(new NomenclaturePanel(),
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			nomAdjustments.getVerticalScrollBar().setUnitIncrement(10);
			focusPanel.add("Adjustments", nomAdjustments);
		}
		return focusPanel;
	}

	/** Event triggered whenever there is new data to (potentially) paint in */
	public synchronized void receiveData(Object layerData) {
		// do nothing
	}

	protected Object createRequest(Rectangle2D where) {
		return (null);
	}

	protected Layer.LView _new() {
		return new NomenclatureLView((NomenclatureLayer) getLayer(), null);
	}

	public String getName() {
		return "Nomenclature";
	}

	protected void viewChangedPost() {
		redraw();
		repaint();

		if (ThreeDManager.getInstance().isReady()) {
			ThreeDManager.getInstance().updateDecalsForLView(this, true);
		}
	}

	private void redrawAll() {
		redraw();
		repaint();

		if (getChild() != null) {
			((NomenclatureLView) getChild()).redraw();
			((NomenclatureLView) getChild()).repaint();
		}

		if (ThreeDManager.getInstance().isReady()) {
			ThreeDManager.getInstance().updateDecalsForLView(this, true);
		}
	}

	private synchronized void redraw() {
		labelFont = ThemeFont.getBold();
		labelLocations.clear();
		clearOffScreen();

		Graphics2D g1 = getOffScreenG2Direct();
		if (g1 == null) {
			return;
		}	        
		g1.setColor(settings.labelColor);
		Graphics2D g2 = getOffScreenG2();
		SpatialGraphics2D spatialG2 = getProj().createSpatialGraphics(g2);
		if (spatialG2 == null) {
			log.println("Skipping redraw(), since there is no graphics to draw to");
			return;
		}

		for (int i = 0; i < landmarks.size(); i++) {
			MarsFeature mf = landmarks.get(i);
			if (settings.showLandmarkTypes.contains(mf.landmarkType)) {
				Point2D p = new Point2D.Double(mf.longitude, mf.latitude);
				Point2D[] worldPoints = spatialG2.spatialToWorlds(p);
				mf.draw(g1, g2, worldPoints);
			}
		}
	}

	protected Component[] getContextMenuTop(Point2D worldPt) {
		Component[] menuItems = super.getContextMenuTop(worldPt);

		String msg = getStringForPoint(worldPt, true);

		if (msg == null)
			return menuItems;

		Component[] descriptionItems = new Component[menuItems.length + 1];

		// transfer menus into the new parent
		descriptionItems[0] = new JMenuItem(msg);
		for (int i = 0; i < menuItems.length; i++)
			descriptionItems[i + 1] = menuItems[i];

		return descriptionItems;
	}

	protected Component[] getContextMenu(Point2D worldPt) {
		Component[] menuItems = super.getContextMenu(worldPt);

		final String msg = getStringForPoint(worldPt, false);

		if (msg == null)
			return menuItems;

		// transfer menus into the new parent
		Component[] newMenuItems = new Component[menuItems.length + 1];
		for (int i = 0; i < menuItems.length; i++) {
			newMenuItems[i] = menuItems[i];
		}

		newMenuItems[menuItems.length] = new JMenuItem(
				new AbstractAction("Copy Nomenclature Description to Clipboard") {
					public void actionPerformed(ActionEvent e) {
						StringSelection sel = new StringSelection(msg);
						getToolkit().getSystemClipboard().setContents(sel, sel);
						Main.setStatus("Nomenclature description copied to clipboard");
					}
				});

		return newMenuItems;
	}

	public String getToolTipText(MouseEvent event) {
		if (!settings.enableLabelTips)
			return "";

		try {
			Point2D mouseWorld = getProj().screen.toWorld(event.getPoint());
			return getStringForPoint(mouseWorld, true);
		} catch (Exception ex) {
			// ignore
		}

		return "";
	}

	// Used to display information for InvestigateTool
	public InvestigateData getInvestigateData(MouseEvent event) {
		InvestigateData invData = new InvestigateData(getName());

		Point2D worldPoint = getProj().screen.toWorld(event.getPoint());
		Set<MarsFeature> copyKeys = new HashSet<MarsFeature>(labelLocations.keySet());

		for (MarsFeature mf : copyKeys) {
			Rectangle2D r = labelLocations.get(mf);

			if (settings.showLandmarkTypes.contains(mf.landmarkType)) {
				if (r != null && r.contains(worldPoint)
						|| r.contains(new Point2D.Double(worldPoint.getX() - 360.0, worldPoint.getY()))) {
					String key = mf.name + " (" + f.format((360 - mf.longitude) % 360) + "E, " + f.format(mf.latitude)
							+ "N)";
					String val = "Diameter=" + mf.diameter + "km. Origin=" + mf.origin;

					invData.add(key, val);
				}
			}
		}
		return invData;
	}

	protected String getStringForPoint(Point2D worldPoint, boolean showAsHTML) {
		for (MarsFeature mf : labelLocations.keySet()) {
			Rectangle2D r = labelLocations.get(mf);

			if (settings.showLandmarkTypes.contains(mf.landmarkType)) {
				if (r != null && r.contains(worldPoint)
						|| r.contains(new Point2D.Double(worldPoint.getX() - 360.0, worldPoint.getY()))) {
					return mf.getPopupInfo(showAsHTML);
				}
			}
		}

		return null;
	}

	static class NomenclatureSettings extends LViewSettings {
		private static final long serialVersionUID = 3853034040394687088L;
		boolean showMainPoints = true;
		boolean showPannerPoints = true;
		boolean show3DPoints = true;
		Color pointColor = Color.red;
		boolean showMainLabels = true;
		boolean showPannerLabels = true;
		boolean show3DLabels = false;
		Color labelColor = Color.white;
		boolean enableLabelTips = true;
		ArrayList<String> showLandmarkTypes = new ArrayList<String>();
		ArrayList<String> showLandmarks = new ArrayList<String>();
		Font labelFont = ThemeFont.getRegular();
		Color outlineColor = ((ThemeText) GUITheme.get("text")).getTextcontrastcolor();
	}

	NomenclatureSettings settings = new NomenclatureSettings();

	/**
	 * Override to update view specific settings
	 */
	protected void updateSettings(boolean saving) {
		if (saving) {
			viewSettings.put("nomenclature", settings);
		} else {
			if (viewSettings.containsKey("nomenclature")) {
				settings = (NomenclatureSettings) viewSettings.get("nomenclature");	
				checkSettings(settings);
				LView pannerView = (NomenclatureLView) getChild();
				if (pannerView != null) {
					((NomenclatureLView) pannerView).settings = settings;
				}
				// need to rebuild focus panel to reflect session settings
				this.focusPanel = null;
			}
		}
	}

	private void checkSettings(NomenclatureSettings settings) {
		if (settings.showLandmarkTypes == null)
			settings.showLandmarkTypes = new ArrayList<String>();
		if (settings.showLandmarks == null)
			settings.showLandmarks = new ArrayList<String>();		
	}

	/**
	 * Clear the settings so if the factory uses this object again, the settings do
	 * not remain.
	 * 
	 * @since change bodies
	 */
	public void clearSettings() {
		settings = new NomenclatureSettings();
	}

	private class NomenclaturePanel extends JPanel
			implements ActionListener, ListSelectionListener, MouseListener, KeyListener {
		JCheckBox chkShowMainPoints;
		JCheckBox chkShowPannerPoints;
		JCheckBox chkShow3dPoints;
		ColorButton btnPointColor;
		JCheckBox chkShowMainLabels;
		JCheckBox chkShowPannerLabels;
		JCheckBox chkShow3dLabels;
		ColorButton btnLabelColor;
		JCheckBox chkEnableLabelTips;
		JList listLandmarks, listLandmarksDetail;
		JButton btnGoto;
		JButton btnAll;
		JButton btnClear, labelFormat;
		TreeSet<String> sortedList = new TreeSet<String>();
		JTextField landmarkSearchField;
		JScrollPane landmarkScrollPane;
		JSplitPane landmarkSplitPane;

		NomenclaturePanel() {
			// Set up the layout and borders
			setLayout(new BorderLayout(10, 10));
			Box box = Box.createVerticalBox();
			add(box, BorderLayout.CENTER);

			JPanel p0 = new JPanel(new GridLayout(4, 2));
			p0.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Points:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			box.add(p0);

			chkShowMainPoints = new JCheckBox("Show Points in Main", settings.showMainPoints);
			chkShowMainPoints.addActionListener(this);
			p0.add(chkShowMainPoints);

			chkShowPannerPoints = new JCheckBox("Show Points in Panner", settings.showPannerPoints);
			chkShowPannerPoints.addActionListener(this);
			p0.add(chkShowPannerPoints);

			chkShow3dPoints = new JCheckBox("Show Points in 3D View", settings.show3DPoints);
			chkShow3dPoints.addActionListener(this);			
			chkShow3dPoints.setEnabled(true);
			p0.add(chkShow3dPoints);

			JPanel colorbuttonPanel = new JPanel(new BorderLayout());
			btnPointColor = new ColorButton("Point Color".toUpperCase(), settings.pointColor, false);
			btnPointColor.setBackground(settings.pointColor);
			btnPointColor.addPropertyChangeListener(colorListener);
			colorbuttonPanel.add(btnPointColor, BorderLayout.WEST);
			p0.add(colorbuttonPanel, BorderLayout.SOUTH);

			JPanel p1 = new JPanel(new GridLayout(4, 0));
			p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Labels:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			box.add(p1);

			chkShowMainLabels = new JCheckBox("Show Labels in Main", settings.showMainLabels);
			chkShowMainLabels.addActionListener(this);
			p1.add(chkShowMainLabels);

			chkShowPannerLabels = new JCheckBox("Show Labels in Panner", settings.showPannerLabels);
			chkShowPannerLabels.addActionListener(this);
			p1.add(chkShowPannerLabels);

			chkShow3dLabels = new JCheckBox("Show Labels in 3D View", settings.show3DLabels);
			// chkShow3dLabels.addActionListener(this);
			/* temporary disabled until 3D library can render labels well (10/14/2019) */
			chkShow3dLabels.setEnabled(false);
			p1.add(chkShow3dLabels);

			JPanel labelcolorbuttonPanel = new JPanel(new BorderLayout());
			btnLabelColor = new ColorButton("Label Color".toUpperCase(), settings.labelColor, false);
			btnLabelColor.setBackground(settings.labelColor);
			btnLabelColor.addPropertyChangeListener(colorListener);
			labelcolorbuttonPanel.add(btnLabelColor, BorderLayout.WEST);
			p1.add(labelcolorbuttonPanel, BorderLayout.SOUTH);

			JPanel p2 = new JPanel(new GridLayout(1, 0));
			p2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Tooltips:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			box.add(p2);

			chkEnableLabelTips = new JCheckBox("Enable Label Tips", settings.enableLabelTips);
			chkEnableLabelTips.addActionListener(this);
			p2.add(chkEnableLabelTips);

			JPanel p3 = new JPanel(new BorderLayout());
			p3.setBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Selected Landmarks Types:"),
							BorderFactory.createEmptyBorder(5, 5, 5, 5)));			
			box.add(p3);
            
			JPanel landmarkPanel = new JPanel(new GridLayout(1, 0));			
			p3.add(landmarkPanel, BorderLayout.CENTER);

			for (String key : landmarkTypes) {
				sortedList.add(key);
			}

			listLandmarks = new JList(sortedList.toArray());
			listLandmarks.setPrototypeCellValue("this is a prototypical value");
			listLandmarks.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			listLandmarks.addListSelectionListener(this);
			listLandmarks.setToolTipText(
					"This list indicates which landmarks appear. Use the shift and control keys with the mouse to make multiple selections.");

			listLandmarksDetail = new JList(sortedListDetails.toArray());

			JScrollPane listScrollPane = new JScrollPane(listLandmarks);

			listLandmarksDetail.setCellRenderer(new ComboBoxRenderer());
			listLandmarksDetail.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listLandmarksDetail.setPrototypeCellValue("this is a prototypical value");
			listLandmarksDetail.addMouseListener(this);
			listLandmarksDetail.addKeyListener(this);
			listLandmarksDetail.addListSelectionListener(this);
			listLandmarksDetail.setToolTipText("Double Click on an Item to see it on the map.");
		
			JPanel landmarkScrollPanel = new JPanel(new BorderLayout());
			landmarkSearchField = new JTextField();
			landmarkSearchField.setText(SEARCH_PROMPT);
			landmarkSearchField.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					if ((SEARCH_PROMPT).equals(landmarkSearchField.getText().trim()))
						landmarkSearchField.setText("");	
				}

				@Override
				public void focusLost(FocusEvent e) {
					if ("".equals(landmarkSearchField.getText().trim()))
						landmarkSearchField.setText(SEARCH_PROMPT);			
				}
			});

			landmarkSearchField.setToolTipText("Enter part of a landmark to narrow list, press enter to see more results");
			
			landmarkSearchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				pareSearchResults();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {				
				pareSearchResults();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				pareSearchResults();
			}
		});
			
			JPanel textfieldPanel = new JPanel(new BorderLayout());
			textfieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 0));
			textfieldPanel.add(landmarkSearchField);

			landmarkScrollPane = new JScrollPane(listLandmarksDetail);
			landmarkScrollPanel.add(textfieldPanel, BorderLayout.NORTH);
			landmarkScrollPanel.add(landmarkScrollPane, BorderLayout.CENTER);

			landmarkSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, landmarkScrollPanel);
			landmarkSplitPane.setDividerLocation(100);
			landmarkScrollPane.setPreferredSize(new Dimension(100, 0));
			landmarkScrollPane.addKeyListener(this);
			landmarkScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

			landmarkPanel.add(landmarkSplitPane, BorderLayout.CENTER);
			JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

			btnGoto = new JButton("Goto".toUpperCase());
			btnGoto.addActionListener(this);
			btnGoto.setEnabled(false);
			btnGoto.setToolTipText(
					"Select a landmark type and then specific landmark, then click here to go to that landmark.");
			buttonPanel.add(btnGoto, BorderLayout.WEST);
			p3.add(buttonPanel, BorderLayout.SOUTH);
			setSelectedItems();
			updateList();		
		}
		
		private void pareSearchResults()
		{		
			listLandmarksDetail.setListData(sortedListDetails.toArray());	
			
			String[] possibleRecords = new String[listLandmarksDetail.getModel().getSize()];
			
			for (int i = 0; i < listLandmarksDetail.getModel().getSize(); i++) {
				possibleRecords[i] = (String) listLandmarksDetail.getModel().getElementAt(i);
			}
			
			ArrayList<String> limitedValues = new ArrayList<String>();
			String searchText = landmarkSearchField.getText();
			
			for (String value : possibleRecords) {
				int indexValue = value.toLowerCase().indexOf(searchText.toLowerCase());
				if (indexValue != -1) {
					limitedValues.add(value);
				}
			}
			
			if (limitedValues.isEmpty())
			{				
				limitedValues.addAll(Arrays.asList(possibleRecords)); 
			}
			settings.showLandmarks.clear();
			settings.showLandmarks.addAll(limitedValues);
			getPannerViewSettings().ifPresent(psettings -> {
				psettings.showLandmarks.clear();
				psettings.showLandmarks.addAll(limitedValues);
			});
	
			listLandmarksDetail.setListData((String[]) limitedValues.toArray(new String[limitedValues.size()]));
			listLandmarksDetail.setPreferredSize(new Dimension(700, listLandmarksDetail.getModel().getSize() * 15));
	
			if (listLandmarksDetail.getModel().getSize() > 0) {
				listLandmarksDetail.setSelectedIndex(0);
			}
		}		

		private PropertyChangeListener colorListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String propertyName = evt.getPropertyName();
				if ("background".equals(propertyName)) {
					if (btnPointColor.isColorSelected()) {
						Color newColor = btnPointColor.getColor();
						if (newColor != null) {
							btnPointColor.setBackground(newColor);
							settings.pointColor = newColor;
							getPannerViewSettings().ifPresent(psettings -> {
								psettings.pointColor = newColor;
							});
							redrawAll();
							myLayer.increaseStateId(NomenclatureLayer.IMAGES_BUFFER);
						}
						btnPointColor.setColorSelected(false);
					} else if (btnLabelColor.isColorSelected()) {
						Color newColor = btnLabelColor.getColor();
						if (newColor != null) {
							btnLabelColor.setBackground(newColor);
							settings.labelColor = newColor;
							getPannerViewSettings().ifPresent(psettings -> {
								psettings.labelColor = newColor;
							});
							redrawAll();
							myLayer.increaseStateId(NomenclatureLayer.LABELS_BUFFER);
						}
						btnLabelColor.setColorSelected(false);
					}
				}
			}
		};

		protected void setSelectedItems() {
			// now mark all of the types which are not hidden
			int[] indexes = new int[settings.showLandmarkTypes.size()];

			int i = 0;
			int j = 0;
			for (String type : sortedList) {
				if (settings.showLandmarkTypes.contains(type)) {
					indexes[j++] = i;
				}
				i++;
			}
			listLandmarks.setSelectedIndices(indexes);
		}

		protected void updateList() {
			sortedListDetails.removeAll(sortedListDetails);
			listLandmarksDetail.removeAll();
			for (MarsFeature key : landmarks) {
				if (settings.showLandmarkTypes.contains(key.landmarkType)) {
					sortedListDetails.add(key.name);
				} else if (settings.showLandmarkTypes.contains(" All")) {
					sortedListDetails.add(key.name);
				}
			}			
			listLandmarksDetail.setListData(sortedListDetails.toArray());
			listLandmarksDetail.setPreferredSize(new Dimension(700, listLandmarksDetail.getModel().getSize() * 15));
			btnGoto.setEnabled(true);
			listLandmarksDetail.setSelectedIndex(0);
		}

		public void mouseClicked(MouseEvent e) {			
			if (e.getClickCount() > 1) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				for (int i = 0; i < landmarks.size(); i++) {
					MarsFeature mf = (MarsFeature) landmarks.get(i);

					if (mf.name.compareTo((String) listLandmarksDetail.getSelectedValue()) == 0) {
						Point2D worldPoint = Main.PO.convSpatialToWorld(new Point2D.Double(mf.longitude, mf.latitude));
						centerAtPoint(worldPoint);
						break;
					}
				}
				this.setCursor(Cursor.getDefaultCursor());
			}
		}

		public void valueChanged(ListSelectionEvent e) {
			Object source = e.getSource();             
			if (source == listLandmarks) {			
				
				if (e.getValueIsAdjusting())
					return;
				landmarkSearchField.setText(SEARCH_PROMPT);
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				Object[] selVals = listLandmarks.getSelectedValues();
				List<Object> tmpList = new ArrayList<Object>();
				for (Object selVal : selVals) {
					tmpList.add(selVal);
				}
				settings.showLandmarks.clear();
				settings.showLandmarkTypes.clear();
				getPannerViewSettings().ifPresent(psettings -> {
					psettings.showLandmarks.clear();
					psettings.showLandmarkTypes.clear();
				});

				for (String type : sortedList) {
					if (tmpList.contains(type)) {
						settings.showLandmarkTypes.add(type);
						getPannerViewSettings().ifPresent(psettings -> {
							psettings.showLandmarkTypes.add(type);
						});
					}
				}
				listLandmarksDetail.setPreferredSize(new Dimension(700, listLandmarksDetail.getModel().getSize() * 15));
				updateList();
				redrawAll();
				this.setCursor(Cursor.getDefaultCursor());
			} else if (source == listLandmarksDetail) {				
				if (listLandmarksDetail.getSelectedValue() != null) {
					btnGoto.setEnabled(true);
					String landmarkSelected = (String) listLandmarksDetail.getSelectedValue();
					for (MarsFeature mf : landmarks) {
						if (mf.name.compareTo(landmarkSelected) == 0) {

							String description = mf.name + "--" + mf.landmarkType + "--" + mf.origin + "\n||"
									+ mf.latitude + "E  " + mf.longitude + "N ";
						}
					}
				} else {
					btnGoto.setEnabled(false);
				}
			}
		}
	
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == chkShowMainPoints && settings.showMainPoints != chkShowMainPoints.isSelected()) {
				settings.showMainPoints = chkShowMainPoints.isSelected();
				redrawAll();
			} else if (source == chkShowMainLabels && settings.showMainLabels != chkShowMainLabels.isSelected()) {
				settings.showMainLabels = chkShowMainLabels.isSelected();
				redrawAll();
			} else if (source == chkShow3dPoints && settings.show3DPoints != chkShow3dPoints.isSelected()) {
				settings.show3DPoints = chkShow3dPoints.isSelected();
				// increase the proper state id buffer
				myLayer.increaseStateId(NomenclatureLayer.IMAGES_BUFFER);
				if (ThreeDManager.getInstance().isReady()) {
					ThreeDManager.getInstance().updateDecalsForLView(NomenclatureLView.this, true);
				}
			} else if (source == chkShowPannerPoints) {
				getPannerViewSettings().ifPresent(psettings -> {
					if (psettings.showPannerPoints != chkShowPannerPoints.isSelected()) {
						psettings.showPannerPoints = chkShowPannerPoints.isSelected();
						redrawAll();
					}
				});
			} else if (source == chkShowPannerLabels) {
				getPannerViewSettings().ifPresent(psettings -> {
					if (psettings.showPannerLabels != chkShowPannerLabels.isSelected()) {
						psettings.showPannerLabels = chkShowPannerLabels.isSelected();
						redrawAll();
					}
				});
			} else if (source == chkShow3dLabels && settings.show3DLabels != chkShow3dLabels.isSelected()) {
				settings.show3DLabels = chkShow3dLabels.isSelected();
				// increase the proper state id buffer
				myLayer.increaseStateId(NomenclatureLayer.LABELS_BUFFER);
				if (ThreeDManager.getInstance().isReady()) {
					ThreeDManager.getInstance().updateDecalsForLView(NomenclatureLView.this, true);
				}
			} else if (source == chkEnableLabelTips) {
				if (settings.enableLabelTips != chkEnableLabelTips.isSelected())
					settings.enableLabelTips = chkEnableLabelTips.isSelected();
				getPannerViewSettings().ifPresent(psettings -> {
					if (psettings.enableLabelTips != chkEnableLabelTips.isSelected())
						psettings.enableLabelTips = chkEnableLabelTips.isSelected();
				});				
			} else if (source == btnGoto) {
				if (listLandmarksDetail.getSelectedIndex() != -1) {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					for (int i = 0; i < landmarks.size(); i++) {
						MarsFeature mf = (MarsFeature) landmarks.get(i);
						if (mf.name.compareTo((String) listLandmarksDetail.getSelectedValue()) == 0) {
							Point2D worldPoint = Main.PO
									.convSpatialToWorld(new Point2D.Double(mf.longitude, mf.latitude));
							centerAtPoint(worldPoint);
							break;
						}
					}

					this.setCursor(Cursor.getDefaultCursor());
				}
			}
		}// actionPerformed

	@Override
	public void keyPressed(KeyEvent e) {			
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (listLandmarksDetail.getSelectedIndex() != -1) {
			    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				for (int i = 0; i < landmarks.size(); i++) {
					MarsFeature mf = (MarsFeature) landmarks.get(i);							
					if (mf.name.compareTo((String) listLandmarksDetail.getSelectedValue()) == 0) {
						Point2D worldPoint = Main.PO
								.convSpatialToWorld(new Point2D.Double(mf.longitude, mf.latitude));
						centerAtPoint(worldPoint);
						break;
				 }
			  }
				   this.setCursor(Cursor.getDefaultCursor());
			}
		}
	}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}	

		private Optional<NomenclatureSettings> getPannerViewSettings() {
			LView pannerView = getChild();
			if (pannerView != null) {
				return Optional.ofNullable(((NomenclatureLView) pannerView).settings);
			}

			return Optional.empty();
		}

		@Override
		public void keyReleased(KeyEvent e) {			
		}

	}// NomenclaturePanel

	public class MarsFeature {
		public String landmarkType = "";
		public String name = "";
		public double latitude;
		public double longitude;
		public double diameter;
		public String origin = "";
		// do not assume this to be the same value always
		public Point2D thisWorldPoint = new Point2D.Double(0, 0);

		public String getPopupInfo(boolean showAsHTML) {
			String info = "";

			if (showAsHTML)
				info += "<html>";

			try {

				info += name;

				if (name.indexOf(landmarkType) == -1)
					info += " " + landmarkType;

				// JMARS west lon => USER east lon
				info += " (" + f.format((360 - longitude) % 360) + "E, " + f.format(latitude) + "N, "
						+ f.format(this.diameter) + "km )";

				if (showAsHTML)
					info += "<br>";

				info += origin;
			} catch (Exception ex) {
				// ignore
			}

			if (showAsHTML)
				info += "</html>";

			return info;
		}

		public void draw(Graphics2D g1, Graphics2D g2, Point2D[] worldPoints) {
			// nothing to draw
			if (worldPoints == null || worldPoints.length == 0)
				return;

			Rectangle2D worldwin = getProj().getWorldWindow();

			for (int i = 0; i < worldPoints.length; i++) {

				if (!worldwin.contains(worldPoints[i]))
					continue;

				drawPoint(g2, worldPoints[i]);
				drawLabel(g1, g2, worldPoints[i]);

			}
		}

		protected void drawPoint(Graphics2D g2, Point2D worldPoint) {
			if (!settings.showMainPoints && getChild() != null)
				return;

			if (!settings.showPannerPoints && getChild() == null)
				return;

			Dimension2D pSize = getProj().getPixelSize();
			int zoomFactor = 1;

			pSize.setSize(pSize.getWidth() * zoomFactor, pSize.getHeight() * zoomFactor);

			g2.setPaint(settings.pointColor);
			g2.setStroke(new BasicStroke(0));
			Rectangle2D.Double box = new Rectangle2D.Double(worldPoint.getX() - pSize.getWidth() * 2,
					worldPoint.getY() + pSize.getHeight() * 2, pSize.getWidth() * 4, pSize.getHeight() * 4);
			g2.fill(box);

			// store the labels location
			labelLocations.put(this, box);
		}

		protected boolean inConflict(Rectangle2D r) {
			// check for conflicts with other locations
			for (int i = 0; i < landmarks.size(); i++) {

				MarsFeature mf = (MarsFeature) landmarks.get(i);

				// skip this one
				if (mf.equals(MarsFeature.this))
					continue;

				if (r.contains(mf.thisWorldPoint))
					return true;
			}

			// check for conflicts with other labels
			for (Rectangle2D existingRect : labelLocations.values()) {
				if (r.intersects(existingRect))
					return true;
			}

			return false;
		}

		protected void drawLabel(Graphics2D g1, Graphics2D g2, Point2D pt) {

			if (!settings.showMainLabels && getChild() != null)
				return;

			if (!settings.showPannerLabels && getChild() == null)
				return;

			FontMetrics fontMetrics = g2.getFontMetrics(labelFont);

			// calculate new label location and see if it intersects with one
			// already.

			// get the width and enlarge by 20%
			double xWidth = fontMetrics.stringWidth(name) * getProj().getPixelSize().getWidth() * 1.2;
			double xLoc = pt.getX() - xWidth / 2;

			double yHeight = fontMetrics.getHeight() * getProj().getPixelSize().getHeight();
			// we want to include the point in the bounds
			double yLoc = pt.getY() - yHeight * .7;

			Rectangle2D.Double labelLoc = new Rectangle2D.Double(xLoc, yLoc, xWidth, yHeight);
			if (inConflict(labelLoc)) {
				// try some different locations if there is a conflict with
				// other labels or points
				yLoc = pt.getY() + yHeight * .3;
				labelLoc = new Rectangle2D.Double(xLoc, yLoc, xWidth, yHeight);

				if (inConflict(labelLoc)) {
					xLoc = pt.getX() + getProj().getPixelSize().getWidth() * 5;
					yLoc = pt.getY();
					labelLoc = new Rectangle2D.Double(xLoc, yLoc, xWidth, yHeight);
				}
			}

			// store the labels location - ok to overwrite point locations since
			// they are inclusive
			labelLocations.put(this, labelLoc);

			pt = getProj().world.toScreen(labelLoc.getX(), labelLoc.getY());

			// g2.draw(labelLoc);
			g1.drawString(name, (float) pt.getX(), (float) pt.getY());
		}
	}

	// The following two methods are used to query for the
	// info panel fields (description, citation, etc)
	public String getLayerKey() {
		return "Nomenclature";
	}

	public String getLayerType() {
		return "nomenclature";
	}

	// comobox start
	class ComboBoxRenderer extends JLabel implements ListCellRenderer {

		public int height = 15;
		public int width = 20;
		String stringToPaint = "";

		public ComboBoxRenderer() {

			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		private String getToolTip(String objectName) {

			for (NomenclatureLView.MarsFeature mf : getLandmarks()) {
				if (mf.name.equals(objectName)) {
					return mf.origin;
				}
			}
			return "Description not found";
		}

		/*
		 * This method finds the image and text corresponding to the selected value and
		 * returns the label, set up to display the text and image.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			String selectedItem = ((String) value);

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			this.setHorizontalAlignment(SwingConstants.LEFT);
			this.setText(selectedItem);
			this.setToolTipText(getToolTip(selectedItem));
			this.setIgnoreRepaint(true);
			this.setOpaque(true);

			return this;
		}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				int string1Width = g2.getFontMetrics().stringWidth(this.getText());
				int fontHeight = g2.getFontMetrics().getHeight();				
				g2.setFont(ThemeFont.getBoldItalic());
				g2.drawString(this.getToolTipText(), string1Width + 5, fontHeight - 4);
				g2.setStroke(new BasicStroke(1));
			} finally {
				g2.dispose();
			}
		}

		public int getWidth() {
			return 700;
		}

		public int getHeight() {
			return 15;
		}
	} // comboboxRender		
	
	
}// NomenclatureLView
