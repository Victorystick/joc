package mjc.minijava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mjc.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;


public class MiniJavaSymbolizer extends MiniJavaBaseListener {
	private final CompilerOptions options;
	private final ParseTreeProperty<Symbol> meta;
	private final SymbolTable<TypeSymbol> symbolTable;
	private final Validator validate;

	public MiniJavaSymbolizer(
			CompilerOptions opts,
			SymbolTable<TypeSymbol> syms,
			ParseTreeProperty<Symbol> ctxToSym) {
		options = opts;
		symbolTable = syms;
		meta = ctxToSym;
		validate = new Validator(opts);
	}

	@Override
	public void enterProgram(MiniJavaParser.ProgramContext ctx) {
		validate.main(ctx.main());
		List<MiniJavaParser.IdentifierContext> ids = ctx.main().identifier();

		String className = ids.get(0).getText();

		MainSymbol ms = new MainSymbol(className);

		symbolTable.add(MainClassSymbol.getInstance(ms));
		meta.put(ctx.main(), ms);

		ClassSymbol cs;
		TypeSymbol s;
		for (MiniJavaParser.ClassDeclContext cc : ctx.classDecl()) {
			validate.generics(cc);

			className = cc.identifier().getText();

			s = symbolTable.lookup(className);

			if (s != null) {
				Errors.fatal(cc.start,
					String.format(
						"Attempted to redefine class '%s'.",
						className));
			}

			cs = new ClassSymbol(className);

			symbolTable.add(cs);

			meta.put(cc, cs);
		}
	}

	@Override
	public void enterClassDecl(MiniJavaParser.ClassDeclContext ctx) {
		String type, name = ctx.identifier().getText();
		String supr;

		ClassSymbol cls = (ClassSymbol)meta.get(ctx);

		if (ctx.extension() != null) {
			supr = ctx.extension().identifier().getText();

			TypeSymbol ts = symbolTable.lookup(supr);

			if (ts == null) {
				Errors.fatal(ctx.extension().start,
					String.format("Class '%s' attempted to extend undefined type '%s'.",
						cls.getIdentifier(),
						supr));
			}

			if (!(ts instanceof ClassSymbol) && !(ts instanceof MainSymbol)) {
				Errors.fatal(ctx.extension().start,
					String.format("Class '%s' attempted to extend non-class '%s'.",
						cls.getIdentifier(),
						ts.getType()));
			}

			if (ts instanceof ClassSymbol) {
				boolean ok = cls.setSuper((ClassSymbol)ts);

				if (!ok) {
					Errors.fatal(ctx.extension().start,
						String.format("Having class '%s' extend '%s' causes cyclic inheritance.",
							cls.getIdentifier(),
							supr));
				}
			}
		}

		for (MiniJavaParser.VarDeclContext field : ctx.varDecl()) {
			validate.genericField(field);

			name = field.identifier().getText();
			type = field.type().getText();

			TypeSymbol typSym = symbolTable.lookup(type);

			if (typSym == null) {
				Errors.fatal(field.type().start,
					"Type '" + type + "' is never defined." );
			}

			// doesn't have field
			VarSymbol s = cls.lookupVar(name);

			if (s != null) {
				String inSupr = "";

				if (s.getOwner() != cls) {
					inSupr = "super-";
				}

				Errors.fatal(field.identifier().start,
					String.format(
						"field '%s.%s' is already defined in %sclass '%s'.",
						cls.getIdentifier(),
						name,
						inSupr,
						s.getOwner().getIdentifier()));
			}

			cls.addField(new VarSymbol(name, typSym, cls));
		}

		MethodSymbol ms, otherMs;
		for (MiniJavaParser.MethodDeclContext method : ctx.methodDecl()) {
			name = method.identifier().getText();
			type = method.type().getText();

			TypeSymbol returnType = symbolTable.lookup(type);

			if (returnType == null) {
				Errors.fatal(method.type().start,
					"Return type '" + type + "' is never defined." );
			}

			List<VarSymbol> paramTypes = new ArrayList<VarSymbol>();

			for (MiniJavaParser.ParamContext param : method.param()) {
				String paramType = param.type().getText();

				TypeSymbol typSym = symbolTable.lookup(paramType);

				if (typSym == null) {
					Errors.fatal(param.type().start,
						String.format(
							"Type of argument %s: '%s' is never defined.",
							param.identifier().getText(),
							paramTypes));
				}

				paramTypes.add(new VarSymbol(
					param.identifier().getText(),
					typSym));
			}

			ms = new MethodSymbol(name, returnType, paramTypes, cls, symbolTable);

			// doesn't have local and
			// overrides correct type signature
			otherMs = cls.lookupMethod(name);

			if (otherMs != null) {
				if (otherMs.getOwner() == cls) {
					Errors.fatal(method.identifier().start,
						String.format(
							"method '%s.%s' already defined.",
							cls.getIdentifier(),
							name));
				}

				if (!ms.typeSignatureMatches(otherMs)) {
					Errors.fatal(method.identifier().start,
						String.format(
							"method '%s.%s' does not match type signature of '%s.%s'.",
							cls.getIdentifier(),
							name,
							otherMs.getOwner().getIdentifier(),
							otherMs.getIdentifier()));
				}
			}

			VarSymbol var;
			int i = 0;
			for (VarSymbol param : paramTypes) {
				// Is the parameter name already defined?
				var = ms.localVar(param.getIdentifier());
				if (var != null) {
					Errors.fatal(method.param().get(i).identifier().start,
						String.format(
							"Variable %s is already defined in %s%s.",
							param.getIdentifier(),
							ms.getIdentifier(),
							ms.toString()));
				}

				// All arguments are initially assigned values,
				// do not make it constant.
				param.addAssignment();

				ms.add(param);
				param.setOwner(ms);

				++i;
			}

			cls.addMethod(ms);
			meta.put(method, ms);
		}
	}

