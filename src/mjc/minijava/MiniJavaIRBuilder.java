package mjc.minijava;

import mjc.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mjc.canon.Canon;
import mjc.frame.*;
import mjc.ir.*;
import mjc.asm.data.*;
import mjc.translate.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;


public class MiniJavaIRBuilder extends MiniJavaBaseListener {
	private static final String INIT_ARRAY = "_minijavalib_initarray";
	private static final String NULL_POINTER = "_minijavalib_nullpointertoobject";
	private static final String PRINT_BOOL = "_minijavalib_println_bool";
	private static final String PRINT_INT = "_minijavalib_println";

	ParseTreeProperty<Symbol> meta;
	private FrameFactory frameFactory;
	private Frame frame;
	private List<ClassData> classes;
	public List<Function> funcs;
	public List<DataInstruction> data;
	private Function mainFunc;
	private final int WORDSIZE;
	private Map<String, Access> idToAccess;
	private ClassData classData;
	private VarLookupper variablesInScope;
	private Map<String, Temp> localVariables;

	public MiniJavaIRBuilder(FrameFactory factory, ParseTreeProperty<Symbol> ctx2Syms) {
		WORDSIZE = factory.getWordSize();
		frameFactory = factory;
		meta = ctx2Syms;
		funcs = new ArrayList<Function>();
		classes = new ArrayList<ClassData>();
	}

	@Override
	public void enterProgram(MiniJavaParser.ProgramContext program) {
		for (MiniJavaParser.ClassDeclContext cls : program.classDecl()) {
			classes.add(ClassData.create((ClassSymbol)meta.get(cls)));
		}
	}

	@Override
	public void exitProgram(MiniJavaParser.ProgramContext program) {
		funcs.add(mainFunc);

		data = new ArrayList<DataInstruction>();

		for (ClassData cls : classes) {
			data.add(DataLabel.create(Label.create("_mthds_" + cls.name)));

			for (String method : cls.getMethodIndex().getList()) {
				data.add(Data.create(Label.create(method).getName()));
			}
		}
	}

	@Override
	public void enterClassDecl(MiniJavaParser.ClassDeclContext cls) {
		classData = ClassData.create((ClassSymbol)meta.get(cls));
	}

	@Override
	public void enterMain(MiniJavaParser.MainContext ctx) {
		MainSymbol main = (MainSymbol)meta.get(ctx);
		frame = frameFactory.create(Label.create("main"), 0);

		classes.add(ClassData.create(MainClassSymbol.getInstance(main)));

		idToAccess = new HashMap<String, Access>();
		variablesInScope = main;
		localVariables = new HashMap<String, Temp>();

		for (MiniJavaParser.VarDeclContext vctx : ctx.varDecl()) {
			String var = vctx.identifier().getText();
			localVariables.put(var, Temp.create(var));
		}

		mainFunc = new Function("main", true, frame, StmtSequence.create(
			ir(ctx.stmt()),
			Move.create(frame.getReturnReg(), Const.create(0))
		));
	}

	@Override
	public void enterMethodDecl(MiniJavaParser.MethodDeclContext methodCtx) {
		MethodSymbol method = (MethodSymbol)meta.get(methodCtx);
		frame = frameFactory.create(method.getCompleteIdentifier(), method.numParams() + 1);

		idToAccess = new HashMap<String, Access>();
		variablesInScope = method;
		localVariables = new HashMap<String, Temp>();

		int i = 0;

		idToAccess.put("this", frame.params.get(i));

		for (MiniJavaParser.ParamContext param : methodCtx.param()) {
			idToAccess.put(
				param.identifier().getText(),
				frame.params.get(++i));
		}

		for (VarSymbol varSym : method.indexedVars) {
			String var = varSym.getIdentifier();
			localVariables.put(var, Temp.create(var));
		}

		funcs.add(new Function(method.getIdentifier(), false, frame, StmtSequence.create(
			ir(methodCtx.stmt()),
			Move.create(frame.getReturnReg(), ir(methodCtx.returnStmt()))
		)));
	}

