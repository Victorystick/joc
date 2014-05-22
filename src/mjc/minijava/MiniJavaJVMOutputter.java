package mjc.minijava;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import mjc.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import underscore.*;

public class MiniJavaJVMOutputter extends MiniJavaBaseListener {
	private ParseTreeProperty<Symbol> ctxToSym;
	private ParseTreeProperty<Integer> ctxToSize;
	private String filename;
	private PrintStream out;
	private int label;
	private Deque<Integer> labels;
	private Mapper<TypeSymbol, String> typeMapper;
	private MethodSymbol currentMethod;

	public MiniJavaJVMOutputter(ParseTreeProperty<Symbol> ctxToSym,
								ParseTreeProperty<Integer> ctxToSize,
								String filename) {
		this.ctxToSym = ctxToSym;
		this.ctxToSize = ctxToSize;
		this.filename = filename;
		labels = new ArrayDeque<Integer>();

		typeMapper = new Mapper<TypeSymbol, String>() {
			public String map(TypeSymbol ts) {
				return getJasminType(ts);
			}
		};
	}

	private String getJasminType(TypeSymbol ts) {
		String type = ts.getType();
		if (type.equals("int")) {
			return "I";
		} else if (type.equals("int[]")) {
			return "[I";
		} else if (type.equals("boolean")) {
			return "I";
		}

		return "L" + type + ";";
	}

	public void enterMain(MiniJavaParser.MainContext ctx) {
		MainSymbol ms = (MainSymbol)ctxToSym.get(ctx);
		String name = ms.getIdentifier();
		label = 0;
		try {
			out = new PrintStream(new File("./" + name + ".j"));
		} catch (FileNotFoundException e) {
			System.err.println("Exception while attempting to open class file for writing");
			e.printStackTrace();
			System.exit(1);
		}

		createClass(name, "java/lang/Object");

		createInit("java/lang/Object");

		out.println(".method public static main([Ljava/lang/String;)V ");
		out.printf("\t.limit stack %d\n", ctxToSize.get(ctx));
		out.printf("\t.limit locals %d\n", ms.numLocals());
	}

	public void exitMain(MiniJavaParser.MainContext ctx) {
		out.println("\treturn");
		out.println(".end method\n");
	}

	private void createClass(String name, String supr) {
		out.format(".source %s\n", filename);
		out.format(".class public '%s'\n", name);
		out.format(".super '%s'\n", supr);
	}

	private void createInit(String supr) {
		out.println(".method public <init>()V");
		out.println("\taload_0");
		out.printf("\tinvokespecial '%s/<init>()V'\n", supr);
		out.println("\treturn");
		out.println(".end method\n");
	}

	public void enterClassDecl(MiniJavaParser.ClassDeclContext ctx) {
		ClassSymbol cs = (ClassSymbol)ctxToSym.get(ctx);

		String name = cs.getIdentifier();
		String superName = "java/lang/Object";

		if (cs.getSuper() != null) {
			superName = cs.getSuper().getType();
		}

		try {
		out = new PrintStream(new File("./" + name + ".j"));
		} catch (FileNotFoundException e) {
			System.err.println("Exception while attempting to open class file for writing");
			e.printStackTrace();
			System.exit(1);
		}
		label = 0;

		createClass(name, superName);

		for (MiniJavaParser.VarDeclContext vctx : ctx.varDecl()) {
			String var = vctx.identifier().getText();
			String type = getJasminType(cs.lookupVar(var).getType());
			out.format(".field private '%s' %s\n", var, type);
		}

		out.println();
		createInit(superName);
	}

	public void enterMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		MethodSymbol ms = (MethodSymbol)ctxToSym.get(ctx);
		currentMethod = ms;

