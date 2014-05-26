package mjc;

import java.util.List;
import java.util.Iterator;
import underscore.*;

public class CompilerOptions implements Cloneable {
	private final static CompilerOptions defaults =
		new CompilerOptions(Backend.JVM);

	public final boolean debug;
	public final boolean generics;
	public final boolean assembly;
	public final boolean warnings;
	public final Backend backend;
	public final Frontend frontend;

	public enum Backend {
		JVM("jvm"), ARM("arm"), X64("x64"), MIPS("mips");

		private final String target;

		Backend(String t) {
			this.target = t;
		}

		public String toString() {
			return target;
		}
	}

	public enum Frontend {
		MiniJava("minijava"), LISP("lisp"), Cyba("cyba");
		private final String lang;

		Frontend(String l) {
			this.lang = l;
		}

		public String toString() {
			return lang;
		}
	}

	public CompilerOptions(Backend be) {
		backend = be;
		frontend = Frontend.MiniJava;
		debug = false;
		generics = false;
		assembly = false;
		warnings = false;
	}

	public CompilerOptions(Backend be, boolean d, boolean g, boolean s, boolean w) {
		backend = be;
		frontend = Frontend.MiniJava;
		debug = d;
		generics = g;
		assembly = s;
		warnings = w;
	}

	public static CompilerOptions parse(List<String> args) {
		return parse(args, defaults);
	}

	public static CompilerOptions parse(List<String> args, CompilerOptions defaults) {
		Backend backend = defaults.backend;

		boolean generics = defaults.generics;
		boolean debug = defaults.debug;
		boolean assembly = defaults.assembly;
		boolean warnings = defaults.warnings;

		Iterator<String> it = args.iterator();

		while (it.hasNext()) {
			String arg = it.next();
			boolean shouldRemove = false;

			if (arg.startsWith("--")) {
				arg = arg.substring(2);
				if (arg.equals("generics")) {
					shouldRemove = generics = true;
				}
				if (arg.equals("debug")) {
					shouldRemove = debug = true;
				}
				if (arg.equals("assembly")) {
					shouldRemove = assembly = true;
				}
				if (arg.contains("=")) {
					String[] temp = arg.split("=", 2);
					arg = temp[0];
					if (temp.length < 2) {
						Errors.fatal("Invalid use of option " + arg + ". Need value on other side of =.");
					}
					String value = temp[1];

					if (arg.equals("target")) {
						backend = Backend.valueOf(value.toUpperCase());
						shouldRemove = true;
					}
				}
			} else if (arg.startsWith("-")) {
				if (arg.contains("g")){
					shouldRemove = generics = true;
				}
				if (arg.contains("d")){
					shouldRemove = debug = true;
				}
				if (arg.contains("S")){
					shouldRemove = assembly = true;
				}
				if (arg.contains("w")){
					shouldRemove = warnings = true;
				}
			}

			if (shouldRemove)
				it.remove();
		}

		return new CompilerOptions(backend, debug, generics, assembly, warnings);
	}

	public static CompilerOptions getDefaults() {
		try {
		return (CompilerOptions) defaults.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return defaults;
		}
	}

	public void print() {
		System.out.println("Compiler options:");
		System.out.format("\tlanguage : %s\n", frontend);
		System.out.format("\ttarget   : %s\n", backend);
		System.out.format("\tdebug    : %b\n", debug);
		System.out.format("\tgenerics : %b\n", generics);
		System.out.format("\twarnings : %b\n", warnings);
		System.out.format("\tgenerate : %s\n", assembly ? "assembly" : "binary" );
		System.out.println();
	}

	public static void printFlags() {
		System.out.println("Options:");
		System.out.println("  -S, --assembly                   Compile only; do not assemble or link");
		System.out.println("  -d, --debug                      Produce debugging messages");
		System.out.println("  -g, --generics                   Enable generics support");
		System.out.println("  -w, --warnings                   Produce warnings for code");
		System.out.format( "  --target=[arm|jvm|x64]           Output target               (default %s)\n", defaults.backend);
		System.out.format( "  --language=[minijava|lisp|cyba]  Compile language selection  (default %s)\n", defaults.frontend);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
