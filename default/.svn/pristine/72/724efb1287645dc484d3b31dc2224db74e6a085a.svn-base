package edu.asu.jmars.viz3d.renderer.gl.scene;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.vecmath.Matrix3d;

import com.jogamp.common.ExceptionUtils;
import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.JoglVersion;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;
import edu.asu.jmars.ui.looknfeel.ThemeFont;
import edu.asu.jmars.util.DebugLog;
import edu.asu.jmars.util.HVector;
import edu.asu.jmars.util.Util;
import edu.asu.jmars.viz3d.ThreeDManager;
import edu.asu.jmars.viz3d.core.geometry.ONode;
import edu.asu.jmars.viz3d.core.geometry.OctTree;
import edu.asu.jmars.viz3d.core.geometry.Polygon;
import edu.asu.jmars.viz3d.core.geometry.Ray;
import edu.asu.jmars.viz3d.core.geometry.Triangle;
import edu.asu.jmars.viz3d.core.geometry.TriangleMesh;
import edu.asu.jmars.viz3d.renderer.gl.ClearBufferMask;
import edu.asu.jmars.viz3d.renderer.gl.GLRenderable;
import edu.asu.jmars.viz3d.renderer.gl.GLRenderableSet;
import edu.asu.jmars.viz3d.renderer.gl.OutLineType;
import edu.asu.jmars.viz3d.renderer.gl.PolygonType;
import edu.asu.jmars.viz3d.renderer.gl.event.FittingListener;
import edu.asu.jmars.viz3d.renderer.gl.event.IntersectListener;
import edu.asu.jmars.viz3d.renderer.gl.event.IntersectResult;
import edu.asu.jmars.viz3d.renderer.gl.event.SynchronizeListener;
import edu.asu.jmars.viz3d.renderer.gl.event.SynchronizeResult;
import edu.asu.jmars.viz3d.renderer.gl.outlines.OutLine;
import edu.asu.jmars.viz3d.renderer.gl.queues.DeleteQueue;
import edu.asu.jmars.viz3d.renderer.gl.queues.RenderQueue;
import edu.asu.jmars.viz3d.renderer.gl.text.BasicText;
import edu.asu.jmars.viz3d.renderer.gl.text.LabelText;
import edu.asu.jmars.viz3d.scene.terrain.Star3D;


/**
 * This class implements a basic 3D scene renderer using JOGL/OpenGL 2.1
 * 
 * This class should never be called directly except by the ThreeDManager class
 * or the system.
 * 
 * Java Swing and JOGL version 2.3.1
 * 
 * not thread safe
 */
public class Scene implements GLEventListener {

	private int prevMouseX;
	private int prevMouseY;
	private float transX;
	private float transY;
	private float transZ;
	private float zoomFactor = 0.88f;
	
	private boolean mouseMoving = false;
	private boolean resetView = true;
	private boolean resetAxis = true;
	private float rotationAngle = 0f;
	private float[] lastDragPos = new float[3];
	private float[] axis = new float[3];
	private Quaternion rotQ = new Quaternion();
	private float[] axisModelView = new float[]{1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f};
	private float[] axisLoc = new float[3];
	
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	
	private RenderQueue renderFifo = RenderQueue.getInstance();
	private DeleteQueue deleteFifo = DeleteQueue.getInstance();
	private ArrayList<GLRenderable> renderables = new ArrayList<>();
	private ArrayList<Star3D> stars = new ArrayList<>();
	private ArrayList<LabelText> labels = new ArrayList<>();

	private boolean clear = false;
	private boolean reset = false;
	private boolean light = true;
	private boolean enableTooltips = false;

	private GLU glu = new GLU();
	private float maxMeshLength = 3f;
	private double fovy = 45.0;
	
	private int STAR_X_POS = 30;
	private int STAR_Y_POS = 60;
	private int BUSY_X_POS = 60;
	private int BUSY_Y_POS = 90;
	
	private BasicText stellarText = new BasicText(" ", new float[]{1.0f, 0.5f, 0.3f, 0.5f}, STAR_X_POS, STAR_Y_POS);
	private BasicText busyText = new BasicText("Processing 3D Updates...", new float[]{1.0f, 0.0f, 0.0f, 1.f}, BUSY_X_POS, BUSY_Y_POS);
	
//	private LabelText theSign = new LabelText("The Label", new float[]{1.0f, 0.5f, 0.3f, 0.5f}, 45f, 45f);

	private ArrayList<TriangleMesh> meshes = new ArrayList<>();
	private ThreeDManager mgr = null;
	private boolean drawAxis = true;
	private boolean pole = false;
	private boolean inspect = false;
	private boolean synch = false;		// if true, need to send synchronization data to the Main View
	private boolean synchTo = false;	// if true, need to synch 3D to the Main View
	private boolean VBO = true;
	private boolean fittingEnabled;
	private boolean debugIntersect;
	private boolean displayBusyText;
	
	private float[] synchVec;
	
	private Object lock = new Object();
	private Star3D pickedStar;

	private int projMouseX, projMouseY;

	private GLJPanel parent;

	private float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] lightPosition = { 100.0f, 100.0f, 100.0f, 0.0f };
	private float[] lightAmbient =	 { 0.1f, 0.1f, 0.1f, 1.0f };

	private float[] pickColor = { 1f, 0f, 0f };
	private boolean highlightFacet = false;
	
	private boolean printScreen = false;
	private String filePath = "";
	
    private GLUquadric quadric = null;

	private static DebugLog log = DebugLog.instance();	

	private List<IntersectListener> intersectListeners;
	private List<SynchronizeListener> synchListeners;
	private List<FittingListener> fittingListeners;

	
	//these are used when passing on interset results to listeners
	private boolean mouseClicked = false;
	private boolean controlDown = false;
	
	private int ppd = 2;
	/** Six sets of four numbers (six planes, each with an A, B, C, and D value).
	 * Equation of a plane: ax + by + cz + d = 0.
	 */
	private float[][] frustum = new float[6][4];
	float[] projection = null;
	float[] modelView = null;
	boolean drawFrustum = false;
	
	float[] cameraEye;
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 *            the GLJPanel this Scene has been added to
	 * @param maxLen
	 *            the max vector length in the shape model
	 */
	public Scene(GLJPanel parent, float maxLen) {
		this.parent = parent;
		maxMeshLength = maxLen;
		renderFifo.addActionListener(qListener);
		deleteFifo.addActionListener(qListener);
		// set the rotation quaternion to the identity quaternion
		rotQ = rotQ.setIdentity();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jogamp.opengl.GLEventListener#display(com.jogamp.opengl.GLAutoDrawable
	 * )
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		// toss everything but the mesh out!
		if (clear) {
			clear(gl);
		}
		// toss everything including the mesh out!
		if (reset) {
			reset(gl);
		}

		// delete any requested objects
		while (!deleteFifo.isEmpty()) {
			Integer id = deleteFifo.poll();
			synchronized (lock) {
				Iterator<GLRenderable> iter = renderables.iterator();
				while (iter.hasNext()) {
					GLRenderable action = iter.next();
					if (action.getId() == id) {
						action.delete(gl);
						iter.remove();
					}
				}
				Iterator<Star3D> starIter = stars.iterator();
				while (starIter.hasNext()) {
					Star3D action = starIter.next();
					if (action.getId() == id) {
						action.delete(gl);
						starIter.remove();
					}
				}
				Iterator<LabelText> labelIter = labels.iterator();
				while (labelIter.hasNext()) {
					LabelText action = labelIter.next();
					if (action.getId() == id) {
						action.delete(gl);
						labelIter.remove();
					}
				}
			}
		}
		// grab any new stuff to be rendered
		while (!renderFifo.isEmpty()) {
			GLRenderable action = renderFifo.poll();
			if (action == null) {
				continue;
			} else {
			// if we are not at Bennu, assume we are at a planet and using a unit sphere shape model
				if (action.isScalable()) {
					float maxScalar = mgr.getShapeModel().getMaxEllipsoidAxisLen();
					action.scaleByDivision(maxScalar);					
				}
				synchronized (lock) {
					if (action instanceof Star3D) {
						stars.add((Star3D)action);
					}
					renderables.add(action);
				}
			}
		}

		// clear screen
		gl.glClearColor(0f, 0f, 0f, 1f);
		gl.glClearDepth(1.0);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		// update the camera position
		update(drawable);
		// apply lighting if enabled
		this.applyLighting(gl);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glCullFace(GL2.GL_BACK);
		gl.glFrontFace(GL2.GL_CCW);
		// render the scene
		render(gl);
		// draw the axis indicator
		if (drawAxis) {
			// turn off lighting if it happens to be on as we don't want the 
			// axis indicator to be shaded 
			if (light) {
				gl.glDisable(GL2.GL_LIGHTING);
				gl.glDisable(GL2.GL_LIGHT0);
			}
			// make sure the model view is active
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			// save the current mv matrix on the stack
			gl.glPushMatrix();
			if (mouseMoving) {
				// clear all view transformations
				gl.glLoadIdentity();
				// apply the mouse drag as a rotation
				gl.glRotatef(rotationAngle, axis[X], axis[Y], axis[Z]);
				// apply it to our axis indicator mv matrix
				gl.glMultMatrixf(axisModelView, 0);
			} else if (resetAxis) {
				// we are resetting to start position so
				// invert the current matrix to remove any rotations
				float[] modelView = new float[16];
				gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
				float[] newModelView = new float[16];
				// calculate the inverse
				FloatUtil.invertMatrix(modelView, newModelView);
				// multiply current model view by its inverse to undo any rotations
				gl.glMultMatrixf(newModelView, 0);
				// rotate to the starting orientation
				gl.glRotatef(-90f, 1f, 0f, 0f);
				gl.glRotatef(-90f, 0f, 0f, 1f);
				resetAxis = false;
			}
			// calculate the location of the axis indicator origin
			axisLoc = getAxisIndicatorLocation(gl);
			// translate to the location
			gl.glTranslatef(axisLoc[X], axisLoc[Y], axisLoc[Z]);
			// save the current axis indicator mv matrix for use in
			// accumulating the next rotation/translation
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, axisModelView, 0);

			this.drawAxisIndicator(gl);
			gl.glColor3f(1, 1, 1);
			// restore everything for the main view
			gl.glPopMatrix();
			if (light) {
				gl.glEnable(GL2.GL_LIGHTING);
				gl.glEnable(GL2.GL_LIGHT0);
			}
		}

		if (printScreen) {
			final String captureFile = filePath;
     		// workaround for Mac OS X
			// see https://jogamp.org/bugzilla/show_bug.cgi?id=1044
            // get and write out same image but WITHOUT alpha channel
            BufferedImage image = new BufferedImage(parent.getSurfaceWidth(),parent.getSurfaceHeight(), BufferedImage.TYPE_3BYTE_BGR);
            DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
            Buffer b = ByteBuffer.wrap(buffer.getData());
            gl.glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
            gl.glReadPixels(0, 0, parent.getSurfaceWidth(), parent.getSurfaceHeight(), GL2.GL_BGR, GL2.GL_UNSIGNED_BYTE, b);
            AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
            tx.translate(0, -image.getHeight());
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);
            final BufferedImage thePic = image;
     		// save the screen as a PNG file
     		(new Runnable() {
     			public void run() {
    	    		Util.saveAsPng(thePic, captureFile);
     			}
     		}).run();
     		printScreen = false;
		}
		
		ppd = getPPD(gl);
		
