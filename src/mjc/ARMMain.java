package mjc;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import mjc.arm.ARMFrameFactory;
import mjc.arm.ARMInstructionSet;
import mjc.asm.Coder;
import mjc.asm.MunchCoder;

public class ARMMain extends IRMain {
	public static void main(String[] args) throws Exception {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));

		CompilerOptions co = init(argList, CompilerOptions.Backend.ARM);

		if (argList.isEmpty()) {
			System.out.println("No input files.");
			System.exit(0);
		}

		new ARMMain(argList.get(0), co);
	}

	public ARMMain(String filename, CompilerOptions opts) throws Exception {
		super(filename, opts,
			new ARMFrameFactory(),
			new MunchCoder(ARMInstructionSet.getInstance()));
	}
}
