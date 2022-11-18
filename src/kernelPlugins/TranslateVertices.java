package kernelPlugins;

import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clSetKernelArg;

import org.jocl.Pointer;
import org.jocl.Sizeof;

import memoryPlugins.WireFrame;
import tbh.gfxInterface.KernelPlugin;

public class TranslateVertices extends KernelPlugin{

	@Override
	public void run(Object[] arg0) {
		int frameIndex;
		float[] translation, modTranslation;
		WireFrame frame;
		
		try {
			frameIndex = (int) arg0[0];
			translation = (float[]) arg0[1];
			modTranslation = new float[] {translation[0], translation[1], translation[2], 0};//need 4 values because float3 doesn't exist?f
			frame = (WireFrame) gfx.getMemoryPlugin(frameIndex);
		} catch (Exception e) {
			gfx.GfxLog(2, "Illegal arguments for kernel plugin TranslateVertices, args are:\n"
					+ "int frameIndex\n"
					+ "float[] translation");
			e.printStackTrace();
			return;
		}
		
		float[] center = frame.getCenter();
		frame.setCenter(new float[] {center[0] + translation[0], center[1] + translation[1], center[2] + translation[2]});
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(gfx.getMemoryObject(frameIndex)));
		clSetKernelArg(kernel, 1, Sizeof.cl_float4, Pointer.to(modTranslation));
		
		long local_work_size[] = new long[]{1, 1};
		long global_work_size[] = new long[]{ (long) frame.getVertices().length, 0L};

		int err = clEnqueueNDRangeKernel(gfx.getCommandQueue(), kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		if (err != org.jocl.CL.CL_SUCCESS) System.out.println("Failed to translate vertices: " + org.jocl.CL.stringFor_errorCode(err));
		gfx.updateResource(frameIndex);
	}

}
