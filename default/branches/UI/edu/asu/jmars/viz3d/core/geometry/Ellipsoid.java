/**
 * 
 */
package edu.asu.jmars.viz3d.core.geometry;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import edu.asu.jmars.util.DebugLog;
import edu.asu.jmars.viz3d.Disposable;
import edu.asu.jmars.viz3d.renderer.gl.GLRenderable;
import edu.asu.jmars.viz3d.renderer.textures.Texture2D;

/**
 * This class represents a tri-axial ellipsoid.
 *
 * Intended for representation of bodies for which accurate shape models do not exist
 *
 * not thread safe
 */
public class Ellipsoid implements GLRenderable, Disposable {
	
	private float mean;
	private float A;
	private float B;
	private float C;
	private float xScale;
	private float yScale;
	private float zScale;
	private float[] color;
	private int[] textureID;
	private float maxLen = Float.MAX_VALUE;
	
	private Texture2D tex;
	private BufferedImage image;
	
    private GLUquadric quadric = null;
	private GLU glu = new GLU();
	private boolean preRendered = false;
	private boolean isScalable = false;

    private static DebugLog log = DebugLog.instance();
	
    private float[] lightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] lightAmbient =	 { 0.4f, 0.4f, 0.4f, 1.0f };
	private float[] lightEmission =	 { 0f, 0f, 0f, 1.0f };
	
    private float alpha = 1f;
    private Float displayAlpha;
    private float scaleFactor = 1f;
    
    private float[] matrix;

	/**
     * Default Ellipsoid constructor without a texture (decal) applied
     * @param axisA axis at the equator on the prime meridian
     * @param axisB axis at the equator perpendicular to the prime meridian
     * @param axisC polar axis
     * @param color ellipsoid color; if the array length is &gt 3, index 3 is assumed to be the alpha value
     * @param matrix rotation + translation matrix (4x4 matrix in column major-order)
     * @throws IllegalArgumentException
     */
	public Ellipsoid (float axisA, float axisB, float axisC, float[] color, float[] matrix) throws IllegalArgumentException {
		mean = findSmallestAxis(axisA, axisB, axisC);
		maxLen = findLargestAxis(axisA, axisB, axisC);
		A = axisA;
		B = axisB;
		C = axisC;
		xScale = A / mean;
		yScale = B / mean;
		zScale = C / mean;
		this.color = color;
		this.matrix = matrix;
		if (color.length > 3){
			this.alpha = color[3];
		}
	}

	/**
     * Default Ellipsoid constructor without a texture (decal) applied
     * @param axisA axis at the equator on the prime meridian
     * @param axisB axis at the equator perpendicular to the prime meridian
     * @param axisC polar axis
     * @param color ellipsoid color 
     * @throws IllegalArgumentException
     */
	public Ellipsoid (float axisA, float axisB, float axisC, float[] color) throws IllegalArgumentException {
		mean = findSmallestAxis(axisA, axisB, axisC);
		maxLen = findLargestAxis(axisA, axisB, axisC);
		A = axisA;
		B = axisB;
		C = axisC;
		xScale = A / mean;
		yScale = B / mean;
		zScale = C / mean;
		this.color = color;
	}

	/**
	 * Textured Ellipsoid contructor
     * @param axisA axis at the equator on the prime meridian
     * @param axisB axis at the equator perpendicular to the prime meridian
     * @param axisC polar axis
	 * @param img BufferedImage of the image to be applied as a decal...north pole should be top of image
	 * @throws IllegalArgumentException
	 */
	public Ellipsoid (float axisA, float axisB, float axisC, BufferedImage img) throws IllegalArgumentException{
		mean = findSmallestAxis(axisA, axisB, axisC);
		maxLen = findLargestAxis(axisA, axisB, axisC);
		A = axisA;
		B = axisB;
		C = axisC;
		xScale = A / mean;
		yScale = B / mean;
		zScale = C / mean;
		image = img;
	}

	private float findSmallestAxis(float a, float b, float c) {
		float small = Float.MAX_VALUE;
		
		if (a <= small) {
			small = a;
		}
		if (b <= small) {
			small = b;
		}
		if (c <= small) {
			small = c;
		}
		
		return small;
	}

	private float findLargestAxis(float a, float b, float c) {
		float large = Float.MIN_VALUE;
		
		if (a >= large) {
			large = a;
		}
		if (b >= large) {
			large = b;
		}
		if (c >= large) {
			large = c;
		}
		
		return large;
	}
	
	/**
	 *  Returns the length of the longest vertex used to construct this Ellipsoid
	 *
	 * @return float
	 *
	 * thread-safe
	 */
	public float getMaxLen() {
		return maxLen;
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.Disposable#dispose()
	 */
	@Override
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.Renderable#execute(com.jogamp.opengl.GL2)
	 */
	@Override
	public void execute(GL2 gl) {
		if (tex == null && color != null) {
			if (matrix != null){
				gl.glColor4f(color[0], color[1], color[2], getDisplayAlpha());
		       	gl.glPushMatrix();			
		       	gl.glMultMatrixf(matrix, 0);
				gl.glScalef(xScale, yScale, zScale);
				glu.gluSphere(quadric, mean, 192, 192);
				gl.glPopMatrix();
			}
			else {
				gl.glColor4f(color[0], color[1], color[2], getDisplayAlpha());
				gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, lightAmbient, 0);
				gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, lightDiffuse, 0);
				glu.gluSphere(quadric, mean, 192, 192);
			}
		} else if (tex != null) {
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, lightAmbient, 0);
			gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, lightDiffuse, 0);
