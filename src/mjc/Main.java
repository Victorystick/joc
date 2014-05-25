package mjc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mjc.arm.*;
import mjc.canon.*;
import mjc.ir.*;
import mjc.minijava.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.*;
import underscore.*;

public class Main {
	public static void main(String[] args) throws Exception {
		List<String> argList = new ArrayList<String>(Arrays.asList(args));

		CompilerOptions co = init(argList, CompilerOptions.Backend.JVM);
		Errors.setOptions(co);

		if (argList.isEmpty()) {
			System.out.println("No input files.");
			System.exit(0);
		}

		switch (co.backend) {
			case JVM: new JVMMain(argList.get(0), co); break;
			case ARM: new ARMMain(argList.get(0), co); break;
			case X64: new X64Main(argList.get(0), co); break;
			default:
				System.out.println("Not implemented yet. ;)");
				System.exit(1);
		}
	}

	public static CompilerOptions init(List<String> args, CompilerOptions.Backend def) {
		if (args.isEmpty()) {
			printUsage();
			System.exit(0);
		}

		CompilerOptions opts = CompilerOptions.parse(args);

		if (opts.debug)
			opts.print();

		return opts;
	}

	protected static void printUsage() {
		System.out.println(
			"joc MiniJava Compiler\n"+
			"Authors:\n" +
				"\tJohan Fogelström\n" +
				"\tOskar Segersvärd");

		System.out.println("\nUsage: joc filename [options]");
		CompilerOptions.printFlags();
	}

	protected String filename;
	protected ParseTree tree;
	protected ParseTreeWalker walker;

	protected SymbolTable<TypeSymbol> symbolTable;
	ParseTreeProperty<Symbol> ctxToSym;
	ParseTreeProperty<Integer> ctxToSize;

	public Main(String filenam, CompilerOptions opts) throws Exception {
		filename = filenam;

		ANTLRFileStream file = new ANTLRFileStream(filename);
		MiniJavaLexer lexer = new MiniJavaLexer(file);
		MiniJavaParser parser = new MiniJavaParser(new CommonTokenStream(lexer));

		parser.setErrorHandler(new DieErrorStrategy());

		tree = parser.program();
		walker = new ParseTreeWalker();

		symbolTable = new SymbolTable<TypeSymbol>();

		symbolTable.add(BuiltIn.INT);
		symbolTable.add(BuiltIn.BOOLEAN);
		symbolTable.add(BuiltIn.INT_ARR);

		ctxToSym = new ParseTreeProperty<Symbol>();
		ctxToSize = new ParseTreeProperty<Integer>();

		walker.walk(new MiniJavaSymbolizer(opts, symbolTable, ctxToSym), tree);

		walker.walk(new MiniJavaSemanticAnalyzer(symbolTable, ctxToSym), tree);
	}
}
