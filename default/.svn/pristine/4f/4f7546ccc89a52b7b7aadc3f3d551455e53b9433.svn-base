/**
 * This class is the primary interface class for 3D rendering in J-Asteroid.
 *
 * not thread safe
 */
package edu.asu.jmars.viz3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

import edu.asu.jmars.Main;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.layer.Layer.LView3D;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.ThemeFont;
import edu.asu.jmars.util.Config;
import edu.asu.jmars.util.DebugLog;
import edu.asu.jmars.util.MovableList;
import edu.asu.jmars.util.Util;
import edu.asu.jmars.viz3d.core.geometry.TriangleMesh;
import edu.asu.jmars.viz3d.renderer.gl.GLRenderable;
import edu.asu.jmars.viz3d.renderer.gl.GLRenderableSet;
import edu.asu.jmars.viz3d.renderer.gl.event.FittingListener;
import edu.asu.jmars.viz3d.renderer.gl.event.IntersectListener;
import edu.asu.jmars.viz3d.renderer.gl.event.SynchronizeListener;
import edu.asu.jmars.viz3d.renderer.gl.event.SynchronizeResult;
import edu.asu.jmars.viz3d.renderer.gl.queues.DeleteQueue;
import edu.asu.jmars.viz3d.renderer.gl.queues.RenderQueue;
import edu.asu.jmars.viz3d.renderer.gl.scene.Scene;
import edu.asu.jmars.viz3d.renderer.textures.Decal;
import edu.asu.jmars.viz3d.renderer.textures.DecalKey;
import edu.asu.jmars.viz3d.renderer.textures.DecalSet;
import edu.asu.jmars.viz3d.renderer.textures.GlobalDecalFactory;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeMenuBar;

public final class ThreeDManager {

	private HashMap<String, TriangleMesh> shapeModels = new HashMap<>();
	private RenderQueue renderQueue;
	private DeleteQueue deleteQueue;
	private JFrame frame;
	private GLJPanel window;
	private Scene scene;
	private TriangleMesh mesh;
	private JPanel buttonPanel;
	private JFrame controlFrame;
	private static ThreeDManager instance = null;
	private Map<DecalKey, ArrayList<DecalSet>> layerDecals = new HashMap<DecalKey, ArrayList<DecalSet>>();
	private Map<LView3D, GLRenderableSet> layerRenderables = new IdentityHashMap<LView3D, GLRenderableSet>();
	private int ppd = 8;
	private List<LView> lviews = new ArrayList<LView>();

	/**
	 * Used to allow enabling/disabling of close fitting of any polygons or lines to
	 * shape model if one exists
	 */
	public static boolean CLOSE_FITTING = Config.get("viz3d.enable3DCloseFitting", true);

	private static DebugLog log = DebugLog.instance();
	/**
	 * Public factory method to create/acquire the ThreeDManager singleton.
	 * 
	 * thread-safe
	 */
	public static ThreeDManager getInstance() {
		if (instance == null) {
			instance = new ThreeDManager(1, 620, 400, "JMARS 3D");
		}
		return instance;
	}

	// actual class constructor
	private ThreeDManager(int numOfRenderQueues, int windowWidth, int windowHeight, String windowTitle) {

		renderQueue = RenderQueue.getInstance(); // set up messaging queues
		deleteQueue = DeleteQueue.getInstance();

		// need to instantiate the GLProfile and GLCapabilities before anything can be
		// rendered
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int bitDepth = device.getDisplayMode().getBitDepth();
		caps.setRedBits(bitDepth / 4);
		caps.setBlueBits(bitDepth / 4);
		caps.setGreenBits(bitDepth / 4);
		// we do NOT want to set Alpha bits so we can avoid Mac OS X issues

		JFrame frame = new JFrame();
		final GLJPanel window = new GLJPanel(caps);

		window.setSize(windowWidth, windowHeight);

		frame.setTitle(windowTitle);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		frame.setLayout(new BorderLayout());
		frame.add(window, BorderLayout.CENTER);

		// TODO need to initialize the second argument to the body radius from
		// the config file for the general case.
		// Currently the Config file does not provide this value.
		scene = new Scene(window, 0.2887515f);

		// Add a file menu which will have the details for the controls
		// along with anything else that becomes necessary
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		JMenu synchMenu = new JMenu("Sync");
		final JFrame thisFrame = frame;
		JMenuItem screenSave = new JMenuItem(new AbstractAction("Capture Screen as PNG") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Capture to PNG File");
				chooser.setDialogType(JFileChooser.SAVE_DIALOG);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG - Portable Network Graphics", "png",
						"PNG");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showSaveDialog(thisFrame);
				String filename = null;
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					filename = chooser.getCurrentDirectory() + System.getProperty("file.separator")
							+ chooser.getSelectedFile().getName();
					if (filename != null && !filename.toLowerCase().contains(".png")) {
						filename = filename + ".png";
					}
				}