	// return exp
	public IRNode ir(MiniJavaParser.ReturnStmtContext ret) {
		return ir(ret.exp());
	}

	// stmt*
	public IRStatement ir(List<MiniJavaParser.StmtContext> stmts) {
		StmtSequence seq = new StmtSequence();

		for (MiniJavaParser.StmtContext stmt : stmts) {
			seq.then(ir(stmt));
		}

		return seq.done();
	}

	// stmt
	public IRStatement ir(MiniJavaParser.StmtContext stmt) {
		if (stmt instanceof MiniJavaParser.BlockContext) {
			return ir((MiniJavaParser.BlockContext) stmt);

		} else if (stmt instanceof MiniJavaParser.PrintContext) {
			return ir((MiniJavaParser.PrintContext) stmt);

		} else if (stmt instanceof MiniJavaParser.IfContext) {
			return ir((MiniJavaParser.IfContext) stmt);

		} else if (stmt instanceof MiniJavaParser.WhileContext) {
			return ir((MiniJavaParser.WhileContext) stmt);

		} else if (stmt instanceof MiniJavaParser.AssignmentContext) {
			return ir((MiniJavaParser.AssignmentContext) stmt);
		}

		return null;
	}

	// print(exp)
	public IRStatement ir(MiniJavaParser.PrintContext print) {
		TypeSymbol type = (TypeSymbol)meta.get(print.exp());

		String printfn = type == BuiltIn.BOOLEAN ?
			PRINT_BOOL : PRINT_INT;

		return SideEffect.create(
			Call.create(
				printfn,
				NodeList.create(ir(print.exp()))));
	}

	// { stmt* }
	public IRStatement ir(MiniJavaParser.BlockContext ctx) {
		return ir(ctx.stmt());
	}

	// while (exp) stmt
	public IRStatement ir(MiniJavaParser.WhileContext ctx) {
		IRNode condition = ir(ctx.exp());

		if (condition instanceof Const) {
			if (condition == Const.ZERO) {
				return null;
			} else {
				Errors.warn(ctx.start,
					"Infinite while-loop detected.");
			}
		}

		return While.create(condition, ir(ctx.stmt()));
	}

	/**
	 * Generates IR for an if statement.
	 */
	// if (exp) stmt elseStmt?
	public IRStatement ir(MiniJavaParser.IfContext ctx) {
		if (ctx.elseStmt() == null) {
			return If.create(
				ir(ctx.exp()),
				ir(ctx.stmt())
			);
		}

		return If.create(
			ir(ctx.exp()),
			ir(ctx.stmt()),
			ir(ctx.elseStmt().stmt())
		);
	}

	// x[y] = z
	public IRStatement ir(MiniJavaParser.AssignmentContext ctx) {
		String varName = ctx.identifier().getText();
		VarSymbol varSym = (VarSymbol) meta.get(ctx.identifier());

		Symbol varOwner = varSym.getOwner();

		Access a = idToAccess.get(varName);

		MiniJavaParser.ExpContext index = null, value = null;
		IRNode location = null;

		int valueIndex = 0;

		if (ctx.exp().size() == 2) {
			index = ctx.exp().get(0);
			valueIndex = 1;
		}

		value = ctx.exp().get(valueIndex);

		if (varOwner instanceof ClassSymbol) {
			// Nothing to do about it.
		} else if (varSym.getAssignments() == 1) {
			IRNode node = ir(value);

			if (node instanceof Const) {
				varSym.setConstValue((Const) node);
				return null;
			}
		}

		if (a != null) {
			location = a.value(frame.getFramePointer());
		} else {
			VarSymbol var = variablesInScope.lookupVar(varName);

			if (var.getOwner() instanceof ClassSymbol) {
				location = Memory.create(
					Binary.create(
						BinOp.PLUS,
						idToAccess.get("this").value(frame.getFramePointer()),
						classData.getOffset(varName)));
			} else {
				location = localVariables.get(varName);
			}
		}

		if (index != null) {
			location = Memory.create(
				Binary.create(
					BinOp.PLUS,
					location,
					Binary.create(
						BinOp.MULT,
						ir(index),
						4)));
		}

		return Move.create(location, ir(value));
	}

