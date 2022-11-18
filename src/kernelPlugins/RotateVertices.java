package kernelPlugins;

import java.util.ArrayList;

import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clSetKernelArg;

import org.jocl.Pointer;
import org.jocl.Sizeof;

import memoryPlugins.WireFrame;
import tbh.gfxInterface.KernelPlugin;

public class RotateVertices extends KernelPlugin{

	@Override
	public void run(Object[] arg0) {
		int frameIndex, axis;
		float rotation;
		WireFrame frame;
		try {
			frameIndex = (int) arg0[0];
			axis = (int) arg0[1];
			rotation = (float) arg0[2];
			frame = (WireFrame) gfx.getMemoryPlugin(frameIndex);
		} catch (Exception e) {
			gfx.GfxLog(2, "Illegal arguments for kernel plugin RotateVertices, args are:\n"
					+ "int frameIndex\n"
					+ "int axis\n"
					+ "float rotation");
			e.printStackTrace();
			return;
		}		
		
		float[] center = frame.getCenter();
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(gfx.getMemoryObject(frameIndex)));
		clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[] {(axis + 1) % 3}));
		clSetKernelArg(kernel, 2, Sizeof.cl_float2, Pointer.to(new float[] {center[(axis + 1) % 3], center[(axis+2)%3]}));
		clSetKernelArg(kernel, 3, Sizeof.cl_float, Pointer.to(new float[] {rotation}));
		
		long local_work_size[] = new long[]{1, 1};
		long global_work_size[] = new long[]{ (long) frame.getVertices().length, 0L};

		int err = clEnqueueNDRangeKernel(gfx.getCommandQueue(), kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		if (err != org.jocl.CL.CL_SUCCESS) System.out.println("Failed to rotate vertices: " + org.jocl.CL.stringFor_errorCode(err));
		gfx.updateResource(frameIndex);
	}
}
