package mjc.minijava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mjc.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import underscore.*;

public class MiniJavaSemanticAnalyzer extends MiniJavaBaseListener {
	private SymbolTable<TypeSymbol> symbolTable;
	ParseTreeProperty<Symbol> ctxToSym;

	private TypeSymbol currentClass = null;
	private MethodSymbol currentMethod = null;
	private VarLookupper variablesInScope;

	public MiniJavaSemanticAnalyzer(SymbolTable<TypeSymbol> syms,
			ParseTreeProperty<Symbol> ctxToSym) {
		symbolTable = syms;
		this.ctxToSym = ctxToSym;
	}

	private void fatal(Token t, String str) {
		Errors.fatal(t, String.format("In %s.%s: %s",
			currentClass.getIdentifier(),
			currentMethod != null ? currentMethod.getIdentifier() : "main",
			str));
	}

	@Override
	public void enterMain(MiniJavaParser.MainContext ctx) {
		MainSymbol main = (MainSymbol)ctxToSym.get(ctx);

		variablesInScope = main;
		currentClass = main;

		for (MiniJavaParser.StmtContext statement : ctx.stmt()) {
			verifyStatement(statement);
		}
	}

	public void enterMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		MethodSymbol method = (MethodSymbol)ctxToSym.get(ctx);

		variablesInScope = method;
		currentMethod = method;
		currentClass = method.getOwner();

		for (MiniJavaParser.StmtContext statement : ctx.stmt()) {
			verifyStatement(statement);
		}

		TypeSymbol sym = typeOfExp(ctx.returnStmt().exp());

