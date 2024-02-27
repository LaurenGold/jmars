package edu.asu.jmars.lmanager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import edu.asu.jmars.Main;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.LViewFactory;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.layer.LayerParameters;
import edu.asu.jmars.layer.SavedLayer;
import edu.asu.jmars.layer.landing.LandingFactory;
import edu.asu.jmars.layer.map2.MapLViewFactory;
import edu.asu.jmars.layer.map2.MapServer;
import edu.asu.jmars.layer.map2.MapServerFactory;
import edu.asu.jmars.layer.map2.MapSource;
import edu.asu.jmars.layer.map2.custom.CM_Manager;
import edu.asu.jmars.layer.shape2.ShapeFactory;
import edu.asu.jmars.layer.shape2.ShapeLView;
import edu.asu.jmars.layer.shape2.ShapeLayer;
import edu.asu.jmars.layer.slider.SliderFactory;
import edu.asu.jmars.layer.stamp.StampFactory;
import edu.asu.jmars.layer.streets.StreetLViewFactory;
import edu.asu.jmars.util.HttpRequestType;
import edu.asu.jmars.util.JmarsHttpRequest;

public class AddLayerActionListener implements ActionListener {
    private LayerParameters layer = null;   
    private boolean isCustomMap = false;
    
    public AddLayerActionListener(LayerParameters layerVal, boolean isCustom) {
        this.layer = layerVal;
        this.isCustomMap = isCustom;    
    }
  
