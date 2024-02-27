package edu.asu.jmars.layer.map2.msd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.asu.jmars.Main;
import edu.asu.jmars.ProjObj;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.LayerParameters;
import edu.asu.jmars.layer.map2.AutoFillException;
import edu.asu.jmars.layer.map2.CompStageFactory;
import edu.asu.jmars.layer.map2.CustomMapServer;
import edu.asu.jmars.layer.map2.FileUploadDialog;
import edu.asu.jmars.layer.map2.MapAttr;
import edu.asu.jmars.layer.map2.MapAttrReceiver;
import edu.asu.jmars.layer.map2.MapChannel;
import edu.asu.jmars.layer.map2.MapChannelReceiver;
import edu.asu.jmars.layer.map2.MapData;
import edu.asu.jmars.layer.map2.MapLViewFactory;
import edu.asu.jmars.layer.map2.MapServer;
import edu.asu.jmars.layer.map2.MapServerFactory;
import edu.asu.jmars.layer.map2.MapServerListener;
import edu.asu.jmars.layer.map2.MapSource;
import edu.asu.jmars.layer.map2.Pipeline;
import edu.asu.jmars.layer.map2.PipelineEvent;
import edu.asu.jmars.layer.map2.PipelineEventListener;
import edu.asu.jmars.layer.map2.PipelineProducer;
import edu.asu.jmars.layer.map2.WMSMapServer;
import edu.asu.jmars.layer.map2.custom.CM_Manager;
import edu.asu.jmars.layer.map2.msd.AvailableMapsTree.MapTreeCellRenderer;
import edu.asu.jmars.layer.map2.stages.composite.BandAggregator;
import edu.asu.jmars.layer.map2.stages.composite.BandAggregatorSettings;
import edu.asu.jmars.layer.map2.stages.composite.CompositeStage;
import edu.asu.jmars.layer.map2.stages.composite.SingleCompositeSettings;
import edu.asu.jmars.layer.util.features.FPath;
import edu.asu.jmars.util.Config;
import edu.asu.jmars.util.DebugLog;
import edu.asu.jmars.util.Util;

/**
 * MapSettings component provides a user with the Map Settings dialog.
 * 
 * This class defines all controller actions.
 * 
 * 'MapSettings.xml' together with CookSwing provides the layout.
 * 
 * MapProcessingSettings contains the user's processing selections.
 * 
 * MapViewPrefs provides persistent visual preferences.
 */
public class MapSettingsDialog implements PipelineProducer {
    private static DebugLog log = DebugLog.instance();   
    
