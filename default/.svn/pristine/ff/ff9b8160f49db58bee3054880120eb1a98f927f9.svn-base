package edu.asu.jmars.viz3d.core.geometry;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.VectorUtil;

import edu.asu.jmars.util.DebugLog;
import edu.asu.jmars.viz3d.Disposable;
import edu.asu.jmars.viz3d.renderer.gl.GLRenderable;
import edu.asu.jmars.viz3d.renderer.gl.PolygonType;
import edu.asu.jmars.viz3d.renderer.gl.outlines.OutLine;

/**
 * This class represents a filled polygon including a an outline to be drawn in 3D.
 * The only polygons that are currently supported are closed, convex polygons.
 *
 * not thread safe
 */
public class Polygon extends OutLine implements GLRenderable, Disposable {
	
	private float[] fillColor;
	float[] polyPoints;
	private float[] fittedPoints;
	private float polyScale = 1.005f;
	private float lineScale = 1.009f;
	private boolean drawOutline = true;
	PolygonType type;
	private boolean fitted = false;
	private boolean fittingEnabled = false;
	private float alpha = 1.0f;
    private Float displayAlpha;
    private static DebugLog log = DebugLog.instance();


	/**
	 * @param idNumber not currently used, can be any integral value
	 * @param points redundant at this point will be eliminated in a future release
	 * @param lineColor color of the outline or border
	 * @param fillColor color of the filled region of the polygon
	 * @param lineWidth width of the outline or border
	 * @param lineScale offset of the outline from the origin should be set to 1.0f if no offset is desired
	 * @param polyScale offset of the filled polygon from the origin should be set to 1.0f if no offset is desired
	 * @param opacity only floating point numbers from 0 to 1 are allowed, 0 = invisible, 1 = fully opaque
	 */
	public Polygon(int idNumber, float[] points, float[] lineColor, float[] fillColor,
			int lineWidth, float lineScale, float polyScale, float alpha) {
		this(idNumber, points, lineColor, fillColor, lineWidth, lineScale, polyScale, alpha, false);
	}
	/**
	 * @param idNumber not currently used, can be any integral value
	 * @param points redundant at this point will be eliminated in a future release
	 * @param lineColor color of the outline or border
	 * @param fillColor color of the filled region of the polygon
	 * @param lineWidth width of the outline or border
	 * @param lineScale offset of the outline from the origin should be set to 1.0f if no offset is desired
	 * @param polyScale offset of the filled polygon from the origin should be set to 1.0f if no offset is desired
	 * @param opacity only floating point numbers from 0 to 1 are allowed, 0 = invisible, 1 = fully opaque
	 * @param drawOutline if <code>true</code> draws the polygon's perimeter
	 */
	public Polygon(int idNumber, float[] points, float[] lineColor, float[] fillColor,
			int lineWidth, float lineScale, float polyScale, float alpha, boolean drawOutline) {
		super(idNumber, points, lineColor, lineWidth, lineScale, true);
		
		if (points == null || points.length < 9) { // we need at lease 3 points (3x3 vertices) to create a polygon
			log.aprintln("Cannot create a polygon with less than 3 points...");
			return;
		} else {
			this.drawOutline = drawOutline;
		}
		
		this.fillColor = fillColor;
		this.alpha = alpha;
		this.polyScale = (polyScale < 0f) ? 1f : polyScale;
		this.polyPoints = new float[points.length];
		for (int i=0; i<points.length; i++) {
			this.polyPoints[i] = points[i]  / lineScale * polyScale;
		}
	}

