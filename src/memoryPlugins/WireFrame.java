package memoryPlugins;

import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clEnqueueReadBuffer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.jocl.Pointer;
import org.jocl.Sizeof;

import tbh.gfxInterface.MemoryPlugin;

public class WireFrame extends MemoryPlugin{
	
	private int[][] edges;
	private float[][] vertices;
	private float[] center;
	
	public WireFrame(int [][] edges, float[][] vertices, float[] center) {
		this.edges = edges;
		this.vertices = vertices;
		this.center = center;
		forceCollision = true;
	}
	
	public int[][] getEdges(){
		return edges;
	}
	
	public float[][] getVertices(){
		return vertices;
	}
	
	public float[] getCenter() {
		return center;
	}
	
	public void setCenter(float[] center) {
		this.center = center;
	}
	
	public int[][] getProjectedVertices(float focalLen, float[] screenCenter){
		int[][] projected = new int[vertices.length][2];
		for (int i = 0; i < vertices.length; i++) {
			float denominator = ((vertices[i][2] - screenCenter[2]) + focalLen);
			projected[i][0] =  (int) ((((vertices[i][0] - screenCenter[0]) * focalLen) / denominator) + screenCenter[0]);
			projected[i][1] = (int) ((((vertices[i][1] - screenCenter[1]) * focalLen) / denominator) + screenCenter[1]);
		}
			
		return projected;
	}

	@Override
	public void addMemoryObject(int arg0) {
		int[] err = new int[1];
		float[] storeArray = new float[vertices.length * 3];
		try {
			for (int i = 0; i < vertices.length; i++) {
				for (int j = 0; j < 3; j++) {
					storeArray[(i * 3) + j] = vertices[i][j];
				}
			}
			gfx.getMemObjects()[arg0] = clCreateBuffer(gfx.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * (vertices.length * vertices[0].length), Pointer.to(storeArray), err);
			if (err[0] != org.jocl.CL.CL_SUCCESS) {
				gfx.GfxLog(2, "Failed to allocate memory for new wireFrame " + org.jocl.CL.stringFor_errorCode(err[0]));
			}
		} catch (IndexOutOfBoundsException e) {
			gfx.GfxLog(2, "Vertex array of wrong size, cannot load new wireframe");
			e.printStackTrace();
		} catch (Exception e) {
			gfx.GfxLog(2, "Unknown error loading new wireframe");
			e.printStackTrace();
		}
	}

	@Override
	public String getDefaultDesignation() {
		return "WireFrame";
	}

	@Override
	public void removeMemoryObject(int arg0) {
		org.jocl.CL.clReleaseMemObject(gfx.getMemObjects()[arg0]);
	}

	@Override
	public Object retrieveMemoryObject(int arg0) {
		float[] storeArray = new float[vertices.length * 3];
		int err = clEnqueueReadBuffer(gfx.getCommandQueue(), gfx.getMemoryObject(arg0), CL_TRUE, 0, Sizeof.cl_int * (vertices.length*vertices[0].length), Pointer.to(storeArray), 0, null, null);
	    if (err != org.jocl.CL.CL_SUCCESS) gfx.GfxLog(2, "Readbuffer Failed: " + org.jocl.CL.stringFor_errorCode(err));
	    for (int i = 0; i < vertices.length; i++) {
	    	for (int j = 0; j < 3; j++) {
	    		vertices[i][j] = storeArray[(i * 3) + j];
	    	}
	    }
		return vertices;
	}

	@Override
	public void updateDebug(int arg0) {
		retrieveMemoryObject(arg0);
		getContentPane().removeAll();
		JPanel vPanel = new JPanel();
		JLabel vLabel = new JLabel("Vertices");
		String[] prettyVertices = new String[vertices.length];
		for (int i = 0; i < vertices.length; i++)
			prettyVertices[i] = "{" + vertices[i][0] + "," + vertices[i][1] + "," + vertices[i][2] + "}";
		JList<String> vList = new JList<String>(prettyVertices);
		vPanel.setLayout(new BoxLayout(vPanel, BoxLayout.Y_AXIS));
		vPanel.add(vLabel);
		vPanel.add(vList);
		//vPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		JPanel ePanel = new JPanel();
		JLabel eLabel = new JLabel("Edges");
		String[] prettyEdges = new String[edges.length];
		for (int i = 0; i < edges.length; i++)
			prettyEdges[i] = "{" + edges[i][0] + "," + edges[i][1] + "}";
		JList<String> eList = new JList<String>(prettyEdges);
		ePanel.setLayout(new BoxLayout(ePanel, BoxLayout.Y_AXIS));
		ePanel.add(eLabel);
		ePanel.add(eList);
		
		add(vPanel, "East");
		add(ePanel, "West");
		
		pack();
	}

}
