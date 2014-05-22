package mjc.minijava;

import java.util.List;
import mjc.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import underscore.*;

public class MiniJavaJVMStackalyzer extends MiniJavaBaseListener {
	private ParseTreeProperty<Symbol> ctxToSym;
	private int label;
	private MethodSymbol currentMethod;
	private ParseTreeProperty<Integer> ctxToSize;
	private MaxStack stack;

	public MiniJavaJVMStackalyzer(ParseTreeProperty<Symbol> ctxToSym, ParseTreeProperty<Integer> ctxToSize) {
		this.ctxToSym = ctxToSym;
		this.ctxToSize = ctxToSize;
	}

	public void enterMain(MiniJavaParser.MainContext ctx) {
		stack = new MaxStack();
	}

	public void exitMain(MiniJavaParser.MainContext ctx) {
		ctxToSize.put(ctx, stack.getMaximalSize());
	}

	public void enterMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		stack = new MaxStack();
	}

	public void exitMethodDecl(MiniJavaParser.MethodDeclContext ctx) {
		ctxToSize.put(ctx, stack.getMaximalSize());
	}

	public void exitPrint(MiniJavaParser.PrintContext ctx) {
		stack.push();
		analyzeExp(ctx.exp());
		stack.pop();
		stack.pop();
	}

	public void exitAssignment(MiniJavaParser.AssignmentContext ctx) {
		VarSymbol var = (VarSymbol)ctxToSym.get(ctx.identifier());

		if (var.getType() == BuiltIn.INT_ARR && ctx.exp().size() > 1) {
			stack.push(); // array
			analyzeExp(ctx.exp().get(0)); // index
			analyzeExp(ctx.exp().get(1)); // value
			stack.pop();
			stack.pop();
			stack.pop();
			return;
		}

		analyzeExp(ctx.exp().get(0)); // value
		store(var); // store
	}

	public void exitReturnStmt(MiniJavaParser.ReturnStmtContext ctx) {
		analyzeExp(ctx.exp());
		stack.pop();
	}

	public void enterIf(MiniJavaParser.IfContext ctx) {
		analyzeExp(ctx.exp());
		stack.pop();
	}

	public void enterWhile(MiniJavaParser.WhileContext ctx) {
		analyzeExp(ctx.exp());
		stack.pop();
	}

	// x || x
	public void analyzeExp(MiniJavaParser.ExpContext exp) {
		List<MiniJavaParser.AndContext> list = exp.and();

		if (list.size() == 1) {
			analyzeExp(list.get(0));
			return;
		}

		int end = label++;
		analyzeExp(list.get(0));
		stack.push();
		stack.pop();

		for (int i = 1; i < list.size(); i++) {
			analyzeExp(list.get(i));
			// stack.pop();
			// stack.push();
			stack.pop();
		}
	}

	// x && x
	public void analyzeExp(MiniJavaParser.AndContext exp) {
		List<MiniJavaParser.ComparativeContext> list = exp.comparative();

		if (list.size() == 1) {
			analyzeExp(list.get(0));
			return;
		}

		int end = label++;
		analyzeExp(list.get(0));
		stack.push();
		stack.pop();

		for (int i = 1; i < list.size(); i++) {
			analyzeExp(list.get(i));
			// stack.pop();
			// stack.push();
			stack.pop();
		}
	}

	// x == x
	public void analyzeExp(MiniJavaParser.ComparativeContext exp) {
		List<MiniJavaParser.RelationalContext> list = exp.relational();

		if (list.size() == 1) {
			analyzeExp(list.get(0));
			return;
		}

		int end = label++;
		analyzeExp(list.get(0));
		stack.push();
		stack.pop();

		for (int i = 1; i < list.size(); i++) {
			analyzeExp(list.get(i));
			// stack.pop();
			// stack.push();
			stack.pop();
		}
	}

	// x < x
	public void analyzeExp(MiniJavaParser.RelationalContext exp) {
		if (exp.additative().size() == 1) {
			analyzeExp(exp.additative().get(0));
			return;
		}

		analyzeExp(exp.additative().get(0));
		analyzeExp(exp.additative().get(1));
		// stack.pop();
		// stack.push();
		stack.push();
		stack.pop();
		// stack.pop();
		// stack.push();
	}

	// x + x - x
	public void analyzeExp(MiniJavaParser.AdditativeContext exp) {
		if (exp.multiplicative().size() == 1) {
			analyzeExp(exp.multiplicative().get(0));
			return;
		}

		List<MiniJavaParser.MultiplicativeContext> list = exp.multiplicative();

		analyzeExp(list.get(0));

		for (int i = 1; i < list.size(); i++) {
			analyzeExp(list.get(i));
			stack.pop();
		}
	}

	// x * x
	public void analyzeExp(MiniJavaParser.MultiplicativeContext exp) {
		if (exp.unary().size() == 1) {
			analyzeExp(exp.unary().get(0));
			return;
		}

		List<MiniJavaParser.UnaryContext> list = exp.unary();

		analyzeExp(list.get(0));

		for (int i = 1; i < list.size(); i++) {
			analyzeExp(list.get(i));
			stack.pop();
		}
	}

	public void analyzeExp(MiniJavaParser.UnaryContext exp) {
		if (exp instanceof MiniJavaParser.CallContext) {
			analyzeExp((MiniJavaParser.CallContext) exp);
			return;
		}

		if (exp instanceof MiniJavaParser.ArrayIndexContext) {
			analyzeExp((MiniJavaParser.ArrayIndexContext) exp);
			return;
		}

		if (exp instanceof MiniJavaParser.ArrayLengthContext) {
			analyzeExp((MiniJavaParser.ArrayLengthContext) exp);
			return;
		}

		if (exp instanceof MiniJavaParser.PrimContext) {
			analyzeExp(((MiniJavaParser.PrimContext) exp).primary());
		}
	}

	public void analyzeExp(MiniJavaParser.ArrayLengthContext ctx) {
		analyzeExp(ctx.unary());
		// stack.pop();
		// stack.push();
	}

	public void analyzeExp(MiniJavaParser.ArrayIndexContext ctx) {
		analyzeExp(ctx.unary());
		analyzeExp(ctx.exp());
		stack.pop();
		// stack.pop();
		// stack.push();
	}

	// x.call(x, x) -> x
	public void analyzeExp(MiniJavaParser.CallContext ctx) {
		int size = stack.size();

		analyzeExp(ctx.unary());

		for (MiniJavaParser.ExpContext exp : ctx.exps().exp()) {
			analyzeExp(exp);
		}

		// TO-POP:
		//   this
		//   num-args
		// RETURN
		//   something
		stack.pop(ctx.exps().exp().size());
	}

	public void analyzeExp(MiniJavaParser.PrimaryContext ctx) {
		if (ctx instanceof MiniJavaParser.NewObjectContext) {
			analyzeExp((MiniJavaParser.NewObjectContext) ctx);
			return;
		}

		if (ctx instanceof MiniJavaParser.NewArrayContext) {
			analyzeExp((MiniJavaParser.NewArrayContext) ctx);
			return;
		}

		if (ctx instanceof MiniJavaParser.NotContext) {
			analyzeExp((MiniJavaParser.NotContext) ctx);
			return;
		}

		if (ctx instanceof MiniJavaParser.ParenExpContext) {
			analyzeExp(
				((MiniJavaParser.ParenExpContext) ctx)
					.exp());
			return;
		}

		// get an identifier
		//     an integer
		//     a boolean
		//     this
		stack.push();
	}

	public void analyzeExp(MiniJavaParser.NewObjectContext ctx) {
		stack.push(); // new
		stack.push(); // dup
		stack.pop();  // <init>
	}

	public void analyzeExp(MiniJavaParser.NewArrayContext ctx) {
		analyzeExp(ctx.exp());
		stack.pop();
		stack.push(); // newarray
	}

	public void analyzeExp(MiniJavaParser.NotContext ctx) {
		analyzeExp(ctx.unary());
		stack.push();
		stack.pop();
	}

	public void store(VarSymbol var) {
		Symbol owner = var.getOwner();

		if (owner instanceof ClassSymbol) {
			stack.push(); // this
			stack.pop(); // pop this
			stack.pop(); // pop value
		} else {
			stack.pop(); // set
		}
	}

	private class MaxStack {
		int size = 0;
		int maximalSize = 0;

		public void push() {
			size++;
			if (size > maximalSize) maximalSize = size;
		}

		public void pop() {
			size--;
		}

		public void pop(int n) {
			size -= n;
		}

		public int size() {
			return size;
		}

		public int getMaximalSize() {
			return maximalSize;
		}
	}

}