	// exp (x || y)
	public IRNode ir(MiniJavaParser.ExpContext exp) {
		List<MiniJavaParser.AndContext> list = exp.and();

		if (list.size() == 1) {
			return ir(list.get(0));
		}

		IRNode prim = ir(list.get(0));

		for (int i = 1; i < list.size(); i++) {
			prim = Binary.create(BinOp.OR, prim, ir(list.get(i)));
		}

		return prim;
	}

	// exp (x && y)
	public IRNode ir(MiniJavaParser.AndContext exp) {
		List<MiniJavaParser.ComparativeContext> list = exp.comparative();

		if (list.size() == 1) {
			return ir(list.get(0));
		}

		IRNode prim = ir(list.get(0));

		for (int i = 1; i < list.size(); i++) {
			prim = Binary.create(BinOp.AND, prim, ir(list.get(i)));
		}

		return prim;
	}

	// exp (x == y)
	public IRNode ir(MiniJavaParser.ComparativeContext exp) {
		if (exp.relational().size() == 1) {
			return ir(exp.relational().get(0));
		}

		List<MiniJavaParser.RelationalContext> list = exp.relational();

		IRNode prim = ir(list.get(0));

		BinOp op;

		for (int i = 1; i < list.size(); i++) {
			if (exp.eqOp().get(i - 1).getText().equals("==")) {
				op = BinOp.EQ;
			} else {
				op = BinOp.NEQ;
			}


			prim = Binary.create(op, prim, ir(list.get(i)));
		}

		return prim;
	}

	// exp (x < y)
	public IRNode ir(MiniJavaParser.RelationalContext exp) {
		if (exp.additative().size() == 1) {
			return ir(exp.additative().get(0));
		}

		return Binary.create(
			BinOp.get(exp.cmpOp().getText()),
			ir(exp.additative().get(0)),
			ir(exp.additative().get(1)));
	}

	// exp (x + y - z)
	public IRNode ir(MiniJavaParser.AdditativeContext exp) {
		if (exp.multiplicative().size() == 1) {
			return ir(exp.multiplicative().get(0));
		}

		List<MiniJavaParser.MultiplicativeContext> list = exp.multiplicative();

		IRNode prim = ir(list.get(0));

		BinOp op;
		for (int i = 1; i < list.size(); i++) {

			if (exp.addOp().get(i - 1).getText().equals("+")) {
				op = BinOp.PLUS;
			} else {
				op = BinOp.MINUS;
			}

			prim = Binary.create(op, prim, ir(list.get(i)));
		}

		return prim;
	}

	// exp (x * y)
	public IRNode ir(MiniJavaParser.MultiplicativeContext exp) {
		if (exp.unary().size() == 1) {
			return ir(exp.unary().get(0));
		}

		List<MiniJavaParser.UnaryContext> list = exp.unary();

		IRNode prim = ir(list.get(0));

		for (int i = 1; i < list.size(); i++) {
			prim = Binary.create(BinOp.MULT, prim, ir(list.get(i)));
		}

		return prim;
	}

	// unary
	public IRNode ir(MiniJavaParser.UnaryContext exp) {
		if (exp instanceof MiniJavaParser.CallContext) {
			return ir((MiniJavaParser.CallContext) exp);
		}

		if (exp instanceof MiniJavaParser.ArrayIndexContext) {
			return ir((MiniJavaParser.ArrayIndexContext) exp);
		}

		if (exp instanceof MiniJavaParser.ArrayLengthContext) {
			return ir((MiniJavaParser.ArrayLengthContext) exp);
		}

		if (exp instanceof MiniJavaParser.PrimContext) {
			return ir(((MiniJavaParser.PrimContext) exp).primary());
		}

		return null;
	}