	@Override
	public void enterMain(MiniJavaParser.MainContext ctx) {
		VarSymbol var;
		TypeSymbol typSym;
		String type, name = ctx.identifier().get(0).getText();

		MainSymbol m = (MainSymbol)meta.get(ctx);

		// Add name of arguments array to known symbols.
		// String args = ctx.identifier().get(1).getText();
		// typSym = BuiltIn.STRING_ARR;
		// m.add(new VarSymbol(args, typSym, m));

		for (MiniJavaParser.VarDeclContext field : ctx.varDecl()) {
			name = field.identifier().getText();
			type = field.type().getText();

			typSym = symbolTable.lookup(type);

			if (typSym == null) {
				Errors.fatal(field.type().start,
					"Type " + type + " is never defined." );
			}

			var = m.lookupVar(name);

			if (var != null) {
				Errors.fatal(field.identifier().start,
					String.format(
						"Variable %s is already defined in main().",
						name));
			}

			m.add(new VarSymbol(name, typSym, m));
		}
	}

	@Override
	public void enterMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		VarSymbol var;
		TypeSymbol typSym;
		String type, name = ctx.identifier().getText();

		MethodSymbol m = (MethodSymbol)meta.get(ctx);

		for (MiniJavaParser.VarDeclContext field : ctx.varDecl()) {
			name = field.identifier().getText();
			type = field.type().getText();

			typSym = symbolTable.lookup(type);

			if (typSym == null) {
				Errors.fatal(field.type().start,
					"Type " + type + " is never defined." );
			}

			var = m.localVar(name);

			if (var != null) {
				Errors.fatal(field.identifier().start,
					String.format(
						"Variable %s is already defined in %s%s.",
						name,
						m.getIdentifier(),
						m.toString()));
			}

			m.add(new VarSymbol(name, typSym, m));
		}
	}
}
