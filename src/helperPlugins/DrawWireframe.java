package helperPlugins;

import memoryPlugins.WireFrame;
import tbh.gfxInterface.RunnablePlugin;

public class DrawWireframe extends RunnablePlugin{

	@Override
	public void run(Object[] arg0) {
		int canvas, wireFrameIndex, color;
		float focalLen;
		float[] screenCenter;
		WireFrame frame;
		
		try {
			canvas = (int) arg0[0];
			wireFrameIndex = (int) arg0[1];
			color = (int) arg0[2];
			focalLen = (float) arg0[3];
			screenCenter = (float[]) arg0[4];
			frame = (WireFrame) gfx.getMemoryPlugin(wireFrameIndex);
		} catch (Exception e) {
			gfx.GfxLog(2, "Invalid arguments for helper plugin DrawWireFrame, args are:\n"
					+ "int canvas\n"
					+ "int wireFrameIndex\n"
					+ "int color\n"
					+ "float focalLen\n"
					+ "float[] screenCenter");
			e.printStackTrace();
			return;
		}
		
		gfx.retrieveMemory(wireFrameIndex);
		int [][] vertices = frame.getProjectedVertices(focalLen, screenCenter);
		int [][] edges = frame.getEdges();
		
		for (int[] e : edges) {
			gfx.runPlugin("DrawLine", new Object[] {vertices[e[0]][0], vertices[e[0]][1], vertices[e[1]][0], vertices[e[1]][1], color, canvas});
		}
		
	}

}
