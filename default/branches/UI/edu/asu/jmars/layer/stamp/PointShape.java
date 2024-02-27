package edu.asu.jmars.layer.stamp;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import edu.asu.jmars.Main;
import edu.asu.msff.StampInterface;

public class PointShape extends StampShape {

	public PointShape(StampInterface stamp, StampLayer stampLayer) {
		super(stamp, stampLayer);
	}
	    
    private Point2D origin = null;
    
    public synchronized Point2D getOrigin() {
    	if (origin==null) {
    		getPath();
    	}
    	return origin;
    }	
        
    public synchronized List<Area> getFillAreas() {
    	List<GeneralPath> paths = getPath();
    	
    	ArrayList<Area> areas = new ArrayList<Area>();
    	
    	for (GeneralPath s : paths) {
    		areas.add(new Area(s));    		
    	}
    	return areas;    	
    }
    
    public synchronized List<GeneralPath> getPath()
    {
        if(paths == null)
        {
        	paths = new ArrayList<GeneralPath>();
                        
            double pts[] = myStamp.getPoints();

            origin = Main.PO.convSpatialToWorld(pts[0],pts[1]);
            
            GeneralPath path = new GeneralPath();
            
        	path.moveTo((float)origin.getX(),
                    (float)origin.getY());
        	
        	path.lineTo((float)(origin.getX()+0.0001),
                    (float)origin.getY());

        	path.lineTo((float)(origin.getX()+0.0001),
                    (float)origin.getY()+0.0001);

        	paths.add(path);
        } 
        return paths;
    }

}