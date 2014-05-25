package mjc;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import mjc.alloc.*;
import mjc.asm.Coder;
import mjc.asm.InstructionSequence;
import mjc.asm.InstructionSet;
import mjc.frame.FrameFactory;
import mjc.ir.StmtSequence;
import mjc.ir.Temp;
import mjc.live.FlowAnalysis;
import mjc.live.Interference;
import mjc.live.LiveAnalysis;
import mjc.minijava.*;
import mjc.translate.Function;
import underscore.*;

public abstract class IRMain extends Main {
	private final CompilerOptions options;

	public IRMain(final String filename, final CompilerOptions opts,
			final FrameFactory frameFactory, final Coder c) throws Exception {
		super(filename, opts);

		options = opts;

		try {
			MiniJavaIRBuilder irBuilder = new MiniJavaIRBuilder(frameFactory, ctxToSym);

			walker.walk(irBuilder, tree);

			c.generateData(irBuilder.data);

			InstructionSet is = frameFactory.getInstructionSet();

			StringBuilder asm = new StringBuilder();

			asm.append(is.beginData());
			asm.append("\n");

			asm.append(c.doneData());
			asm.append("\n");

			asm.append(is.endData());
			asm.append("\n");

			asm.append(is.beginCode());
			asm.append("\n");

			Mapper<Temp, String> mapper =
				new Mapper<Temp, String>() {
					public String map(Temp t) {
						if (t == null) {
							return null;
						}

						String s = frameFactory.map(t);

						if (s != null) {
							return s;
						}

						return t.toString();
					}
				};

			Allocator allocator =
				new OptimisticAllocator(opts, frameFactory);
				// new ColoringAllocator(opts, frameFactory);

			InstructionSequence function;

			for (Function func : irBuilder.funcs) {
				if (func.isExported()) {
					asm.append(is.export(func.getName()));
					asm.append("\n");
				}

				function = c.generateCode(func.frame, func.body);

				allocator.allocate(function);

				asm.append(function.allocateAll(
					this.<Temp, Temp, String>chainMap(
						allocator.getMapper(), mapper)));
				asm.append("\n");
			}

			asm.append(is.endCode());
			asm.append("\n");

			if (!Errors.fine()) {
				Errors.print();
			}

			if (Errors.errorsExists()) {
				System.exit(1);
			}

			String assembly = format(asm.toString());

			// debug(assembly);

			// OUTPUT 2 file

			String[] splitPath = filename.split(
				System.getProperty("file.separator"));
			String[] splitFile = splitPath[splitPath.length - 1].split("\\.");
			String outputFilename = _.join(
				Arrays.copyOfRange(splitFile, 0, splitFile.length - 1), ".");

			PrintStream out = new PrintStream(new File(outputFilename + ".s"));
			out.println(assembly);

			if (opts.assembly) {
				// We're done here.
				return;
			}

			// TODO: Try to output binary...

		} catch (Exception e) {
			e.printStackTrace();
			if (Errors.warningsExists() && opts.warnings) Errors.printWarnings();
			if (Errors.errorsExists()) Errors.printErrors();
			System.exit(1);
		}
	}

	private String format(String asm) {
		String[] lines = asm.split("\n");

		boolean indent = false;
		for (int i = 0; i < lines.length; i++) {
			if (indent && lines[i].startsWith("@ End")) {
				indent = false;
			}

			if (indent) {
				lines[i] = "\t" + lines[i];
			}

			if (!indent && lines[i].startsWith("@ Function")) {
				indent = true;
				i++;
			}
		}

		return _.join(lines, "\n");
	}

	private <F,S,T> Mapper<F, T> chainMap(final Mapper<F, S> t2t, final Mapper<S, T> t2s) {
		return new Mapper<F, T>() {
			public T map(F t) {
				return t2s.map(t2t.map(t));
			}
		};
	}

	private void debug(Object o) {
		if (options.debug) {
			System.out.println(o);
		}
	}
}