				if (filename == null) {
					return;
				}
				scene.printScreen(filename);
				window.repaint();
			}

		});

		JMenuItem controlItm = new JMenuItem(new AbstractAction("View 3D Controls") {
			public void actionPerformed(ActionEvent e) {
				// build the frame and panel to display
				if (controlFrame == null) {
					controlFrame = new JFrame();
					// hide the window when it's closed instead of disposing of it
					controlFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					controlFrame.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent windowEvent) {
							controlFrame.setVisible(false);
						}
					});

					// build display
					JPanel backPnl = new JPanel();
					backPnl.setLayout(new BorderLayout());
					backPnl.setBackground(Util.lightBlue);
					backPnl.setBorder(new EmptyBorder(8, 8, 8, 8));
					JPanel controlPnl = new JPanel();
					controlPnl.setLayout(new GridBagLayout());
					controlPnl.setBorder(new TitledBorder("3D Controls"));
					JLabel keyLbl = new JLabel("Key Operations");
					JLabel mouseLbl = new JLabel("Mouse Operations");
					Font headerFont = ThemeFont.getBold().deriveFont(15f);
					Map attributes = headerFont.getAttributes();
					attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
					keyLbl.setFont(headerFont.deriveFont(attributes));
					mouseLbl.setFont(headerFont.deriveFont(attributes));

					// Control Labels
					Font descripFont = ThemeFont.getRegular();
					JLabel leftLbl = new JLabel("Translate scene to the left");
					leftLbl.setFont(descripFont);
					JLabel rightLbl = new JLabel("Translate scene to the right");
					rightLbl.setFont(descripFont);
					JLabel upLbl = new JLabel("Translate scene up");
					upLbl.setFont(descripFont);
					JLabel downLbl = new JLabel("Translate scene down");
					downLbl.setFont(descripFont);
					JLabel zLbl = new JLabel("Zoom out");
					zLbl.setFont(descripFont);
					JLabel ZLbl = new JLabel("Zoom in");
					ZLbl.setFont(descripFont);
					JLabel bLbl = new JLabel("Hide the shape model");
					bLbl.setFont(descripFont);
					JLabel BLbl = new JLabel("Display the shape model");
					BLbl.setFont(descripFont);
					JLabel fLbl = new JLabel("Do not fit camera FOVs to the shape model");
					fLbl.setFont(descripFont);
					JLabel FLbl = new JLabel("Fit camera FOVs to the shape model");
					FLbl.setFont(descripFont);
					JLabel gLbl = new JLabel("Hide shape model facet grid");
					gLbl.setFont(descripFont);
					JLabel GLbl = new JLabel("Display shape model facet grid");
					GLbl.setFont(descripFont);
					JLabel pLbl = new JLabel("Hide shape model vertex points");
					pLbl.setFont(descripFont);
					JLabel PLbl = new JLabel("Display shape model vertex points");
					PLbl.setFont(descripFont);
					JLabel aLbl = new JLabel("Hide axis indicator");
					aLbl.setFont(descripFont);
					JLabel ALbl = new JLabel("Display axis indicator");
					ALbl.setFont(descripFont);
					JLabel nLbl = new JLabel("Hide North Pole indicator");
					nLbl.setFont(descripFont);
					JLabel NLbl = new JLabel("Display North Pole indicator");
					NLbl.setFont(descripFont);
					JLabel rLbl = new JLabel("Reset view to original state");
					rLbl.setFont(descripFont);
					JLabel scrollLbl = new JLabel("Zoom in/out");
					scrollLbl.setFont(descripFont);
					JLabel lClickLbl = new JLabel("Rotate shape model");
					lClickLbl.setFont(descripFont);
					JLabel lDragLbl = new JLabel("Translate Scene");
					lDragLbl.setFont(descripFont);
					JLabel mouseNoteLbl = new JLabel("Note: for best results click and drag in a straight line.");
					JLabel mouseNoteLbl2 = new JLabel("To rotate about multiple axis, release mouse between");
					JLabel mouseNoteLbl3 = new JLabel("rotation about different axis.");
					Font noteFont = ThemeFont.getItalic();
					mouseNoteLbl.setFont(noteFont);
					mouseNoteLbl2.setFont(noteFont);
					mouseNoteLbl3.setFont(noteFont);

					int row = 0;
					int pad = 1;
					Insets in = new Insets(pad, 5 * pad, pad, 5 * pad);
					controlPnl.add(keyLbl, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Arrow Left"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(leftLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Arrow Right"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(rightLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Arrow Up"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(upLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Arrow Down"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(downLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("z"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(zLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Z"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(ZLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("b"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(bLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("B"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(BLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("f"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(fLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("F"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(FLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("g"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(gLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("G"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(GLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("p"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(pLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("P"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(PLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("a"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(aLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("A"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(ALbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("n"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(nLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("N"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(NLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("r"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(rLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(Box.createVerticalStrut(10), new GridBagConstraints(0, row++, 1, 1, 0, 0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(mouseLbl, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Scroll Wheel"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(scrollLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0,
							GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Control and Drag"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(lDragLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0,
							GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(new JLabel("Left Click and Drag"), new GridBagConstraints(0, row, 1, 1, 0, 0,
							GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(lClickLbl, new GridBagConstraints(1, row++, 1, 1, 0, 0,
							GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(mouseNoteLbl, new GridBagConstraints(0, row++, 2, 1, 0, 0, GridBagConstraints.CENTER,
							GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(mouseNoteLbl2, new GridBagConstraints(0, row++, 2, 1, 0, 0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(mouseNoteLbl3, new GridBagConstraints(0, row++, 2, 1, 0, 0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
					controlPnl.add(Box.createVerticalStrut(5), new GridBagConstraints(0, row++, 1, 1, 0, 0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));

					backPnl.add(controlPnl);
					// add to frame
					controlFrame.setContentPane(backPnl);
					controlFrame.pack();
					controlFrame.setLocationRelativeTo(getFrame());
				}
				// display the frame
				controlFrame.setVisible(true);
			}
		});

		JMenuItem synch2D = new JMenuItem(new AbstractAction("Synchronize Main View") {
			@Override
			public void actionPerformed(ActionEvent e) {
				scene.sych2D();
				window.repaint();
			}

		});
		
		// add contents to menus and menu bar
		fileMenu.add(screenSave);
		helpMenu.add(controlItm);
		synchMenu.add(synch2D);
		menuBar.add(fileMenu);
		int menuspacing = ((ThemeMenuBar) GUITheme.get("menubar")).getItemSpacing();
		menuBar.add(Box.createHorizontalStrut(menuspacing));
		menuBar.add(helpMenu);
		menuBar.add(Box.createHorizontalStrut(menuspacing));
		menuBar.add(synchMenu);

		// add menubar to frame
		frame.setJMenuBar(menuBar);

		
		//TODO: currently no layers support animating in JMARS,
		// renable if functionality becomes available
		
//		animateBtn = new JButton(new AbstractAction("Animate") {
//			private static final long serialVersionUID = -5179729253219829551L;
//
//			public void actionPerformed(ActionEvent e) {
//				MovableList<LView> views = Main.testDriver.mainWindow.viewList;
//				for (LView lview : views) {
//					LView3D view3d = lview.getLView3D();
//					if (view3d.isEnabled() && view3d.isVisible()) {
//						view3d.animate(Double.parseDouble(stepTf.getText()), Double.parseDouble(waitTf.getText()));
//					}
//				}
//
//			}
//		});
//
//		JButton cancelAniBtn = new JButton(new AbstractAction("Cancel") {
//			private static final long serialVersionUID = -4042195075769658181L;
//
//			public void actionPerformed(ActionEvent e) {
//				MovableList<LView> views = Main.testDriver.mainWindow.viewList;
//				for (LView lview : views) {
//					LView3D view3d = lview.getLView3D();
//					if (view3d.isEnabled() && view3d.isVisible()) {
//						view3d.cancelAnimate();
//					}
//				}
//			}
//		});
//
//		JLabel stepLbl = new JLabel("Step (s):");
//		stepTf = new JTextField("10");
//		stepTf.setColumns(4);
//		JPanel stepPnl = new JPanel();
//		stepPnl.add(stepLbl);
//		stepPnl.add(stepTf);
//		JLabel waitLbl = new JLabel("Wait (ms):");
//		waitTf = new JTextField("500");
//		waitTf.setColumns(4);
//		JPanel waitPnl = new JPanel();
//		waitPnl.add(waitLbl);
//		waitPnl.add(waitTf);
//		animateToolTip = "If animate does not work, try decreasing the step, or increasing the wait times.";
//
//		animateBtn.setToolTipText(animateToolTip);
//		waitPnl.setToolTipText(animateToolTip);
//		stepPnl.setToolTipText(animateToolTip);
//		stepLbl.setToolTipText(animateToolTip);
//		stepTf.setToolTipText(animateToolTip);
//		waitLbl.setToolTipText(animateToolTip);
//		waitTf.setToolTipText(animateToolTip);
//
//		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		buttonPanel.add(stepPnl);
//		buttonPanel.add(animateBtn);
//		buttonPanel.add(cancelAniBtn);
//		buttonPanel.add(waitPnl);
//		buttonPanel.setToolTipText(animateToolTip);
//
//
//		frame.add(buttonPanel, BorderLayout.SOUTH);

		frame.setSize(windowWidth, windowHeight + 200);

		frame.setIconImage(Util.getJMarsIcon());
		window.addGLEventListener(scene);

		this.window = window;
		this.frame = frame;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// listen to view add/remove events
				Main.testDriver.mainWindow.listener.addObserver(new ListListener());
							
				SynchronizeListener syncListener = new SynchronizeListener() {		
					@Override
					public void setResults(SynchronizeResult result) {
						Point2D newLoc = new Point2D.Double(result.getLongitude(), result.getLatitude());
			
						Main.testDriver.locMgr.setLocation(Main.PO.convSpatialToWorld(newLoc), true);
					}
				};
					
				addListener(syncListener);
				
				lviews = new ArrayList<>(LManager.getLManager().getViewList());
			}
		});		
	}

	/**
	 * Method to return a reference to the JFrame that 3D will be rendered into.
	 * 
	 * @return JFrame
	 * 
	 *         thread-safe
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Method to display the underlying JFrame
	 * 
	 * thread-safe
	 */
	public void show() {
		// if no shape model is loaded, load the default dsk
		frame.setVisible(true);
		if (hasShapeModel() == false) {
//			loadDefaultShapeModel();
			log.aprintln("Showing 3D without a shape model");
		}
		Runnable show = new Runnable() {

			public void run() {
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						ThreeDManager.getInstance().update();
					}
				});
			}
		};
		show.run();
	}

	/**
	 * Method to hide the underlying JFrame
	 * 
	 * thread-safe
	 */
	public void hide() {
		frame.setVisible(false);
	}

	/**
	 * Method to render a mesh representation of a body
	 * 
	 * @param mesh
	 *            a triangle mesh object to be rendered
	 * 
	 *            not thread safe
	 */
	public void renderMesh(TriangleMesh mesh) {
		if (mesh != null && renderQueue != null) {
			this.mesh = mesh;
			renderQueue.add(mesh);
		}
	}

	/**
	 * Method to add a GLRenderable object to the scene
	 * 
	 * @param renderable
	 * 
	 *            not thread safe
	 */
	public void addRenderable(GLRenderable renderable) {
		renderQueue.add(renderable);
	}

	/**
	 * Method to delete a GLRenderable object from the scene
	 * 
	 * @param renderable
	 * 
	 *            not thread safe
	 */
	public void deleteRenderable(GLRenderable renderable) {
		deleteQueue.add(renderable.getId());
	}

	/**
	 * Method to delete a GLRenderable object from the scene
	 * 
	 * @param id
	 *            this should be the System.identityHashCode. This is readily
	 *            available by calling getId() on the GLrenderable super class
	 * 
	 *            not thread safe
	 */
	public void deleteRenderable(int id) {
		deleteQueue.add(id);
	}

	/**
	 * Adds a listener
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addListener(IntersectListener listener) {
		if (scene != null) {
			scene.addListener(listener);
		}
	}

	/**
	 * Removes a listener
	 * 
	 * @param listener
	 *            Listener to remove
	 * @return <code>true</code> if the listeners contained the specified element
	 */
	public boolean removeListener(IntersectListener listener) {
		if (scene != null) {
			return scene.removeListener(listener);
		} else {
			return false;
		}
	}

	/**
	 * Adds a Synchronize listener
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addListener(SynchronizeListener listener) {
		if (scene != null) {
			scene.addListener(listener);
		}
	}
	

	/**
	 * Removes a Synchronize listener
	 * 
	 * @param listener
	 *            Listener to remove
	 * @return <code>true</code> if the listeners contained the specified
	 *         element
	 */
	public boolean removeListener(SynchronizeListener listener) {
		if (scene != null) {
			return scene.removeListener(listener);
		} else {
			return false;
		}
	}

	/**
	 * Adds a 3D Fitting listener
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addListener(FittingListener listener) {
		if (scene != null) {
			scene.addListener(listener);
		}
	}

	/**
	 * Removes a 3D Fitting listener
	 * 
	 * @param listener
	 *            Listener to remove
	 * @return <code>true</code> if the listeners contained the specified
	 *         element
	 */
	public boolean removeListener(FittingListener listener) {
		if (scene != null) {
			return scene.removeListener(listener);
		} else {
			return false;
		}
	}

	/**
	 * Method to set the color of the highlighted facet when in Investigate mode.
	 * 
	 * @param c
	 *            the new color to be used as the highlight color.
	 * 
	 *            thread-safe?
	 */
	public void setInvestigateColor(Color c) {
		if (c != null && scene != null) {
			float[] color = new float[3];
			color[0] = c.getRed() / 255f;
			color[1] = c.getGreen() / 255f;
			color[2] = c.getBlue() / 255f;
			scene.setPickColor(color);
		}
	}

	/**
	 * Turn on/off the ability to color the facet under the cursor (used in the
	 * investigate mode for some layers)
	 * 
	 * @param highlight
	 */
	public void setHighlightFacet(boolean highlight) {
		scene.setHighlightFacet(highlight);
	}

	/**
	 * This method will clear all renderable objects from the scene INCLUDING the
	 * triangle mesh.
	 */
	public void reset() {
		clearDecals();
		scene.reset();
	}

	/**
	 * This method will clear all renderable objects from the scene EXCEPT the
	 * triangle mesh.
	 */
	public void clear() {
		if (scene != null) {
			clearDecals();
			scene.clear();
		}
	}

	/**
	 * Method to clear all resources held by this class and all member classes.
	 * 
	 * not thread-safe
	 */
	public void shutdown() {
		if (window != null) {
			window.setVisible(false);
		}

		if (scene != null) {
			clearDecals();
			scene.reset();
		}

		if (frame != null) {
			frame.setVisible(false);
			frame.setEnabled(false);
		}

		renderQueue = null;
		deleteQueue = null;
		mesh = null;
		buttonPanel = null;
		scene = null;

		if (frame != null) {
			frame.dispose();
		}

		if (window != null) {
			window.flushGLRunnables();
		}

		window = null;
		controlFrame = null;

		instance = null;
	}

	/**
	 * Method to inform whether the underlying JOGL/graphics card implementation
	 * supports Vertex Buffer Objects.
	 * 
	 * @return true if VBOs are supported
	 * 
	 *         thread-safe
	 */
	public boolean areVBOsSupported() {
		if (scene != null) {
			return scene.areVBOsSupported();
		} else {
			return false;
		}
	}

	/**
	 * Method to return the name of the underlying body shape model if one exists.
	 * 
	 * @return String shape model name
	 * 
	 *         thread-safe
	 */
	public String getShapeModelName() {
		if (mesh == null) {
			return "";
		}
		return mesh.getMeshName();
	}

	/**
	 * Enable/disable mouse driven surface location identification.
	 * 
	 * @param on
	 *            boolean, if true enable surface location picking via mouse pointer
	 * 
	 *            should be thread-safe
	 */
	public void setInvestigateMode(boolean enable) {
		if (mesh != null) {
			scene.setInvestigating(enable);
		} else {
			scene.setInvestigating(false);
		}
	}

	/**
	 * Method to inquire if investigate mode is enabled
	 * 
	 * @return boolean true if investigate mode is enabled
	 * 
	 *         should be thread-safe
	 */
	public boolean isInvestigateModeEnabled() {
		return scene.isInvestigating();
	}
	

	/**
	 * Method to add a shape model to the ThreeDmanager. The shape model will be
	 * added if the manager doesn't already have a shape model by the same name. If
	 * the shape model is added, it becomes the current shape model to represent the
	 * body.
	 * 
	 * @param triMesh
	 *            mesh to be added
	 * 
	 *            thread-safe
	 */
	public void applyShapeModel(TriangleMesh triMesh) {
		mesh = triMesh;
		
		// add it to the repository if its not already in there or was added with a null value
		if (!shapeModels.containsKey(triMesh.getMeshName()) || shapeModels.get(triMesh.getMeshName()) == null) {
			shapeModels.put(triMesh.getMeshName(), triMesh);
		}
		// update the lmanager
		if (LManager.getLManager() != null) {
			// TODO is this needed???
//			LManager.getLManager().refreshShapeMenu();
		}
	}
	
	//This method is used to add names to the repository Map with the ability to load a null TriangleMesh (build the list without loading the kernel)
	private void addShapeModelToRepository(String name, TriangleMesh triMesh) {
	    // add it to the repository if its not already in there
        if (!shapeModels.containsKey(name) || shapeModels.get(triMesh.getMeshName()) == null) {
            shapeModels.put(name, triMesh);
        }
	}
	/**
	 * To be used for testing purposes only. Method to add a shape model to the
	 * ThreeDmanager. The shape model will be added if the manager doesn't already
	 * have a shape model by the same name. If the shape model is added, it becomes
	 * the current shape model to represent the body.
	 * 
	 * @param triMesh
	 *            mesh to be added
	 * 
	 *            thread-safe
	 */
	public void applyShapeModelNoLManager(TriangleMesh triMesh) {
		mesh = triMesh;
		// add it to the repository if its not already in there
		if (!shapeModels.containsKey(triMesh.getMeshName()) || shapeModels.get(triMesh.getMeshName()) == null) {
			shapeModels.put(triMesh.getMeshName(), triMesh);
		}
	}

	/**
	 * Method to return all the names of the shape models the ThreeDManager
	 * currently contains.
	 * 
	 * @return List names of all the contained shape models, if any
	 * @throws thread-safe
	 */
	public List<String> getShapeModelNames() {
	    //check to see if a shape model has been loaded. If not, load the default which populates the list
	    if (!hasShapeModel()) {
	    	// TODO is this needed???
//	        loadDefaultShapeModel();
	    }
		return Collections.list(Collections.enumeration(shapeModels.keySet()));
	}

	/**
	 * Method to return a reference to the current shape model if one exists.
	 * 
	 * @return TriangleMesh
	 * 
	 *         thread-safe
	 */
	public TriangleMesh getShapeModel() {
		return mesh;
	}

	/**
	 * Method to inform the manager has a shape model and it is in use.
	 * 
	 * @return true if there is a current shape model
	 * @throws thread
	 *             -safe
	 */
	public boolean hasShapeModel() {
		if (mesh != null) {
			return true;
		}
		return false;
	}

	/**
	 * Method to enable tight fitting of polygons and lines to a shape model if one
	 * exists
	 *
	 * @param fit
	 *
	 *            not thread-safe
	 */
	public void enableFitting(boolean fit) {
		if (scene != null) {
			scene.enableFitting(fit);
		}
	}

	/**
	 * Returns the status of polygon and line tight fitting
	 *
	 * @return true if tight fitting is enabled
	 *
	 *         thread-safe
	 */
	public boolean isFittingEnabled() {
		if (scene != null) {
			return scene.isFittingEnabled();
		} else {
			return false;
		}
	}

	/**
	 * Method to trigger a redraw of all the Scene objects contained by the 3D
	 * manager.
	 * 
	 * thread-safe
	 */
	public void update() {
		update(true);
	}

	/**
	 * Method to trigger a redraw of all Scene objects with current data from all
	 * LView3D instances. The optional parameter will determine whether any existing
	 * renderable data will be cleared before adding current LView3D data.
	 * 
	 * 
	 * @param clear
	 *            if true any existing renderable data will be cleared
	 * 
	 *            not thread-safe
	 */
	public void update(boolean clear) {
		// System.out.println("update");
		MovableList<LView> views = Main.testDriver.mainWindow.viewList;

		// if the mesh is null, no sense in updating
		if (mesh == null) {
			return;
		}

		if (clear) {
			// clearDecals(); //TODO layer specific decal clearing should occur elsewhere
			scene.clear();
		}

		for (LView lView : views) {
			updateDecalsForLView(lView, false);
		}
	}

	
	private static ExecutorService pool = Executors.newFixedThreadPool(1, new ThreadFactory() {
    	int id = 0;
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("3DManager-updateDecals-" + (id++));
			t.setPriority(Thread.MIN_PRIORITY);
			t.setDaemon(true);
			return t;
		}
    });

	/**
	 * Convenience method to allow a layer to replace its set of Decals
	 * 
	 * @param view
	 *            LView of interest
	 */
	public void updateDecalsForLView(LView view, boolean remove) {
		final LView3D view3d = view.getLView3D();

		// TODO: Why does this have to be called once after the layer is populated?
		// remove the decals for this layer
//		removeLayerDecalSet(view3d);

		if (view3d.isEnabled() && view3d.isVisible() && mesh != null) {
			if(view3d.usesDecals()){
				// SLD: Not sure why we'd want to remove the decals when we're updating?
//				if(remove){
//					// remove the decals for this layer
//					removeLayerDecalSet(view3d);
//				}
//				(new Exception()).printStackTrace();

				Runnable updateDecals = new Runnable() {
				     public void run() {
				 		// then read them
				 		view3d.getDecals();
		
						ArrayList<DecalSet> decalSets = getLayerDecalSet(view3d);

						for (DecalSet ds : decalSets) {
							if (ds != null) {
							//then check to see if the alpha needs to be updated
							// we have decals so just update the alpha channel
								for (Decal d : ds.getDecals()){
//									if (d.getImage() == null) {
//										log.aprintln("Seting opacity on a decal with no image.  "+view3d.toString());
//									}
									float displayAlpha = view3d.getAlpha();
									if(displayAlpha != d.getDisplayAlpha()){
										d.setDisplayAlphaAndDisplay(displayAlpha);
									}
								}	
							}
						}
						
//						// TODO: How do we get rid of old decals when we draw new ones?
						ArrayList<GLRenderable> renderables = view3d.getRenderables();
						if (renderables != null) {
							GLRenderableSet rSet = getLayerGLRenderableSet(view3d);
							rSet.setRenderables(renderables);
						}
		
				 		// redraw
				 		window.repaint();
				     }
				 };
		
				pool.execute(updateDecals);
			}
		}
	}

	/**
	 * Method to return a reference to the associated JPanel that contains any
	 * JButtons specifically associated with the 3D manager. <Description>
	 * 
	 * @return JPanel
	 * 
	 *         thread-safe
	 */
	public JPanel getButtonPanel() {
		return buttonPanel;
	}

	/**
	 * Method to trigger a 3D redraw without polling the LView3Ds
	 */
	public void refresh() {
		// When switching bodies, the old ThreeDManager may have been shut down, but old threads may still have a reference to it
		if (window!=null) {
			window.repaint();
		}
	}

	/**
	 * Method to determine if the scene is currently lit.
	 *
	 * @return true if the scene is lit
	 */
	public boolean isLightOn() {
		return scene.isLightOn();
	}

	/**
	 * Method to allow lighting to be switched on/off
	 * 
	 * @param on
	 */
	public void switchLight(boolean on) {
		scene.setLightOn(on);
	}

	/**
	 * Method to reposition the light source location
	 * 
	 * @param pos
	 *            a 3D (X,Y,Z) location
	 * @throws IllegalArgumentException
	 */
	public void setLightPosition(float[] pos) throws IllegalArgumentException {
		if (scene != null) {
			if (pos != null && pos.length == 3) {
				scene.setLightPosition(pos);
			} else {
				throw new IllegalArgumentException("Invalid light position argument!");
			}
		}
	}

	/**
	 * Return a reference to the actual 3D rendering window
	 * 
	 * @return
	 */
	public GLJPanel getWindow() {
		return window;
	}
	
	public void generateExtents() {
		getVisibleExtents();
	}

	private ArrayList<Decal> getVisibleExtents() {
		return GlobalDecalFactory.getInstance().getGlobalDecals(getShapeModelName());
	}

	/**
	 * Method to retrieve the DecatSet for a specific LView3D
	 *
	 * @param layer
	 *            LView3D
	 * @return The assigned DecalSet for the Lview3D/Layer
	 */
	public ArrayList<DecalSet> getLayerDecalSet(LView3D layer) {
		DecalKey key = new DecalKey(getShapeModelName(), layer, 8); // TODO ppd needs to be dynamic!!!
		if (mesh != null && layerDecals.containsKey(key)) {
			ArrayList<DecalSet> decals = layerDecals.get(key);
			return decals;
		} else if (mesh == null) {
			return null;
		} else { // doesn't exist so create one
			ArrayList<DecalSet> decals = new ArrayList<DecalSet>();
			int numSets = layer.getNumberOfDecalSets();
			
			for (int i=0; i<numSets; i++) {
				DecalSet decalSet = new DecalSet(getVisibleExtents());
				decalSet.setRenderable(false);
				decals.add(decalSet);
				layerDecals.put(key, decals); 
			}
			return decals;
		}		
	}
	

	/**
	 * Method to return the DecalSets for each LView3D in layer order
	 *
	 * @return All the DecalSets for realized and enabled layers/LView3Ds
	 */
	public ArrayList<DecalSet> getCurrentDecalSets() {
		ArrayList<DecalSet> layerList = new ArrayList<DecalSet>();
		for (LView view : lviews) {
			LView3D v3d = view.getLView3D();
			if (v3d.exists() && v3d.isEnabled() && v3d.isVisible() && v3d.usesDecals()) {
				ArrayList<DecalSet> decalSets = getLayerDecalSet(v3d);
				for (DecalSet dSet : decalSets) {
					if (dSet != null && dSet.isRenderable()) {
						layerList.add(dSet);
					}
				}
			}
		}
		return layerList;
	}
	
	/**
	 * Method to remove the association between a DecalSet and an LView3D. Should be
	 * used when deleting an LView3D or when deleting a lot of stamps
	 *
	 * @param v3d
	 *            LView3D
	 * @return true if the DecalSet was successfully deleted
	 */
	public boolean removeLayerDecalSet(LView3D v3d) {
		ArrayList<DecalSet> decals = layerDecals.remove(new DecalKey(getShapeModelName(), v3d, 8)); // TODO ppd needs to be dynamic!!!
		if (decals == null) {
			return false;
		} else {
			if (mesh != null) {
				for (DecalSet ds : decals) {
					mesh.deleteDecals(ds);
				}
			}
			return true;
		}
	}

	/**
	 * Method to test for the existence of a DecalSet for a specific LView3D
	 *
	 * @param view
	 *            LView3D
	 * @return true if an LView3D has an associated DecalSet
	 */
	public boolean hasLayerDecalSet(LView3D view) {
		return layerDecals.containsKey(new DecalKey(getShapeModelName(), view, 8));
	}

	/**
	 * DON'T call this method unless you really want to get rid of ALL the Decals
	 */
	public void clearDecals() {
		GlobalDecalFactory.getInstance().clearDecalSets();

		Collection<ArrayList<DecalSet>> delSet = layerDecals.values();
		for (ArrayList<DecalSet> decalSets : delSet) {
			for (DecalSet dSet : decalSets) {
				if (mesh != null) {
					mesh.deleteDecals(dSet);
				}
			}
		}
		layerDecals.clear();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ThreeDManager.getInstance().refresh();
			}
		});
//		refresh();
	}
	
	/**
	 * Method to return the RenerableSets for each LView3D in layer order
	 *
	 * @return All the RenderableSets for realized and enabled layers/LView3Ds
	 */
	public ArrayList<GLRenderableSet> getCurrentRenderableSets() {
		ArrayList<GLRenderableSet> layerList = new ArrayList<>();
		for (LView view : lviews) {
			LView3D v3d = view.getLView3D();
			if (v3d.exists() && v3d.isEnabled() && v3d.isVisible() && mesh != null) {
				GLRenderableSet rSet = getLayerGLRenderableSet(v3d);
				if (rSet.isRenderable()) {
					layerList.add(rSet);
				}
			}
		}
		return layerList;
	}

	/**
	 * Method to retrieve the DecatSet for a specific LView3D
	 *
	 * @param layer
	 *            LView3D
	 * @return The assigned DecalSet for the Lview3D/Layer
	 */
	public GLRenderableSet getLayerGLRenderableSet(LView3D layer) {
		if (layerRenderables.containsKey(layer)) {
			return layerRenderables.get(layer);
		} else { // doesn't exist so create one
			GLRenderableSet renderables = new GLRenderableSet();
			renderables.setIsRenderable(false);
			layerRenderables.put(layer, renderables);
			return renderables;
		}
	}

	/**
	 * DON'T call this method unless you really want to get rid of ALL the Decals
	 */
	public void clearRenderables() {
		Collection<GLRenderableSet> delSet = layerRenderables.values();
		for (GLRenderableSet rSet : delSet) {
			rSet.dispose();			
		}
		layerRenderables.clear();
		refresh();
	}
	
	/**
	 * Method to remove the association between a DecalSet and an LView3D. Should be
	 * used when deleting an LView3D or when deleting a lot of stamps
	 *
	 * @param v3d
	 *            LView3D
	 * @return true if the DecalSet was successfully deleted
	 */
	public boolean removeLayerGLRenderableSet(LView3D v3d) {
		GLRenderableSet renderables = layerRenderables.remove(v3d);
		if (renderables == null) {
			return false;
		} else {
			renderables.dispose();
			return true;
		}
	}

	/**
	 * Method to test for the existence of a GLRenderableSet for a specific LView3D
	 *
	 * @param view
	 *            LView3D
	 * @return true if an LView3D has an associated GLRenderableSet
	 */
	public boolean hasLayerGLRenderableSet(LView3D view) {
		return layerRenderables.containsKey(view);
	}
	
	/**
	 * Method to return the radius at a specified location
	 * @param longitude WEST-LEADING
	 * @param latitude
	 * @return mesh radius at input point in KILOMETERS OR Float.MAX_VALUE in case of error
	 */
	public float getRadiusAtLocation(float longitude, float latitude) {
		if (mesh != null) {
			return mesh.getRadiusAtLocation(longitude, latitude);
		} else {
			return Float.MAX_VALUE;
		}		
	}
	
	/**
	 * Rotates the shape model so that the West-leading Lon/Lat position
	 * is pointed directly at the camera eye. Intended to synchronize the 
	 * viewing location in 3D to the the viewing location in the Main View.
	 *  
	 * @param westLon West-leading Longitude
	 * @param lat Latitude
	 */
	public void synchTo2D(float westLon, float lat) {
		scene.synchTo2D(westLon, lat);
		window.repaint();
	}
	
	// Listens for changes in the LManager's view list.
	private class ListListener implements Observer {
		public void update(Observable o, Object arg) {
			lviews = new ArrayList<>(LManager.getLManager().getViewList());
		}
	}

	public GLContext getCurrentContext() {
		return window.getContext();
	}
	
	public void displayBusyText(boolean display) {
		if (scene != null) {
			scene.setDisplayBusyText(display);
		}
	}
	
	public boolean isDisplayBusyText() {
		if (scene != null) {
			return scene.isDisplayBusyText();
		} else {
			return false;
		}
	}	
	
	/**
	 * Should be checked before calling an update from any layer or other source
	 * @return  Only returns true if the frame is visible, the shape model is not 
	 * null, and the window is visible
	 */
	public boolean isReady() {
		return this.getWindow().isVisible() && this.hasShapeModel() && frame.isVisible();
	}
	
	public void clearShapeModels() {
		for (TriangleMesh t : shapeModels.values()) {
			t.clearDecals();
			t.dispose();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ThreeDManager.getInstance().refresh();
			}
		});
		
		shapeModels.clear();
		
		if(mesh!=null){
			mesh.dispose();
			mesh = null;
		}
	}
}