	public void actionPerformed(ActionEvent e) {

        // Tests to make sure the servers have responded before
        // a new layer is added.
        int waitcount = 0;
        while (MapServerFactory.getMapServers() == null) {
            try {
                Thread.sleep(500);
                waitcount++;
                if (waitcount >= 30) {
                    JOptionPane.showMessageDialog(Main.mainFrame, "Unable to add layer, try again.", "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        LayerParameters l = layer;
        //Custom Maps
        if (this.isCustomMap) {
            MapServer cServer = MapServerFactory.getCustomMapServer();
            MapSource display = cServer.getSourceByName(l.options.get(0));
            MapSource plot = cServer.getSourceByName(l.options.get(1));
            if(display == null && plot == null){
                System.out.println("map not found");
                return;
            }

            new MapLViewFactory().createLayer(display, plot);
            LManager.getLManager().repaint();
        } else {
            // MAPS
            if (l.type.equalsIgnoreCase("map")) {
                // check to see if servers is null, if so set to a new list
                // hopefully shouldn't get here...but just in case
                if (l.servers == null) {
                    l.servers = new ArrayList<String>();
                }
    
                ArrayList<MapSource> plotSources = new ArrayList<MapSource>();
    
                List<MapServer> servers = MapServerFactory.getMapServers();
                MapSource display = null;
    
                MapServer server = null;
                // if server is specified try that first, if not try default server
                // first.
                if (l.servers.size() > 0 && l.servers.get(0) != null) {
                    server = MapServerFactory.getServerByName(l.servers.get(0));
                } else {
                    server = MapServerFactory.getServerByName("default");
                }
    
                // try to find map on specified server
                if (server != null) {
                    display = server.getSourceByName(l.options.get(0));
                }
    
                // if it wasn't found cycle through all map servers to find source
                if (display == null) {
                    for (MapServer s : servers) {
                        display = s.getSourceByName(l.options.get(0));
                        if (display != null) {
                            break;
                        }
                    }
                }
    
                int optionsLength = l.options.size();
                // For each map source we want to plot, loop through our list of
                // servers and look for matches
                // If a map source doesn't exist, it will silently disappear from
                // the list of plotted maps....
                for (int i = 1; i < optionsLength; i++) {
                    MapSource plot = null;
                    MapServer pserver = null;
                    // first try and use specified server (none specified means
                    // default server)
                    if (l.servers.size() > i && l.servers.get(i) != null) {
                        pserver = MapServerFactory.getServerByName(l.options.get(i));
                    } else {
                        pserver = MapServerFactory.getServerByName("default");
                    }
                    // try to find map on that server
                    if (pserver != null) {
                        plot = pserver.getSourceByName(l.options.get(i));
                    }
                    // if it finds something add that to all plots
                    if (plot != null) {
                        plotSources.add(plot);
                    }
                    // else cycle through servers looking for map
                    else {
                        for (MapServer s : servers) {
                            plot = s.getSourceByName(l.options.get(i));
                            if (plot != null) {
                                plotSources.add(plot);
                                break;
                            }
                        }
                    }
                }
    
                if (display == null && plotSources.size() == 0) {
                    System.out.println("map not found");
                    return;
                }
    
                new MapLViewFactory().createLayer(display, plotSources, l, null);
                LManager.getLManager().repaint();
            }
            // UPLOAD_MAP
            else if (l.type.equalsIgnoreCase("upload_map")) {
                CM_Manager mgr = CM_Manager.getInstance();
                mgr.setLocationRelativeTo(Main.mainFrame);
                mgr.setVisible(true);
                mgr.setSelectedTab(CM_Manager.TAB_UPLOAD);
                mgr.toFront();
            }
            // ADVANCED_MAP
            else if (l.type.equalsIgnoreCase("advanced_map")) {
                new MapLViewFactory().createLView(true, LManager.getLManager().getMainPanel().getAddLayerDialog());
                LManager.getLManager().repaint();
            }
            // SHAPES
            else if (l.type.equalsIgnoreCase("shape")) {
                if (l.name.equalsIgnoreCase("Custom Shape Layer")) {
                    ShapeLView shpLView = (ShapeLView) new ShapeFactory().newInstance(false, l);
                    LManager.getLManager().receiveNewLView(shpLView);
                    LManager.getLManager().repaint();
                } else {
                    String dirName = l.options.get(0);
                    String fileName = l.options.get(1);
                    String url = l.options.get(2);
                    boolean readOnly = true;
                    if (l.options.size() > 3) {
                        if (l.options.get(3).equals("false")) {
                            readOnly = false;
                        }
                    }
                    ShapeLView shpLView = (ShapeLView) new ShapeFactory().newInstance(readOnly, l);
                    ShapeLayer shpLayer = (ShapeLayer) shpLView.getLayer();
                    shpLayer.loadReadOnlyFile(dirName, fileName, url);
    
                    LManager.getLManager().receiveNewLView(shpLView);
                    LManager.getLManager().repaint();
                }
            }
            // OPENSTREETMAP
            else if (l.type.equalsIgnoreCase("open_street_map")) {
                if (l.options.get(0).equals("0")) {
                    int osmType = 0;
                    new StreetLViewFactory().createLView(osmType, l);
                    LManager.getLManager().repaint();
    
                } else if (l.options.get(0).equals("1")) {
                    int osmType = 1;
                    new StreetLViewFactory().createLView(osmType, l);
                    LManager.getLManager().repaint();
                }
            }
            // STAMPS
            else if (l.type.equalsIgnoreCase("stamp")) {
                ArrayList<String> temp = new ArrayList<String>(l.options);
                String instrument = temp.get(0);
                temp.remove(0);
                String[] initialColumns = new String[temp.size()];
                initialColumns = temp.toArray(initialColumns);
                new StampFactory().addLView(l, instrument, initialColumns, l.layergroup);
                LManager.getLManager().repaint();
            }
            // SAVED_LAYER
            else if (l.type.equalsIgnoreCase("saved_layer")) {
                String url = l.options.get(0);
                try {
                    if (url == null) {
                        System.err.println("Could not read URl for session: " + l.name);
                        return;
                    }
                    JmarsHttpRequest request = new JmarsHttpRequest(url, HttpRequestType.GET);
                    request.setBrowserCookies();
                    request.setConnectionTimeout(10 * 1024);
                    boolean fetchSuccessful = request.send();
                    if (!fetchSuccessful) {
                        int code = request.getStatus();
                        System.err.println("HTTP code " + code + " recieved when downloading session from " + url);
                        return;
                    }
                    List<SavedLayer> layers = SavedLayer.load(request.getResponseAsStream());
                    
                    //handle SavedLayers with multiple Layers in the JLF. 
                    //Bring them in last to first to keep the same order as they were saved.
                    //Making the first one selected because there is only one known case, it is for a demo, desired layer is being selected.
                    LView selectedView = null;
                    for (int c=layers.size()-1; c>=0; c--) {
                        SavedLayer layer = layers.get(c);
                        LView lview = layer.materialize(l);
                        if (selectedView == null) {
                            selectedView = lview;
                        }
                    }
                    if (selectedView != null) {
                        LManager.getLManager().setActiveLView(selectedView);
                    }
                    LManager.getLManager().repaint();
                    request.close();
                } catch (Exception e1) {
                    synchronized (this) {
                        System.err.println("Error processing session named " + l.name);
                        e1.printStackTrace();
                    }
                }
            }
            // TIMESLIDER
            else if (l.type.equalsIgnoreCase("timeslider")) {
                new SliderFactory().createLView(l);
                LManager.getLManager().repaint();
            }
            // LandingSite
            else if (l.type.equalsIgnoreCase("landing_site")) {
                String layerName = null;
                String config = null;
                if (l.options.size() > 0) {
                    layerName = l.options.get(0);
                    config = l.options.get(1);
                }
                new LandingFactory().createLView(false, l, layerName, config);
                LManager.getLManager().repaint();
            }
    
            // Queries the LViewFactory to get the matching factory and then create
            // and display lview.
            else {
                LViewFactory factory = LViewFactory.findFactoryType(l.type);
                if (factory != null) {
                    factory.createLView(false, l);
                    LManager.getLManager().repaint();
                } else {
                    JOptionPane.showMessageDialog(LManager.getLManager().getMainPanel().getAddLayerDialog(),
                            "Unable to add the selected layer.  Please update to the latest version of JMARS or contact the support team.");
                }
            }
        }         
    } 
}
