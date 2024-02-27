package edu.asu.jmars.lmanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import org.jdesktop.swingx.JXTaskPane;
import edu.asu.jmars.Main;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.Layer;
import edu.asu.jmars.layer.Layer.LView3D;
import edu.asu.jmars.layer.LayerParameters;
import edu.asu.jmars.swing.CompoundIcon;
import edu.asu.jmars.swing.IconButtonUI;
import edu.asu.jmars.swing.quick.add.layer.LayerQuickAccessPopup;
import edu.asu.jmars.ui.image.factory.ImageCatalogItem;
import edu.asu.jmars.ui.image.factory.ImageFactory;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeButton;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeImages;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeComboBox;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeTextField;
import edu.asu.jmars.util.Config;
import edu.asu.jmars.util.Util;
import edu.asu.jmars.viz3d.ThreeDManager;


public class MainPanel extends JLayeredPane implements ComponentListener {
    public static final int NUMBER_OF_OVERLAY_LAYERS = 4;
    private static int ADD_LAYER_DIALOG_OFFSET = 8;
    Row dragRow;
    private Row curRow;
    int dragPos;
    int dragSrc;
    int dragDst;
    public List<Row> rows = new ArrayList<Row>();
    public List<Row> overlayRows = new ArrayList<Row>();
    JMenuBar addBtn;   
    JButton addLayerButton;
    JCheckBoxMenuItem tooltip, tooltipsItem;

    JXTaskPane cartographyPane;
    JScrollPane cartographySP;
    JDialog newAddLayerDialog;
    JPanel mainResultsPane;
    JScrollPane resultsSP;
    JPanel headerPanel;
    JLabel loadingLbl = new JLabel("Loading...");
    Dimension dialogSize;
    JPanel searchPanel;
    JDialog searchDialog;
    JTextField searchTF;
    JDialog resultsDialog;
    JPanel resultsPanel;
    JScrollPane searchResultsSP;
    JScrollPane favoritesSP;
    JPanel favoritesPanel;
    int selectedSuggestion = -1;
    private boolean arrowSuggestionFlag = false;
    private boolean clearButtonFlag = false;
    private boolean suppressSuggestionDialog = false;
    private boolean tooManyResultsFlag = false;
    LayerQuickAccessPopup lqapopup = new LayerQuickAccessPopup();
    HashMap<String, SearchResultRow> allResultRows = new HashMap<String, SearchResultRow>();
    HashMap<String,ArrayList<LayerParameters>> layersById = new HashMap<String, ArrayList<LayerParameters>>();
    HashMap<String, Set<String>> layersByDesc = new HashMap<String, Set<String>>();
    Color imgColor = ((ThemeImages) GUITheme.get("images")).getFill();

    String[] suggestionList = {"category:","subcategory:","instrument:", "topic:", "custom:"};
    
    private SearchProvider searchProvider = SearchProvider.getInstance();
    
    private static Icon tooltipIcon = new ImageIcon(ImageFactory.createImage(ImageCatalogItem.INFO));
    
    public JDialog getAddLayerDialog() {
        return newAddLayerDialog;
    }
    
    JTabbedPane tabs = null;
    int[] layerCounts = {0,0,0};
    CompoundIcon icon = null;
	CompoundIcon hoverIcon = null;
    
    
    public JScrollPane rowScrollPane;
    public JLayeredPane rowsPanel = new JLayeredPane() {
        public void layout() {
            if (rows.isEmpty())
                return;

            Insets insets = rowsPanel.getInsets();
            int h = ((Row) rows.get(0)).getPreferredSize().height;
            int w = rowsPanel.getWidth() - insets.left - insets.right;

            for (int i = 0; i < rows.size(); i++) {
                Row r = (Row) rows.get(i);

                int y;
                if (i == dragSrc)
                    y = dragPos;
                else if (i > dragSrc && i <= dragDst)
                    y = h * (i - 1);
                else if (i < dragSrc && i >= dragDst)
                    y = h * (i + 1);
                else
                    y = h * i;
                r.setSize(w, h);
                r.setLocation(insets.left, insets.top + y);
            }
        }

        public Dimension getPreferredSize() {
            int width = 0;
            int height = 0;
            Dimension size = new Dimension(width, height);

            if (rows.size() == 0)
                return size;
            int h = ((Row) rows.get(0)).getPreferredSize().height;
            height = rows.size() * h;
            width = 1; // make it expand to take whatever space is available
            size.setSize(width, height);
            return size;
        }

    };

    public JLayeredPane overlayRowsPanel = new JLayeredPane() {
        public void layout() {
            if (overlayRows.isEmpty())
                return;

            Insets insets = overlayRowsPanel.getInsets();
            int h = ((Row) overlayRows.get(0)).getPreferredSize().height;
            int w = overlayRowsPanel.getWidth() - insets.left - insets.right;

            for (int i = 0; i < overlayRows.size(); i++) {
                Row r = (Row) overlayRows.get(i);

                int y;
                if (i == dragSrc)
                    y = dragPos;
                else if (i > dragSrc && i <= dragDst)
                    y = h * (i - 1);
                else if (i < dragSrc && i >= dragDst)
                    y = h * (i + 1);
                else
                    y = h * i;
                r.setSize(w, h);
                r.setLocation(insets.left, insets.top + y);
            }
        }

        public Dimension getPreferredSize() {
            int width = 0;
            int height = 0;
            Dimension size = new Dimension(width, height);

            if (overlayRows.size() == 0)
                return size;
            int h = ((Row) overlayRows.get(0)).getPreferredSize().height;
            height = overlayRows.size() * h;
            width = 1; // make it expand to take whatever space is available
            size.setSize(width, height);
            return size;
        }

    };

    public void delete(int selectedIdx) {
        // Remove the row... the row indices are reversed relative to everything
        // else.
        Row r = (Row) rows.remove(rows.size() - 1 - selectedIdx);
        rowsPanel.remove(r);
        rowsPanel.repaint();
        if (rowScrollPane != null) {
            rowScrollPane.revalidate();
            rowScrollPane.repaint();
        }
    }

    private JButton getAddBtn() {
        addLayerButton = new JButton("Layers".toUpperCase());
        addLayerButton.addMouseListener(addbtnMouseListener);
        Color imgColor = ((ThemeImages) GUITheme.get("images")).getFill();
		Color imgHover = ((ThemeButton) GUITheme.get("button")).getThemehilightbackground();
		ImageIcon newlayerIcon = new ImageIcon(
                ImageFactory.createImage(ImageCatalogItem.NEW_LAYER.withDisplayColor(imgColor)));
		ImageIcon newlayerHoverIcon = new ImageIcon(
				ImageFactory.createImage(ImageCatalogItem.NEW_LAYER.withDisplayColor(imgHover)));		
		ImageIcon ellipse = new ImageIcon(
                ImageFactory.createImage(ImageCatalogItem.ELLIPSE_MENU.withDisplayColor(imgColor)));
        addLayerButton.setHorizontalTextPosition(SwingConstants.LEFT);
        addLayerButton.setHorizontalAlignment(SwingConstants.LEFT);
        addLayerButton.setIconTextGap(85);
		icon = new CompoundIcon(CompoundIcon.Axis.X_AXIS, 15, newlayerIcon, ellipse);
        icon.setUseWidthOfComponent(true);
        icon.setWidthOfComponentOffset(63);
		hoverIcon = new CompoundIcon(CompoundIcon.Axis.X_AXIS, 15, newlayerHoverIcon, ellipse);
		hoverIcon.setUseWidthOfComponent(true);
		hoverIcon.setWidthOfComponentOffset(63);		
        addLayerButton.setIcon(icon);
		addLayerButton.setRolloverIcon(hoverIcon);
        return addLayerButton;
    }

    public Point getAddButtonLocation() {
        return addLayerButton.getLocation();
    }