    public static void main(String[] args) throws Exception {
        final MapSettingsDialog msd = new MapSettingsDialog(null,null);
        msd.dialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });

        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                msd.dialog.setVisible(true);
            }
        });
    }

    public final JDialog dialog;

    
    // Local state
    
    private AvailableMapsTree availTree;
    private AvailableMapsModel availModel;
    private ProcTree procTree;
    private JButton okButton, cancelButton, uploadButton, shareButton, delButton, groupsButton, removePlotSourceButton;
    private JList plotSourcesList;
    // true when all pipelines are ok.
    boolean okay = false;
    private List<PipelineEventListener> pipelineEventListeners = new ArrayList<PipelineEventListener>();
    Pipeline[] savedLViewPipeline = null;
    Pipeline[] savedChartPipeline = null;
    private JLabel mapPreview = null;
    private JTextArea mapAbstractTA = null;
    private AbstractAction cancelAction = new CancelAction();
    
    public MapSettingsDialog(JDialog ownerDialog) {
        this(null,ownerDialog);
    }
    public MapSettingsDialog(JFrame ownerFrame) {
        this(ownerFrame, null);
    }
    private MapSettingsDialog(JFrame ownerFrame, JDialog ownerDialog) {
        if (ownerDialog != null) {
            dialog = new JDialog(ownerDialog, "New Map Settings Dialog", true);
        } else {
            if (ownerFrame == null) {
                ownerFrame = Main.mainFrame;
            }
            dialog = new JDialog(ownerFrame, "New Map Settings Dialog", true);
        }
        dialog.setSize(800, 600);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
        //Main panel
        JPanel panel = new JPanel(new BorderLayout());
        
        //Left panel of main panel
        JPanel leftMainPanel = new JPanel(new BorderLayout());
        
        //split pane for top section of map browser
        JSplitPane mapBrowserTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        mapBrowserTop.setResizeWeight(1);
        
        //object for setting contstraints as components are added
        CellConstraints cc = new CellConstraints();
        
        //Custom map buttons
        FormLayout cmFormLayout = new FormLayout("pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu, pref", "pref");
        PanelBuilder cmButtonPanel = new PanelBuilder(cmFormLayout);
        cmButtonPanel.setDefaultDialogBorder();
        uploadButton = new JButton("Upload Map...".toUpperCase());
        cmButtonPanel.add(uploadButton,cc.xy(1,1));//might need a new CC there?
        delButton = new JButton("Delete Map...".toUpperCase());
        cmButtonPanel.add(delButton,cc.xy(3,1));
        shareButton = new JButton("Share Map...".toUpperCase());
        cmButtonPanel.add(shareButton,cc.xy(5,1));
        groupsButton = new JButton("Groups...".toUpperCase());
        cmButtonPanel.add(groupsButton,cc.xy(7,1));
        
        //Available maps
        FormLayout formLayoutAvailMaps = new FormLayout("4dlu, fill:default:grow, 4dlu", "4dlu, pref, 4dlu, fill:default:grow, 4dlu, pref, 4dlu");
        PanelBuilder availMapsPB = new PanelBuilder(formLayoutAvailMaps);
        availMapsPB.setDefaultDialogBorder();
        availMapsPB.addLabel("Available Maps", cc.xy(2,2));
        availTree = new AvailableMapsTree();
        availTree.setRootVisible(false);
        JScrollPane availTreeSP = new JScrollPane(availTree);
        availMapsPB.add(availTreeSP, cc.xy(2,4));
        availMapsPB.add(cmButtonPanel.getPanel(), cc.xy(2,6));
        
        
        //Selected map panel
        FormLayout formLayoutSelMap = new FormLayout("right:pref, 4dlu, left:10dlu:grow", "pref");
        PanelBuilder selMapPanel = new PanelBuilder(formLayoutSelMap);
        selMapPanel.addLabel("Selected Map", cc.xy(1,1));
        
        //map abstract text area
        mapAbstractTA = new JTextArea();
        mapAbstractTA.setRows(6);
        mapAbstractTA.setText("");
        mapAbstractTA.setColumns(20);
        mapAbstractTA.setLineWrap(true);
        mapAbstractTA.setWrapStyleWord(true);
        mapAbstractTA.setEditable(false);
        JScrollPane mapAbsSP = new JScrollPane(mapAbstractTA);
        
        //outer panel for selected map and map abstract
        FormLayout formLayoutSelMapOuter1 = new FormLayout("fill:pref:grow", "fill:pref:grow pref");
        PanelBuilder selMapOuter1PB = new PanelBuilder(formLayoutSelMapOuter1);
        selMapOuter1PB.add(selMapPanel.getPanel(), cc.xy(1,1));
        selMapOuter1PB.add(mapAbsSP, cc.xy(1,2));
        
        //map preview label
        mapPreview = new JLabel("");
        mapPreview.setAlignmentX(0.5f);
        mapPreview.setAlignmentY(0.5f);
        mapPreview.setPreferredSize(new Dimension(100, 100));
        EtchedBorder etchedBorder = new EtchedBorder();
        mapPreview.setBorder(etchedBorder);
        
        //panel for bottom section of the map browser-top area of the split pane
        FormLayout mapBrowserTopBotSec1 = new FormLayout("4dlu, fill:pref:grow, 4dlu, pref, 4dlu", "4dlu, top:pref, 4dlu");
        PanelBuilder mapBrowserTopBottomPB = new PanelBuilder(mapBrowserTopBotSec1);
        mapBrowserTopBottomPB.add(selMapOuter1PB.getPanel(), cc.xy(2,2));
        mapBrowserTopBottomPB.add(mapPreview, cc.xy(4,2));
        
        //add components to the map browser top section
        mapBrowserTop.add(availMapsPB.getPanel(), JSplitPane.LEFT);
        mapBrowserTop.add(mapBrowserTopBottomPB.getPanel(), JSplitPane.RIGHT);
        
        //add map browser top to the left section of the main panel
        leftMainPanel.add(mapBrowserTop, BorderLayout.CENTER);
        
        //right main panel
        JPanel rightMainPanel = new JPanel(new BorderLayout());
        
        procTree = new ProcTree();
        procTree.setRootVisible(false);
        JScrollPane scrollPane = new JScrollPane(procTree);
        
        //panel for visible map
        FormLayout rightTop = new FormLayout("4dlu, fill:default:grow, 4dlu","4dlu, pref, 4dlu, fill:40dlu:grow, 4dlu");
        PanelBuilder rightTopPB = new PanelBuilder(rightTop);
        rightTopPB.addLabel("Visible Map",cc.xy(2,2));
        rightTopPB.add(scrollPane,cc.xy(2,4));
        
        JSplitPane rightSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        rightSP.setResizeWeight(.5);
        rightSP.add(rightTopPB.getPanel(),JSplitPane.LEFT);
        
        //right bottom
        removePlotSourceButton = new JButton("Remove".toUpperCase());
        removePlotSourceButton.setEnabled(false);
        
        FormLayout bottomButtonFL = new FormLayout("pref","pref");
        PanelBuilder bottomButtonPB = new PanelBuilder(bottomButtonFL);
        bottomButtonPB.add(removePlotSourceButton,cc.xy(1,1));
        
        FormLayout rightBottomFL = new FormLayout("4dlu, fill:default:grow, 4dlu","4dlu, pref, 4dlu, fill:20dlu:grow, 4dlu, pref, 4dlu");
        PanelBuilder rightBottomPB = new PanelBuilder(rightBottomFL);      
        rightBottomPB.addLabel("Plots",cc.xy(2,2));
        plotSourcesList = new JList();       
        JScrollPane plotSources = new JScrollPane(plotSourcesList);
       
        rightBottomPB.add(plotSources,cc.xy(2,4));
        rightBottomPB.add(bottomButtonPB.getPanel(),cc.xy(2,6));
        //end right bottom
        
        rightSP.add(rightBottomPB.getPanel(),JSplitPane.RIGHT);
        
        rightMainPanel.add(rightSP, BorderLayout.CENTER);
        //end right main panel
        
        
        JSplitPane splitPaneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftMainPanel, rightMainPanel);
        splitPaneMain.setResizeWeight(.5);
        
        //added components
        panel.add(splitPaneMain, BorderLayout.CENTER);
        
        //South main
        okButton = new JButton("OK".toUpperCase());
        cancelButton = new JButton("Cancel".toUpperCase());
        
        JPanel mainSouthPanel = new JPanel(new FlowLayout());
        mainSouthPanel.add(okButton);
        mainSouthPanel.add(cancelButton);
        //end South main
        
        panel.add(mainSouthPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        
        
        Util.addEscapeAction(dialog);
        
        dialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                cancelDialog();
            }
        });
        
        availModel = new AvailableMapsModel();
        availTree.setCellRenderer(new MapTreeCellRenderer());
        availTree.getSelectionModel().addTreeSelectionListener(new AvailableMapSelected());
        availTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                final MapServer server = availTree.getSelectedMapServer();
                if (server != null && SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem refresh = new JMenuItem("Refresh Server");
                    refresh.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                refreshServer(server);
                            } finally {
                                dialog.setCursor(Cursor.getDefaultCursor());
                            }
                        }
                    });
                    menu.add(refresh);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        
        if (availModel.getChildCount(availTree.getModel().getRoot()) > 0){
            availTree.expandPath(new TreePath(new Object[]{
                    availTree.getModel().getRoot(),
                    availTree.getModel().getChild(availTree.getModel().getRoot(), 0)}));
        }
        
        availTree.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2){
                    handleSourceDoubleClick(e);
                }
            }
        });
        
        getViewPipelineModel().addPipelineModelListener(new PipelineModelListener(){
            public void childrenAdded(PipelineModelEvent e) {
                resolveUnresolved(getViewPipelineModel(), unwrapMapSources(e.getChildren()), e.getChildIndices());
            }
            public void childrenChanged(PipelineModelEvent e) {
                resolveUnresolved(getViewPipelineModel(), unwrapMapSources(e.getChildren()), e.getChildIndices());
            }
            public void childrenRemoved(PipelineModelEvent e) {
            }
            public void compChanged(PipelineModelEvent e) {
                resolveUnresolved(getViewPipelineModel(), unwrapMapSources(getViewPipelineModel().getSources()), mkIntSeq(getViewPipelineModel().getSourceCount()));
            }
            public void forwardedEventOccurred(PipelineModelEvent e) {}
            
        });

        
        uploadButton.setAction(new UploadFileAction(availTree));
        
        delButton.setAction(new DeleteMapAction(availTree));
        
        shareButton.setAction(new ShareMapAction(availTree));
        
        groupsButton.setAction(new GroupAdminAction(availTree));
        
        if (Main.USER != null && Main.USER.length() > 0) {
           uploadButton.setEnabled(true);
           delButton.setEnabled(true);
           shareButton.setEnabled(true);
        } else {
            uploadButton.setEnabled(false);
            delButton.setEnabled(false);
            shareButton.setEnabled(false);
        }
        
        // check if the user's authentication domain is authorized to 
        // create/administer groups for custom map sharing
        String defaultGrp = Config.get(Config.CONFIG_DEFAULT_GROUP);
        if (defaultGrp == null && Main.USER != null && Main.USER.length() > 0) {
            groupsButton.setEnabled(true);
        } else {
            groupsButton.setEnabled(false);               
        }
                
