package edu.asu.jmars.viz3d.renderer.input.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import gnu.trove.TFloatArrayList;
import gnu.trove.TIntArrayList;

/**
 * OBJ file reader. An OBJ file is tied to a DSK.
 * Multiple Ancillary FITS files are tied to a single OBJ.
 * @see AncFits
 */
public class Obj implements Serializable{
	private final File objFile;
	private boolean headerRead = false, dataRead = false;
	
	private float[] vertices;
	private int[] triangles;
	
	private String ISM;
	private String IDSK;
	private String ISPK;
	private String fitsFile;
	private String timestamp;
	private String softwareName;
	private String softwareVersion;
	
	private String name;
	
	/**
	 * Constructs an Obj object using the specified file and uses
	 * the filename of that file
	 * @param objFile
	 * @throws IOException
	 */
	public Obj(File objFile) throws IOException{
		this(objFile, objFile.getName());
	}
	
	/**
	 * Constructs an Obj object using the specified file and name
	 * @param objFile
	 * @param objName
	 * @throws IOException
	 */
	public Obj(File objFile, String objName) throws IOException {
		this.objFile = objFile;
		name = objName;
		readHeader();
	}
	
	/**
	 * @return Name associated with this OBJ
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @return File used to create this object
	 */
	public File getFile(){
		return objFile;
	}
	
	/**
	 * @return 3-space vertex coordinates as a linear array x1,y1,z1,x2,y2,z2,...
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public float[] getVertices() throws IOException, NumberFormatException {
		if (!dataRead){
			readData();
		}
		return vertices;
	}
	
	/**
	 * @return triples of indices into vertex array defining triangles
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public int[] getTriangles() throws IOException, NumberFormatException {
		if (!dataRead){
			readData();
		}
		return triangles;
	}
	
	/** @return The name of the input shape model */
	public String getISM() {
		return ISM;
	}

	/** @return The name of the input DSK */
	public String getIDSK() {
		return IDSK;
	}

	/** @return The name of the input SPK */
	public String getISPK() {
		return ISPK;
	}

	/** @return The name of FITS file specified in the OBJ header - TODO this is reversed */
	public String getFitsFile() {
		return fitsFile;
	}

	/** @return The timestamp of the file generation */
	public String getTimestamp() {
		return timestamp;
	}

	/** @return The name of software used to generate this OBJ */
	public String getSoftwareName() {
		return softwareName;
	}

	/** @return The version of software used to generate this OBJ */
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	
	/**
	 * Reads OBJ header
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void readHeader() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(objFile));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				parseObjHeaderLine(line.substring(1));				
			} else if (line.trim().isEmpty()) {
				// skip empty lines
			} else {
				break; // start of data
			}
		}
		br.close();
		
		this.headerRead = true;
	}
	
	/**
	 * Reads OBJ data, i.e. vertices and indices
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NumberFormatException
	 */
	private void readData() throws IOException, NumberFormatException {
		BufferedReader br = new BufferedReader(new FileReader(objFile));
		
		TFloatArrayList objVertices = new TFloatArrayList();
		TIntArrayList objIndices = new TIntArrayList();
		
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("v ")) {
				objVertices.add(parseFloatVals(line.substring(2)));
			} else if (line.startsWith("f ")) {
				objIndices.add(parseIntVals(line.substring(2)));
			}
		}
		br.close();
		
		this.vertices = objVertices.toNativeArray();
		this.triangles = objIndices.toNativeArray();
		
		this.dataRead = true;
	}

	static float[] parseFloatVals(String str) throws NumberFormatException {
		String[] vs = str.trim().split("\\s+");
		
		float[] vertex = new float[vs.length];
		for (int i=0; i<vs.length; i++) {
			vertex[i] = Float.parseFloat(vs[i]);
		}
		return vertex;
	}

	static int[] parseIntVals(String str) throws NumberFormatException {
		String[] is = str.trim().split("\\s+");
		
		int[] triangle = new int[is.length];
		for (int i=0; i<is.length; i++) {
			triangle[i] = Integer.parseInt(is[i]) - 1; // decrement the index to convert from 
		}
		return triangle;
	}

	private void parseObjHeaderLine(String str) {
		String[] lines = str.split("\\s*=\\s*", 2);
		if (lines.length > 1) {
			if (lines[0].equalsIgnoreCase("ISM")) {
				ISM = lines[1].trim();
			} else if (lines[0].equalsIgnoreCase("IDSK")) {
				IDSK = lines[1].trim();
			} else if (lines[0].equalsIgnoreCase("ISPK")) {
				ISPK = lines[1].trim();
			} else if (lines[0].equalsIgnoreCase("Software Name")) {
				softwareName = lines[1].trim();
			} else if (lines[0].equalsIgnoreCase("Software Version")) {
				softwareVersion = lines[1].trim();
			} else if (lines[0].equalsIgnoreCase("DATEPRD")) {
				timestamp = lines[1].trim();
			}
		} else {
			if (softwareVersion == null && lines[0].contains("Software Version ")) {
				softwareVersion = lines[0].substring("Software Version ".length());
			} else if (timestamp == null && lines[0].contains(":")) {
				timestamp = lines[0].trim();
			} else if (lines[0].contains(".FIT") || lines[0].contains(".fit")) {
				fitsFile = lines[0].trim();
			}
		}
	}
	
	
	@SuppressWarnings("boxing")
	public static void main(String[] args) throws Exception {
		Obj obj = null;
//		String fname = "/Users/saadat/Downloads/g_0085cm_tru_obj_0000n00000_v100.obj";
		String fname = "/Users/saadat/Downloads/l_0030cm_tru_dtm_0000s01500_v100.obj";
		
		long t0 = System.currentTimeMillis();
		obj = new Obj(new File(fname));
		System.out.format("triangles=%d vertices=%d\n",obj.getTriangles().length/3, obj.getVertices().length/3);
		long t1 = System.currentTimeMillis();
		System.out.format("Time to read %s: %dms (%gs)\n", fname, (t1-t0), (t1-t0)/1000.0);
	}
}