    public MainPanel() {
        SearchProvider.prepareSearch();
        getAddBtn();

        Main.mainFrame.getRootPane().setDefaultButton(addLayerButton);

        rowScrollPane = new JScrollPane(rowsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rowScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        
        cartographyPane = new JXTaskPane();
        cartographyPane.setCollapsed(false);
        cartographyPane.setTitle("Overlays");        

        cartographyPane.add(overlayRowsPanel);
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);  
        layout.setAutoCreateGaps(true);
        layout.setHonorsVisibility(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(addLayerButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addComponent(rowScrollPane,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addComponent(cartographyPane,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(addLayerButton)
            .addComponent(rowScrollPane)
            .addComponent(cartographyPane));
    }

    private void setAll(Dimension d, JComponent c) {
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        c.setPreferredSize(d);
    }
    // These actions are used in the drop down menu, right click menu, and
    // also double clicking of the rows.

     Action actTooltip = new AbstractAction("Show Tooltip") {
        public void actionPerformed(ActionEvent e) {
            boolean show = true;
            ;
            if (e.getSource() == tooltip) {
                show = tooltip.isSelected();
            }
            if (e.getSource() == tooltipsItem) {
                show = tooltipsItem.isSelected();
            }
            LManager.getLManager().showTooltipForSelectedLayer(show);

        };
    };

    // This action is used to pull up the 'add layer' jframe with the
    // button
    // Action actAdd = new AbstractAction("Layers") {
    // public void actionPerformed(ActionEvent e) {
    // if (newAddLayerDialog.isVisible()) {
    // newAddLayerDialog.setVisible(false);
    // } else {
    // // LManager.getLManager().displayAddNewLayer();
    // Double x = MainPanel.this.addLayerButton.getLocationOnScreen().getX();
    // Double y = MainPanel.this.addLayerButton.getLocationOnScreen().getY();
    // Dimension d = MainPanel.this.addLayerButton.getSize();
    // Double width = d.getWidth();
    // x += width;
    // x += 15;//offset guess
    // newAddLayerDialog.setLocation(x.intValue(), y.intValue());
    // JRootPane rootPane = newAddLayerDialog.getRootPane();
    // rootPane.setWindowDecorationStyle(JRootPane.NONE);
    // newAddLayerDialog.setVisible(true);
    // }
    // }
    // };

    MouseListener addbtnMouseListener = new MouseAdapter() {
        public void mousePressed(MouseEvent mouseEvent) {
            int modifiers = mouseEvent.getModifiers();
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                if (newAddLayerDialog == null) {
                    createAddLayerDialog();
                    MainPanel.this.buildMainResultsPane(searchProvider.buildInitialLayerList(true),true);
                    suppressSuggestionDialog = true;
                    searchTF.grabFocus();
                    suppressSuggestionDialog = false;
                }
                int pointInButton = ((Double)mouseEvent.getPoint().getX()).intValue();
                int ellipseIconStart = icon.getLastIconStart();
                int ellipseIconEnd = icon.getLastIconEnd();
                if ((pointInButton >= ellipseIconStart - 5) && (pointInButton <= ellipseIconEnd + 5)) {
                    newAddLayerDialog.setVisible(false);
                    lqapopup.getPopupmenu().show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                } else {
                    if (newAddLayerDialog.isVisible()) {
                        newAddLayerDialog.setVisible(false);
                    } else {
                        try {
                            // LManager.getLManager().displayAddNewLayer();
                            Double x = MainPanel.this.addLayerButton.getLocationOnScreen().getX();
                            Double y = MainPanel.this.addLayerButton.getLocationOnScreen().getY();
                            Dimension d = MainPanel.this.addLayerButton.getSize();
                            Double width = d.getWidth();
                            x += width;
                            x += ADD_LAYER_DIALOG_OFFSET;// offset guess
                            newAddLayerDialog.setLocation(x.intValue(), y.intValue());
                            setDialogSize();
                            newAddLayerDialog.pack();
                            newAddLayerDialog.setVisible(true);
                        } catch (Exception e) {
                            //should never happen, but protect incase add layer button is not visible
                            newAddLayerDialog.setVisible(false);
                        }
                    }
                }
            } 
            if (SwingUtilities.isRightMouseButton(mouseEvent)) {               
                lqapopup.getPopupmenu().show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            }
        }
    };
    
    public void closeAddLayerDialog(){ 
        if (newAddLayerDialog != null) {
            newAddLayerDialog.setVisible(false);
        }
        if (resultsDialog != null) {
            resultsDialog.setVisible(false);
        }
    }
    
    public void addView(Layer.LView view) {
        Row r = new Row(view, rowMouseHandler);
        r.createGUI();
        rowsPanel.add(r);
        rows.add(0, r);
    }

    public void addOverlayView(Layer.LView view) {
        Row r = new Row(view, overlayRowMouseHandler);
        r.createGUI();
        overlayRowsPanel.add(r);
        overlayRows.add(0, r);
    }

    public void revalidateOverlayPanel() {
        cartographyPane.revalidate();
        overlayRowsPanel.revalidate();
        cartographyPane.setCollapsed(true);
        overlayRowsPanel.repaint();
        cartographyPane.repaint();
    }
    public void updateRows() {
        for (Iterator i = rows.iterator(); i.hasNext();)
            ((Row) i.next()).updateRow();
    }

    public void updateOverlayRows() {
        for (Iterator i = overlayRows.iterator(); i.hasNext();)
            ((Row) i.next()).updateCartographyRow();
    }

    void setDragOffset(int dragOffset) {
        if (dragOffset == 0 || rows.isEmpty()) {
            dragSrc = -1;
            dragDst = -1;
            dragPos = -1;
        } else {
            int h = ((Row) rows.get(0)).getPreferredSize().height;
            dragSrc = rows.indexOf(dragRow);
            dragPos = Util.bound(0, h * dragSrc + dragOffset, h * (rows.size() - 1));
            dragDst = (dragPos + h / 2 - 1) / h;
        }
    }


    public MouseInputListener rowMouseHandler = new MouseInputAdapter() {
        int pressed;

		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				Component c = e.getComponent();
				if (c instanceof Row) {
					dragRow = (Row) c;
				} else {
					dragRow = (Row) c.getParent();
				}
				LManager.getLManager().setActiveLView(dragRow.getView());
				dragRow.rowpopup.getPopupmenu().show(e.getComponent(), e.getX(), e.getY());
			} else {
				if (e.getClickCount() != 2)
					return;
				if (Config.get("openDocked").equalsIgnoreCase("false")) {
					LManager.getLManager().accessSelectedOptions(false);
				} else {
					LManager.getLManager().accessSelectedOptions(true);
				}
			}
		}

        public void mousePressed(MouseEvent e) {
            Component c = e.getComponent();
            if (c instanceof Row) {
                dragRow = (Row) c;
            } else if (c instanceof JLabel) {
                // The label on the row is inside a JPanel ('top')
                // get top, and then get the row
                c = c.getParent();
                dragRow = (Row) c;
            }

            setDragOffset(0);
            pressed = screenY(e);
            rowsPanel.moveToFront(dragRow);
            LManager.getLManager().setActiveLView(dragRow.getView());
            dragRow.setToHoverAppearance();
            }

        public void mouseDragged(MouseEvent e) {
            setDragOffset(screenY(e) - pressed);
            rowsPanel.revalidate();
            rowsPanel.repaint();
            if (rowScrollPane != null) {
                rowScrollPane.revalidate();
                rowScrollPane.repaint();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (dragSrc != dragDst) {
                int dragSrcRev = rows.size() - 1 - dragSrc;
                int dragDstRev = rows.size() - 1 - dragDst;

                // Move the user-visible row, the actual view list
                // order, and the focus tabs. The latter two are
                // in reverse order from the first one.
                Row rowToMove = rows.remove(dragSrc);
                rows.add(dragDst, rowToMove);
                LManager.getLManager().viewList.move(dragSrcRev, dragDstRev);
                LManager.getLManager().setActiveLView(dragDstRev);

                // update the 3d view if the row has 3d data
                LView3D view3d = rowToMove.getView().getLView3D();
                ThreeDManager mgr = ThreeDManager.getInstance();
                if (mgr.isReady() && view3d.isEnabled() && view3d.isVisible()) {
                    if (mgr.hasLayerDecalSet(view3d)) {
                        mgr.update();
                    }
                }
            }

            dragRow.setToHoverAppearance();
            dragRow = null;
            setDragOffset(0);
            rowsPanel.revalidate();
            rowsPanel.repaint();
            if (rowScrollPane != null) {
                rowScrollPane.revalidate();
                rowScrollPane.repaint();
            }

            cartographyPane.revalidate();
            cartographyPane.repaint();
            if (cartographyPane != null) {
                cartographyPane.revalidate();
                cartographyPane.repaint();
            }

        }

        public void mouseMoved(MouseEvent e) {        	       	
            Component c = e.getComponent();
            Row hoverRow = null;
            if (c instanceof Row) {
                hoverRow = (Row) c;
            } else if (c instanceof JLabel) {
                // The label on the row is inside a JPanel ('top')
                // get top, and then get the row
                hoverRow = (Row) c.getParent();
            }

            updateRows();

            if (hoverRow != null) {
                hoverRow.setToHoverAppearance();
            }
        }

		public void mouseExited(MouseEvent e) {
			updateRows();			
			try {
    			Point2D screenPt = e.getLocationOnScreen();
    			Point uL = curRow.getLocationOnScreen();
    			Rectangle2D rect = new Rectangle2D.Double(uL.x, uL.y, curRow.getWidth(), curRow.getHeight());
    			if (rect.contains(screenPt)) {
    				curRow.setToHoverAppearance();
    			} else {
    				curRow.setToNormalAppearance();
    			}
			} catch (Exception e1) {
			    //do nothing
			}
		}        
      

		public void mouseEntered(MouseEvent e) {
			Component c = e.getComponent();
			if (c instanceof Row) {
				curRow = (Row) c;
			} else if (c instanceof JLabel) {
				curRow = (Row) c.getParent();
			}
			updateRows();
			if (curRow != null) {
				curRow.setToHoverAppearance();
			}
		}
    };
    
    

    public MouseInputListener overlayRowMouseHandler = new MouseInputAdapter() {
        int pressed;

        public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				Component c = e.getComponent();
				if (c instanceof Row) {
					dragRow = (Row) c;
				} else {
					dragRow = (Row) c.getParent();
				}
				LManager.getLManager().setActiveLView(dragRow.getView());
				dragRow.rowpopup.getPopupmenu().show(e.getComponent(), e.getX(), e.getY());
			} else {
			    if (e.getClickCount() != 2)
                    return;
                if (Config.get("openDocked").equalsIgnoreCase("false")) {
                    LManager.getLManager().accessSelectedOptions(false);
                } else {
                    LManager.getLManager().accessSelectedOptions(true);
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            Component c = e.getComponent();
            if (c instanceof Row) {
                dragRow = (Row) c;
            } else {
                dragRow = (Row) c.getParent();
            }
            LManager.getLManager().setActiveLView(dragRow.getView());           
        }

		public void mouseDragged(MouseEvent e) {
		}
 
        public void mouseReleased(MouseEvent e) {    
            updateOverlayRows();
            overlayRowsPanel.revalidate();
            overlayRowsPanel.repaint();

            if (cartographyPane != null) {
                cartographyPane.revalidate();
                cartographyPane.repaint();
            }
         }

        public void mouseMoved(MouseEvent e) {
            Component c = e.getComponent();
            Row hoverRow = null;
            if (c instanceof Row) {
                hoverRow = (Row) c;
            } else if (c instanceof JLabel) {  
                hoverRow = (Row) c.getParent();
            }
            updateOverlayRows();
            if (hoverRow != null) {
                hoverRow.setToHoverAppearance();
            }
        }

        public void mouseExited(MouseEvent e) {
        	updateRows();			
			Point2D screenPt = e.getLocationOnScreen();
			Point uL = curRow.getLocationOnScreen();
			Rectangle2D rect = new Rectangle2D.Double(uL.x, uL.y, curRow.getWidth(), curRow.getHeight());
			if (rect.contains(screenPt)) {
				curRow.setToHoverAppearance();
			} else {
				curRow.setToNormalAppearance();
			}     
        }

        public void mouseEntered(MouseEvent e) {
            Component c = e.getComponent();
            if (c instanceof Row) {
                curRow = (Row) c;
            } else if (c instanceof JLabel) {              
                curRow = (Row) c.getParent();
            }
            updateOverlayRows();
            if (curRow != null) {
                curRow.setToHoverAppearance();
            }
        }
    };

    private static int screenY(MouseEvent e) {
        Point pt = e.getPoint();
        SwingUtilities.convertPointToScreen(pt, e.getComponent());
        return pt.y;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        resetAddLayerDialog();
    }
    
    private void resetAddLayerDialog() {
        try {
            if (newAddLayerDialog != null && newAddLayerDialog.isVisible()) {
                Double x = MainPanel.this.addLayerButton.getLocationOnScreen().getX();
                Double y = MainPanel.this.addLayerButton.getLocationOnScreen().getY();
                Dimension d = MainPanel.this.addLayerButton.getSize();
                Double width = d.getWidth();
                x += width;
                x += ADD_LAYER_DIALOG_OFFSET;// offset guess
                newAddLayerDialog.setLocation(x.intValue(), y.intValue());
                setDialogSize();
                newAddLayerDialog.pack();
            }
        } catch (Exception e) {
            //add layer button is not showing (likely), can the right location for resize, close the dialog
            newAddLayerDialog.setVisible(false);
        }
    }
    @Override
    public void componentMoved(ComponentEvent e) {
        resetAddLayerDialog();
    }

    @Override
	public void componentShown(ComponentEvent e) {
	}

    @Override
	public void componentHidden(ComponentEvent e) {
	}

    private AbstractAction doneButtonAct = new AbstractAction("Close".toUpperCase()) {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            newAddLayerDialog.setVisible(false);
            if (resultsDialog != null) {
                resultsDialog.setVisible(false);
            }
        }
    };
    