//      JButton addMapServerButton = (JButton)cookSwing.getId("addServerPB").object;
//      addMapServerButton.setAction(new AddMapServerAction(availTree));
//      
//      JButton delMapServerButton = (JButton)cookSwing.getId("delServerPB").object;
//      delMapServerButton.setAction(new DelMapServerAction(availTree));
        
        PipelineModel plot = new PipelineModel(((CompositeStage)(new BandAggregatorSettings(0)).createStage()));
        plot.addPipelineModelListener(new PipelineModelListener(){
            public void childrenAdded(PipelineModelEvent e) {
//              resolveUnresolved(getChartPipelineModel(), unwrapMapSources(e.getChildren()), e.getChildIndices());
                resolveUnresolved(getChartPipelineModel(), unwrapMapSources(getChartPipelineModel().getSources()), mkIntSeq(getChartPipelineModel().getSourceCount()));
            }
            public void childrenChanged(PipelineModelEvent e) {
                resolveUnresolved(getChartPipelineModel(), unwrapMapSources(e.getChildren()), e.getChildIndices());
            }
            public void childrenRemoved(PipelineModelEvent e) {
                resolveUnresolved(getChartPipelineModel(), unwrapMapSources(getChartPipelineModel().getSources()), mkIntSeq(getChartPipelineModel().getSourceCount()));
            }
            public void compChanged(PipelineModelEvent e) {
                resolveUnresolved(getChartPipelineModel(), unwrapMapSources(getChartPipelineModel().getSources()), mkIntSeq(getChartPipelineModel().getSourceCount()));
            }
            public void forwardedEventOccurred(PipelineModelEvent e) {}
        });
        plotSourcesList.setModel(new PlotSourcesListModel(plot));
        plotSourcesList.setCellRenderer(new PlotSourcesCellRenderer());

        plotSourcesList.setTransferHandler(new PlotSourcesListTransferHandler());
        plotSourcesList.setDragEnabled(true);
        
        removePlotSourceButton.setAction(new RemovePlotMapSourceAction(plotSourcesList));
        
        okButton.setAction(new OkButtonAction(okButton.getActionCommand(), getViewPipelineModel(), getChartPipelineModel(), dialog));
        
        cancelButton.setAction(cancelAction);
        
        // defer processing that requires the map servers to be loaded until
        // they are loaded
        MapServerFactory.whenMapServersReady(new Runnable() {
            public void run() {
                for (MapServer server: MapServerFactory.getMapServers()) {
                    server.addListener(availModel);
                    for (MapSource source: server.getMapSources()) {
                        availModel.add(source);
                    }
                }
                availTree.setModel(availModel);
                
                if (MapServerFactory.getCustomMapServer() != null){
                    MapServerFactory.getCustomMapServer().addListener(new MapServerListener(){
                        public void mapChanged(MapSource source, Type changeType) {
                            // Repaint the MapSource just added
                            if (changeType.equals(MapServerListener.Type.ADDED)){
                                TreePath pathToMapSource = availModel.findUserObject(source);
                                if (pathToMapSource != null)
                                    availTree.setSelectionPath(pathToMapSource);
                                
                                source.getMapAttr(new MapAttrReceiver(){
                                    public void receive(MapAttr attr){
                                        availTree.repaint();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
    
    public void refreshServer(MapServer server) {
        server.loadCapabilities(false);
    }
    
    private int[] mkIntSeq(int n){
        int[] seq = new int[n];
        for(int i=0; i<seq.length; i++)
            seq[i] = i;
        return seq;
    }
    
    private void enableDisableOkButton(){
        okButton.setEnabled(okay = (getViewPipelineModel().isValid() && getChartPipelineModel().isValid()));
    }
    
    private void resolveUnresolved(final PipelineModel ppm, MapSource[] srcs, int[] indices){
        for(int i=0; i<srcs.length; i++){
            if (srcs[i] == null || ppm.getPipelineLeg(indices[i]) != null)
                continue;
            
            final MapSource src = srcs[i];
            final int srcIdx = indices[i];

            log.println("Requesting "+src.getTitle()+" to be resolved.");
            src.getMapAttr(new MapAttrReceiver(){
                public void receive(MapAttr attr) {
                    log.println("Received MapAttr to resolve "+src.getTitle()+".");
                    
                    if (srcIdx >= ppm.getSourceCount() || !src.equals(ppm.getSource(srcIdx).getWrappedSource())){
                        log.println("MapSource "+src+" no longer relevant.");
                    }
                    else {
                        try {
                            Pipeline pipeline = Pipeline.buildAutoFilled(src, ppm.getSource(srcIdx).getStage(), srcIdx);
                            ppm.setInnerStages(srcIdx, pipeline.getInnerStages());
                        }
                        catch(AutoFillException ex){
                            log.println(ex.toString());
                        }
                    }
                    enableDisableOkButton();
                }
            });
        }
    }
    
    private MapSource[] unwrapMapSources(WrappedMapSource[] wrapped){
        if (wrapped == null)
            return null;
        
        MapSource[] unwrapped = new MapSource[wrapped.length];
        for(int i=0; i<wrapped.length; i++)
            unwrapped[i] = wrapped[i].getWrappedSource();
        
        return unwrapped;
    }

    public PipelineModel getViewPipelineModel(){
        return ((ProcTreeModel)procTree.getModel()).getVisNode();
    }
    
    public PipelineModel getChartPipelineModel(){
        return ((PlotSourcesListModel)plotSourcesList.getModel()).getBackingPipelineModel();
    }
    
    public Pipeline[] buildLViewPipeline() {
        try {
            PipelineModel ppm = getViewPipelineModel();
            return Pipeline.getDeepCopy(ppm.buildPipeline());
        }
        catch(CloneNotSupportedException ex){
            throw new RuntimeException(ex);
        }
    }
    
    public Pipeline[] buildChartPipeline() {
        try {
            PipelineModel ppm = getChartPipelineModel();
            return Pipeline.getDeepCopy(ppm.buildPipeline());
        }
        catch(CloneNotSupportedException ex){
            throw new RuntimeException(ex);
        }
    }
    
    public void setLViewPipeline(Pipeline[] pipeline) throws CloneNotSupportedException {
        /*
         * Make a copy of the pipeline to isolate changes made during a MapSettingsDialog session.
         * Also, since the CompositionStage selector combo-box works only with CompositeStages 
         * stored in the CompStageFactory, replace the composition stage with an equivalent
         * CompositionStage from the CompStageFactory.
         */
        pipeline = Pipeline.getDeepCopy(pipeline);
        CompositeStage compStage = Pipeline.getCompStage(pipeline);
        compStage = CompStageFactory.instance().getStageByName(compStage.getStageName());
        pipeline = Pipeline.replaceAggStage(pipeline, compStage);

        PipelineModel ppm = getViewPipelineModel();
        ppm.setFromPipeline(pipeline, Pipeline.getCompStage(pipeline));
        savedLViewPipeline = Pipeline.getDeepCopy(pipeline);
    }
    
    public void setChartPipeline(Pipeline[] pipeline) throws CloneNotSupportedException {
        if (pipeline.length > 0 && !(Pipeline.getCompStage(pipeline) instanceof BandAggregator))
            throw new IllegalArgumentException("Chart pipelines must have "+BandAggregator.class.getName()+" as their composition stage.");
        
        /*
         * Make a copy of the pipeline to isolate changes made during a MapSettingsDialog session.
         */
        PipelineModel ppm = getChartPipelineModel();
        pipeline = Pipeline.getDeepCopy(pipeline);
        ppm.setFromPipeline(pipeline, pipeline.length == 0?
                (CompositeStage)(new BandAggregatorSettings(0)).createStage():
                    Pipeline.getCompStage(pipeline));
        savedChartPipeline = Pipeline.getDeepCopy(pipeline);
    }
    
    /**
     * Returns <code>true</code> if all pipelines are ok, <code>false</code> otherwise.
     */
    public boolean isOkay() { return okay; }
    
    class OkButtonAction extends AbstractAction implements PipelineModelListener {
        public OkButtonAction(String name, PipelineModel viewPPM, PipelineModel chartPPM, JDialog dialog){
            super(name);
            
            viewPPM.addPipelineModelListener(this);
            chartPPM.addPipelineModelListener(this);
            
            dialog.addWindowListener(new WindowAdapter(){
                public void windowOpened(WindowEvent e) {
                    enableDisableOkButton();
                }
                
            });
            enableDisableOkButton();
        }
        
        public void actionPerformed(ActionEvent e){
            saveSourceConfigurationState();
            dialog.setVisible(false);
            okay = true;
            firePipelineEvent();
        }
        
        public void childrenAdded(PipelineModelEvent e) {
            enableDisableOkButton();
        }
        public void childrenChanged(PipelineModelEvent e) {
            enableDisableOkButton();
        }
        public void childrenRemoved(PipelineModelEvent e) {
            enableDisableOkButton();
        }
        public void compChanged(PipelineModelEvent e) {
            enableDisableOkButton();
        }
        public void forwardedEventOccurred(PipelineModelEvent e) {}
    }

    public void addPipelineEventListener(PipelineEventListener l){
        pipelineEventListeners.add(l);
    }
    
    public void removePipelineEventListener(PipelineEventListener l){
        pipelineEventListeners.remove(l);
    }
    
    public void firePipelineEvent() {
        PipelineEvent e = new PipelineEvent(this, true, false);
        for(PipelineEventListener l: pipelineEventListeners) {
            l.pipelineEventOccurred(e);
        }
    }
    
    private void cancelDialog(){
        try {
            restoreSourceConfigurationState();
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(cancelButton, 
                    new String[]{ 
                        "Unable to restore saved configuration.",
                        "Reason: "+ex.toString()
                    },
                    "Error restoring configuration!",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        okay = false;
        okButton.setEnabled(true);
        dialog.setCursor(Cursor.getDefaultCursor());

        // hide the dialog
        dialog.setVisible(false);
    }
    
    private class CancelAction extends AbstractAction {
        public CancelAction(){
            super("Cancel".toUpperCase());
        }

        public void actionPerformed(ActionEvent e) {
            log.println("Cancel action performed");
            // reset the receiver so that we don't erronously push the new pipeline forward
            //recv = null;
            
            cancelDialog();
        }
        
    }
    
    private void saveSourceConfigurationState(){
        try {
            savedLViewPipeline = buildLViewPipeline();
            log.println("Saved LView: "+Arrays.asList(savedLViewPipeline));
        }
        catch(Exception ex){
            log.println("Unable to save LView pipeline.");
            savedLViewPipeline = null;
        }
        
        try {
            savedChartPipeline = buildChartPipeline();
            log.println("Saved Chart: "+Arrays.asList(savedChartPipeline));
        }
        catch(Exception ex){
            log.println("Unable to save Chart pipeline.");
            savedChartPipeline = null;
        }
    }
    
    private void restoreSourceConfigurationState() throws CloneNotSupportedException {
        if (savedLViewPipeline != null){
            setLViewPipeline(savedLViewPipeline);
            log.println("Restored LView: "+Arrays.asList(savedLViewPipeline));
        }
        if (savedChartPipeline != null){
            setChartPipeline(savedChartPipeline);
            log.println("Restored Chart: "+Arrays.asList(savedChartPipeline));
        }
    }
    

    
    static class PlotSourcesCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            MapSource mapSource = ((WrappedMapSource)value).getWrappedSource();
            value = mapSource.getTitle();
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        
    }
    
    private void handleSourceDoubleClick(MouseEvent e) {
        MapSource src = availTree.getSelectedMapSource();
        if (src == null)
            return;
        TreePath[] tgtPaths = procTree.getSelectionPaths();
        if (tgtPaths == null){
            log.println("Target paths are null.");
            return;
        }
        
        for(int i=0; i<tgtPaths.length; i++){
            if (!(tgtPaths[i].getLastPathComponent() instanceof WrappedMapSource))
                continue;
            
            WrappedMapSource wms = (WrappedMapSource)tgtPaths[i].getLastPathComponent();
            PipelineModel ppm = (PipelineModel)tgtPaths[i].getParentPath().getLastPathComponent();
            ppm.setSource(ppm.getSourceIndex(wms), src);
        }
        
        if (tgtPaths.length == 1 && tgtPaths[0].getLastPathComponent() instanceof WrappedMapSource){
            WrappedMapSource wms = (WrappedMapSource)tgtPaths[0].getLastPathComponent();
            PipelineModel ppm = (PipelineModel)tgtPaths[0].getParentPath().getLastPathComponent();
            int idx = ppm.getSourceIndex(wms);
            if (idx >= 0 && idx < (ppm.getSourceCount()-1)){
                TreePath tp = tgtPaths[0].getParentPath().pathByAddingChild(ppm.getSource(idx+1));
                procTree.setSelectionPath(tp);
            }
            else {
                procTree.clearSelection();
            }
        }
    }
    
    static class AddMapServerAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        AvailableMapsTree availMapsTree;
        
        public AddMapServerAction(AvailableMapsTree availMapsTree){
            super("Add Server...".toUpperCase());
            this.availMapsTree = availMapsTree;
        }
        
        public void actionPerformed(ActionEvent e){
            boolean cont = false;
            do {
                String newServerUrl = JOptionPane.showInputDialog(
                    availMapsTree,
                    "Enter new map server URL",
                    "New Map Server",
                    JOptionPane.QUESTION_MESSAGE);
                try {
                    new URI(newServerUrl);
                    int timeout = 10000;
                    int maxRequests = 5;
                    availMapsTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    // TODO: Since the new server will not always be a WMS Map Server, this
                    // code will need modification when we factor out the map server construction
                    // via the MapServerFactory and some generic form of parameters.
                    MapServerFactory.addNewServer(new WMSMapServer(newServerUrl, timeout, maxRequests));
                    availMapsTree.setCursor(Cursor.getDefaultCursor());
                } catch (URISyntaxException badUri) {
                    cont = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                        availMapsTree,
                        "Bad URL, try again?",
                        "Invalid data entered",
                        JOptionPane.YES_NO_OPTION);
                } catch (Exception ex) {
                    cont = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                        availMapsTree,
                        "Unable to get server capabilities, try again?",
                        "Invalid data entered",
                        JOptionPane.YES_NO_OPTION);
                }
            } while (cont);
        }
    }
    
    static class DelMapServerAction extends AbstractAction implements TreeSelectionListener {
        private static final long serialVersionUID = 1L;
        
        AvailableMapsTree availMapsTree;
        
        public DelMapServerAction(AvailableMapsTree availMapsTree){
            super("Del Server...".toUpperCase());
            this.availMapsTree = availMapsTree;
            this.availMapsTree.getSelectionModel().addTreeSelectionListener(this);
            enableDisableAction();
        }
        
        public void actionPerformed(ActionEvent e){
            MapServer mapServer = availMapsTree.getSelectedMapServer();
            int option = JOptionPane.showConfirmDialog(Util.getNearest(availMapsTree, Window.class),
                    "Delete server \""+mapServer.getTitle()+"\"",
                    "Confirm Delete Server", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION){
                MapServerFactory.removeMapServer(mapServer);
                mapServer.delete();
                enableDisableAction();
            }
        }
        
        private void enableDisableAction(){
            MapServer mapServer = availMapsTree.getSelectedMapServer();
            setEnabled(mapServer != null && mapServer.isUserDefined());
        }
        
        public void valueChanged(TreeSelectionEvent e){
            enableDisableAction();
        }
    }
    
    static JFileChooser fileChooser;
    
    class UploadFileAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private AvailableMapsTree availMapsTree;
        private FileUploadDialog fileUploadDialog = null;
        
        public UploadFileAction(AvailableMapsTree availMapsTree){
            super("Upload Map".toUpperCase());
        }
        
        public void actionPerformed(ActionEvent e) {
            CM_Manager mgr = CM_Manager.getInstance(dialog);
            mgr.setLocationRelativeTo(Main.mainFrame);
            mgr.setVisible(true);
            mgr.setSelectedTab(CM_Manager.TAB_UPLOAD);
        }
        
        private void enableDisableAction(){
            MapServerFactory.whenMapServersReady(new Runnable() {
                public void run() {
                    setEnabled(MapServerFactory.getCustomMapServer() != null);
                }
            });
        }
        
        public void valueChanged(TreeSelectionEvent e) {
            enableDisableAction();
        }
    }
    
    class DeleteMapAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        
        public DeleteMapAction(AvailableMapsTree availMapsTree){
            super("Manage Maps".toUpperCase());
        }
        
        public void actionPerformed(ActionEvent e) {
            CM_Manager mgr = CM_Manager.getInstance(dialog);
            mgr.setLocationRelativeTo(Main.mainFrame);
            mgr.setVisible(true);
            mgr.setSelectedTab(CM_Manager.TAB_EXISTING);
        }
    }
    
    /**
     * Action for clicking map sharing
     */
    class ShareMapAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
                
        public ShareMapAction(AvailableMapsTree availMapsTree){
            super("Map Sharing".toUpperCase());
        
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CM_Manager mgr = CM_Manager.getInstance(dialog);
            mgr.setLocationRelativeTo(Main.mainFrame);
            mgr.setVisible(true);
            mgr.setSelectedTab(CM_Manager.TAB_EXISTING);
            mgr.showSharingDialog();
        }
    }
    
    /**
     * A class to implement the UI and functionality for sharing custom maps with other users.
     */
    class GroupAdminAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
                
        public GroupAdminAction(AvailableMapsTree availMapsTree){
            super("Manage Groups".toUpperCase());
        }
        
        public void actionPerformed(ActionEvent e) {
            CM_Manager mgr = CM_Manager.getInstance(dialog);
            mgr.setLocationRelativeTo(Main.mainFrame);
            mgr.setVisible(true);
            mgr.setSelectedTab(CM_Manager.TAB_EXISTING);
            mgr.showSharingDialog();
        }
    }
    
    static class RemovePlotMapSourceAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        JList plotList;
        
        public RemovePlotMapSourceAction(JList plotList){
            super("Remove".toUpperCase());
            setEnabled(false);
            
            this.plotList = plotList;
            this.plotList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                    enableDisableOptions();
                }
            });
        }
        
        public void enableDisableOptions(){
            setEnabled(plotList.getSelectedIndices().length > 0);
        }
        
        public void actionPerformed(ActionEvent e) {
            Object[] selected = plotList.getSelectedValues();
            
            PipelineModel pm = ((PlotSourcesListModel)plotList.getModel()).getBackingPipelineModel();
            for(int i=0; i<selected.length; i++){
                pm.removeSource((WrappedMapSource)selected[i]);
            }
            
            enableDisableOptions();
        }
    }
    
    private static final ImageIcon waitingIcon = Util.loadIcon("resources/hourglass.gif");
    private static final ImageIcon errorIcon = Util.loadIcon("resources/error.gif");
    
    class AvailableMapSelected implements TreeSelectionListener, MapChannelReceiver {
        private final JLabel previewLabel;
        private final JTextArea mapAbstractTextArea;
        MapChannel previewRequest = new MapChannel();
        
        public AvailableMapSelected() {
            previewRequest.addReceiver(this);
            previewLabel = MapSettingsDialog.this.mapPreview;
            previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            mapAbstractTextArea = MapSettingsDialog.this.mapAbstractTA;
        }
        
        private void clearFields(){
            mapAbstractTextArea.setText("");
            previewLabel.setText("");
            previewLabel.setIcon(null);
        }
        
        public void valueChanged(TreeSelectionEvent e) {
            final MapSource source = availTree.getSelectedMapSource();
            if (source == null) {
                clearFields();
            } else {
                String description = "";
                if (source.getOwner() != null && (source.getOwner().length() > 0)) {
                    description = "Uploaded by: " + source.getOwner() + "\n";
                }
                if (source.getAbstract() != null && source.getAbstract().length() > 0) {
                    description += source.getAbstract();
                } else {
                    description += "No description available.";
                }
                if (source.getServer() != null && source.getServer() instanceof CustomMapServer
                        && description != null && description.length() > 0) {
                    description = "Custom Map\n" + description;
                }
                mapAbstractTextArea.setText(description);
                mapAbstractTextArea.setCaretPosition(0);
                previewLabel.setIcon(waitingIcon);
                log.println("Selected MapSource: "+source);
                source.getMapAttr(new MapAttrReceiver(){
                    public void receive(MapAttr attr) {
                        try {
                            // compute projection to be centered on data
                            Rectangle2D geoBounds = Util.swapRect(source.getLatLonBoundingBox());
                            double geoCenterLon = geoBounds.getCenterX();
                            double geoCenterLat;
                            if (geoBounds.getMaxY() >= 90 && geoBounds.getMinY() > -90) {
                                // if bounds go over north pole only use north pole as center
                                geoCenterLat = 90;
                            } else if (geoBounds.getMaxY() < 90 && geoBounds.getMinY() <= -90) {
                                // if bounds go over south pole only use south pole as center
                                geoCenterLat = -90;
                            } else {
                                // in all other cases just use bounds center
                                geoCenterLat = geoBounds.getCenterY();
                            }
                            ProjObj po = new ProjObj.Projection_OC(geoCenterLon,geoCenterLat);
                            
                            // create lon/lat rectangle with extra points, warp it into the data-centered
                            // projection, and use the bounding box as the request bounding box
                            double[][] corners = {
                                {geoBounds.getMinX(), geoBounds.getMinY()},
                                {geoBounds.getMaxX(), geoBounds.getMinY()},
                                {geoBounds.getMaxX(), geoBounds.getMaxY()},
                                {geoBounds.getMinX(), geoBounds.getMaxY()}
                            };
                            int interpSize = 10;
                            List<Point2D> interpolatedPoints = new ArrayList<Point2D>(interpSize*corners.length);
                            for (int i = 0; i < corners.length; i++) {
                                double[] p1 = corners[i];
                                double[] p2 = corners[(i+1)%corners.length];
                                for (int j = 0; j < interpSize; j++) {
                                    double ratio = (double)j/interpSize;
                                    // convert to east-leading longitude
                                    interpolatedPoints.add(new Point2D.Float(
                                        (float)((1-ratio)*p1[0] + ratio*p2[0]),
                                        (float)((1-ratio)*p1[1] + ratio*p2[1])
                                    ));
                                }
                            }
                            Point2D[] points = interpolatedPoints.toArray(new Point2D[interpolatedPoints.size()]);
                            Shape path = new FPath(points,FPath.SPATIAL_WEST,true).getShape();
                            Rectangle2D worldExtent = Util.normalize360(po.convSpatialToWorld(path)).getBounds2D();
                            
                            // request 'pixSides' square pixels, from a square
                            // area centered on the world extent, at the
                            // smallest ppd value large enough to put the
                            // request extent inside the data extent, but not
                            // less than 8 ppd
                            int pixSides = 80;
                            double geoSides = Math.min(worldExtent.getWidth(), worldExtent.getHeight());
                            int ppd = (int)Math.round(Math.pow(2, Math.ceil(Math.log(pixSides / geoSides) / Math.log(2))));
                            ppd = Math.max(4, ppd);
                            geoSides = pixSides / (double)ppd;
                            Rectangle2D requestExtent = new Rectangle2D.Double(
                                worldExtent.getCenterX() - geoSides/2,
                                worldExtent.getCenterY() - geoSides/2,
                                geoSides, geoSides);
                            
                            // send the request
                            MapSource[] sources = new MapSource[]{source};
                            CompositeStage composite = (CompositeStage)(new SingleCompositeSettings()).createStage();
                            Pipeline[] pipes = Pipeline.buildAutoFilled(sources, composite);
                            previewRequest.setPipeline(pipes);
                            previewRequest.setMapWindow(requestExtent, ppd, po);
                        } catch(AutoFillException ex) {
                            log.println(ex.toString());
                            previewLabel.setIcon(errorIcon);
                        }
                    }
                });
            }
        }
        
        public void mapChanged(MapData mapData) {
            if (mapData.isFinished()) {
                if (mapData.getImage() != null) {
                    previewLabel.setIcon(new ImageIcon(mapData.getImage()));
                } else {
                    previewLabel.setIcon(errorIcon);
                }
            }
        }
    }
}
