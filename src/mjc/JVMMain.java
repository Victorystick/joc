package mjc;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import mjc.minijava.*;

public class JVMMain extends Main {
	public static void main(String[] args) throws Exception {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));

		CompilerOptions co = init(argList, CompilerOptions.Backend.JVM);

		if (argList.isEmpty()) {
			System.out.println("No input files.");
			System.exit(0);
		}

		new JVMMain(args[0], co);
	}

	public JVMMain(String filename, CompilerOptions opts) throws Exception {
		super(filename, opts);

		walker.walk(new MiniJavaJVMStackalyzer(ctxToSym, ctxToSize), tree);

		if (Errors.warningsExists() && opts.warnings) {
			Errors.printWarnings();
		}

		if (Errors.errorsExists()) {
			Errors.printErrors();
			System.exit(1);
		}

		walker.walk(new MiniJavaJVMOutputter(ctxToSym, ctxToSize, filename), tree);
	}
}