    private void setDialogSize() {       
        int height = LManager.getLManager().getHeight() - 60;//60 for now because of the tabs and later because of the docked section
        if (height < 600) {
            height = 600;
        }
        dialogSize = new Dimension(600, height);
        newAddLayerDialog.setSize(dialogSize);
        newAddLayerDialog.setPreferredSize(dialogSize);
        newAddLayerDialog.setMinimumSize(dialogSize);
        tabs.setPreferredSize(new Dimension(590, height - 100));
        resultsSP.setPreferredSize(new Dimension(580,height-300));
        resultsSP.revalidate();
        headerPanel.setPreferredSize(new Dimension(580, 30));
   }
    
    
    private void createAddLayerDialog() {
        newAddLayerDialog = new JDialog(Main.mainFrame);
        tabs = new JTabbedPane();
        resultsSP = new JScrollPane();
        searchPanel = new JPanel();
        headerPanel = new JPanel();
        setDialogSize();
        JRootPane rootPane = newAddLayerDialog.getRootPane();
        rootPane.setWindowDecorationStyle(JRootPane.NONE);
        rootPane.setBorder(null);
        newAddLayerDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        newAddLayerDialog.setBackground(Color.black);        
        newAddLayerDialog.add(layoutAddLayerDialog());
        newAddLayerDialog.pack();        
    }
    
    public void resetAddLayer() {
        if (newAddLayerDialog != null && newAddLayerDialog.isVisible()) {
            newAddLayerDialog.setVisible(false);
        }
        if (resultsDialog != null) {
            resultsDialog.setVisible(false);
        }
        newAddLayerDialog = null;
    }