//			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_EMISSION, lightEmission, 0);
			tex.getTexture().enable(gl);
			tex.getTexture().bind(gl);
	       	gl.glPushMatrix();			
			gl.glScalef(xScale, yScale, zScale);
			glu.gluSphere(quadric, mean, 192, 192);
			gl.glPopMatrix();
			tex.getTexture().disable(gl);
		}
		
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.Renderable#preRender(com.jogamp.opengl.GL2)
	 */
	@Override
	public void preRender(GL2 gl) {
		if (preRendered) {
			return;
		}
		if (quadric == null) {
			// Create A Pointer To The Cone Quadric Object
	        quadric = glu.gluNewQuadric();
	        if (quadric == null) {
	    		log.aprint("Could not create Ellipsoid - out of memory on graphics card!");
	    		return;
	        }
	        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
	        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);  // Create Smooth Normals
	        glu.gluQuadricTexture(quadric, true);            // Create Texture Coords	
		}
		
		if (tex == null && image != null) {
			try {
				tex = new Texture2D(gl, image);
			} catch (IOException e) {
				log.println(e.getMessage());
			}
		}
		preRendered = true;
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#postRender(com.jogamp.opengl.GL2)
	 */
	@Override
	public void postRender(GL2 gl) {
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#delete(com.jogamp.opengl.GL2)
	 */
	@Override
	public void delete(GL2 gl) {
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#getId()
	 */
	@Override
	public int getId() {
		return hashCode();
	}
	
	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#getAlpha()
	 */
	@Override
	public float getAlpha() {
		return alpha;
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#getDisplayAlpha()
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
	
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	/* (non-Javadoc)
	 * @see edu.asu.jmars.viz3d.renderer.gl.GLRenderable#isScalable()
	 */
	@Override
	public boolean isScalable() {
		return isScalable;
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
		scaleFactor = 1f / scalar;
		mean *= scaleFactor;
		maxLen *= scaleFactor;
		A *= scaleFactor;
		B *= scaleFactor;
		C  *= scaleFactor;
		xScale = A / mean;
		yScale = B / mean;
		zScale = C / mean;
		if (matrix != null) {
			float[] scaledMatrix = new float[matrix.length]; 
			for (int i=0; i<matrix.length; i++) {
				scaledMatrix[i] = matrix[i];
			}
			scaledMatrix[12] *= scaleFactor;
			scaledMatrix[13] *= scaleFactor;
			scaledMatrix[14] *= scaleFactor;
			matrix = scaledMatrix;
		}

	}

	@Override
	public void scaleToShapeModel(boolean canScale) {
		isScalable = canScale;	
	}

}