	/**
	 * @param points redundant at this point will be eliminated in a future release
	 * @param lineColor color of the outline or border
	 * @param fillColor color of the filled region of the polygon
	 * @param lineWidth width of the outline or border
	 * @param lineScale offset of the outline from the origin should be set to 1.0f if no offset is desired
	 * @param polyScale offset of the filled polygon from the origin should be set to 1.0f if no offset is desired
	 * @param opacity only floating point numbers from 0 to 1 are allowed, 0 = invisible, 1 = fully opaque
	 * @param drawOutline if <code>true</code> draws the polygon's perimeter
	 * @param onBody if <code>true</code> fits the polygon to the associated shape model 
	 */
	public Polygon(int idNumber, float[] points, float[] lineColor, float[] fillColor,
			int lineWidth, float lineScale, float polyScale, float alpha, boolean drawOutline, boolean onBody) {
		super(idNumber, points, lineColor, lineWidth, lineScale, true);
			
		if (points == null || points.length < 9) { // we need at lease 3 points (3x3 vertices) to create a polygon
			log.aprintln("Cannot create a polygon with less than 3 points...");
			return;
		} else {
			this.drawOutline = drawOutline;
		}

		if (onBody) {
			type = PolygonType.OnBody;
		} else {
			type = PolygonType.OffBody;
		}
		this.fillColor = fillColor;
		this.polyScale = (polyScale < 0f) ? 1f : polyScale;
		this.alpha = alpha;
		// need to check if the polygon is closed - the start and end points have the same values
		// if the polygon is closed mark it as such
		boolean isClosed = false;
		if (VectorUtil.isVec2Equal(points, 0, points, points.length - 3, FloatUtil.EPSILON)) {
			isClosed = true;
			this.polyPoints = new float[points.length];
		} else {
			this.polyPoints = new float[points.length + 3];
		}
		if (!onBody) {
			// these will be applied later after the polygon has been fit to the body
			for (int i=0; i<points.length; i++) {
				this.polyPoints[i] = points[i] / lineScale * polyScale;
			}
			// if the polygon is not closed add the start point to the end to close it
			if (!isClosed) {
				this.polyPoints[polyPoints.length - 3] = points[0] / lineScale * polyScale;
				this.polyPoints[polyPoints.length - 2] = points[1] / lineScale * polyScale;
				this.polyPoints[polyPoints.length - 1] = points[2] / lineScale * polyScale;				
			}
		} else {
			for (int i=0; i<points.length; i++) {
				this.polyPoints[i] = points[i];
			}
			// if the polygon is not closed add the start point to the end to close it
			if (!isClosed) {
				this.polyPoints[polyPoints.length - 3] = points[0];
				this.polyPoints[polyPoints.length - 2] = points[1];
				this.polyPoints[polyPoints.length - 1] = points[2];				
			}
		}
		
		if (polyScale > 1f) {
			for (int i=0; i< polyPoints.length; i++) {
				polyPoints[i] *= polyScale;
			}
		}
	}

	/**
	 * Reset the fill color for this polygon to a new value
	 * @param newColor
	 */
	public void setFillColor(float[] newColor) {
		fillColor = newColor;
	}
	
	private void drawFilledPolygon(GL2 gl, float[] pts, float[] color, float opacity) {
	    gl.glColor4f(color[0], color[1], color[2], opacity);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
	    gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
	    gl.glPolygonOffset(-1f,-1f);	    
	    gl.glBegin(GL2.GL_POLYGON);
	    for (int i=0; i<pts.length; i+=3) {
	    	gl.glVertex3f(pts[i], pts[i+1], pts[i+2]);
	    }
	    gl.glEnd();
	}
	
	private void drawFittedPolygon(GL2 gl, float[] pts, float[] color, float opacity) {
	    gl.glColor4f(color[0], color[1], color[2], opacity);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glBegin(GL2.GL_TRIANGLES);
	    for (int i=0; i<pts.length; i+=3) {
	    	gl.glVertex3f(pts[i], pts[i+1], pts[i+2]);
	    }
	    gl.glEnd();
	}

	/**
	 * Method to set vertices of the polygon after it has been fit to an existing shape model
	 *
	 * @param data object containing the triangular tessellation results of fitting the polygon to a shape model
	 *
	 * not thread-safe
	 */
	public void setFittedData(FittedPolygonData data) {
		int i = 0;
		float[] newVerts = new float[data.getTris().size() * 9];
		// map the vertices in one triangle at a time
		for (Triangle t : data.getTris()) {
			// first point
			newVerts[i++] = t.points[0][0];
			newVerts[i++] = t.points[0][1];
			newVerts[i++] = t.points[0][2];
			// second point
			newVerts[i++] = t.points[1][0];
			newVerts[i++] = t.points[1][1];
			newVerts[i++] = t.points[1][2];
			// third point
			newVerts[i++] = t.points[2][0];
			newVerts[i++] = t.points[2][1];
			newVerts[i++] = t.points[2][2];
		}
		
		fittedPoints = newVerts;
		// apply the fitted outline data to the outline of the polygon
		i = 0;
		float[] newLine = new float[data.getPoints().size() * 3];
		for (float[] f : data.getPoints()) {
			newLine[i++] = f[0];
			newLine[i++] = f[1];
			newLine[i++] = f[1];
		}
		super.setFittedPoints(newLine);
		
		fitted = true;
	}