    private void buildMainResultsPane(ArrayList<SearchResultRow> resultSet, boolean initialLayout) {
        JLabel noResultsLbl = new JLabel("No results to display");
        
        if (resultSet == null || resultSet.size() == 0) {
            noResultsLbl.setVisible(true);
        } else {
            noResultsLbl.setVisible(false);
        }
        
        if (mainResultsPane == null) {
            mainResultsPane = new JPanel();
        }
        mainResultsPane.removeAll();
        GroupLayout paneLayout = new GroupLayout(mainResultsPane);
        mainResultsPane.setLayout(paneLayout);
        
        ParallelGroup panePar = paneLayout.createParallelGroup();
        SequentialGroup paneSeq = paneLayout.createSequentialGroup();
 
        if (resultSet != null && resultSet.size() > 0) {
            for(SearchResultRow row : resultSet) {
                panePar.addComponent(row,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);//horizontal
                paneSeq.addComponent(row);//vertical
            }
        } else {
            panePar.addComponent(noResultsLbl);
            paneSeq.addComponent(noResultsLbl);
        }
        paneLayout.setHorizontalGroup(panePar);
        paneLayout.setVerticalGroup(paneSeq);
    }
    
    
    private JPanel layoutSearchPanel() {
        JPanel searchLayersPanel = new JPanel();
        initSearchPanel(true);
        
        JPanel headerPanel = getResultHeaderPanel();
        
        ArrayList<SearchResultRow> resultSet = null; 
        boolean initialLayout = true;
        buildMainResultsPane(resultSet, initialLayout);
        
        resultsSP.add(mainResultsPane);
        resultsSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultsSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsSP.setViewportView(mainResultsPane);
        

        GroupLayout allLayersLayout = new GroupLayout(searchLayersPanel);
        searchLayersPanel.setLayout(allLayersLayout);

        allLayersLayout.setAutoCreateGaps(true);

        allLayersLayout.setHorizontalGroup(allLayersLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(searchPanel)
            .addComponent(headerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(resultsSP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
        allLayersLayout.setVerticalGroup(allLayersLayout.createSequentialGroup()
            .addComponent(searchPanel)
            .addComponent(headerPanel)
            .addComponent(resultsSP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
        
        return searchLayersPanel;
    }
    

    private JPanel getResultHeaderPanel() {

        JLabel addLbl = new JLabel("Add");
        JLabel typeLbl = new JLabel("Type");
        JLabel nameLbl = new JLabel("Name");
        JLabel favLbl = new JLabel("Fave");
        
        GroupLayout layout = new GroupLayout(headerPanel);
        
        headerPanel.setLayout(layout);
        int gapNameToFave = SearchResultRow.LAYER_NAME_PANEL_WIDTH - 25;
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(6,6,6)
            .addComponent(addLbl)
            .addGap(6,6,6)
            .addComponent(typeLbl)
            .addGap(30,30,30)
            .addComponent(nameLbl)
            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, gapNameToFave,gapNameToFave)
            .addComponent(favLbl)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(addLbl)
            .addComponent(typeLbl)
            .addComponent(nameLbl)
            .addComponent(favLbl)
        );
        
        return headerPanel;
    }
    
    private JScrollPane layoutFavoritesPanel() {
        if (favoritesSP == null) {
            favoritesSP = new JScrollPane();
            favoritesSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            favoritesSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        } 
        
        if(!SearchProvider.userLoggedIn()) {
            return favoritesSP;
        }
        
        favoritesPanel = new JPanel();
        GroupLayout favoritesLayout = new GroupLayout(favoritesPanel);
        favoritesPanel.setLayout(favoritesLayout);
        favoritesLayout.setAutoCreateContainerGaps(true);
        favoritesLayout.setAutoCreateGaps(true);

        ParallelGroup colGroup = favoritesLayout.createParallelGroup(Alignment.LEADING);
        SequentialGroup rowGroup = favoritesLayout.createSequentialGroup();
        
        ArrayList<SearchResultRow> faves = searchProvider.getFavoriteLayers();
        for(SearchResultRow fave : faves) {
            colGroup.addComponent(fave);
            rowGroup.addComponent(fave);
        }
        
        favoritesLayout.setHorizontalGroup(colGroup);
        favoritesLayout.setVerticalGroup(rowGroup);
        
        favoritesSP.add(favoritesPanel);
        favoritesSP.setViewportView(favoritesPanel);
        favoritesSP.setPreferredSize(new Dimension(300,300));
        return favoritesSP;
    }
    
    private void refreshFavoritesPanel() { 
        favoritesPanel.removeAll();
        GroupLayout favoritesLayout = new GroupLayout(favoritesPanel);
        favoritesPanel.setLayout(favoritesLayout);
        favoritesLayout.setAutoCreateContainerGaps(true);
        favoritesLayout.setAutoCreateGaps(true);
        JLabel noFaves = new JLabel("No favorites to display");
        
        ParallelGroup colGroup = favoritesLayout.createParallelGroup(Alignment.LEADING);
        SequentialGroup rowGroup = favoritesLayout.createSequentialGroup();
        
        ArrayList<SearchResultRow> faves = searchProvider.getFavoriteLayers();
        if (faves.size() == 0) {
            colGroup.addComponent(noFaves);
            rowGroup.addComponent(noFaves);
        } else {
            for(SearchResultRow fave : faves) {
                colGroup.addComponent(fave);
                rowGroup.addComponent(fave);
            }
        }
        
        favoritesLayout.setHorizontalGroup(colGroup);
        favoritesLayout.setVerticalGroup(rowGroup);
        
        favoritesSP.revalidate();
        favoritesSP.repaint();
        
    }
    private JPanel layoutAddLayerDialog() {
        JPanel searchLayersPanel = layoutSearchPanel();
        JPanel browsePanel = layoutBrowsePanel();
        favoritesSP = layoutFavoritesPanel();
        JButton doneButton = new JButton(doneButtonAct);
        JLabel closeXLbl = new JLabel();        
        
        ImageIcon closeIcon = new ImageIcon(ImageFactory.createImage(
                ImageCatalogItem.CLOSE.withDisplayColor(((ThemeImages) GUITheme.get("images")).getLayerfill())));
        closeXLbl.setIcon(closeIcon);
        closeXLbl.setBackground(((ThemeButton)GUITheme.get("button")).getDefaultback());
        closeXLbl.addMouseListener(new MouseListener() {
            
            @Override
			public void mouseReleased(MouseEvent e) {
			}
            
            @Override
			public void mousePressed(MouseEvent e) {
			}
          
             @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setCursor(Cursor.getDefaultCursor());
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {              
                if (resultsDialog != null) {
                    resultsDialog.setVisible(false);
                }
                newAddLayerDialog.setVisible(false);
            }
        });
        JPanel buttonExtension = new JPanel();
    
        buttonExtension.setBackground(((ThemeButton)GUITheme.get("button")).getDefaultback());
        JLabel titleLbl = new JLabel("Add New Layer");
        titleLbl.setBackground(((ThemeButton)GUITheme.get("button")).getDefaultback());
        buttonExtension.setPreferredSize(new Dimension(550,addLayerButton.getHeight()));
        
        GroupLayout beLayout = new GroupLayout(buttonExtension);
        buttonExtension.setLayout(beLayout);
        beLayout.setAutoCreateContainerGaps(true);
        beLayout.setAutoCreateGaps(true);
        
        beLayout.setHorizontalGroup(beLayout.createSequentialGroup()
            .addComponent(titleLbl)
            .addGap(462)
            .addComponent(closeXLbl));
        beLayout.setVerticalGroup(beLayout.createParallelGroup()
            .addComponent(titleLbl)
            .addComponent(closeXLbl));
        
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.add("SEARCH LAYERS", searchLayersPanel);
        tabs.add("BROWSE LAYERS", browsePanel);        
        tabs.add("FAVORITES", favoritesSP);
        
        if(Main.USER == null || Main.USER.trim().length() == 0) {
            tabs.setEnabledAt(2, false);
        }
        tabs.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JTabbedPane) {
                    JTabbedPane tp = (JTabbedPane) e.getSource();
                    if (tp.getSelectedIndex() == 2) {
                        refreshFavoritesPanel();
                  }                    
                }
            }
        });
        
        JPanel mainPanel = new JPanel();
        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setAutoCreateGaps(true);
        
        mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
            .addComponent(buttonExtension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addComponent(tabs, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup().addComponent(doneButton).addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(mainPanelLayout.createSequentialGroup()
            .addComponent(buttonExtension, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(tabs, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addComponent(doneButton)
            .addContainerGap()
        );
        return mainPanel;
    }

    private JPanel layoutBrowsePanel() {
        BrowseLayerPanel browsePanel = new BrowseLayerPanel();
        browsePanel.buildGUI();
        return browsePanel;
    }
    
    private void layoutOneTagRow(JPanel panel, JLabel text, JLabel icon) {
        int textWidth = 300;
        Color bgColor = ((ThemeTextField)GUITheme.get("textfield")).getBackground();
        text.setBackground(bgColor);
        icon.setBackground(bgColor);
        icon.setForeground(bgColor);

        GroupLayout panelLayout = new GroupLayout(panel);
        panel.setLayout(panelLayout);

        int labelSize = searchTF.getWidth() - icon.getPreferredSize().width;
        panelLayout.setHorizontalGroup(panelLayout.createSequentialGroup()
            .addComponent(text, labelSize,labelSize,labelSize)
            .addComponent(icon, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
        );
        panelLayout.setVerticalGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(text, 25,25,25)
            .addComponent(icon, 25,25,25)
        );
        panel.setBackground(bgColor);
        setMouseListenerOnRow(panel);
        
    }
    private void setMouseListenerOnRow(JPanel panel) {
        panel.addMouseListener(new MouseListener() {
            
            @Override
			public void mouseReleased(MouseEvent e) {
			}
            
            
            @Override
            public void mousePressed(MouseEvent e) {
                Component c = e.getComponent();
                JLabel label = (JLabel)(((JPanel)c).getComponent(0));                

                String space = "  ";
                if (getSearchText().length() == 0) {
                    space = "";
                }
                String val = label.getText();
                if (!val.trim().endsWith(":")) {
                    searchTF.setText(searchTF.getText() + space +val+"  ");
                }   
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setCursor(Cursor.getDefaultCursor());
                updateOffHoverBackground(e.getComponent());
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                updateHoverBackground(e.getComponent());
            }
            
            @Override
			public void mouseClicked(MouseEvent e) {
			}
        });
    }
    
    
    private void updateHoverBackground(Component p) {
        Color bgColor = ((ThemeComboBox)GUITheme.get("combobox")).getSelectedindropdownbackground();
        Color fgColor = ((ThemeComboBox)GUITheme.get("combobox")).getItemSelectionforeground();
        doColorUpdate(p, bgColor, fgColor);
    }
    
    
    private void updateOffHoverBackground(Component p) {
        Color bgColor = ((ThemeTextField)GUITheme.get("textfield")).getBackground();
        Color fgColor = ((ThemeTextField)GUITheme.get("textfield")).getForeground();
        doColorUpdate(p, bgColor, fgColor);
    }

    
    private void doColorUpdate(Component component, Color bg, Color fg) {
        JPanel p = null;
        if (!(component instanceof JPanel)) {
            p = (JPanel) component.getParent();
        } else {
            p = (JPanel) component;
        }
        Component[] comps = p.getComponents();
        
        for(Component comp : comps) {
            JLabel label = null;
            if (comp instanceof JLabel) {
                label = (JLabel) comp;
                label.setBackground(bg);
                label.setForeground(fg);
                label.repaint();
            }
        }
        p.setBackground(bg);
        p.setForeground(fg);
        p.repaint();
    }
    private void buildSearchDialog() {
        Color bgColor = ((ThemeTextField)GUITheme.get("textfield")).getBackground();
        JPanel mainPanel = new JPanel();
        JLabel nameLbl = new JLabel(" name:");
        JLabel catLbl = new JLabel(" category:");
        JLabel subcatLbl = new JLabel(" subcategory:");
        JLabel topicLbl = new JLabel(" topic:");
        JLabel descLbl = new JLabel(" desc:");
        JLabel citationLbl = new JLabel(" citation:");
        JLabel linksLbl = new JLabel(" links:");
        JLabel addNameIcon = new JLabel(" + ");
        JLabel addCatIcon = new JLabel(" + ");
        JLabel addSubcatIcon = new JLabel(" + ");
        JLabel addTopicIcon = new JLabel(" + ");
        JLabel addDescIcon = new JLabel(" + ");
        JLabel addCitationIcon = new JLabel(" + ");
        JLabel addLinksIcon = new JLabel(" + ");
        
        Dimension iconD = new Dimension(28,10);
        addNameIcon.setPreferredSize(iconD);
        addCatIcon.setPreferredSize(iconD);
        addSubcatIcon.setPreferredSize(iconD);
        addTopicIcon.setPreferredSize(iconD);
        addDescIcon.setPreferredSize(iconD);
        addCitationIcon.setPreferredSize(iconD);
        addLinksIcon.setPreferredSize(iconD);
        
        
        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainLayout);
        ParallelGroup hg = mainLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup vg = mainLayout.createSequentialGroup();
        
        JPanel nameP = new JPanel();
        JPanel catP = new JPanel();
        JPanel subcatP = new JPanel();
        JPanel topicP = new JPanel();
        JPanel descP = new JPanel();
        JPanel citationP = new JPanel();
        JPanel linksP = new JPanel();
        
        layoutOneTagRow(nameP, nameLbl, addNameIcon);
        layoutOneTagRow(catP, catLbl, addCatIcon);
        layoutOneTagRow(subcatP, subcatLbl, addSubcatIcon);
        layoutOneTagRow(topicP, topicLbl, addTopicIcon);
        layoutOneTagRow(descP, descLbl, addDescIcon);
        layoutOneTagRow(citationP, citationLbl, addCitationIcon);
        layoutOneTagRow(linksP, linksLbl, addLinksIcon);
        
        hg.addComponent(nameP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        hg.addComponent(catP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        hg.addComponent(subcatP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        hg.addComponent(topicP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        hg.addComponent(descP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        hg.addComponent(citationP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        hg.addComponent(linksP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        
        
        vg.addComponent(nameP, 30,30,30);
        vg.addComponent(catP, 30,30,30);
        vg.addComponent(subcatP, 30,30,30);
        vg.addComponent(topicP, 30,30,30);
        vg.addComponent(descP, 30,30,30);
        vg.addComponent(citationP, 30,30,30);
        vg.addComponent(linksP, 30,30,30);
        
        mainLayout.setHorizontalGroup(hg);
        mainLayout.setVerticalGroup(vg);
        
        searchDialog = new JDialog(newAddLayerDialog);
        searchDialog.setFocusableWindowState(false);

        JRootPane rootPane = searchDialog.getRootPane();
        rootPane.setWindowDecorationStyle(JRootPane.NONE);
        rootPane.setBorder(null);
        searchDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        mainPanel.setBackground(bgColor);
        searchDialog.setBackground(bgColor);
        searchDialog.add(mainPanel);
        searchDialog.pack();
    }
    
    private void buildResultsDialog() {
        Color bgColor = ((ThemeTextField)GUITheme.get("textfield")).getBackground();

        resultsDialog = new JDialog(newAddLayerDialog);
        resultsDialog.setResizable(false);
        resultsDialog.setFocusableWindowState(false);

        resultsPanel = new JPanel();
        JRootPane rootPane = resultsDialog.getRootPane();
        rootPane.setWindowDecorationStyle(JRootPane.NONE);
        rootPane.setBorder(null);
        resultsDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        resultsDialog.setBackground(bgColor);
        resultsPanel.setBackground(bgColor);
        
        searchResultsSP = new JScrollPane(resultsPanel);
        searchResultsSP.getVerticalScrollBar().setUnitIncrement(40);
        
        searchResultsSP.setAlignmentX(LEFT_ALIGNMENT);

        searchResultsSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        searchResultsSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        resultsDialog.add(searchResultsSP);

        resultsDialog.setVisible(false);
    }
    
    private void placeSearchBox() {
        try {
            Double x = searchTF.getLocationOnScreen().getX();
            Double y = searchTF.getLocationOnScreen().getY();
            
            int height = searchTF.getHeight();
            int aX = x.intValue();
            int bY = y.intValue() + height; 
            resultsDialog.setLocation(aX, bY);
        } catch (Exception e) {           
            newAddLayerDialog.setVisible(false);
            resultsDialog.setVisible(false);
        }
     }

    private void handleSuggestion(Component c) {
        JLabel label = (JLabel)(((JPanel)c).getComponent(0));
        String space = "  ";
        String searchText = getSearchText();
        searchText = searchText.trim();
        if (searchText.length() == 0) {
            space = "";
        }
        String text = label.getText();

        if (text.indexOf("<") > -1) {
            text = text.substring(0,text.indexOf("<"));
            text = text.trim();
        }
        int[] idxs = getCurrentTagStartEndIndexes();
        
        int start = idxs[0];
        int startNextTag = idxs[1];
        if (start == -1 && startNextTag == -1) {
            searchTF.setText(text);
        } else {
                int caretPos = searchTF.getCaretPosition();
                if (caretPos > searchText.length()) {
                    caretPos = searchText.length();//after trim, caret can be higher than the end of the String
                }
                    boolean cont = true;
                    int z = caretPos;
                    boolean substring = false;
                    do {
                        z--;
                        if (z < 0) {
                            start = caretPos;
                            break;
                        }
                        char sp = searchText.charAt(z);
                        if (sp == ' ') {
                            start = z;
                            cont = false;
                            substring = true;
                        }
                    } while (cont);
                    String first = searchText.substring(0,start);
                    if (text.trim().toLowerCase().startsWith(first.trim().toLowerCase())) {
                        first = "";
                    }
                    if (substring) {
                        String sub = "";
                        if (caretPos < searchText.length()) {
                            sub = searchText.substring(start);
                        } else {
                            sub = searchText.substring(start,caretPos);
                        }
                        if (sub != null && sub.trim().length() > 0) { 
                            if (sub.trim().length() > 1) {//prevent name: phase s subcategory: bob becoming name: phase subcategory: bob
                                if (!text.trim().toLowerCase().startsWith(sub.trim().toLowerCase())) { 
                                    first = searchText.substring(0,caretPos);
                                } else {
                                    first = first + sub;
                                }
                            } else {
                                first = first + sub;
                            }
                        }
                    }
                    String last = "";
                    if (searchText.trim().length() > first.trim().length()) {
                        last = searchText.substring(caretPos);
                    }
                    String val = first + " " + text+" "+last;
                    searchTF.setText(val);

        }
    }
    private void layoutOneResultRow(JPanel panel, JLabel text, boolean tagOnly) {
        Color bgColor = ((ThemeTextField)GUITheme.get("textfield")).getBackground();
        text.setBackground(bgColor);
        
        GroupLayout panelLayout = new GroupLayout(panel);
        panel.setLayout(panelLayout);

        int labelSize = searchTF.getWidth();
        panelLayout.setHorizontalGroup(panelLayout.createSequentialGroup()
            .addComponent(text, labelSize,labelSize,labelSize));
        panelLayout.setVerticalGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(text, 20,20,20));
        panel.setBackground(bgColor);
        panel.addMouseListener(new MouseListener() {
            
            @Override
			public void mouseReleased(MouseEvent e) {
			}
            
            @Override
            public void mousePressed(MouseEvent e) {
                
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setCursor(Cursor.getDefaultCursor());
                updateOffHoverBackground(e.getComponent());
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                updateHoverBackground(e.getComponent());
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                Component c = e.getComponent();
                handleSuggestion(c);
                if (!tagOnly) {
                    searchTF.postActionEvent();
//                    if (presentSuggestion(suggestionList)) {
//                        doContentSearch();
//                    } else {
//                        searchTF.postActionEvent();
//                    }
                } else {
                    doContentSearch();
                }
            } 
        });     
    }
    
    
    private String getSearchText() {
        String temp = searchTF.getText().trim().toLowerCase();
        temp = temp.replace(":",": ");
        return temp;
    }
    
    
    private String getCategory() {
        String cat = getSpecificTagValue("category:");
        if (cat == null) {
            cat = getSpecificTagValue("instrument:");
            if (cat != null) {
                cat = "instrument";
            } else {
                cat = getSpecificTagValue("imagery:");
                if (cat != null) {
                    cat = "imagery";
                } else {
                    cat = getSpecificTagValue("custom:");
                    if (cat != null) {
                        cat = "custom";
                    }
                }
            } 
        }
        return cat;
    }
    
    
    private String getSubcategory() {
        String subcat = getSpecificTagValue("subcategory:");
        if (subcat == null) {
            subcat = getSpecificTagValue("instrument:");
            if (subcat == null) {
                subcat = getSpecificTagValue("imagery:");
            }
        }
        return subcat;
    }
    
    
    private String getSpecificTagValue(String tag) {
        String searchText = getSearchText();
        int endInputIdx = -1;
        int currentTagIdx = -1;
        
        int start = 0;
        boolean next = false;
        boolean found = false;
        String[] splitVals = searchText.split(" ",0);
        for (String val : splitVals) {
            val = val.trim();
            if (val.equalsIgnoreCase(tag.trim())) {
                currentTagIdx = searchText.indexOf(":",start)+1;
                next = true;
                found = true;
            } else if (next && searchProvider.getSearchOptions().contains(val)) {
                endInputIdx = searchText.indexOf(val,start);//we found the start of another tag, that is the end of the value for our tag
                break;
            }
            start += val.length();
            
        }
        if (found) {
            String result;
            if (endInputIdx == -1) {
                result = searchText.substring(currentTagIdx);
            } else {
                result = searchText.substring(currentTagIdx,endInputIdx);
            }
            return result;
        } else {
            return null;
        }
        
    }
    
    private int[] getSpecificTagIndexes(String tag) {
        String searchText = getSearchText();
        int[] idxs = new int[2];
        int endInputIdx = -1;
        int currentTagIdx = -1;
        
        int start = 0;
        boolean next = false;
        String[] splitVals = searchText.split(" ",0);
        for (String val : splitVals) {
            val = val.trim();
            if (val.equalsIgnoreCase(tag.trim())) {
                currentTagIdx = searchText.indexOf(val,start);
                next = true;
            } else if (next && searchProvider.getSearchOptions().contains(val)) {
                endInputIdx = searchText.indexOf(val,start);//we found the start of another tag, that is the end of the value for our tag
                break;
            }
            start += val.length();
            
        }
        idxs[0] = currentTagIdx;
        idxs[1] = endInputIdx;
        return idxs;
    }
    private boolean presentSuggestion(String[] tags) {
        for (String tag : tags) {
            int[] idxs = getSpecificTagIndexes(tag);
            if (idxs[0] > -1 && idxs[1] == -1) {//if there is one tag, return true
                return true;
            }
        }
        return false;
    }
    
    private int[] getCurrentTagStartEndIndexes() {
        int caret = searchTF.getCaretPosition();
        String searchText = getSearchText();
        int[] idxs = new int[2];
        int endInputIdx = -1;
        int currentTagIdx = -1;
        
        
        int start = 0;
        String[] splitVals = searchText.split(" ",0);
        for (String val : splitVals) {
            val = val.trim();
            if (searchProvider.getSearchOptions().contains(val)) {
                start = searchText.indexOf(val,start);
                if (start > caret) {
                    endInputIdx = start;//mark the start of the next tag as the end of the search input
                    break;
                } else {
                    currentTagIdx = start; 
                }
                start += val.length();
            }
        }
        idxs[0] = currentTagIdx;
        idxs[1] = endInputIdx;
        return idxs;
    }
    private ArrayList<SearchResultRow> searchLayers(String searchText) {
        return searchProvider.searchLayers(searchText);
    }
    private void doContentSearch() {
        selectedSuggestion = -1;
        placeSearchBox();
        resultsPanel.removeAll();
        int count = 0;
        ArrayList<String> results = contentSearch();
//        Collections.sort(results);
        if (results != null && results.size() > 0) {
            for(String x : results) {
                JPanel p = new JPanel();
                JLabel lbl = new JLabel(x);
                boolean tagOnly = (x.endsWith("<search terms>") ? true : false);
                layoutOneResultRow(p, lbl, tagOnly);
                resultsPanel.add(p);
                resultsPanel.add(Box.createRigidArea(new Dimension(0,3)));
            }
        } else {
            if (!suppressSuggestionDialog) {
                if (searchTF.getText().length() > 0) {
                    JPanel p = new JPanel();
                    String msg = "  No results to display.";
                    if (tooManyResultsFlag) {
                        msg = "Please enter more than two letters to execute search.";
                        tooManyResultsFlag = false;
                    }
                    JLabel lbl = new JLabel(msg);
                    layoutOneResultRow(p, lbl, true);
                    resultsPanel.add(p);
                    resultsPanel.add(Box.createRigidArea(new Dimension(0,3)));
                } else {
                    for(String option : searchProvider.getSearchOptions()) {
                        JPanel p = new JPanel();
                        JLabel lbl = new JLabel(" "+option);
                        layoutOneResultRow(p, lbl, true);
                        resultsPanel.add(p);
                        resultsPanel.add(Box.createRigidArea(new Dimension(0,3)));
                    }
                }
            }
        }
        
        Dimension d = new Dimension(searchTF.getWidth(), Math.min(600,resultsPanel.getPreferredSize().height));
        resultsDialog.setMaximumSize(d);
        resultsDialog.setPreferredSize(d);
        resultsDialog.setSize(d);
        
        resultsPanel.setMaximumSize(d);
        
        searchResultsSP.revalidate();
        resultsDialog.pack();
        
        resultsDialog.setVisible(true);
        resultsDialog.repaint();
        
//        int addHeight = newAddLayerDialog.getHeight();
//        Dimension d = resultsPanel.getPreferredSize();
//        if (resultsPanel.getPreferredSize().getHeight() > addHeight) {//if there is more content, we can expand the dialog size
//            d = new Dimension(searchTF.getWidth(), newAddLayerDialog.getHeight()-160);
//        }
//        
//        resultsDialog.setMaximumSize(d);
//        resultsDialog.setPreferredSize(d);
//        resultsDialog.setSize(d);
        

    }
    private ArrayList<String> contentSearch() {
        ArrayList<String> finalResults = new ArrayList<String>();
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<String> nameResults = new ArrayList<String>();
//        Set<String> finalResults = new LinkedHashSet<String>();
//        HashMap<String,String> results = new HashMap<String,String>();
//        HashMap<String,String> nameResults = new HashMap<String,String>();
        
        String searchText = getSearchText();
        if (searchText.length() == 0) { 
            return null;
        }
        int currentTagIdx = -1;
        int endInputIdx = -1;
        
        int[] idxs = getCurrentTagStartEndIndexes();
        currentTagIdx = idxs[0];
        endInputIdx = idxs[1];
        
        String tag = "all";
        String value = null;
        boolean noEndTag = false;
        if (currentTagIdx > -1){ //if there is a tag in the search
            tag = searchText.substring(currentTagIdx,searchText.indexOf(":",currentTagIdx));
            int valueBeginIdx = searchText.indexOf(":",currentTagIdx) + 1;
            if (endInputIdx == -1) {
                noEndTag = true;
                value = searchText.substring(valueBeginIdx);
            } else {
                value = searchText.substring(valueBeginIdx,endInputIdx);
            }
        } else {
            noEndTag = true;
            value = searchText;
        }
        if (noEndTag && value.trim().length() > 0) {
            for(String opt : searchProvider.getSearchOptions()) {
                if (opt.trim().startsWith(value.trim()) && !opt.trim().equalsIgnoreCase(value.trim()+":")) {
                    nameResults.add(" "+opt);
                }
            }
        }
        value = value.toLowerCase().trim();
        if (value.length() == 0 && currentTagIdx < 1) {
            loadSuggestions(results, tag);
        } else {
            String[] split = value.split(" ", 0);
            String catValue = null;
            String subcatValue = null;
            String topicValue = null;
            catValue = getCategory();
            subcatValue = getSubcategory();
            topicValue = getSpecificTagValue("topic:");
            
            if (catValue != null) {
                catValue = catValue.trim();
            }
            if (subcatValue != null) {
                subcatValue = subcatValue.trim();
            }
            if (topicValue != null) {
                topicValue = topicValue.trim();
            }
            
            switch(tag) {
                case "category":
                    results.addAll(searchProvider.getPartialSuggestionCat(catValue));
                    break;
                case "subcategory":
                    results.addAll(searchProvider.getPartialSuggestionSubcat(catValue,subcatValue));
                    break;
                case "topic": 
                    results.addAll(searchProvider.getPartialSuggestionTopic(catValue,subcatValue,topicValue));
                    break;
                case "name":
                    if (value.length() > 2) {
                        if (catValue != null || subcatValue != null || topicValue != null) {
                            results.addAll(searchProvider.getSuggestionWithHierarchy(catValue,subcatValue,topicValue, split, SearchProvider.TAG_NAME));
                        } else {
                            results.addAll(searchProvider.getPartialSuggestion(split,SearchProvider.TAG_NAME));
                        }
                    } else {
                        tooManyResultsFlag = true;
                    }
                    break;
                case "description":
                case "desc":
                    if (value.length() > 2) {
                        if (catValue != null || subcatValue != null || topicValue != null) {
                            results.addAll(searchProvider.getSuggestionWithHierarchy(catValue,subcatValue,topicValue, split, SearchProvider.TAG_DESC));
                        } else {
                            results.addAll(searchProvider.getPartialSuggestion(split, SearchProvider.TAG_DESC));
                        }
                    } else {
                        tooManyResultsFlag = true;
                    }
                    break;
                case "citation":
                    if (value.length() > 2) {
                        if (catValue != null || subcatValue != null || topicValue != null) {
                            results.addAll(searchProvider.getSuggestionWithHierarchy(catValue,subcatValue,topicValue, split, SearchProvider.TAG_CITATION));
                        } else {
                            results.addAll(searchProvider.getPartialSuggestion(split,SearchProvider.TAG_CITATION));
                        }
                    } else {
                        tooManyResultsFlag = true;
                    }
                    break;
                case "links":
                    if (value.length() > 2) {
                        if (catValue != null || subcatValue != null || topicValue != null) {
                            results.addAll(searchProvider.getSuggestionWithHierarchy(catValue,subcatValue,topicValue, split, SearchProvider.TAG_LINKS));
                        } else {
                            results.addAll(searchProvider.getPartialSuggestion(split,SearchProvider.TAG_LINKS));
                        }
                    } else {
                        tooManyResultsFlag = true;
                    }
                    break;   
                case "imagery":
                    results.addAll(searchProvider.getPartialSuggestionCat("Imagery"));
                    break;
                case "instrument":
                    results.addAll(searchProvider.getPartialSuggestionCat("Instrument"));
                    break;
                case "custom":
                    results.addAll(searchProvider.getPartialSuggestionCustom(value));
                    break;
                case "favorite":
                    results.addAll(searchProvider.getPartialSuggestionFavorite(value));
                    break;
                case "all":
                    if (value.length() > 2) {
                        results.addAll(searchProvider.getPartialSuggestionAll(split));   
                    } else {
                        tooManyResultsFlag = true;
                    }
                    break;
            }
        }
        finalResults.addAll(nameResults);
        finalResults.addAll(results);
        return finalResults;        
    }
    
    
    private void loadSuggestions(ArrayList<String> results, String tag) {
        switch(tag) {
            case "name":
                results.addAll(searchProvider.getSuggestionHome());
                break;
            case "category":
                results.addAll(searchProvider.getSuggestionCategory());
                break;
            case "subcategory":
                results.addAll(searchProvider.getSuggestionSubcategory());
                break; 
            case "topic":
                results.addAll(searchProvider.getSuggestionTopic());
                break;
            case "description":
            case "desc":
            case "links":
            case "citation":
                results.add(" "+tag+": <search terms>");
                results.add(" "+tag+": THEMIS");
                results.add(" "+tag+": Mars MOLA");
                results.add(" "+tag+": mars day ir");
                results.add(" "+tag+": JPL NASA");
//                results.addAll(searchProvider.getSuggestionHome());
                break; 
            case "custom":
                results.addAll(searchProvider.getSuggestionCustom());
                break;
            case "favorite":
                results.addAll(searchProvider.getSuggestionFavorite());
                break;
            case "instrument":
                results.addAll(searchProvider.getSuggestionSubcatsAndTopicsForCat("Instrument"));
                break;
            case "imagery":
                results.addAll(searchProvider.getSuggestionSubcatsAndTopicsForCat("Imagery"));
                break;
            default : 
                results.addAll(searchProvider.getSuggestionHome());
                break;
        }
    }

    public void setupAddLayerData() {
        for (LayerParameters layer : LayerParameters.lParameters) {
            //make a HashMap by layer id with all the layers that are associated with that id
            //each one of these entries are essentially "layer". The layer parameters that are stored in the 
            //associated ArrayList are all really buttons created to add that layer. They might
            //have a different categorization or button name. But in the end, they are all the same layer.
            ArrayList<LayerParameters> layerList = layersById.get(layer.id);
            if (layerList == null) {
                layerList = new ArrayList<LayerParameters>();
                layersById.put(layer.id, layerList);
            }
            layerList.add(layer);
            
            //now make a HashMap by the hash of description storing all layer ids with that description
            String hash = String.valueOf(layer.description.hashCode());
            Set<String> layerIdList = layersByDesc.get(hash);
            if (layerIdList == null) {
                layerIdList = new HashSet<String>();
                layersByDesc.put(hash,layerIdList);
            }
            layerIdList.add(layer.id);
        }
    }
    
    
    private boolean checkValue(String key, String layerVal, String check, String[] split, ArrayList<String> results, String tag, boolean exact) {
        boolean containsAll = true;
        boolean found = false;
        for (String one : split) {
            if (check.contains(one)) {
                found = true;
            } else {
                containsAll = false;
                break;
            }
        }
        if (containsAll && found) {
            String value = "";
            if (!exact){
                value = " name: "+layerVal+" <"+tag+" match>"; 
            } else {
                value = " "+tag + ": "+layerVal;
            }
            if (key == null) {
                key = value;
            }
            results.add(value);
            return true;
        }
        return false;
    }
    private void selectSuggestion(int arrow) {
        if (!resultsPanel.isVisible()) {
            return;
        }
        Component c = null;
        
        //if we have a valid selected suggestion from using arrows, reset the hover color
        if (selectedSuggestion > -1 && selectedSuggestion < resultsPanel.getComponentCount()) {
            c = resultsPanel.getComponent(selectedSuggestion);
            if (c instanceof JPanel) {
                arrowSuggestionFlag = false;
                updateOffHoverBackground(c);
            }
        }
        c = null;//reset this as it is used later
        do {//find the next panel that would be need to have the hover color...there are panels and boxlayout spacers in the component list
            if (arrow == KeyEvent.VK_DOWN) {
                if (selectedSuggestion == resultsPanel.getComponentCount()) {
                    selectedSuggestion = -1;
                }
                selectedSuggestion++;
            } else if(arrow == KeyEvent.VK_UP){
                if (selectedSuggestion == -1) {
                    selectedSuggestion = resultsPanel.getComponentCount();
                }
                
                selectedSuggestion--;
            }
            if (selectedSuggestion < 0 ) {//if they up arrowed too far, reset
                selectedSuggestion = -1;
                c = null;
                break;
            } else if (selectedSuggestion >= resultsPanel.getComponentCount()) {//if they down arrow too far, reset
                selectedSuggestion = resultsPanel.getComponentCount();
                c = null;
                break;
            }
            c = resultsPanel.getComponent(selectedSuggestion);//get the component at this index and check if it is a JPanel
        } while (!(c instanceof JPanel));
        if (c != null) {//if we have a valid JPanel, set the hover color
            arrowSuggestionFlag = true;
            updateHoverBackground(c);
        }
        
    }
    
    private void initSearchPanel(boolean searchAllLayers) {
        JLabel searchLbl = new JLabel();
        searchTF = new JTextField(50);

        searchLbl.setText("Search all layers  ");
        
        JLabel layerTooltip = new JLabel(tooltipIcon);
        layerTooltip.setOpaque(false);
        
        StringBuffer help = new StringBuffer();
        help.append("<html>");
        help.append("<div width=\"400px\"");
        help.append("<center><h3>Search hints:</h3></center>");
        help.append("<ul>Press Enter to execute a search.</ul>");
        help.append("<ul>Click the magnifying glass to execute a search.</ul>");
        help.append("<ul>Click the magnifying glass with no search text to see the Home layers.</ul>");
        help.append("<ul>Click the x icon to clear the text input.</ul>");
        help.append("<ul>Press Escape to clear the suggestion dialog.</ul>");
        help.append("<ul>Click on a tag (such as name:) and enter search text after it to search only names (or the selected tag). </ul>");
        
        
        layerTooltip.setToolTipText(help.toString());
        
        JButton clearButton = new JButton();        
        clearButton.setIcon(new ImageIcon(ImageFactory
        		           .createImage(ImageCatalogItem.CLEAR
        		           .withDisplayColor(imgColor))));
        clearButton.setUI(new IconButtonUI());

        clearButton.addActionListener(new AbstractAction(){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                clearButtonFlag = true;
                searchTF.setText("");
                searchTF.grabFocus();
            }
        });
        
        JButton searchButton = new JButton();        
        searchButton.setIcon(new ImageIcon(ImageFactory
        		           .createImage(ImageCatalogItem.SEARCH
        		           .withDisplayColor(imgColor))));
        searchButton.setUI(new IconButtonUI());
        searchButton.addActionListener(new AbstractAction() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                arrowSuggestionFlag = false;//make the click of the magnifying glass always be a full search
                searchTF.postActionEvent();                
            }
        });
        
      //layout the entire search panel
        GroupLayout searchLayout = new GroupLayout(searchPanel);
        searchPanel.setLayout(searchLayout);
        
        searchLayout.setHorizontalGroup(searchLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(searchLayout.createSequentialGroup()    
                .addComponent(searchLbl)
                .addComponent(layerTooltip))
            .addGroup(searchLayout.createSequentialGroup()
                .addComponent(searchTF)
                .addComponent(searchButton)
                .addComponent(clearButton)));
        searchLayout.setVerticalGroup(searchLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(searchLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(searchLbl)
                .addComponent(layerTooltip))
            .addGroup(searchLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(searchTF, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                .addComponent(searchButton)
                .addComponent(clearButton)));
        
        searchTF.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent e) {
                selectedSuggestion = -1;
                arrowSuggestionFlag = false;
                resultsDialog.setVisible(false);
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                if (clearButtonFlag) {
                    clearButtonFlag = false;
                    if (resultsDialog != null) {
                        resultsDialog.setVisible(false);
                    }
                } else {
                    if (resultsDialog == null) {
                        buildResultsDialog();
                    }
                    selectedSuggestion = -1;
                    placeSearchBox();
                }
            }
        });
        searchTF.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (arrowSuggestionFlag && selectedSuggestion > -1) {
                    Component c = resultsPanel.getComponent(selectedSuggestion);
                    if (c instanceof JPanel) {
                        handleSuggestion(c);
                        if (presentSuggestion(suggestionList)) {
                            doContentSearch();
                            return;
                        }
                    }
                }
                resultsDialog.setVisible(false);
                arrowSuggestionFlag = false;
                Runnable r = new Runnable() {
                    
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        String text = getSearchText();
                        final ArrayList<SearchResultRow> searchLayers = searchLayers(text);
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    MainPanel.this.buildMainResultsPane(searchLayers,false);
                                }
                            });
                        } catch(Exception e) {
                            e.printStackTrace();
                            //TODO: reset everything
                        }
                        
                    }
                };
                Thread t = new Thread(r);
                t.start();
                
            }
        });
        searchTF.addMouseListener(new MouseListener() {            
            @Override
			public void mouseReleased(MouseEvent e) {
			}
 
            
            @Override
			public void mousePressed(MouseEvent e) {
			}
            
            @Override
			public void mouseExited(MouseEvent e) {
			}
            
            @Override
			public void mouseEntered(MouseEvent e) {
			}
   
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchTF.getText().trim().length() == 0) {
                   if (resultsDialog == null) {
                        buildResultsDialog();
                    }
                    selectedSuggestion = -1;
                    placeSearchBox();
                    doContentSearch(); 
                }
            }
        });
        searchTF.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                doContentSearch();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                doContentSearch();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                doContentSearch();
            }
        });
        
        searchTF.addKeyListener(new KeyListener() {
            
            @Override
			public void keyTyped(KeyEvent e) {
			}
            
            @Override
			public void keyReleased(KeyEvent e) {
			}
            
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                switch(code) {
                    case KeyEvent.VK_DOWN :
                        selectSuggestion(KeyEvent.VK_DOWN);
                        break;
                    case KeyEvent.VK_UP :
                        selectSuggestion(KeyEvent.VK_UP);
                        break;
                    case KeyEvent.VK_ESCAPE :
                        if (resultsDialog != null) {
                            resultsDialog.setVisible(false);
                        }
                        break;
                }
            }
        });
    }
}