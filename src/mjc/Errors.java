package mjc;

import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.LinkedList;

public class Errors {
	private static final String ERROR_LINE = "Line %d, column %d:\n\t%s\n";

	private static List<String> warnings = new LinkedList<String>();
	private static List<String> errors = new LinkedList<String>();
	private static CompilerOptions options = CompilerOptions.getDefaults();
	
	public static void setOptions(CompilerOptions co) {
		options = co;
	}
	
	public static void debug(String s) {
		if (options.debug) {
			System.out.println(s);
		}
	}

	public static boolean fine() {
		return errors.isEmpty() && warnings.isEmpty();
	}

	public static boolean errorsExists() {
		return !errors.isEmpty();
	}

	public static boolean warningsExists() {
		return !warnings.isEmpty();
	}
	
	public static void print() {
		if (options.warnings) {
			printWarnings();
		}
		
		printErrors();
	}

	public static void printErrors() {
		if (errorsExists()) {
			System.err.println("Errors:");
			for (String error : errors) {
				System.err.println(error);
			}
			System.err.println();
		}
	}

	public static void printWarnings() {
		if (warningsExists()) {
			System.err.println("Warnings:");
			for (String warning : warnings) {
				System.err.println(warning);
			}
			System.err.println();
		}
	}

	public static void warn(String s) {
		warnings.add(s);
		//throw new RuntimeException("Where did that come from?").printStackTrace();
	}

	public static void warn(Token t, String s) {
		warn(String.format(ERROR_LINE, t.getLine(), t.getCharPositionInLine(), s));
	}

	public static void error(String s) {
		errors.add(s);
		// new RuntimeException("Where did that come from?").printStackTrace();
	}

	public static void error(Token t, String s) {
		error(String.format(ERROR_LINE, t.getLine(), t.getCharPositionInLine(), s));

	}

	public static void error(Token t, Exception e) {
		error(t, e.getMessage());
	}

	public static void fatal(String s) {
		error(s);
		print();
		crash();
	}

	public static void fatal(Token t, Exception e) {
		error(t, e);
		print();
		crash();
	}

	public static void fatal(Token t, String s) {
		error(t, s);
		print();
		crash();
	}
	
	public static void crash() {
		System.exit(1);
	}
}