//System.err.println("PPD = "+ppd);
	if (drawFrustum) {
		if (projection == null) {
			projection = new float[16];
			modelView = new float[16];
			extractFrustum(gl);
		}
		drawFrustum(gl, projection, modelView);
		// get a normal vector facing the viewer
		if (cameraEye == null) {
			cameraEye = unProjectedMouse(gl, parent.getSurfaceWidth()/2, parent.getSurfaceHeight()/2,
				0);
		
System.err.println("cameraEye raw "+cameraEye[X]+", "+cameraEye[Y]+", "+cameraEye[Z]);		
			cameraEye = VectorUtil.scaleVec3(new float[3], VectorUtil.normalizeVec3(cameraEye), 2f);
System.err.println("cameraEye scaled "+cameraEye[X]+", "+cameraEye[Y]+", "+cameraEye[Z]);	
		}
		
		gl.glColor3f(1f, 1f, 0f);
//		gl.glPointSize(2f);
//		gl.glBegin(GL2.GL_POINTS);
//		gl.glVertex3d(cameraEye[X], cameraEye[Y], cameraEye[Z]);
//		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINES);

		gl.glVertex3d(0f, 0f, 0f);
		gl.glVertex3d(cameraEye[X], cameraEye[Y], cameraEye[Z]);
		gl.glEnd();
		
		OctTree oct = ThreeDManager.getInstance().getShapeModel().getOctTree();
		ArrayList<Triangle> tris = new ArrayList<>();
		oct.looseOctTreeFrustumIntersect(cameraEye, frustum, oct.getRoot(), tris);
		for (Triangle t : tris) {
			this.drawTriangle(gl, t.points, 2, new float[] {1f, 0f, 0f});
		}
		gl.glColor3f(1f, 1f, 1f);

	}
		
		
		
		
		// log any OpenGL error that may have occurred during the rendering pass
		int errCode = GL2.GL_NO_ERROR;
		if ((errCode = gl.glGetError()) != GL2.GL_NO_ERROR) {
			String errString = glu.gluErrorString(errCode);
			log.aprintln("OpenGL Error: " + errString);
		}
	}
	
	private float[] getAxisIndicatorLocation(GL2 gl) {
		int width = this.parent.getSurfaceWidth();
		int height = this.parent.getSurfaceHeight();
				
		int xPos = width / 10;
		int yPos = height - height / 10;
		
		float[] nearPt = unProjectedMouse(gl, xPos, yPos, 0.05f);
		
		return nearPt;
	}

	private void applyLighting(GL2 gl) {

		if (light) {
			gl.glEnable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_LIGHT0);
		} else {
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glDisable(GL2.GL_LIGHT0);
		}

		// Set light parameters.
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jogamp.opengl.GLEventListener#dispose(com.jogamp.opengl.GLAutoDrawable
	 * )
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();

		synchronized (lock) {
			for (GLRenderable action : renderables) {
				action.delete(gl);
			}
		}

		if (meshes.size() > 0) {
			for (TriangleMesh mesh : meshes) {
				mesh.delete(gl);
			}
			meshes.clear();
			;
		}

		renderFifo = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jogamp.opengl.GLEventListener#init(com.jogamp.opengl.GLAutoDrawable)
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		mgr = ThreeDManager.getInstance();
		log.aprint(" Scene.init: " + Thread.currentThread());
		log.aprintln("width x height: "+drawable.getSurfaceWidth()+" x "+drawable.getSurfaceHeight());
		log.aprint("Chosen GLCapabilities: "
				+ drawable.getChosenGLCapabilities());
		GL2 gl = drawable.getGL().getGL2();
		log.aprint("INIT GL IS: " + gl.getClass().getName());

		log.aprint(JoglVersion.getGLStrings(gl, null, false).toString() + "\n");

		// Check if extensions are available.
		boolean extValid = gl
				.isExtensionAvailable("GL_ARB_vertex_buffer_object");
		log.println("VBO extension: " + extValid);
		boolean texNPOT = gl
				.isExtensionAvailable("GL_ARB_texture_non_power_of_two");
		log.println("Texture NPOT extension: " + texNPOT);

		// Check for VBO functions.
		boolean funcsValid = gl.isFunctionAvailable("glGenBuffers")
				&& gl.isFunctionAvailable("glBindBuffer")
				&& gl.isFunctionAvailable("glBufferData")
				&& gl.isFunctionAvailable("glDeleteBuffers");
		log.println("Needed JOGL Functions Available: " + funcsValid);

		if (!extValid || !funcsValid) {
			// VBOs are not supported.
			log.println("VBOs are not supported.");
			VBO = false;
		}

		// Enable z- (depth) buffer for hidden surface removal.
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthMask(true);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glDepthRange(0.0f, 1.0f);

		// Enable smooth shading.
		gl.glShadeModel(GL2.GL_SMOOTH);

		// We want a nice perspective...maybe???
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

		gl.glClear(ClearBufferMask.ColorBufferBit);

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);

		// Create A Pointer To The Pole Indicator Quadric Object 
        quadric = glu.gluNewQuadric();
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);  // Create Smooth Normals
        glu.gluQuadricTexture(quadric, true);            // Create Texture Coords
        
		MouseListener simpleMouse = new SimpleMouseAdapter();
		KeyListener simpleKeys = new SimpleKeyAdapter();

		if (drawable instanceof Window) {
			Window window = (Window) drawable;
			window.addMouseListener(simpleMouse);
			window.addKeyListener(simpleKeys);
		} else if (GLProfile.isAWTAvailable()
				&& drawable instanceof java.awt.Component) {
			java.awt.Component comp = (java.awt.Component) drawable;
			new AWTMouseAdapter(simpleMouse, drawable).addTo(comp);
			new AWTKeyAdapter(simpleKeys, drawable).addTo(comp);
		}
		busyText.setFont(ThemeFont.getBold().deriveFont(22f));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mgr.update(false);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jogamp.opengl.GLEventListener#reshape(com.jogamp.opengl.GLAutoDrawable
	 * )
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
	}

	private void update(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		// set camera and move scene
		setCamera(gl, glu, parent.getSurfaceWidth(), parent.getSurfaceHeight(),
				maxMeshLength * 3f, transX, transY, transZ);
		
		if (mouseMoving) {
			// save the current mv matrix
			float[] modelView = new float[16];
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
			gl.glLoadIdentity();
			
			gl.glRotatef(rotationAngle, axis[X], axis[Y], axis[Z]);
			gl.glMultMatrixf(modelView, 0);
		} else if (resetView) {
			// save the current mv matrix
			float[] modelView = new float[16];
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
			float[] newModelView = new float[16];
			// calculate the inverse
			FloatUtil.invertMatrix(modelView, newModelView);
			// multiply current model view by its inverse to undo any rotations
			gl.glMultMatrixf(newModelView, 0);
			gl.glRotatef(-90f, 1f, 0f, 0f); // point the North Pole (Z axis) up
			gl.glRotatef(-90f, 0f, 0f, 1f); // point the X axis at the user
			resetView = false;
		}
		// we have a request to synch the 3D shape model to the same lon/lat center as the Main View
		// we also want to do this after any other rotations that may be scheduled...probably
		if (synchTo && mgr.getShapeModel() != null && synchVec != null) {
			// save the current mv matrix
			float[] modelView = new float[16];
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
			float[] newModelView = new float[16];
			// calculate the inverse
			FloatUtil.invertMatrix(modelView, newModelView);
			// multiply current model view by its inverse to undo any rotations
			gl.glMultMatrixf(newModelView, 0);
			gl.glRotatef(-90f, 1f, 0f, 0f); // point the North Pole (Z axis) up
			gl.glRotatef(-90f, 0f, 0f, 1f); // point the X axis at the user

			// Get the Camera Eye location  ?should this calculation just be static?
			int height = parent.getSurfaceHeight();
			int width = parent.getSurfaceWidth();
			float[] eyeLoc = VectorUtil.normalizeVec3(new float[3], unProjectedMouseNoTranslate(gl, width/2, height/2f, 0f));
			float[] synchNorm = VectorUtil.normalizeVec3(new float[3], synchVec);
			float[] axis = VectorUtil.normalizeVec3(new float[3], VectorUtil.crossVec3(new float[3], synchNorm, eyeLoc));
			float ang = VectorUtil.angleVec3(synchNorm, eyeLoc) * 180f / FloatUtil.PI;
			// save the current mv matrix
			modelView = new float[16];
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
			gl.glLoadIdentity();
			
			gl.glRotatef(ang, axis[Y], axis[Z], axis[X]);
			gl.glMultMatrixf(modelView, 0);
			synchTo = false;
			synchVec = null;
		}

	}

	private void render(GL2 gl) {
		gl.glClear(ClearBufferMask.ColorBufferBit);
		if (mgr.getShapeModel() != null) {
			this.maxMeshLength = mgr.getShapeModel().getMaxEllipsoidAxisLen();
			mgr.getShapeModel().preRender(gl);
			mgr.getShapeModel().execute(gl);
			logAnyErrorCodes(mgr.getShapeModel(), gl,
					"Scene.Render() Shape Model");
		}

		synchronized (lock) {
			
			ArrayList<GLRenderableSet> renderableSets = null;
			renderableSets = ThreeDManager.getInstance().getCurrentRenderableSets();
			for (GLRenderableSet set : renderableSets) {
				for (int i=0; i<set.getRenderables().size(); i++) {
					GLRenderable action = set.getRenderables().get(i);

					if (action == null) {
						continue;
					}
					if (action instanceof Polygon) {
						Polygon poly = (Polygon) action;
						if (fittingEnabled && poly.isFitted()) {
							poly.setFittingEnabled(true);
						} else if (fittingEnabled && !poly.isFitted()
								&& mgr.getShapeModel() != null
								&& poly.getPolygonType() == PolygonType.OnBody
								&& ThreeDManager.CLOSE_FITTING) {
							// we need to fit the polygon to the body
							poly.setFittingEnabled(true);
							mgr.getShapeModel().fitToMesh(poly);
						} else {
							poly.setFittingEnabled(false);
						}
					} else if (action instanceof OutLine) {
						OutLine line = (OutLine) action;
						if (fittingEnabled && !line.isFitted() && mgr.getShapeModel() != null
								&& line.getOutLineType() == OutLineType.OnBody
								&& ThreeDManager.CLOSE_FITTING) {
							// we need to fit the line to the body
							line.setFittingEnabled(true);
							mgr.getShapeModel().fitToMesh(line);
						} else if (fittingEnabled && line.isFitted()) {
							line.setFittingEnabled(true);
						} else {
							line.setFittingEnabled(false);
						}
					} else if (action instanceof Star3D) {
						stars.add((Star3D)action);
					} else if (action instanceof LabelText) {
						labels.add((LabelText)action);
					}

					if (light) {
						gl.glDisable(GL2.GL_LIGHTING);
						gl.glDisable(GL2.GL_LIGHT0);
					}	
					action.preRender(gl);
					action.execute(gl);
					if (light) {
						gl.glEnable(GL2.GL_LIGHTING);
						gl.glEnable(GL2.GL_LIGHT0);
					}

					logAnyErrorCodes(action, gl, "Scene.Render() Renderables");
				}
			}
		}
		//notify the listeners if this came from a click
		if(mouseClicked){
			ArrayList<Triangle> selectedTri = new ArrayList<>();
			Ray ray = getPickingRay(gl);
			if (mgr.getShapeModel().rayIntersect(ray, selectedTri)) {
				for (Triangle t : selectedTri) {
					IntersectResult result = getIntersectResultFromTriangle(t);
					result.setClicked(true);
					if(controlDown){
						result.setControlDown(true);
					}

					// trigger the action listener
					notifyInvestigators(result);
				}
			}
			mouseClicked = false;
			controlDown = false;
		}

		if (inspect && mgr.getShapeModel() != null) {
			ArrayList<Triangle> selectedTri = new ArrayList<>();
			Ray ray = getPickingRay(gl);
			if (mgr.getShapeModel().rayIntersect(ray, selectedTri)) {
				for (Triangle t : selectedTri) {
					// just a second check to make sure we have an accurate
					// triangle pick
					// convert the pick to Lon/Lat and pass that and the
					// triangle ID to the action listener
					//if facet highlighting is turned on, draw the triangle
					if(highlightFacet){
						this.drawTriangle(gl, t.points, 2, pickColor);
					}

					IntersectResult result = getIntersectResultFromTriangle(t);;
					// trigger the action listener
					notifyInvestigators(result);
				}
			}
		} else if (inspect && mgr.getShapeModel() == null) {
			log.aprintln("Cannot implement investigate functionality. There is no available shape model.");
		}
		
		if (synch && mgr.getShapeModel() != null) {
			synchViews(gl);
			synch = false;
		}
				
		if (debugIntersect && mgr.getShapeModel() != null) {
			ArrayList<Triangle> selectedTri = new ArrayList<>();
			Ray ray = getPickingRay(gl);
			if (mgr.getShapeModel().rayIntersect(ray, selectedTri)) {
				for (Triangle t : selectedTri) {
					// just a second check to make sure we have an accurate
					// triangle pick
					// convert the pick to Lon/Lat and pass that and the
					// triangle ID to the action listener
					this.drawTriangle(gl, t.points, 2, pickColor);
					System.err.println("intersected " + t.id);
					for (int i = 0; i < t.points.length; i++) {
						System.err.println("" + t.points[i][0] + ", "
								+ t.points[i][1] + ", " + t.points[i][2]);
					}
				}
			}
		}
		// TODO from here to the start of error checking needs to optimized and consolidated
		if (enableTooltips && stars.size() > 0) {
			float[][] line = getPickingLineSegment(gl);
			// check for intersection with any stars
			Star3D tmpStar = null;
			float tmpDist = Float.MAX_VALUE; // distance from the star to the picking ray
			float tmpScrDist = Float.MAX_VALUE; // distance from the star to the screen
			for (Star3D star : stars) {
				float dist = OctTree.closestDistancePointToLine(line[0], line[1], star.getLocation());
				float minThreshold = (float)((0.25*1.5/100.0)/zoomFactor); // .25 should be size at scale=1
				if (dist <= minThreshold) {  
					// we are close enough to the star to select it
					float scrDist = VectorUtil.normSquareVec3(VectorUtil.subVec3(new float[3], star.getLocation(), line[0]));
					// if the star is closer to the screen OR its closer to the picking ray its now the winner
					if (dist < tmpDist) {
						tmpDist = dist;
						tmpStar = star;
						tmpScrDist = scrDist;
					}
				}
			}
			if (tmpStar != null) {
				pickedStar = tmpStar;
			} else {
				pickedStar = null;
			}
		}
		
		if (pickedStar != null) {
		    gl.glColor4f(pickColor[0], pickColor[1], pickColor[2], 1f);
		    gl.glPointSize(pickedStar.getStarSize() * 1.01f);
			gl.glEnable(GL2.GL_POINT_SMOOTH);
		    gl.glBegin(GL2.GL_POINTS);
		    	gl.glVertex3f(pickedStar.getLocation()[X], pickedStar.getLocation()[Y], pickedStar.getLocation()[Z]);
		    gl.glEnd();
		    
		    stellarText.setText(pickedStar.getTooltip());
		    stellarText.preRender(gl);
		    stellarText.execute(gl);
		}
		
		stars.clear();
		
		if (displayBusyText) {
			busyText.preRender(gl);
			busyText.execute(gl);
		}
//		labels.add(theSign);
		if (labels.size() > 0) {
			renderLabels(gl);
		}
		labels.clear();
		
		if (pole) {
			drawPoleIndicator(gl);
		}
		
		int errCode = GL2.GL_NO_ERROR;
		if ((errCode = gl.glGetError()) != GL2.GL_NO_ERROR) {
			String errString = glu.gluErrorString(errCode);
			log.aprintln("OpenGL Error: " + errString);
		}
	}
	
	private IntersectResult getIntersectResultFromTriangle(Triangle t){
		float[] intersection = t.getIntersection();
		HVector temp = new HVector();
		temp = temp.set(intersection[0], intersection[1],
				intersection[2]);
		double latitude = temp.lat();
		//internally jmars works in degrees West
		double longitude = temp.lonW();
		IntersectResult result = new IntersectResult();
		result.setFacetId(t.id);
		result.setLatitude((float) latitude);
		result.setLongitude((float) longitude);
		
		return result;
	}
	
	private void renderLabels(GL2 gl) {
		// do we have a valid Shape Model to map to?
		if (mgr.getShapeModel() == null) {
			log.aprintln("Unable to render 3D textual labels! No avialable shape model!");
			return;
		}
		float scalar = 2f;
		if (labels.size() > 0 && mgr.getShapeModel().getMaxMeshLen() >= 1f) {
			scalar = mgr.getShapeModel().getMaxMeshLen() * 2f;			
		}
		
		for (int i=0; i<labels.size(); i++) {
			LabelText label = labels.get(i);
			// convert lat lon to a 3D space location on the shape model
			HVector hv = new HVector(label.getLon(), label.getLat());
			ArrayList<Triangle> tri = new ArrayList<>();
			mgr.getShapeModel().getOctTree().looseOctTreeRayIntersectShortCircuit(new Ray(new float[] {(float)hv.x*scalar, (float)hv.y*scalar, (float)hv.z*scalar}, new float[] {(float)-hv.x*scalar, (float)-hv.y*scalar, (float)-hv.z*scalar}), mgr.getShapeModel().getOctTree().getRoot(), tri);
			if (tri.size() < 1) {
				log.aprintln("Unable to render 3D textual label at Lon: "+label.getLon()+" Lat: "+label.getLat()+" No shape model intersection!");
				continue;
			}
			float[] surfaceLoc = tri.get(0).getIntersection();
			// verify the 3D location is on the side of the body facing the viewer
			float[] cameraEye = unProjectedMouse(gl, parent.getSurfaceWidth()/2, parent.getSurfaceHeight()/2,
					0);
			if (cameraEye == null) {
				log.aprintln("Unable to render 3D textual label. Unable to determine camera angle!");
				continue;
			}
			float tmpDot = VectorUtil.dotVec3(VectorUtil.normalizeVec3(new float[3], surfaceLoc), VectorUtil.normalizeVec3(cameraEye));
			if (tmpDot < 0) {
				// location is away from viewer on backside - ignore it
//				label.postRender(gl);
				continue;
			} else if (FloatUtil.isEqual(VectorUtil.dotVec3(surfaceLoc, cameraEye), VectorUtil.normVec3(cameraEye), FloatUtil.EPSILON)) {
				// surface is behind the near clipping plane
//				label.postRender(gl);
				continue;
			}
			// project the 3D location to a screen location
			float[] screenLoc = project(gl, surfaceLoc);
			// if the screen location is null, or negative in either coordinate or greater than the screen size in either coordinate...don't render
			if (screenLoc == null || screenLoc[X] < 0 || screenLoc[X] > (parent.getSurfaceWidth() - 1) || screenLoc[Y] < 0 || screenLoc[Y] > (parent.getSurfaceHeight() - 1)) {
//				label.postRender(gl);
				continue;
			}
			
			// render
			label.setXPos((int)screenLoc[X]);
			label.setYPos((int)screenLoc[Y]);
			
//			label.set3dXPos(surfaceLoc[X]);
//			label.set3dYPos(surfaceLoc[Y]);
//			label.set3dZPos(surfaceLoc[Z]);
//			label.setRender3d(true);

			
			label.preRender(gl);
			label.execute(gl);
		}
		//		label.dispose();
		// we need one more refresh to remove stale labels
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				ThreeDManager.getInstance().refresh();
//			}
//		});		
	}

	private void setCamera(GL2 gl, GLU glu, int width, int height,
			float distance, float lookX, float lookY, float lookZ) {
		// Change to projection matrix.
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		// Perspective.
		float widthHeightRatio = (float) width / (float) height;
		glu.gluPerspective(fovy, widthHeightRatio, 0.05 /* distance * 3f */, /*
																		 * Integer.
																		 * MAX_VALUE
																		 */
				distance * 30f);
		glu.gluLookAt(lookX, lookY, distance, lookX, lookY, lookZ, 0, 1, 0);

		gl.glScalef(zoomFactor, zoomFactor, zoomFactor);
		
		// Change back to model view matrix.
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}
	
	private Ray getPickingRay(GL2 gl) {
		float[] selectedPoint = unProjectedMouse(gl, projMouseX, projMouseY,
				0);
		float[] selectedFarPoint = unProjectedMouse(gl, projMouseX,
				projMouseY, 1);
		return new Ray(selectedPoint, VectorUtil.subVec3(new float[3],
				selectedFarPoint, selectedPoint));
	}

	private float[][] getPickingLineSegment(GL2 gl) {
		float[] selectedPoint = unProjectedMouse(gl, projMouseX, projMouseY,
				0);
		float[] selectedFarPoint = unProjectedMouse(gl, projMouseX,
				projMouseY, 1);
		return new float[][]{selectedPoint, selectedFarPoint};
	}

	// next two methods need to be combined using a dynamic scaler value...
	private void drawAxisIndicator(GL2 gl) {
		
		float scale = (1f / zoomFactor) / 10f;
		this.drawLine(gl, 0f * scale, 0f * scale, 0f * scale, 0.02f * scale, 0f * scale, 0f * scale, 2, 1, 0, 0, 1); // x axis
		this.drawLine(gl, 0.02f * scale, 0f * scale, 0f * scale, 0.016f * scale, 0.004f * scale, 0f * scale, 2, 1, 0, 0, 1); // x
																				// axis
																				// arrowhead
		this.drawLine(gl, 0.02f * scale, 0f * scale, 0f * scale, 0.016f * scale, -0.004f * scale, 0f * scale, 2, 1, 0, 0, 1); // x
																				// axis
																				// arrowhead
		this.drawLine(gl, 0.024f * scale, 0.004f * scale, 0f * scale, 0.032f * scale, -0.004f * scale, 0f * scale, 2, 1, 0, 0,
				1); // x axis label
		this.drawLine(gl, 0.032f * scale, 0.004f * scale, 0f * scale, 0.024f * scale, -0.004f * scale, 0f * scale, 2, 1, 0, 0,
				1); // x axis label

		this.drawLine(gl, 0f * scale, 0f * scale, 0f * scale, 0f * scale, 0.02f * scale, 0f * scale, 2, 0, 1, 0, 1); // y axis
		this.drawLine(gl, 0f * scale, 0.02f * scale, 0f * scale, -0.004f * scale, 0.016f * scale, 0f * scale, 2, 0, 1, 0, 1); // y
																				// axis
																				// arrowhead
		this.drawLine(gl, 0f * scale, 0.02f * scale, 0f * scale, 0.004f * scale, 0.016f * scale, 0f * scale, 2, 0, 1, 0, 1); // y
																				// axis
																				// arrowhead
		this.drawLine(gl, 0f * scale, 0.028f * scale, 0f * scale, -0.004f * scale, 0.032f * scale, 0f * scale, 2, 0, 1, 0, 1); // y
																				// axis
																				// label
		this.drawLine(gl, 0f * scale, 0.028f * scale, 0f * scale, 0.004f * scale, 0.032f * scale, 0f * scale, 2, 0, 1, 0, 1); // y
																				// axis
																				// label
		this.drawLine(gl, 0f * scale, 0.028f * scale, 0f * scale, 0f * scale, 0.024f * scale, 0f * scale, 2, 0, 1, 0, 1); // y
																			// axis
																			// label

		this.drawLine(gl, 0f * scale, 0f * scale, 0f * scale, 0f * scale, 0f * scale, 0.02f * scale, 2, 0, 0, 1, 1); // z axis
		this.drawLine(gl, 0f * scale, 0f * scale, 0.02f * scale, -0.004f * scale, 0f * scale, 0.016f * scale, 2, 0, 0, 1, 1); // z
																				// axis
																				// arrowhead
		this.drawLine(gl, 0f * scale, 0f * scale, 0.02f * scale, 0.004f * scale, 0f * scale, 0.016f * scale, 2, 0, 0, 1, 1); // z
																				// axis
																				// arrowhead
		this.drawLine(gl, -0.004f * scale, 0f * scale, 0.024f * scale, 0.004f * scale, 0f * scale, 0.024f * scale, 2, 0, 0, 1,
				1); // z axis label
		this.drawLine(gl, 0.004f * scale, 0f * scale, 0.024f * scale, -0.004f * scale, 0f * scale, 0.032f * scale, 2, 0, 0, 1,
				1); // z axis label
		this.drawLine(gl, -0.004f * scale, 0f * scale, 0.032f * scale, 0.004f * scale, 0f * scale, 0.032f * scale, 2, 0, 0, 1,
				1); // z axis label
	}

	private void drawPoleIndicator(GL2 gl) {
		TriangleMesh mesh = mgr.getShapeModel();
		if (mesh != null) {
			float meshLen = mesh.getMaxEllipsoidAxisLen();
			if (pole) {
				if (light) {
					gl.glDisable(GL2.GL_LIGHTING);
					gl.glDisable(GL2.GL_LIGHT0);
				}
				float[] prevColor = new float[4];
			    gl.glGetFloatv(GL2.GL_CURRENT_COLOR, prevColor, 0);
				float len = meshLen * 1.1f;
		        this.drawLine(gl, 0f, 0f, meshLen, 0f, 0f, len, 1, 1, 0, 0, 1); // z axis aka North
//		        this.drawLine(gl, 0f, 0f, -meshLen, 0f, 0f, -len, 1, 1, 0, 0, 1); // -z axis aka South
		        
//		        HVector zeroZero = HVector.fromSpatial(0.0, 0.0);
//		        float[] zeroS = VectorUtil.scaleVec3(new float[3], new float[]{(float)zeroZero.x, (float)zeroZero.y, (float)zeroZero.z}, new float[]{meshLen, meshLen, meshLen});
//		        float[] zeroE = VectorUtil.scaleVec3(new float[3], new float[]{(float)zeroZero.x, (float)zeroZero.y, (float)zeroZero.z}, new float[]{len, len, len});
//		        HVector forty5forty5 = HVector.fromSpatial(315.0, 45.0);
//		        float[] forty5S = VectorUtil.scaleVec3(new float[3], new float[]{(float)forty5forty5.x, (float)forty5forty5.y, (float)forty5forty5.z}, new float[]{meshLen, meshLen, meshLen});
//		        float[] forty5E = VectorUtil.scaleVec3(new float[3], new float[]{(float)forty5forty5.x, (float)forty5forty5.y, (float)forty5forty5.z}, new float[]{len, len, len});
//
//		        this.drawLine(gl, zeroS[X], zeroS[Y], zeroS[Z], zeroE[X], zeroE[Y], zeroE[Z], 1, 0, 1, 0, 1); // 0,0 aka prime meridian at equator
//		        this.drawLine(gl, forty5S[X], forty5S[Y], forty5S[Z], forty5E[X], forty5E[Y], forty5E[Z], 1, 1, 1, 0, 1); // 45, 45

		        
		        gl.glPushMatrix();
		        gl.glTranslatef(0.0f, 0.0f, len);   // position the cone
		        // draw the cone 
		        // (GLUquadric, base, top, height, #slices, #stacks)
		        gl.glColor4f(1f, 0f, 0f, 1f);
		        glu.gluCylinder(quadric, meshLen * 0.01f, 0.0f, meshLen * 0.04f, 32, 32);  
		        gl.glPopMatrix();
		        			    
			    gl.glColor4f(prevColor[0], prevColor[1], prevColor[2], prevColor[3]);
				if (light) {
					gl.glEnable(GL2.GL_LIGHTING);
					gl.glEnable(GL2.GL_LIGHT0);
				}
			}
		} else {
			log.aprintln("Cannot draw North Pole indicator with out a shape model.");
		}
	}
	
	private void drawLine(GL2 gl, float ax, float ay, float az, float bx, float by,
			float bz, int width, int r, int g, int b, int a) {
		gl.glColor3f(r, g, b);

		gl.glLineWidth(width);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(ax, ay, az);
		gl.glVertex3f(bx, by, bz);
		gl.glEnd();
	}

	/**
	 * Returns the current light position
	 *
	 * @return the current light position
	 *
	 * thread-safe
	 */
	public float[] getLightPosition() {
		return lightPosition;
	}

	/**
	 * Sets the light position
	 *
	 * @param lightPosition float[X, Y, Z]
	 *
	 * not thread-safe
	 */
	public void setLightPosition(float[] lightPosition) {
		this.lightPosition = lightPosition;
	}

	/**
	 * Returns the current on/off state of the light
	 *
	 * @return true if the light is on
	 *
	 * thread-safe
	 */
	public boolean isLightOn() {
		return light;
	}

	/**
	 * Sets the light on/off condition
	 *
	 * @param light light turned on if true
	 *
	 * thread-safe
	 */
	public void setLightOn(boolean light) {
		this.light = light;
	}

	/**
	 * Returns if the Scene is in Investigate mode allowing the user to 
	 * mouse over, highlight, and return facet number and Lat/Lon
	 *
	 * @return true if in Investigate mode
	 *
	 * thread-safe
	 */
	public boolean isInvestigating() {
		return inspect;
	}

	/**
	 * Sets the Investigate mode on/off condition
	 *
	 * @param doInvestigate enables Investigate mode if true
	 *
	 * thread-safe
	 */
	public void setInvestigating(boolean doInvestigate) {
		this.inspect = doInvestigate;
	}
	
	/**
	 * Sets the trigger for synchronizing the Main Window to this view
	 */
	public void sych2D() {
		synch = true;
	}
	
	/**
	 * Rotates the shape model so that the West-leading Lon/Lat position
	 * is pointed directly at the camera eye. Intended to synchronize the 
	 * viewing location in 3D to the the viewing location in the Main View.
	 *  
	 * @param lon West-leading Longitude
	 * @param lat Latitude
	 */
	public void synchTo2D(float lon, float lat) {
		HVector hv = new HVector(lon, lat);
		synchVec = new float[] {(float)hv.x, (float)hv.y, (float)hv.z};
		synchTo = true;
	}

	/**
	 * Sets the color used to highlight the selected facet while in Investigate mode
	 *
	 * @param color float[R, G, B]
	 *
	 * not thread-safe
	 */
	public void setPickColor(float[] color) {
		if (color != null && color.length > 2) {
			pickColor = color;
		} else {
			log.aprintln("Cannot set Investigate highlight color due to inavlid input color.");
		}
	}
	
	
	/**
	 * Turns on the ability to color the facet under the cursor
	 * (used in the investigate mode for some layers)
	 * @param highlight
	 */
	public void setHighlightFacet(boolean highlight){
		highlightFacet = highlight;
	}

	/**
	 * Returns the color used to highlight the selected facet while in Investigate mode
	 *
	 * @return float[R, G, B]
	 *
	 * thread-safe
	 */
	public float[] getPickColor() {
		return pickColor;
	}

	/** 
	 * Returns the length of the largest shape model vector
	 *
	 * @return float
	 *
	 * thread-safe
	 */
	public float getMaxMeshLength() {
		return maxMeshLength;
	}

	/**
	 * Sets the value used to set the Camera distance from the origin
	 * To be used in the event there is no shape model
	 * 
	 * @param maxMeshLength
	 *
	 * not thread-safe
	 */
	public void setMaxMeshLength(float maxMeshLength) {
		this.maxMeshLength = maxMeshLength;
	}

	/**
	 * Sets the height in pixels of this GLJPanel
	 *
	 * @param height
	 *
	 * not thread-safe
	 */
	public void setHeight(int height) {
		this.setHeight(height);
	}

	/**
	 * Sets the width in pixels of this GLJPanel
	 *
	 * @param width
	 *
	 * not thread-safe
	 */
	public void setWidth(int width) {
		this.setWidth(width);
	}

	/**
	 * Clears all the GLRenderables in the Scene EXCEPT for the shape model
	 *
	 * thread-safe
	 */
	public void clear() {
		synchronized (lock) {
			renderables.clear();
			stars.clear();
		}
		renderFifo.clear();
		clear = true;
		parent.repaint();
	}

	/**
	 * Clears all the GLRenderable in the Scene INCLUDING the shape model
	 *
	 * thread-safe
	 */
	public void reset() {
		synchronized (lock) {
			renderables.clear();
			stars.clear();
		}
		renderFifo.clear();
		reset = true;
		parent.repaint();
	}

	/**
	 * This method will clear all renderable objects EXCEPT the triangle mesh.
	 * This method should really only ever be called by the ThreeDManager
	 * 
	 * @param gl
	 *            - the current drawing context
	 * @throws not
	 *             thread safe
	 */
	private void clear(GL2 gl) {
		synchronized (lock) {
			renderables.clear();
			stars.clear();
		}
		clear = false;
		parent.repaint();
	}

	/**
	 * This method will clear all renderable objects INCLUDING the triangle
	 * mesh. This method should really only ever be called by the ThreeDManager
	 * 
	 * @param gl
	 *            - the current drawing context
	 * @throws not
	 *             thread safe
	 */
	private void reset(GL2 gl) {
		meshes.clear();
		synchronized (lock) {
			renderables.clear();
			stars.clear();
		}
		reset = false;
		parent.repaint();
	}

	public boolean areVBOsSupported() {
		return VBO;
	}

	/**
	 * Resets Camera position to its starting location when the Scene is initially rendered
	 *
	 * not thread-safe
	 */
	public void resetCamera() {
		transX = 0f;
		transY = 0f;
		transZ = 0f;
		zoomFactor = 0.88f;
		resetView = true;
		resetAxis = true;
	}
	
	/**
	 * Adds an Intersect listener
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addListener(IntersectListener listener) {
		if (intersectListeners == null) {
			intersectListeners = new ArrayList<IntersectListener>();
		}
		this.intersectListeners.add(listener);
	}

	/**
	 * Removes an Intersect listener
	 * 
	 * @param listener
	 *            Listener to remove
	 * @return <code>true</code> if the listeners contained the specified
	 *         element
	 */
	public boolean removeListener(IntersectListener listener) {
		if (intersectListeners != null) {
			return this.intersectListeners.remove(listener);
		} else {
			return false;
		}
	}

	/**
	 * Notifies Intersect listeners that the investigation has ended
	 */
	protected void notifyInvestigators(IntersectResult result) {
		if (intersectListeners != null) {
			for (IntersectListener l : intersectListeners) {
System.err.println("Notifying Intersect Listeners");				
				l.setResults(result);
			}
		}
	}

	/**
	 * Adds a Synchronize listener
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addListener(SynchronizeListener listener) {
		if (synchListeners == null) {
			synchListeners = new ArrayList<SynchronizeListener>();
		}
		this.synchListeners.add(listener);
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
		if (synchListeners != null) {
			return this.synchListeners.remove(listener);
		} else {
			return false;
		}
	}

	/**
	 * Notifies Synchronize listeners that the investigation has ended
	 */
	protected void notifySynchronizeInvestigators(SynchronizeResult result) {
		if (synchListeners != null) {
			for (SynchronizeListener l : synchListeners) {
				l.setResults(result);
			}
		}
	}

	/**
	 * Adds a Fitting listener
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addListener(FittingListener listener) {
		if (fittingListeners == null) {
			fittingListeners = new ArrayList<FittingListener>();
		}
		this.fittingListeners.add(listener);
	}

	/**
	 * Removes a Fitting listener
	 * 
	 * @param listener
	 *            Listener to remove
	 * @return <code>true</code> if the listeners contained the specified
	 *         element
	 */
	public boolean removeListener(FittingListener listener) {
		if (fittingListeners != null) {
			return this.fittingListeners.remove(listener);
		} else {
			return false;
		}
	}

	/**
	 * Notifies Fitting listeners that the investigation has ended
	 */
	protected void notifyFittingInvestigators(boolean fittingState) {
		if (fittingListeners != null) {
			for (FittingListener l : fittingListeners) {
				l.setResults(fittingState);
			}
		}
	}

	/**
	 * Returns whether there are any GLRenderables currently added to the Scene
	 *
	 * @return true is the are GLRenderables present in the Scene
	 * 
	 * thread-safe
	 */
	public boolean hasRenderables() {
		synchronized (lock) {
			if (renderables.size() > 0) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns the current number of GLRenderable objects in the Scene
	 *
	 * @return count of currently loaded GLRenderables
	 *
	 * not thread-safe
	 */
	public int getLoadedRenderablesCount() {
		synchronized (lock) {
			return renderables.size();
		}
	}
	
	/**
	 * Enables or disables tight fitting of polygons and lines to a shape model if one exists.
	 *
	 * @param fit
	 *
	 * not thread-safe
	 */
	public void enableFitting(boolean fit) {
		fittingEnabled = fit;
		
	}
	
	/**
	 * Returns true if tight fitting is enabled for polygons and lines
	 *
	 * @return true if enabled
	 *
	 * thread-safe
	 */
	public boolean isFittingEnabled() {
		return fittingEnabled;
	}
	
	/**
	 * @return the displayBusyText
	 */
	public boolean isDisplayBusyText() {
		return displayBusyText;
	}

	/**
	 * @param displayBusyText the displayBusyText to set
	 */
	public void setDisplayBusyText(boolean displayBusyText) {
		this.displayBusyText = displayBusyText;
	}
	
	/**
	 * Port of Mark Morley's OpenGL frustum extraction code
	 * found at http://www.crownandcutlass.com/features/technicaldetails/frustum.html
	 * @param gl
	 */
	private void extractFrustum(GL2 gl) {
	   float[] proj = new float[16];
	   float[] modl = new float[16];
	   float[] clip = new float[16];
	   float   t = 0;
	   
	   // Get the current PROJECTION matrix from OpenGL
	   gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, proj, 0);

	   // Get the current MODELVIEW matrix from OpenGL 
	   gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modl, 0);

	   // save the P and MV out for use in rendering the frustum
	   projection = proj;
	   modelView = modl;

	   // Combine the two matrices (multiply projection by modelview) 
	   clip[0] = modl[0] * proj[0] + modl[1] * proj[4] + modl[2] * proj[8] + modl[3] * proj[12];
	   clip[1] = modl[0] * proj[1] + modl[1] * proj[5] + modl[2] * proj[9] + modl[3] * proj[13];
	   clip[2] = modl[0] * proj[2] + modl[1] * proj[6] + modl[2] * proj[10] + modl[3] * proj[14];
	   clip[3] = modl[0] * proj[3] + modl[1] * proj[7] + modl[2] * proj[11] + modl[3] * proj[15];

	   clip[4] = modl[4] * proj[0] + modl[5] * proj[4] + modl[6] * proj[8] + modl[7] * proj[12];
	   clip[5] = modl[4] * proj[1] + modl[5] * proj[5] + modl[6] * proj[9] + modl[7] * proj[13];
	   clip[6] = modl[4] * proj[2] + modl[5] * proj[6] + modl[6] * proj[10] + modl[7] * proj[14];
	   clip[7] = modl[4] * proj[3] + modl[5] * proj[7] + modl[6] * proj[11] + modl[7] * proj[15];

	   clip[8] = modl[8] * proj[0] + modl[9] * proj[4] + modl[10] * proj[8] + modl[11] * proj[12];
	   clip[9] = modl[8] * proj[1] + modl[9] * proj[5] + modl[10] * proj[9] + modl[11] * proj[13];
	   clip[10] = modl[8] * proj[2] + modl[9] * proj[6] + modl[10] * proj[10] + modl[11] * proj[14];
	   clip[11] = modl[8] * proj[3] + modl[9] * proj[7] + modl[10] * proj[11] + modl[11] * proj[15];

	   clip[12] = modl[12] * proj[0] + modl[13] * proj[4] + modl[14] * proj[8] + modl[15] * proj[12];
	   clip[13] = modl[12] * proj[1] + modl[13] * proj[5] + modl[14] * proj[9] + modl[15] * proj[13];
	   clip[14] = modl[12] * proj[2] + modl[13] * proj[6] + modl[14] * proj[10] + modl[15] * proj[14];
	   clip[15] = modl[12] * proj[3] + modl[13] * proj[7] + modl[14] * proj[11] + modl[15] * proj[15];

	   // Extract the numbers for the RIGHT plane 
	   frustum[0][0] = clip[3] - clip[0];
	   frustum[0][1] = clip[7] - clip[4];
	   frustum[0][2] = clip[11] - clip[8];
	   frustum[0][3] = clip[15] - clip[12];

	   // Normalize the result 
	   t = (float)Math.sqrt(frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2]);
	   frustum[0][0] /= t;
	   frustum[0][1] /= t;
	   frustum[0][2] /= t;
	   frustum[0][3] /= t;

	   // Extract the numbers for the LEFT plane
	   frustum[1][0] = clip[3] + clip[0];
	   frustum[1][1] = clip[7] + clip[4];
	   frustum[1][2] = clip[11] + clip[8];
	   frustum[1][3] = clip[15] + clip[12];

	   // Normalize the result
	   t = (float)Math.sqrt( frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2] );
	   frustum[1][0] /= t;
	   frustum[1][1] /= t;
	   frustum[1][2] /= t;
	   frustum[1][3] /= t;

	   // Extract the BOTTOM plane 
	   frustum[2][0] = clip[3] + clip[1];
	   frustum[2][1] = clip[7] + clip[5];
	   frustum[2][2] = clip[11] + clip[9];
	   frustum[2][3] = clip[15] + clip[13];

	   // Normalize the result 
	   t = (float)Math.sqrt( frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2] );
	   frustum[2][0] /= t;
	   frustum[2][1] /= t;
	   frustum[2][2] /= t;
	   frustum[2][3] /= t;

	   // Extract the TOP plane 
	   frustum[3][0] = clip[3] - clip[1];
	   frustum[3][1] = clip[7] - clip[5];
	   frustum[3][2] = clip[11] - clip[9];
	   frustum[3][3] = clip[15] - clip[13];

	   // Normalize the result 
	   t = (float)Math.sqrt( frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2] );
	   frustum[3][0] /= t;
	   frustum[3][1] /= t;
	   frustum[3][2] /= t;
	   frustum[3][3] /= t;

	   // Extract the FAR plane 
	   frustum[4][0] = clip[3] - clip[2];
	   frustum[4][1] = clip[7] - clip[6];
	   frustum[4][2] = clip[11] - clip[10];
	   frustum[4][3] = clip[15] - clip[14];

	   // Normalize the result 
	   t = (float)Math.sqrt( frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2] );
	   frustum[4][0] /= t;
	   frustum[4][1] /= t;
	   frustum[4][2] /= t;
	   frustum[4][3] /= t;

	   // Extract the NEAR plane 
	   frustum[5][0] = clip[3] + clip[2];
	   frustum[5][1] = clip[7] + clip[6];
	   frustum[5][2] = clip[11] + clip[10];
	   frustum[5][3] = clip[15] + clip[14];

	   // Normalize the result
	   t = (float)Math.sqrt( frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2] );
	   frustum[5][0] /= t;
	   frustum[5][1] /= t;
	   frustum[5][2] /= t;
	   frustum[5][3] /= t;
	}
	
	// Bundle of planes: A set of planes sharing a point in common.
	// From mathworld (Hessian Normal Form):
	//   nx = a/sqrt(a^2+b^2+c^2); ny = b/sqrt(a^2+b^2+c^2); nz = c/sqrt(a^2+b^2+c^2); p = d/sqrt(a^2+b^2+c^2);
	// See: http://mathworld.wolfram.com/Plane-PlaneIntersection.html
	// See: https://stackoverflow.com/questions/32597503/finding-the-intersection-point-of-a-bundle-of-planes-3-in-three-js
	
	/**
	 * Computes normal to the specified plane.
	 * (ref: http://mathworld.wolfram.com/HessianNormalForm.html)
	 * @param plane 4-element vector containing A, B, C, D for plane equation Ax+By+Cz+D=0.
	 * @param p outputs constant in the Hessian Normal Form in the zero'th element
	 *   (containing distance of plane to the origin), if non-null.
	 * @return Normal to the plane.
	 */
	public static HVector planeNormal(double[] plane, double[] p) {
		double a2b2c2 = Math.sqrt(Math.pow(plane[0],2)+Math.pow(plane[1],2)+Math.pow(plane[2],2));
		HVector n = new HVector(plane[0]/a2b2c2, plane[1]/a2b2c2, plane[2]/a2b2c2);
		if (p != null) {
			p[0] = plane[3]/a2b2c2;
		}
		return n;
	}
	
