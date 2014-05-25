package mjc;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import mjc.x64.X64FrameFactory;
import mjc.x64.X64InstructionSet;
import mjc.asm.Coder;
import mjc.asm.MunchCoder;

public class X64Main extends IRMain {
	public static void main(String[] args) throws Exception {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));

		CompilerOptions co = init(argList, CompilerOptions.Backend.X64);

		if (argList.isEmpty()) {
			System.out.println("No input files.");
			System.exit(0);
		}

		new X64Main(argList.get(0), co);
	}

	public X64Main(String filename, CompilerOptions opts) throws Exception {
		super(filename, opts,
			new X64FrameFactory(),
			new MunchCoder(X64InstructionSet.getInstance()));
	}
}