		out.format(".method %s(%s)%s\n",
			ms.getIdentifier(),
			_.join(_.map(ms.paramTypes, typeMapper)),
			getJasminType(ms.returnType));
		out.format("\t.limit stack %d\n", ctxToSize.get(ctx));
		out.format("\t.limit locals %d\n", ms.numLocals());
	}

	public void exitMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		out.println(".end method");
		out.println();
	}

	public void exitPrint(MiniJavaParser.PrintContext ctx) {
		out.println("\tgetstatic java/lang/System/out Ljava/io/PrintStream;");
		codeGen(ctx.exp());

		TypeSymbol type = (TypeSymbol)ctxToSym.get(ctx.exp());

		String chr = "I";

		if (type == BuiltIn.BOOLEAN) {
			chr = "Z";
		}

		out.format("\tinvokevirtual java/io/PrintStream/println(%s)V\n", chr);
	}

	public void exitAssignment(MiniJavaParser.AssignmentContext ctx) {
		VarSymbol var = (VarSymbol)ctxToSym.get(ctx.identifier());

		if (var.getType() == BuiltIn.INT_ARR && ctx.exp().size() > 1) {
			codeGen(var, false); // load array
			codeGen(ctx.exp().get(0)); // index
			codeGen(ctx.exp().get(1)); // value
			out.println("\tiastore"); // store array
			return;
		}

		codeGen(ctx.exp().get(0)); // value
		codeGen(var, true); // store
	}

	public void exitReturnStmt(MiniJavaParser.ReturnStmtContext ctx) {
		String type = "a";

		if (currentMethod.returnType == BuiltIn.INT ||
				currentMethod.returnType == BuiltIn.BOOLEAN) {
			type = "i";
		}

		codeGen(ctx.exp());
		out.printf("\t%sreturn\n", type);
	}

	// if () {
	public void enterIf(MiniJavaParser.IfContext ctx) {
		out.println();

		int _else = -1, end = label++;

		labels.push(end); //Push label END

		if (ctx.elseStmt() != null) {
			_else = label++;
			labels.push(_else); //Push label _ELSE
			labels.push(end); //Push label END again (needs it twice).
		}

		codeGen(ctx.exp());

		out.format("\tifeq Label%d ; if {\n",
			ctx.elseStmt() == null ? end : _else);
	}

	// } else {
	public void enterElseStmt(MiniJavaParser.ElseStmtContext ctx) {
		out.format("\tgoto Label%d\n", labels.pop()); //pop Y from stack.
		out.format("Label%d:\n", labels.pop()); //pop X from stack
	}

	// } // if
	public void exitIf(MiniJavaParser.IfContext ctx) {
		out.format("Label%d: ; }\n", labels.pop()); //pop END from stack
	}

	public void enterWhile(MiniJavaParser.WhileContext ctx) {
		out.println();
		int test = label++;
		int end = label++;

		labels.push(end); //Push End to stack
		labels.push(test); //Push Test to stack

		out.format("Label%d: ; while {\n", test);
		codeGen(ctx.exp());
		out.format("\tifeq Label%d\n", end);
	}

	public void exitWhile(MiniJavaParser.WhileContext ctx) {
		out.format("\tgoto Label%d\n", labels.pop()); //pop Test from stack
		out.format("Label%d: ; }\n", labels.pop()); //pop End from stack
	}

	// x || x
	public void codeGen(MiniJavaParser.ExpContext exp) {
		List<MiniJavaParser.AndContext> list = exp.and();

		if (list.size() == 1) {
			codeGen(list.get(0));
			return;
		}

		int end = label++;
		codeGen(list.get(0));
		out.println("\tdup");
		out.format("\tifne Label%d\n", end);

		for (int i = 1; i < list.size(); i++) {
			codeGen(list.get(i));
			out.println("\tior");
			out.println("\tdup");
			out.format("\tifne Label%d\n", end);
		}

		out.format("Label%d:\n", end);
	}

	// x && x
	public void codeGen(MiniJavaParser.AndContext exp) {
		List<MiniJavaParser.ComparativeContext> list = exp.comparative();

		if (list.size() == 1) {
			codeGen(list.get(0));
			return;
		}

		int end = label++;
		codeGen(list.get(0));
		out.println("\tdup");
		out.format("\tifeq Label%d\n", end);

		for (int i = 1; i < list.size(); i++) {
			codeGen(list.get(i));
			out.println("\tiand");
			out.println("\tdup");
			out.format("\tifeq Label%d\n", end);
		}

		out.format("Label%d:\n", end);
	}

	//x != y
	public void codeGen(MiniJavaParser.ComparativeContext exp) {
		List<MiniJavaParser.RelationalContext> list = exp.relational();

		if (list.size() == 1) {
			codeGen(list.get(0));
			return;
		}

		String i_or_a = "i", op = "ne";

		TypeSymbol ts = (TypeSymbol)ctxToSym.get(list.get(0));

		if (ts instanceof ClassSymbol || ts.getIdentifier().equals("int[]")) {
			i_or_a = "a";
		}

		if (exp.eqOp().get(0).getText().equals("!=")) {
			op = "eq";
		}

		codeGen(list.get(0));
		codeGen(list.get(1));
		booleanBreak(String.format("if_%scmp%s", i_or_a, op));

		for (int i = 2; i < list.size(); i++) {
			codeGen(list.get(i));

			op = "ne";
			if (exp.eqOp().get(i-1).getText().equals("!=")) {
				op = "eq";
			}

			booleanBreak(String.format("if_icmp%s", op));
		}
	}

	//x < y
	public void codeGen(MiniJavaParser.RelationalContext exp) {
		if (exp.additative().size() == 1) {
			codeGen(exp.additative().get(0));
			return;
		}


		String op = "ge",
			opText = exp.cmpOp().getText();

		if (opText.equals(">")) {
			op = "le";
		} else if (opText.equals(">=")) {
			op = "lt";
		} else if (opText.equals("<=")) {
			op = "gt";
		}

		codeGen(exp.additative().get(0));
		codeGen(exp.additative().get(1));

		int fals = label++;
		int join = label++;

		out.format("\tif_icmp%s Label%d\n", op, fals);
		out.println("\ticonst_1");
		out.format("\tgoto Label%d\n", join);
		out.format("Label%d:\n", fals);
		out.println("\ticonst_0");
		out.format("Label%d:\n", join);

	}

	public void codeGen(MiniJavaParser.AdditativeContext exp) {
		if (exp.multiplicative().size() == 1) {
			codeGen(exp.multiplicative().get(0));
			return;
		}

		List<MiniJavaParser.MultiplicativeContext> list = exp.multiplicative();

		codeGen(list.get(0));

		for (int i = 1; i < list.size(); i++) {
			codeGen(list.get(i));

			if (exp.addOp().get(i - 1).getText().equals("+")) {
				out.println("\tiadd");
			} else {
				out.println("\tisub");
			}
		}
	}

	public void codeGen(MiniJavaParser.MultiplicativeContext exp) {
		if (exp.unary().size() == 1) {
			codeGen(exp.unary().get(0));
			return;
		}

		List<MiniJavaParser.UnaryContext> list = exp.unary();

		codeGen(list.get(0));

		for (int i = 1; i < list.size(); i++) {
			codeGen(list.get(i));
			out.println("\timul");
		}
	}

	public void codeGen(MiniJavaParser.UnaryContext exp) {
		if (exp instanceof MiniJavaParser.CallContext) {
			codeGen((MiniJavaParser.CallContext) exp);
			return;
		}

		if (exp instanceof MiniJavaParser.ArrayIndexContext) {
			codeGen((MiniJavaParser.ArrayIndexContext) exp);
			return;
		}

		if (exp instanceof MiniJavaParser.ArrayLengthContext) {
			codeGen((MiniJavaParser.ArrayLengthContext) exp);
			return;
		}

		if (exp instanceof MiniJavaParser.PrimContext) {
			codeGen(((MiniJavaParser.PrimContext) exp).primary());
		}
	}

	public void codeGen(MiniJavaParser.ArrayLengthContext ctx) {
		codeGen(ctx.unary());
		out.println("\tarraylength");
	}

	public void codeGen(MiniJavaParser.ArrayIndexContext ctx) {
		codeGen(ctx.unary());
		codeGen(ctx.exp());
		out.println("\tiaload");
	}

	public void codeGen(MiniJavaParser.CallContext ctx) {
		MethodSymbol ms = (MethodSymbol)ctxToSym.get(ctx);

		codeGen(ctx.unary());

		for (MiniJavaParser.ExpContext exp : ctx.exps().exp()) {
			codeGen(exp);
		}

		out.printf("\tinvokevirtual %s/%s(%s)%s\n",
			ms.getOwner().getIdentifier(),
			ms.getIdentifier(),
			_.join(_.map(ms.paramTypes, typeMapper)),
			getJasminType(ms.returnType));
	}

	public void codeGen(MiniJavaParser.PrimaryContext ctx) {
		if (ctx instanceof MiniJavaParser.IdContext) {
			codeGen((VarSymbol)ctxToSym.get(
				(MiniJavaParser.IdContext) ctx), false);
			return;
		}

		if (ctx instanceof MiniJavaParser.NewObjectContext) {
			codeGen((MiniJavaParser.NewObjectContext) ctx);
			return;
		}

		if (ctx instanceof MiniJavaParser.NewArrayContext) {
			codeGen((MiniJavaParser.NewArrayContext) ctx);
			return;
		}

		if (ctx instanceof MiniJavaParser.NotContext) {
			codeGen((MiniJavaParser.NotContext) ctx);
			return;
		}

		if (ctx instanceof MiniJavaParser.ParenExpContext) {
			codeGen(
				((MiniJavaParser.ParenExpContext) ctx)
					.exp());
			return;
		}

		if (ctx instanceof MiniJavaParser.IntegerContext) {
			out.printf("\tldc %s\n",
				((MiniJavaParser.IntegerContext) ctx).getText());
			return;
		}

		if (ctx instanceof MiniJavaParser.BooleanContext) {
			out.printf("\tldc %d\n",
				((MiniJavaParser.BooleanContext) ctx)
					.getText().equals("true") ? 1 : 0);
			return;
		}

		if (ctx instanceof MiniJavaParser.ThisContext) {
			out.println("\taload_0 ; this");
		}
	}

	public void codeGen(MiniJavaParser.NewObjectContext ctx) {
		ClassSymbol cs = (ClassSymbol)ctxToSym.get(ctx);
		out.printf("\tnew '%s'\n",
			cs.getIdentifier());
		out.println("\tdup");
		out.printf("\tinvokespecial %s/<init>()V\n",
			cs.getIdentifier());
	}

	public void codeGen(MiniJavaParser.NewArrayContext ctx) {
		codeGen(ctx.exp());
		out.println("\tnewarray int");
	}

	// !x
	public void codeGen(MiniJavaParser.NotContext ctx) {
		codeGen(ctx.unary());
		out.println("\ticonst_1");
		out.println("\tixor");
	}

	public void codeGen(VarSymbol var, boolean set) {
		Symbol owner = var.getOwner();

		String spc = " "; // variable > 3
		String type = "a"; // load reference

		String load = "load";
		String get = "get";

		if (set) {
			load = "store";
			get = "put";
		}

		if (var.getType() == BuiltIn.INT || var.getType() == BuiltIn.BOOLEAN) {
			type = "i"; // load integer
		}

		if (owner instanceof ClassSymbol) {
			out.printf("\taload_0 ; this\n");
			if (set) {
				out.println("\tswap");
			}
			out.printf("\t%sfield %s/%s %s ; this.%s\n",
				get,
				owner.getIdentifier(),
				var.getIdentifier(),
				getJasminType(var.getType()),
				var.getIdentifier());
		} else {
			int i = ((VarIndexer) owner).indexOf(var);

			if (i < 4) {
				spc = "_";
			}

			out.printf("\t%s%s%s%d ; %s\n",
				type, load, spc, i, var.getIdentifier());
		}
	}

	private void booleanBreak(String op) {
		int fail = label++;
		int end = label++;

		out.printf("\t%s Label%d\n", op, fail);
		out.println("\ticonst_1");
		out.printf("\tgoto Label%d\n", end);
		out.printf("Label%d:\n", fail);
		out.println("\ticonst_0");
		out.printf("Label%d:\n", end);
	}
}