//	/**
//	 * (ref: https://math.stackexchange.com/questions/2563909/find-points-on-a-plane).
//	 * @param plane
//	 * @return
//	 */
//	public static HVector ptOnPlane(double[] plane) {
//		
//	}
	
	/**
	 * Computes intersection point of a bundle of planes.
	 * A bundle of planes is a set of planes sharing a point in common
	 * (ref: http://mathworld.wolfram.com/BundleofPlanes.html).
	 * @param plane1 Plane 1 (A,B,C,D)
	 * @param plane2 Plane 2 (A,B,C,D)
	 * @param plane3 Plane 3 (A,B,C,D)
	 * @return Intersection point.
	 */
	public static HVector intersect3(double[] plane1, double[] plane2, double[] plane3) {
		double[] p1 = new double[1], p2 = new double[1], p3 = new double[1];
		HVector n1 = planeNormal(plane1,p1).unit();
		HVector n2 = planeNormal(plane2,p2).unit();
		HVector n3 = planeNormal(plane3,p3).unit();
		
		HVector x1 = n1.mul(-p1[0]);
		HVector x2 = n2.mul(-p2[0]);
		HVector x3 = n3.mul(-p3[0]);
		
		HVector v = n2.cross(n3).mul(x1.dot(n1)).add(n3.cross(n1).mul(x2.dot(n2))).add(n1.cross(n2).mul(x3.dot(n3)));
		
		Matrix3d m3d = new Matrix3d(n1.x, n1.y, n1.z, n2.x, n2.y, n2.z, n3.x, n3.y, n3.z);
		m3d.transpose();
		double det = m3d.determinant();
		
		if (det == 0) { // determinant == 0 means two planes are parallel
			throw new IllegalArgumentException("zero determinant - two planes are parallel");
		}
		
		v.divEq(det);
		
		return v;
	}
	
	public static double[] toDouble(float[] vals) {
		double[] d = new double[vals.length];
		for(int i=0; i<d.length; i++) {
			d[i] = vals[i];
		}
		return d;
	}
	
	private void drawFrustum(GL2 gl, float[] proj, float[] mv) {
		{
			int right=0, left=1, bottom=2, top=3, far=4, near=5;
			HVector nearTopLeft = intersect3(toDouble(frustum[near]),toDouble(frustum[top]),toDouble(frustum[left]));
			HVector nearTopRight = intersect3(toDouble(frustum[near]),toDouble(frustum[top]),toDouble(frustum[right]));
			HVector nearBotLeft = intersect3(toDouble(frustum[near]),toDouble(frustum[bottom]),toDouble(frustum[left]));
			HVector nearBotRight = intersect3(toDouble(frustum[near]),toDouble(frustum[bottom]),toDouble(frustum[right]));
			HVector farTopLeft = intersect3(toDouble(frustum[far]),toDouble(frustum[top]),toDouble(frustum[left]));
			HVector farTopRight = intersect3(toDouble(frustum[far]),toDouble(frustum[top]),toDouble(frustum[right]));
			HVector farBotLeft = intersect3(toDouble(frustum[far]),toDouble(frustum[bottom]),toDouble(frustum[left]));
			HVector farBotRight = intersect3(toDouble(frustum[far]),toDouble(frustum[bottom]),toDouble(frustum[right]));

			gl.glColor3f(0f, 1f, 0f);
			gl.glBegin(GL2.GL_LINES);

			// near
			gl.glVertex3d(nearTopLeft.x, nearTopLeft.y, nearTopLeft.z);
			gl.glVertex3d(nearTopRight.x, nearTopRight.y, nearTopRight.z);

			gl.glVertex3d(nearTopRight.x, nearTopRight.y, nearTopRight.z);
			gl.glVertex3d(nearBotRight.x, nearBotRight.y, nearBotRight.z);

			gl.glVertex3d(nearBotRight.x, nearBotRight.y, nearBotRight.z);
			gl.glVertex3d(nearBotLeft.x, nearBotLeft.y, nearBotLeft.z);

			gl.glVertex3d(nearBotLeft.x, nearBotLeft.y, nearBotLeft.z);
			gl.glVertex3d(nearTopLeft.x, nearTopLeft.y, nearTopLeft.z);

			// far
			gl.glVertex3d(farTopLeft.x, farTopLeft.y, farTopLeft.z);
			gl.glVertex3d(farTopRight.x, farTopRight.y, farTopRight.z);

			gl.glVertex3d(farTopRight.x, farTopRight.y, farTopRight.z);
			gl.glVertex3d(farBotRight.x, farBotRight.y, farBotRight.z);

			gl.glVertex3d(farBotRight.x, farBotRight.y, farBotRight.z);
			gl.glVertex3d(farBotLeft.x, farBotLeft.y, farBotLeft.z);

			gl.glVertex3d(farBotLeft.x, farBotLeft.y, farBotLeft.z);
			gl.glVertex3d(farTopLeft.x, farTopLeft.y, farTopLeft.z);

			// sides
			gl.glVertex3d(nearTopLeft.x, nearTopLeft.y, nearTopLeft.z);
			gl.glVertex3d(farTopLeft.x, farTopLeft.y, farTopLeft.z);

			gl.glVertex3d(nearTopRight.x, nearTopRight.y, nearTopRight.z);
			gl.glVertex3d(farTopRight.x, farTopRight.y, farTopRight.z);

			gl.glVertex3d(nearBotLeft.x, nearBotLeft.y, nearBotLeft.z);
			gl.glVertex3d(farBotLeft.x, farBotLeft.y, farBotLeft.z);

			gl.glVertex3d(nearBotRight.x, nearBotRight.y, nearBotRight.z);
			gl.glVertex3d(farBotRight.x, farBotRight.y, farBotRight.z);

			gl.glEnd();
			gl.glLineWidth(3);
		}


		gl.glColor3f(1f, 1f, 1f);
	}
	
	/*
	 * Method to determine current pixel per-degree resolution of the scene based on
	 * a loaded and rendered shape model centered at the origin. This method will work
	 * even if the shape model is not in the field of view although it should never be
	 * called in that case.
	 *
	 * @param gl Current GL context
	 * @return the calculated PPD  or -1 if the camera is inside the shape model
	 * 
	 */	
	private int getPPD(GL2 gl) {
		int height = parent.getSurfaceHeight();
		int width = parent.getSurfaceWidth();
		float[] leftIntersection = null;
		float[] rightIntersection = null;
		// get the left and right edge of the screen in model view coordinates
		float[] left = unProjectedMouseNoTranslate(gl, 0f, height/2f, 0f);
		float[] right = unProjectedMouseNoTranslate(gl, width, height/2f, 0f);
		
		TriangleMesh shapeModel = ThreeDManager.getInstance().getShapeModel();
		if (shapeModel == null) {
			return -1;
		}
		OctTree oct = shapeModel.getOctTree();
		ArrayList<Triangle> tris = new ArrayList<>();
		oct.looseOctTreeRayIntersectShortCircuit(new Ray(left, new float[]{-left[X], -left[Y], -left[Z]}), oct.getRoot(), tris);
		if (tris.size() > 0) {
			leftIntersection = tris.get(0).getIntersection();
		} else {
			return -1;
		}
		tris.clear();
		oct.looseOctTreeRayIntersectShortCircuit(new Ray(right, new float[]{-right[X], -right[Y], -right[Z]}), oct.getRoot(), tris);
		if (tris.size() > 0) {
			rightIntersection = tris.get(0).getIntersection();
		} else {
			return -1;
		}
		
		float ang = VectorUtil.angleVec3(VectorUtil.normalizeVec3(new float[3], leftIntersection), VectorUtil.normalizeVec3(new float[3], rightIntersection));
		// if we are so far away that the angle will cause division by zero, default to 1 PPD
		if (ang < FloatUtil.EPSILON) {
			return 1;
		}
		
		// convert to degrees
		ang *= (180f / FloatUtil.PI);	
		
		// now we need to project the 2 intersect points back to screen space
		float[] newLeft = projectNoTranslate(gl, leftIntersection);
		float[] newRight = projectNoTranslate(gl, rightIntersection);
		// 1st order calculation of the PPD
		int ppd = (int)((newRight[X] - newLeft[X]) / ang);
		// find the next highest power of 2
		int powOf2 = Integer.highestOneBit(ppd);
		if (powOf2 != ppd) {
			ppd = powOf2 << 1;
		}
		// we never want to drop below 1 PPD if we are outside the body
		return (ppd < 1) ? 1 : ppd;
	}
	
	private void synchViews(GL2 gl) {
		int height = parent.getSurfaceHeight();
		int width = parent.getSurfaceWidth();
		float[] eyeLoc = unProjectedMouseNoTranslate(gl, width/2, height/2f, 0f);
		float[] intersection = null;
		TriangleMesh shapeModel = ThreeDManager.getInstance().getShapeModel();
		if (shapeModel == null) {
			return;
		}
		OctTree oct = shapeModel.getOctTree();
		ArrayList<Triangle> tris = new ArrayList<>();
		oct.looseOctTreeRayIntersectShortCircuit(new Ray(eyeLoc, new float[]{-eyeLoc[X], -eyeLoc[Y], -eyeLoc[Z]}), oct.getRoot(), tris);
		if (tris.size() > 0) {
			intersection = tris.get(0).getIntersection();
			HVector temp = new HVector();
			temp = temp.set(intersection[0], intersection[1],
					intersection[2]);
			double latitude = temp.lat();
			//internally jmars works in degrees West
			double longitude = temp.lonW();
			SynchronizeResult result = new SynchronizeResult();
			result.setLatitude((float) latitude);
			result.setLongitude((float) longitude);
			notifySynchronizeInvestigators(result);
		} else {
			return;
		}		
	}
	
	private float[] unProjectedMouse(GL2 gl, float mx, float my, float pickDepth) {

		final float pickedPointDepth = pickDepth;
		final float[] sceneModelViewValues = new float[16];
		final float[] projectionValues = new float[16];
		final int[] viewport = new int[4];
		final float[] result = new float[] { 0f, 0f, 0f };

		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, sceneModelViewValues, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projectionValues, 0);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		
		/* note viewport[3] is height of window in pixels */
		int realY = viewport[3] - (int) my - 1;

		my = viewport[3] - my;
		final int[] pickedPoint = new int[] {(int) mx, realY};

		if (glu.gluUnProject(pickedPoint[0], pickedPoint[1], pickedPointDepth,
				sceneModelViewValues, 0, projectionValues, 0, viewport, 0,
				result, 0)) {
			return result;
		} else {
			return null;
		}
	}
	
	private float[] unProjectedMouseNoTranslate(GL2 gl, float mx, float my, float pickDepth) {

		final float pickedPointDepth = pickDepth;
		final float[] modelView = new float[16];
		final float[] projectionValues = new float[16];
		final int[] viewport = new int[4];
		final float[] result = new float[] { 0f, 0f, 0f };

		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projectionValues, 0);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		
		// remove camera translation from the model view		
		modelView[3] *= -1f;
		modelView[7] *= -1f;
		modelView[11] *= -1f;
		
		my = viewport[3] - my;
		final int[] pickedPoint = new int[] { (int) mx, (int) my };

		if (glu.gluUnProject(pickedPoint[0], pickedPoint[1], pickedPointDepth,
				modelView, 0, projectionValues, 0, viewport, 0,
				result, 0)) {
			return result;
		} else {
			return null;
		}
	}

	private float[] project(GL2 gl, float[] obj3D) {
		
		final float[] sceneModelView = new float[16];
		final float[] projection = new float[16];
		final int[] viewport = new int[4];
		final float[] result = new float[]{0f, 0f, 0f};

		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, sceneModelView, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection, 0);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		
		 if (glu.gluProject(obj3D[X], obj3D[Y], obj3D[Z], sceneModelView, 0, projection, 0, viewport, 0, result, 0)) {
			 return result;
		 } else {
			 return null;
		 }
	}
	
	private float[] projectNoTranslate(GL2 gl, float[] obj3D) {
		
		final float[] modelView = new float[16];
		final float[] projection = new float[16];
		final int[] viewport = new int[4];
		final float[] result = new float[]{0f, 0f, 0f};

		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection, 0);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		
		// remove camera translation from the model view		
		modelView[3] *= -1f;
		modelView[7] *= -1f;
		modelView[11] *= -1f;
		
		 if (glu.gluProject(obj3D[X], obj3D[Y], obj3D[Z], modelView, 0, projection, 0, viewport, 0, result, 0)) {
			 return result;
		 } else {
			 return null;
		 }
	}
	
	boolean isPowerOf2(int i) {
		return i > 2 && ((i&-i)==i);
	}

	private void drawTriangle(GL2 gl, float[][] points, int width, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);

		float[] a = points[0];
		float[] b = points[1];
		float[] c = points[2];

		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex3f(a[0], a[1], a[2]);
		gl.glVertex3f(b[0], b[1], b[2]);
		gl.glVertex3f(c[0], c[1], c[2]);
		gl.glEnd();
	}

	private static void logAnyErrorCodes(final Object obj, final GL gl,
			final String prefix) {

		final int glError = gl.glGetError();
		if (glError != GL.GL_NO_ERROR) {
			final String errStr = "GL-Error: " + prefix + " on obj 0x"
					+ Integer.toHexString(obj.hashCode())
					+ ", OpenGL error: 0x" + Integer.toHexString(glError);
			if (glError == GL2.GL_INVALID_ENUM) {
				System.err.println("Invalid GL Enum!!!");
			}
			log.aprint(errStr);
			ExceptionUtils.dumpStack(DebugLog.getOutputStream());
		}

		final int status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		// the following code will only be called when using a fully
		// programmable pipeline
		if (status != GL.GL_FRAMEBUFFER_COMPLETE) {
			final String errStr = "GL-Error: " + prefix + " on obj 0x"
					+ Integer.toHexString(obj.hashCode())
					+ ", glCheckFramebufferStatus: 0x"
					+ Integer.toHexString(status);
			log.aprint(errStr);
			ExceptionUtils.dumpStack(DebugLog.getOutputStream());
		}
	}
	
	private void print4x4Matrix(String label, float[] m) {
		if (m == null || m.length != 16) {
			System.err.println("Cannot print invalid 4x4 matrix!");
		} else {
			if (label != null && label.length() > 0) {
				System.err.println(label+" Matrix:");
			} else {
				System.err.println("Matrix:");
			}
			System.err.println(""+m[0]+", "+m[1]+", "+m[2]+", "+m[3]);
			System.err.println(""+m[4]+", "+m[5]+", "+m[6]+", "+m[7]);
			System.err.println(""+m[8]+", "+m[9]+", "+m[10]+", "+m[11]);
			System.err.println(""+m[12]+", "+m[13]+", "+m[14]+", "+m[15]);
		}
	}
	
	/*
	 * Method to project x,y onto a hemisphere centered within width, height, z is "up"
	 * 
	 * @param x coordinate to be projected
	 * @param y coordinate to be projected
	 * @param width width of the drawing surface
	 * @param height height of the drawing surface
	 * @param projPt returned 3D point on the virtual hemisphere
	 */
	private void projectToSphere (int x, int y, int width, int height, float[] projPt) {		
		float d=0;
		// project x,y onto a hemisphere centered within width, height, z is "up"
		// center and normalize the X coordinate on a unit circle
		projPt[X] = (2f * x - width) / width;
		// center and normalize the Y coordinate on a unit circle
		projPt[Y] = (height - 2f * y) / height;
		// calculate the magnitude of [X, Y] 
		d = FloatUtil.sqrt(projPt[X] * projPt[X] + projPt[Y] * projPt[Y]);
		d = (d < 1f) ? d : 1f;
		// calculate the Z coordinate
		projPt[Z] = FloatUtil.sqrt(1.001f - d * d);
		// normalize the projected point
		projPt = OctTree.normalizeVec3(projPt);
	}
	
	private void startRotation(int x, int y) {
		mouseMoving = true;
		projectToSphere(x, y, parent.getSurfaceWidth(), parent.getSurfaceHeight(), lastDragPos);
	}
	
	private void stopRotation(int x, int y) {
		rotationAngle = 0f;
		mouseMoving = false;
	}
	
	/**
	 * Method to trigger a 3D window screen capture to a PNG file.
	 * 
	 * @param filepath a string containing an OS specific fie path and file name for the PNG file.
	 */
	public void printScreen (String filepath) {
		filePath = filepath;
		printScreen = true;
	}
	
	private void printVector(String label, float[] vec) {
		if (vec == null) {
			log.aprintln("Cannot print a null vector!");
		} else {
			System.err.print((label == null) ? " " : label);
			for (int i=0; i<vec.length; i++) {
				System.err.print(" "+vec[i]);
				System.err.print("\n");
			}
		}
	}

	private class SimpleMouseAdapter extends MouseAdapter {
		// Silly default value
		private float dragScalar = 0.000002f;
		
		public void mouseMoved(final MouseEvent e) {
			projMouseX = e.getX();
			projMouseY = e.getY();
			if (inspect) {
				Scene.this.parent.repaint();
			}
			if (!e.isAnyButtonDown()) {
				enableTooltips = true;
				Scene.this.parent.repaint();
			}
		}
		
		public void mouseClicked(MouseEvent e){
			mouseClicked = true;
			if(e.isControlDown()){
				controlDown = true;
			}
			//trigger a notify to the listeners
			if(intersectListeners != null && intersectListeners.size()>0){
				Scene.this.parent.repaint();
			}
		}

		public void mousePressed(MouseEvent e) {
			projMouseX = e.getX();
			projMouseY = e.getY();
			prevMouseX = e.getX();
			prevMouseY = e.getY();
			
			if (e.getButton() == MouseEvent.BUTTON3) {
				enableTooltips = true;
				Scene.this.parent.repaint();
			} else if (e.getButton() == MouseEvent.BUTTON1) {
				startRotation(e.getX(), e.getY());
				Scene.this.parent.requestFocusInWindow();
			}
			
		}

		public void mouseReleased(MouseEvent e) {
			
			int width = 0, height = 0;
			Object source = e.getSource();
			if (source instanceof Window) {
				Window window = (Window) source;
				width = window.getWidth();
				height = window.getHeight();
			} else if (GLProfile.isAWTAvailable()
					&& source instanceof java.awt.Component) {
				java.awt.Component comp = (java.awt.Component) source;
				width = comp.getWidth();
				height = comp.getHeight();
			}
			
			if (e.getButton() == MouseEvent.BUTTON3) {
				enableTooltips = false;
				Scene.this.parent.repaint();
			}

			if (mouseMoving && e.getButton() == MouseEvent.BUTTON1) {
				stopRotation(e.getX(), e.getY());
			}
		}
		
		private float maxLen = Float.MAX_VALUE;
		
		public void mouseDragged(MouseEvent e) {

			int x = e.getX();
			int y = e.getY();
			int width = 0, height = 0;
			Object source = e.getSource();
			if (source instanceof Window) {
				Window window = (Window) source;
				width = window.getSurfaceWidth();
				height = window.getSurfaceHeight();
			} else if (source instanceof GLJPanel) {
				GLJPanel panel = (GLJPanel) source;
				width = panel.getSurfaceWidth();
				height = panel.getSurfaceHeight();
			} else if (GLProfile.isAWTAvailable()
					&& source instanceof java.awt.Component) {
				java.awt.Component comp = (java.awt.Component) source;
				width = comp.getWidth();
				height = comp.getHeight();
			} else {
				throw new RuntimeException(
						"Event source neither Window nor Component: " + source);
			}
			
			if (mouseMoving && !e.isControlDown()) {
				float[] curPosition = new float[3];
				// compute the mouse in model space
				projectToSphere (x, y, width, height, curPosition);                                                                                                                                                                         
				float dx = 0f;
				float dy = 0f;
				float dz = 0f;
				
				// calculate the change in position 
				dx = curPosition[X] - lastDragPos[X];
				dy = curPosition[Y] - lastDragPos[Y];
				dz = curPosition[Z] - lastDragPos[Z];
				
				if (!FloatUtil.isEqual(dx, 0f, OctTree.MINI_EPSILON) || !FloatUtil.isEqual(dy, 0f, OctTree.MINI_EPSILON) || !FloatUtil.isEqual(dz, 0f, OctTree.MINI_EPSILON)) {
					// calculate the angle and axis
					float ra = 90f * FloatUtil.sqrt(dx * dx + dy * dy + dz * dz);
					if (ra > OctTree.MINI_EPSILON) {
						float[] raxis = new float[3];
						raxis[X] = lastDragPos[Y] * curPosition[Z] - lastDragPos[Z] * curPosition[Y];
						raxis[Y] = lastDragPos[Z] * curPosition[X] - lastDragPos[X] * curPosition[Z];
						raxis[Z] = lastDragPos[X] * curPosition[Y] - lastDragPos[Y] * curPosition[X];
						
						float[] raxnorm = OctTree.normalizeVec3(raxis);
						
						
						rotationAngle = ra;
						axis[X] = raxnorm[X];
						axis[Y] = raxnorm[Y];
						axis[Z] = raxnorm[Z];
						
						// convert the rotation angle to radians
						float rAngle = ra * FloatUtil.PI / 180f;						
						rotQ = rotQ.rotateByAngleNormalAxis(rAngle, raxnorm[X], raxnorm[Y], raxnorm[Z]);
						// update the position
						lastDragPos[X] = curPosition[X];
						lastDragPos[Y] = curPosition[Y];
						lastDragPos[Z] = curPosition[Z];	
					}
				}
			}

			if (e.isControlDown()) {
				if (ThreeDManager.getInstance().hasShapeModel()) {
					maxLen = ThreeDManager.getInstance().getShapeModel().getMaxLen();
				}
				// TODO Very hacky! Just a temp stop gap solution...
				if (maxLen >= 0.9f) {
					dragScalar = 0.000008f;
				} else {
					dragScalar = 0.000004f;
				}
				// translate
				if (x < prevMouseX) {
					transX += width * dragScalar;
				} else if (x > prevMouseX) {
					transX -= width * dragScalar;
				}
				if (y < prevMouseY) {
					transY -= height * dragScalar;
				} else if (y > prevMouseY) {
					transY += height * dragScalar;
				}
			} 

			prevMouseX = x;
			prevMouseY = y;

			if (inspect) {
				projMouseX = e.getX();
				projMouseY = e.getY();
			}
			Scene.this.parent.repaint();
		}

		public void mouseWheelMoved(MouseEvent e) {
			/*
			 * Returns a 3-component float array filled with the values of the
			 * rotational axis in the following order: horizontal-, vertical-
			 * and z-axis. A vertical rotation of > 0.0f is up and < 0.0f is
			 * down. A horizontal rotation of > 0.0f is left and < 0.0f is
			 * right. A z-axis rotation of > 0.0f is back and < 0.0f is front.
			 * However, on some OS this might be flipped due to the OS default
			 * behavior. The latter is true for OS X 10.7 (Lion) for example. On
			 * PointerClass onscreen devices, i.e. touch screens, rotation
			 * events are usually produced by a 2-finger movement, where
			 * horizontal and vertical rotation values are filled. On
			 * PointerClass offscreen devices, i.e. mouse, either the horizontal
			 * or the vertical rotation value is filled. The
			 * InputEvent.SHIFT_MASK modifier is set in case |horizontal| >
			 * |vertical| value. This can be utilized to implement only one 2d
			 * rotation direction, you may use InputEvent.isShiftDown() to query
			 * it. In case the pointer type is mouse, events are usually send in
			 * steps of one, ie. -1.0f and 1.0f. Higher values may result due to
			 * fast scrolling. Fractional values may result due to slow
			 * scrolling with high resolution devices. Here the button number
			 * refers to the wheel number. In case the pointer type is of class
			 * MouseEvent.PointerClass.Onscreen, e.g. touch screen, see
			 * getRotationScale() for semantics.
			 */

			float[] direction = e.getRotation();
			if (direction[1] < 0) {
				// safeties off! going to allow infinite zooming :8
				zoomFactor /= 1.05f;
			} else {
				zoomFactor *= 1.05f;
			}
			Scene.this.parent.repaint();
		}

	}

	private class SimpleKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int width = 0, height = 0;
			Object source = e.getSource();
			if (source instanceof Window) {
				Window window = (Window) source;
				width = window.getSurfaceWidth();
				height = window.getSurfaceHeight();
			} else if (source instanceof GLJPanel) {
				GLJPanel panel = (GLJPanel) source;
				width = panel.getSurfaceWidth();
				height = panel.getSurfaceHeight();
			} else if (GLProfile.isAWTAvailable()
					&& source instanceof java.awt.Component) {
				java.awt.Component comp = (java.awt.Component) source;
				width = comp.getWidth();
				height = comp.getHeight();
			} else {
				throw new RuntimeException(
						"Event source neither Window nor Component: " + source);
			}
			float maxLen = 1f;
			float stepScalar = 0.000008f;
			if (ThreeDManager.getInstance().hasShapeModel()) {
				maxLen = ThreeDManager.getInstance().getShapeModel().getMaxLen();
			}
			// TODO Very hacky! Just a temp stop gap solution...
			if (maxLen >= 0.9f) {
				stepScalar = 0.000064f;
			} else {
				stepScalar = 0.000032f;
			}

			int kc = e.getKeyCode();
			int kchar = e.getKeyChar();
			switch (kc) {
			case KeyEvent.VK_LEFT:
				transX += (width * stepScalar);
				break;
			case KeyEvent.VK_RIGHT:
				transX -= (width * stepScalar);
				break;
			case KeyEvent.VK_UP:
				transY -= (height * stepScalar);
				break;
			case KeyEvent.VK_DOWN:
				transY += (height * stepScalar);
				break;
			case KeyEvent.VK_Z:
				switch (kchar) {
				case 'Z':
					zoomFactor *= 1.1f;
					break;
				case 'z':
					zoomFactor /= 1.1f;
					break;
				}
				break;
			case KeyEvent.VK_B:
				switch (kchar) {
				case 'B':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().setDrawBody(true);
					break;
				case 'b':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().setDrawBody(false);
					break;
				}
				break;
			case KeyEvent.VK_D:
				switch (kchar) {
				case 'D':
					debugIntersect = true;
					break;
				case 'd':
					debugIntersect = false;
					break;
				}
				break;
			case KeyEvent.VK_F:
				switch (kchar) {
				case 'f':
					fittingEnabled = false;
					notifyFittingInvestigators(false);
					break;
				case 'F':
					fittingEnabled = true;
					notifyFittingInvestigators(true);
					break;
				}
				break;
			case KeyEvent.VK_G:
				switch (kchar) {
				case 'g':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().drawMesh(false);
					break;
				case 'G':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().drawMesh(true);
					break;
				}
				break;
			case KeyEvent.VK_N:
				switch (kchar) {
				case 'n':
					pole = false;
					break;
				case 'N':	
					pole = true;					
					break;
				}
				break;
			case KeyEvent.VK_P:
				switch (kchar) {
				case 'p':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().drawPoints(false);
					break;
				case 'P':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().drawPoints(true);
					break;
				}
				break;
			case KeyEvent.VK_A:
				switch (kchar) {
				case 'a':
					drawAxis = false;
					break;
				case 'A':
					drawAxis = true;
					break;
				}
				break;
			case KeyEvent.VK_I:
				switch (kchar) {
				case 'i':
					inspect = false;
					break;
				case 'I':
					inspect = true;
					break;
				}
				break;
			case KeyEvent.VK_L:
				switch (kchar) {
				case 'l':
					light = false;
					break;
				case 'L':
					light = true;
					break;
				}
				break;
			case KeyEvent.VK_S:
				switch (kchar) {
				case 's':
					drawFrustum = false;
					projection = null;
					break;
				case 'S':
					drawFrustum = true;
					break;
				}
				break;
			case KeyEvent.VK_T:
				switch (kchar) {
				case 'T':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().setDisplayTileGrid(true);
					break;
				case 't':
					if (mgr.getShapeModel() != null)
						mgr.getShapeModel().setDisplayTileGrid(false);
					break;
				}
				break;
			case KeyEvent.VK_R:
				resetCamera();
			}

			Scene.this.parent.repaint();
		}
	}

	/**
	 * Action Listener to trigger redraws. Primarily intended to provide event
	 * driven refreshes such as when new renderables <GLRenderable> are
	 * available on the queues
	 */
	private ActionListener qListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Scene.this.parent.repaint();
		}
	};
}