	/**
	 * Method to inform whether the polygon has been fitted to a shape model
	 */
	public boolean isFitted() {
		return fitted;
	}
	
	/**
	 * Method to enable or disable the display of the polygon outline.
	 *
	 * @param display the outline will be displayed if set to true (default).
	 *
	 * This method may even be thread safe
	 */
	public void displayOutline(boolean display) {
		drawOutline = display;		
	}
	
	/**
	 * Method for user of the class to determine if the polygon outline will be
	 * drawn when rendered.
	 *
	 * @return true if the outline will be drawn
	 *
	 * Thread safe
	 */
	public boolean isOutlineDisplayed() {
		return drawOutline;
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.Disposable#dispose()
	 */
	@Override
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#execute(com.jogamp.opengl.GL2)
	 */
	@Override
	public void execute(GL2 gl) {
		if (polyPoints == null || polyPoints.length < 9) { // we need at lease 3 points (3x3 vertices) to create a polygon
			return;
		}

		if (fitted && fittingEnabled) {
			drawFittedPolygon(gl, this.fittedPoints, fillColor, getDisplayAlpha()*alpha);
		} else {
			drawFilledPolygon(gl, this.polyPoints, fillColor, getDisplayAlpha()*alpha);
		}
		if (drawOutline) {
			super.execute(gl);
		}
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#preRender(com.jogamp.opengl.GL2)
	 */
	@Override
	public void preRender(GL2 gl) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#postRender(com.jogamp.opengl.GL2)
	 */
	@Override
	public void postRender(GL2 gl) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#delete(com.jogamp.opengl.GL2)
	 */
	@Override
	public void delete(GL2 gl) {
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#getAlpha(com.jogamp.opengl.GL2)
	 */
	@Override
	public float getAlpha() {
		return alpha;
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#getDisplayAlpha(com.jogamp.opengl.GL2)
	 */
	@Override
	public float getDisplayAlpha() {
		if(displayAlpha == null){
			return alpha;
		}
		return displayAlpha;
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#setDisplayAlpha(float)
	 */
	@Override
	public void setDisplayAlpha(float alpha) {
		displayAlpha = alpha;
	}
	
	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#getId()
	 */
	@Override
	public int getId() {
		return System.identityHashCode(this);
	}
	
	/**
	 * Method to return the PolygonType
	 *
	 * @return the PolygonType
	 *
	 * thread-safe
	 */
	public PolygonType getPolygonType() {
		return type;
	}
	
	/**
	 * Method to return whether the polygon will be fit to the surface of the shape model if if one exists
	 *
	 * @return true of the polygon will be fit to the shape model when rendered
	 *
	 * thread-safe
	 */
	public boolean isFittingEnabled() {
		return fittingEnabled;
	}
	
	/**
	 * Method for the encapsulating code to enable/disable fitting the polygon to the shape model if one exists
	 *
	 * @param fittingEnabled
	 *
	 * thread-safe
	 */
	public void setFittingEnabled(boolean fittingEnabled) {
		this.fittingEnabled = fittingEnabled;
	}
	
	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#isScalable()
	 */
	@Override
	public boolean isScalable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#scaleByDivision(float)
	 */
	@Override
	public void scaleByDivision(float scalar) {
		if (Float.compare(scalar, 0f) == 0) {
			log.aprintln("Attempting to scale a GLRenderable by dividing by zero.");
			return;
		}
		float scaleFactor = 1f / scalar;
		for (int i=0; i<polyPoints.length; i++) {
			polyPoints[i] *= scaleFactor;
		}
		if (fittedPoints != null && fittedPoints.length > 0) {
			for (int i=0; i<fittedPoints.length; i++) {
				fittedPoints[i] *= scaleFactor;
			}
		}
		
	}

	public float[] getOrigPoints() {
		return polyPoints;
	}
}