	// unary.call(exps)
	public IRNode ir(MiniJavaParser.CallContext ctx) {
		MethodSymbol ms = (MethodSymbol)meta.get(ctx);

		NodeList args = NodeList.create();

		StmtSequence seq = StmtSequence.create();

		IRNode _this = ir(ctx.unary());

		boolean justCreated = false;

		if (_this instanceof StmtAndValue) {
			StmtAndValue sav = (StmtAndValue) _this;
			seq.then(sav.stmt);
			_this = sav.value;
			justCreated = true;
		}

		args.add(_this); // this

		for (MiniJavaParser.ExpContext exp : ctx.exps().exp()) {
			args.add(ir(exp));
		}


		if (!justCreated && !ctx.unary().getText().equals("this")) {
			seq.then(If.create(
					Binary.create(
						BinOp.EQ,
						_this,
						Const.ZERO),
					Jump.create(Label.create(NULL_POINTER))));
		}

		ClassSymbol owner = ms.getOwner();

		if (owner.isSubclassed()) {
			return StmtAndValue.create(
				seq,
				Call.create(
					Memory.create(
						Binary.create(
							BinOp.PLUS,
							Memory.create(_this),
							ms.getOffset() * WORDSIZE)),
					args));
		} else {
			return StmtAndValue.create(
				seq,
				Call.create(
					Label.create(
						ms.getCompleteIdentifier()).getName(),
					args));
		}
	}

	// arr[i]
	public IRNode ir(MiniJavaParser.ArrayIndexContext ctx) {
		return Memory.create(
			Binary.create(
				BinOp.PLUS,
				ir(ctx.unary()),
				Binary.create(
					BinOp.MULT,
					ir(ctx.exp()),
					Const.create(WORDSIZE))));
	}

	// arr.length
	public IRNode ir(MiniJavaParser.ArrayLengthContext ctx) {
		// &arr - 1 == arr.length
		return Memory.create(
			Binary.create(
				BinOp.MINUS,
				ir(ctx.unary()),
				Const.create(WORDSIZE)));
	}

	// primary
	public IRNode ir(MiniJavaParser.PrimaryContext ctx) {
		if (ctx instanceof MiniJavaParser.IdContext) {
			VarSymbol varSym = (VarSymbol) meta.get(ctx);

			Const val = varSym.getConstValue();

			if (val != null) {
				return val;
			}

			String varName = ctx.getText();

			Access a = idToAccess.get(varName);

			if (a != null) {
				return a.value(frame.getFramePointer());
			}

			if (localVariables.containsKey(varName)) {
				return localVariables.get(varName);
			}

			return Memory.create(
				Binary.create(
					BinOp.PLUS,
					frame.params.get(0).value(frame.getFramePointer()),
					Const.create(classData.getOffset(varName))));
		}

		if (ctx instanceof MiniJavaParser.NewObjectContext) {
			return ir((MiniJavaParser.NewObjectContext) ctx);
		}

		if (ctx instanceof MiniJavaParser.NewArrayContext) {
			return ir((MiniJavaParser.NewArrayContext) ctx);
		}

		if (ctx instanceof MiniJavaParser.NotContext) {
			return ir((MiniJavaParser.NotContext) ctx);
		}

		if (ctx instanceof MiniJavaParser.ParenExpContext) {
			// Reduced to exp
			return ir(((MiniJavaParser.ParenExpContext) ctx).exp());
		}

		if (ctx instanceof MiniJavaParser.IntegerContext) {
			return Const.create(Integer.parseInt(ctx.getText()));
		}

		if (ctx instanceof MiniJavaParser.BooleanContext) {
			return ctx.getText().equals("true") ? Const.ONE : Const.ZERO;
		}

		if (ctx instanceof MiniJavaParser.ThisContext) {
			return idToAccess.get("this").value(frame.getFramePointer());
		}

		return null;
	}

	// new Class()
	public IRNode ir(MiniJavaParser.NewObjectContext ctx) {
		ClassSymbol cs = (ClassSymbol) meta.get(ctx);
		return New.create(cs, cs.getIdentifier());
	}

	// new int[exp]
	public IRNode ir(MiniJavaParser.NewArrayContext ctx) {
		return Call.create(
			INIT_ARRAY,
			NodeList.create().then(ir(ctx.exp())));
	}

	public IRNode ir(MiniJavaParser.NotContext not) {
		return Not.create(ir(not.unary()));
	}
}