		if (!sym.isCompatibleWith(method.returnType)) {
			fatal(ctx.returnStmt().start,
				String.format("attempted to return '%s' where '%s' was expected.",
					sym.getIdentifier(),
					method.returnType.getIdentifier()));
		}
	}

	public void verifyStatement(MiniJavaParser.StmtContext stmt) {
		if (stmt instanceof MiniJavaParser.PrintContext) {
			MiniJavaParser.PrintContext print = (MiniJavaParser.PrintContext)stmt;

			TypeSymbol type = typeOfExp(print.exp());

			if (type != BuiltIn.INT && type != BuiltIn.BOOLEAN) {
				fatal(print.start, "only integers and booleans may be printed.");
			}

			ctxToSym.put(print.exp(), type);

			return;
		}

		if (stmt instanceof MiniJavaParser.AssignmentContext) {
			MiniJavaParser.AssignmentContext assign = (MiniJavaParser.AssignmentContext)stmt;

			VarSymbol var = variablesInScope.lookupVar(assign.identifier().getText());

			ctxToSym.put(assign.identifier(), var);

			if (var == null) {
				fatal(assign.start,
					String.format("attempted to assign to unknown identifier '%s'.",
						assign.identifier().getText()));
			}

			TypeSymbol indexType = null, valueType = null;
			int valueIndex = 0;
			boolean indexAssignment = assign.exp().size() > 1;

			if (indexAssignment) {
				indexType = typeOfExp(assign.exp().get(0));
				valueIndex++;
			}

			valueType = typeOfExp(assign.exp().get(valueIndex));

			if (valueType == null) {
				fatal(assign.start,
					String.format("unknown value type in assignment to '%s'.",
						var.getIdentifier()));
			}

			if (indexAssignment) {
				if (var.getType() != BuiltIn.INT_ARR) {
					fatal(assign.start,
						String.format("attempting to assign to index of non-array type '%s'.",
							var.getIdentifier()));
				}

				if (indexType != BuiltIn.INT) {
					fatal(assign.start,
						String.format("attempting to assign to int[] '%s' using type %s as index.",
							var.getIdentifier(),
							indexType.getType()));
				}

				if (valueType != BuiltIn.INT) {
					fatal(assign.start,
						String.format("attempting to assign %s to '%s' type int[].",
							valueType.getType(),
							var.getIdentifier()));
				}
			} else {
				if (!valueType.isCompatibleWith(var.getType())) {
					fatal(assign.start,
						String.format("attempting to assign %s to '%s' with incompatible type %s.",
							valueType.getIdentifier(),
							var.getIdentifier(),
							var.getType().getType()));
				}
			}

			var.addAssignment();

			return;
		}

		if (stmt instanceof MiniJavaParser.BlockContext) {
			MiniJavaParser.BlockContext block = (MiniJavaParser.BlockContext)stmt;

			for (MiniJavaParser.StmtContext statement : block.stmt()) {
				verifyStatement(statement);
			}

			return;
		}

		if (stmt instanceof MiniJavaParser.IfContext) {
			MiniJavaParser.IfContext _if = (MiniJavaParser.IfContext)stmt;

			if (typeOfExp(_if.exp()) != BuiltIn.BOOLEAN) {
				fatal(_if.start, "If expression isn't boolean.");
			}

			verifyStatement(_if.stmt());

			if (_if.elseStmt() != null) {
				verifyStatement(_if.elseStmt().stmt());
			}

			return;
		}

		if (stmt instanceof MiniJavaParser.WhileContext) {
			MiniJavaParser.WhileContext _while = (MiniJavaParser.WhileContext)stmt;

			if (typeOfExp(_while.exp()) != BuiltIn.BOOLEAN) {
				fatal(_while.start, "While expression isn't boolean.");
			}

			verifyStatement(_while.stmt());

			return;
		}
	}

	// x || x
	public TypeSymbol typeOfExp(MiniJavaParser.ExpContext exp) {
		List<MiniJavaParser.AndContext> list = exp.and();

		if (list.size() == 1) {
			return typeOfExp(list.get(0));
		}

		for (MiniJavaParser.AndContext and : list) {
			TypeSymbol type = typeOfExp(and);

			if (type != BuiltIn.BOOLEAN) {
				fatal(exp.start, "attempted logical or with non-boolean.");
			}
		}

		return BuiltIn.BOOLEAN;
	}

	// x && x
	public TypeSymbol typeOfExp(MiniJavaParser.AndContext exp) {
		List<MiniJavaParser.ComparativeContext> list = exp.comparative();

		if (list.size() == 1) {
			return typeOfExp(list.get(0));
		}

		for (MiniJavaParser.ComparativeContext rel : list) {
			TypeSymbol type = typeOfExp(rel);

			if (type != BuiltIn.BOOLEAN) {
				fatal(exp.start, "attempted logical and with non-boolean.");
			}
		}

		return BuiltIn.BOOLEAN;
	}

	// a == b
	public TypeSymbol typeOfExp(MiniJavaParser.ComparativeContext exp) {
		List<MiniJavaParser.RelationalContext> list = exp.relational();

		if (list.size() == 1) {
			return typeOfExp(list.get(0));
		}

		TypeSymbol a = typeOfExp(list.get(0));
		TypeSymbol b = typeOfExp(list.get(1));

		// Store Type information.
		ctxToSym.put(list.get(0), a);

		if (!a.isCompatibleWith(b) && !b.isCompatibleWith(a)) {
			fatal(exp.start,
				String.format("attempted to compare incompatible types '%s' and '%s'",
					a.getType(),
					b.getType()));
		}

		for (int i = 2; i < list.size(); i++) {
			TypeSymbol type = typeOfExp(list.get(i));

			if (type != BuiltIn.BOOLEAN) {
				fatal(exp.start, "attempted to compare non-booleans.");
			}
		}

		return BuiltIn.BOOLEAN;
	}

	// x < x
	public TypeSymbol typeOfExp(MiniJavaParser.RelationalContext exp) {
		if (exp.additative().size() == 1) {
			return typeOfExp(exp.additative().get(0));
		}

		TypeSymbol left, right;

		left = typeOfExp(exp.additative().get(0));
		right = typeOfExp(exp.additative().get(1));

		if (left != BuiltIn.INT || right != BuiltIn.INT) {
			fatal(exp.start, "Attempted to compare non-integers.");
		}

		return BuiltIn.BOOLEAN;
	}

	public TypeSymbol typeOfExp(MiniJavaParser.AdditativeContext exp) {
		if (exp.multiplicative().size() == 1) {
			return typeOfExp(exp.multiplicative().get(0));
		}

		for (MiniJavaParser.MultiplicativeContext mult : exp.multiplicative()) {
			TypeSymbol type = typeOfExp(mult);

			if (type != BuiltIn.INT) {
				fatal(exp.start, "Attempted additative operator with non-integer.");
			}
		}

		return BuiltIn.INT;
	}

	public TypeSymbol typeOfExp(MiniJavaParser.MultiplicativeContext exp) {
		if (exp.unary().size() == 1) {
			return typeOfExp(exp.unary().get(0));
		}

		for (MiniJavaParser.UnaryContext unary : exp.unary()) {
			TypeSymbol type = typeOfExp(unary);

			if (type != BuiltIn.INT) {
				fatal(exp.start, "Attempted multiplicative operator with non-integer.");
			}
		}

		return BuiltIn.INT;
	}

	public TypeSymbol typeOfExp(MiniJavaParser.UnaryContext exp) {
		if (exp instanceof MiniJavaParser.CallContext) {
			return typeOfExp((MiniJavaParser.CallContext) exp);
		}

		if (exp instanceof MiniJavaParser.ArrayIndexContext) {
			return typeOfExp((MiniJavaParser.ArrayIndexContext) exp);
		}

		if (exp instanceof MiniJavaParser.ArrayLengthContext) {
			return typeOfExp((MiniJavaParser.ArrayLengthContext) exp);
		}

		if (exp instanceof MiniJavaParser.PrimContext) {
			return typeOfExp(((MiniJavaParser.PrimContext) exp).primary());
		}

		return null;
	}

	public TypeSymbol typeOfExp(MiniJavaParser.PrimaryContext exp) {
		if (exp instanceof MiniJavaParser.NotContext) {
			return typeOfExp((MiniJavaParser.NotContext) exp);
		}

		if (exp instanceof MiniJavaParser.ThisContext) {
			if (currentClass instanceof MainSymbol) {
				fatal(exp.start, "cannot use 'this' in static method");
			}

			return (ClassSymbol) currentClass;
		}

		if (exp instanceof MiniJavaParser.IdContext) {
			return typeOfExp((MiniJavaParser.IdContext) exp);
		}

		if (exp instanceof MiniJavaParser.BooleanContext) {
			return BuiltIn.BOOLEAN;
		}

		if (exp instanceof MiniJavaParser.IntegerContext) {
			try {
				Integer.parseInt(exp.getText());
				return BuiltIn.INT;
			} catch (NumberFormatException e) {
				fatal(exp.start,
					String.format("integer literal '%s' isn't representable by an int.",
						exp.getText()));
			}
		}

		if (exp instanceof MiniJavaParser.NewArrayContext) {
			return typeOfExp((MiniJavaParser.NewArrayContext) exp);
		}

		if (exp instanceof MiniJavaParser.NewObjectContext) {
			return typeOfExp((MiniJavaParser.NewObjectContext) exp);
		}

		if (exp instanceof MiniJavaParser.ParenExpContext) {
			return typeOfExp((MiniJavaParser.ParenExpContext) exp);
		}

		return null;
	}

	// ! Exp
	public TypeSymbol typeOfExp(MiniJavaParser.NotContext exp) {
		TypeSymbol type = typeOfExp(exp.unary());

		if (type != BuiltIn.BOOLEAN) {
			fatal(exp.start,
				String.format("cannot not non-boolean '%s'.",
					exp.getText()));
		}

		return BuiltIn.BOOLEAN;
	}

	// identifier
	public TypeSymbol typeOfExp(MiniJavaParser.IdContext exp) {
		VarSymbol sym = variablesInScope.lookupVar(exp.identifier().getText());

		if (sym == null) {
			fatal(exp.start,
				String.format("couldn't find symbol '%s'.",
					exp.identifier().getText()));
		}

		ctxToSym.put(exp, sym);

		return sym.getType();
	}

	// new int[]
	public TypeSymbol typeOfExp(MiniJavaParser.NewArrayContext exp) {
		if (typeOfExp(exp.exp()) != BuiltIn.INT) {
			fatal(exp.start,
				String.format("attempted to create int[] with non-integer length, type '%s'.",
					typeOfExp(exp.exp()).getType()));
		}

		return BuiltIn.INT_ARR;
	}

	// new Object()
	public TypeSymbol typeOfExp(MiniJavaParser.NewObjectContext ctx) {
		TypeSymbol ts = symbolTable.lookup(ctx.identifier().getText());
		ClassSymbol cs;

		if (ts == null) {
			fatal(ctx.start,
				String.format("attempted to instantiate unknown class '%s'.",
					ctx.identifier().getText()));
		}

		if ( !(ts instanceof ClassSymbol) ) {
			fatal(ctx.start,
				String.format("attempted to instantiate non-class '%s'.",
					ctx.identifier().getText()));
		}

		 // Could we use some common type here for both ClassSymbol and MainSymbol?
		cs = (ClassSymbol) ts;

		ctxToSym.put(ctx, cs);
		return cs;
	}

	// unary.call()
	public TypeSymbol typeOfExp(MiniJavaParser.CallContext exp) {
		ClassSymbol cs;
		MethodSymbol ms;
		Symbol s = typeOfExp(exp.unary());

		if (!(s instanceof ClassSymbol)) {
			fatal(exp.start,
				String.format("attempted to call method '%s' of non-object type '%s'.",
					exp.identifier().getText(),
					s.getIdentifier()));
		}

		cs = (ClassSymbol) s;

		// Any method in inheritance chain
		ms = cs.lookupMethod(exp.identifier().getText());

		if (ms == null) {
			fatal(exp.start,
				String.format("attempted to call undefined method '%s.%s'.",
					cs.getIdentifier(),
					exp.identifier().getText()));
		}

		if (exp.exps().exp().size() != ms.paramTypes.size()) {
			fatal(exp.identifier().start,
				String.format(
					"attempted to call '%s.%s' with wrong number of arguments.",
					cs.getIdentifier(),
					ms.getIdentifier()));
		}

		List<TypeSymbol> actualTypes = _.map(exp.exps().exp(), new Mapper<MiniJavaParser.ExpContext, TypeSymbol>() {
			public TypeSymbol map(MiniJavaParser.ExpContext exp) {
				return typeOfExp(exp);
			}
		});

		List<TypeSymbol> expectedTypes = ms.paramTypes;

		for (int i = 0; i < actualTypes.size(); i++) {
			if (!actualTypes.get(i).isCompatibleWith(expectedTypes.get(i))) {
				fatal(exp.identifier().start,
					String.format("attempted to call '%s.%s(%s)' with '(%s)'.",
						cs.getIdentifier(),
						ms.getIdentifier(),
						_.join(expectedTypes, ", "),
						_.join(actualTypes, ", ")));
			}
		}

		ctxToSym.put(exp, ms);

		return ms.returnType;
	}

	// unary.length
	public TypeSymbol typeOfExp(MiniJavaParser.ArrayLengthContext exp) {
		TypeSymbol ts = typeOfExp(exp.unary());

		if (ts != BuiltIn.INT_ARR) {
			fatal(exp.start,
				String.format("the 'length' property only accessible on int[], not %s",
					ts.getType()));
		}

		return BuiltIn.INT;
	}

	// unary[0]
	public TypeSymbol typeOfExp(MiniJavaParser.ArrayIndexContext exp) {
		if (exp.unary() instanceof MiniJavaParser.PrimContext) {
			MiniJavaParser.PrimContext prim = (MiniJavaParser.PrimContext)exp.unary();
			if (prim.primary() instanceof MiniJavaParser.NewArrayContext) {
				fatal(exp.start, "attempted to directly index new int[]!");
			}
		}

		if (typeOfExp(exp.unary()) != BuiltIn.INT_ARR) {
			fatal(exp.start,
				String.format("attempted to index %s",
					typeOfExp(exp.unary()).getType()));
		}

		if (typeOfExp(exp.exp()) != BuiltIn.INT) {
			fatal(exp.start,
				String.format("attempted to index int[] using %s",
					typeOfExp(exp.exp()).getType()));
		}

		return BuiltIn.INT;
	}

	// (exp)
	public TypeSymbol typeOfExp(MiniJavaParser.ParenExpContext exp) {
		return typeOfExp(exp.exp());
	}
}
