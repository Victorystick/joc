package mjc.asm;

import mjc.ir.*;
import mjc.frame.*;
import mjc.Errors;
import java.util.List;
import java.util.ArrayList;
import mjc.asm.data.*;

public class MunchCoder implements Coder {
	InstructionSet is;
	InstructionSequence ins;
	InstructionSequence curr;
	StringBuilder data;
	Frame frame;

	public MunchCoder(InstructionSet i) {
		is = i;
		ins = new InstructionSequence();
	}

	public InstructionSequence doneCode() {
		return ins;
	}

	public String doneData() {
		return data.toString();
	}

	/**
	 * Appends an Instruction to the current InstructionSequence.
	 */
	public void emit(Instruction i) {
		curr.then(i);
	}

	//Pure emit.
	//Does not unpack a InstructionSequence when appending it.
	// This allows atomic operations in the allocator later.
	public void emit_(Instruction i) {
		curr.then_(i);
	}

	public void emitData(String d) {
		emitData(d, true);
	}

	public void emitData(String d, boolean indent) {
		data.append(indent ? '\t': "");
		data.append(d);
		data.append('\n');
	}

	public InstructionSequence generateCode(Frame f, IRStatement stm) {

		frame = f;
		curr = new InstructionSequence();
		if (stm instanceof StmtSequence) {
			for (IRStatement stmt : (StmtSequence) stm) {
				munchStatement(stmt);
			}
		} else {
			munchStatement(stm);
		}
		curr = f.scaffold(curr);
		ins.then(curr);
		return curr;
	}

	private void munchStatement(IRStatement s) {
		try {
			if (s instanceof Label) {
				munchLabel((Label) s);
			} else if (s instanceof Move) {
				Move move = (Move) s;
				munchMove(move.dest, move.value);
			} else if (s instanceof Call) {
				Call call = (Call) s;
				munchCall(call.func, call.args);
			} else if (s instanceof Jump) {
				Jump jmp = (Jump) s;
				munchJump(jmp.target);
			} else if (s instanceof CJump) {
				CJump cjmp = (CJump) s;
				munchCJump(cjmp.cmp, cjmp.left, cjmp.right, cjmp.tru);
			} else if (s instanceof SideEffect) {
				SideEffect side = (SideEffect) s;
				munchSideEffect(side.node);
			}
		} catch (NullPointerException e) {
			System.out.println(e);
			System.exit(1);
		}
	}

	private void munchLabel(Label l) {
		emit(is.label(l));
	}

	private void munchMove(IRNode dest, IRNode value) {
		if (dest instanceof Memory) {
			munchMove((Memory) dest, value);
		} else if (dest instanceof Temp) {
			munchMove((Temp) dest, value);
		} else {
			Errors.fatal("Wat happened here? dest is neither Memory nor Temp.");
		}
	}

	private void munchMove(Memory dest, IRNode source) {
		if (dest.value instanceof Binary) {
			Binary bin = (Binary) dest.value;

			if (bin.op == BinOp.PLUS && bin.right instanceof Const) {

				Temp base = munchExp(bin.left);
				Temp val = munchExp(source);
				emit(is.comment(String.format("Mem[%s + #%s] = %s", base, bin.right, val)));
				emit(is.store(base, (Const) bin.right, val));
			} else  {
				Temp val = munchExp(source);
				Temp mem = munchExp(bin);
				emit(is.comment(String.format("Mem[%s] = %s", mem, val)));
				emit(is.store(mem, val));
			}
		} else if (dest.value instanceof Const) {
			// Const c = (Const) dest.value;
			// emit(is.store)
			Errors.error("Constant memory destination!");
		} else {
			Temp mem = munchExp(dest.value);

			if (source instanceof Memory) {
				Temp src = munchExp(((Memory)source).value);
				emit(is.comment(String.format("Mem[%s] = Mem[%s]", mem, src)));
				emit(is.mmove(mem, src));
			} else {
				Temp res = munchExp(source);
				emit(is.comment(String.format("Mem[%s] = %s", mem, res)));
				emit(is.store(mem, res));
			}
		}
	}

	private void munchMove(Temp dest, IRNode source) {
		if (source instanceof Const) {
			emit(is.comment("Move statement with constant to " + dest + "."));
			emit(is.move(dest, (Const) source));

		} else if (source instanceof Binary) {
			emit(is.comment("Move statement with binary source."));
			emit(is.comment(dest.toString() + " = " + source.toString()));
			munchMove(dest, (Binary) source);

		} else if (source instanceof Name) {
			emit(is.laddr(dest, ((Name) source).label));

		} else if (source instanceof Memory) {
		 	munchMove(dest, (Memory) source);
		} else {
			emit(is.comment("Move statement with expression source."));
			emit(is.comment(dest.toString() + " = " + source.toString()));
			Temp res = munchExp(source);
			emit(is.move(dest, res));
		}
	}

	private void munchMove(Temp dest, Memory mem) {
		emit(is.comment("loaded into " + dest +	" from " + mem.value));
		if (mem.value instanceof Binary) {
			Binary bin = (Binary) mem.value;

			if (bin.op != BinOp.PLUS) {
				Temp t = munchExp(bin);
				emit(is.load(dest, t));
				return;
			}

			if (!(bin.left instanceof Temp)) {
				Errors.fatal("Move to temp from non-temp address.");
			} else {
				if (bin.right instanceof Temp) {
					// emit(is.comment("loaded into " + dest +
					// 	" from " + mem.value));
					emit(is.load(dest, (Temp) bin.left, (Temp) bin.right));
				} else if (bin.right instanceof Const) {
					// emit(is.comment("loaded into " + dest +
					// 	" from " + mem.value));
					emit(is.load(dest, (Temp) bin.left, (Const) bin.right));
				} else {
					Errors.fatal("Move to temp from non-temp address.");
				}
			}

		} else if (mem.value instanceof Temp) {
			// emit(is.comment("loaded into " + dest +
			// 	" from " + mem.value));
			emit(is.load(dest, (Temp) mem.value));
		} else {
			Errors.fatal("Move to temp from non-temp address.");
		}
	}

	private void munchMove(Temp dest, Binary source) {
		if (source.right instanceof Const) {
			Const c = (Const) source.right;
			Temp main = (Temp) source.left;

			switch (source.op) {
				case PLUS:
					emit(is.add(dest, main, c)); break;
				case MINUS:
					emit(is.sub(dest, main, c)); break;
				case MULT:
					emit(is.mul(dest, main, c)); break;
				case AND:
					emit(is.and(dest, main, c)); break;
				case OR:
					emit(is.or(dest, main, c)); break;
				case EQ:
					emit(is.cmpeq(dest, main, c)); break;
				case NEQ:
					emit(is.cmpneq(dest, main, c)); break;
				case LT:
					emit(is.cmplt(dest, main, c)); break;
				case LEQ:
					emit(is.cmpleq(dest, main, c)); break;
				case GT:
					emit(is.cmpgt(dest, main, c)); break;
				case GEQ:
					emit(is.cmpgeq(dest, main, c)); break;
				default:
					Errors.warn(
						"munchMove(Temp,Binary(Temp, Const)) doesn't handle " +
						source.op + " natively.");

					Temp t = munchExp(source);
					emit(is.move(dest, t));
			}
			return;
		}

		if (source.right instanceof Temp) {
			Temp right = (Temp) source.right;

			if (source.left instanceof Temp) {
				Temp left = (Temp) source.left;

				switch (source.op) {
					case PLUS:
						emit(is.add(dest, left, right)); break;
					case MINUS:
						emit(is.sub(dest, left, right)); break;
					case MULT:
						emit(is.mul(dest, left, right)); break;
					case AND:
						emit(is.and(dest, left, right)); break;
					case OR:
						emit(is.or(dest, left, right)); break;
					case EQ:
						emit(is.cmpeq(dest, left, right)); break;
					case NEQ:
						emit(is.cmpneq(dest, left, right)); break;
					case LT:
						emit(is.cmplt(dest, left, right)); break;
					case LEQ:
						emit(is.cmpleq(dest, left, right)); break;
					case GT:
						emit(is.cmpgt(dest, left, right)); break;
					case GEQ:
						emit(is.cmpgeq(dest, left, right)); break;
					default:
						Errors.warn(
							"munchMove(Temp,Binary(Temp, Temp)) doesn't handle " +
							source.op + " natively.");

						Temp t = munchExp(source);
						emit(is.move(dest, t));
				}
				return;
			}

			if (source.left instanceof Const && source.op == BinOp.MINUS) {
				Const c = (Const) source.left;

				emit(is.subr(dest, right, c));
				return;
			}
		}

		emit(is.comment("Move statement with expression source."));
		emit(is.comment(dest.toString() + " = " + source.toString()));
		Temp res = munchExp(source);
		emit(is.move(dest, res));
	}

	private void munchCall(IRNode func, NodeList args) {
		emit(is.comment("Call statement."));

		// Load args into correct argument registers.
		// CallInstructionSequence cis = new CallInstructionSequence();

		// //Save reference to curr.
		// InstructionSequence temp = curr;
		// curr = cis; //Bait-and-switch

		emit(is.comment("Expression call"));

		// frame.saveRegs(curr);

		/*
			I suppose you all deserve an explanation.
			I am temporarily replacing curr,
			because I wish to package all instructions
			related to the call in one neat package
			that I can control the use and defs of.
		*/

		List<Temp> argTemps = munchArgs(args);

		if (func instanceof Name) {
			emit(is.call((Name) func, argTemps, args.size()));
		} else {
			Temp func_temp = munchExp(func);
			emit(is.call(func_temp, argTemps, args.size()));
		}

		// curr = temp;
		// It's time to quietly change back.
		// I hope no one noticed...

		// curr.then_(cis);
	}

	private void munchJump(Name target) {
		emit(is.jump(target.label));
	}

	private void munchCJump(BinOp cmp, IRNode left, IRNode right, Label dest) {
		if (right instanceof Const) munchCJump(cmp, left, (Const) right, dest);
		else {
			Temp lt = munchExp(left);
			Temp rt = munchExp(right);
			switch (cmp) {
				case LT:
					emit(is.jumplt(dest, lt, rt)); break;

				case LEQ:
					emit(is.jumpleq(dest, lt, rt)); break;

				case GT:
					emit(is.jumpgt(dest, lt, rt)); break;

				case GEQ:
					emit(is.jumpgeq(dest, lt, rt)); break;

				case EQ:
					emit(is.jumpeq(dest, lt, rt)); break;

				case NEQ:
					emit(is.jumpneq(dest, lt, rt)); break;
			}
		}
	}

	private void munchCJump(BinOp cmp, IRNode left, Const right, Label dest) {
		Temp lt = munchExp(left);
		Const rt = (Const) right;
		switch (cmp) {
			case LT:
				emit(is.jumplt(dest, lt, rt)); break;

			case LEQ:
				emit(is.jumpleq(dest, lt, rt)); break;

			case GT:
				emit(is.jumpgt(dest, lt, rt)); break;

			case GEQ:
				emit(is.jumpgeq(dest, lt, rt)); break;

			case EQ:
				emit(is.jumpeq(dest, lt, rt)); break;

			case NEQ:
				emit(is.jumpneq(dest, lt, rt)); break;
		}
	}

	private void munchSideEffect(IRNode s) {
		munchExp(s);
	}

	/*
		Expression munchers below!
		BE AWARE! DANGER!
	*/

	private Temp munchExp(IRNode exp) {
		if (exp instanceof Const) return munchExp((Const) exp);
		if (exp instanceof Temp) return munchExp((Temp) exp);
		if (exp instanceof Binary) return munchExp((Binary) exp);
		if (exp instanceof Memory) return munchExp((Memory) exp);
		if (exp instanceof Name) return munchExp((Name) exp);
		if (exp instanceof Call) return munchExp((Call) exp);

		Errors.fatal(
			String.format(
				"munchExp got unknown IRNode: %s", exp));
		return null;
	}

	private Temp munchExp(Const c) {
		Temp t = Temp.create();
		emit(is.comment(t + " = Loading Constant " + c.value + "."));
		emit(is.move(t, c));
		return t;
	}

	private Temp munchExp(Temp t) {
		return t;
	}

	//private Temp munchExp(Not t) {
		// emit(is.comment("Not operation."));
		// Temp res = munchExp(t.value);
		// emit(is.not(res));
		// return res;
	//}

	private Temp munchExp(Name name) {
		Temp res = Temp.create();
		emit(is.laddr(res, name.label));
		return res;
	}

	private Temp munchExp(Binary exp) {
		Temp res = Temp.create();
		Temp lt;
		Temp rt;

		if (exp.op == BinOp.MINUS) {
			emit(is.comment("Subtraction expression."));
			if (exp.right instanceof Const) {
				lt = munchExp(exp.left);
				emit(is.sub(res, lt, (Const) exp.right));
				return res;
			} else if (exp.left instanceof Const) {
				rt = munchExp(exp.right);
				emit(is.subr(res, rt, (Const) exp.left));
				return res;
			} else {
				res = Temp.create();
				lt = munchExp(exp.left);
				rt = munchExp(exp.right);
				emit(is.sub(res, lt, rt));
				return res;
			}
		}

		lt = munchExp(exp.left);

		switch(exp.op) {
			case PLUS:
				emit(is.comment("Addition expression."));
				if (exp.right instanceof Const) {
					emit(is.add(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					emit(is.add(res, lt, rt));
					return res;
				}

			case MULT:
				emit(is.comment("Multiplication expression."));
				if (exp.right instanceof Const) {
					emit(is.mul(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					emit(is.mul(res, lt, rt));
					return res;
				}

			case AND:
				emit(is.comment("&& expression."));
				if (exp.right instanceof Const) {
					emit(is.and(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					emit(is.and(res, lt, rt));
					return res;
				}

			case OR:
				emit(is.comment("|| expression."));
				if (exp.right instanceof Const) {
					emit(is.or(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					emit(is.or(res, lt, rt));
					return res;
				}

			case LT:
				emit(is.comment("< expression."));
				if (exp.right instanceof Const) {
					emit(is.cmplt(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					emit(is.cmplt(res, lt, rt));
					return res;
				}

			case GT:
				emit(is.comment("> expression."));
				if (exp.right instanceof Const) {
					emit(is.cmpgt(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					emit(is.cmpgt(res, lt, rt));
					return res;
				}

			case LEQ:
				emit(is.comment("<= expression."));
				if (exp.right instanceof Const) {
					// emit(is.cmpgt(res, lt, (Const) exp.right));
					// emit(is.not(res));
					emit(is.cmpleq(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					// emit(is.cmpgt(res, rt, lt));
					// emit(is.not(res));
					emit(is.cmpleq(res, lt, rt));
					return res;
				}

			case GEQ:
				emit(is.comment(">= expression."));
				if (exp.right instanceof Const) {
					// emit(is.cmplt(res, lt, (Const) exp.right));
					// emit(is.not(res));
					emit(is.cmpgeq(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					// emit(is.cmplt(res, rt, lt));
					// emit(is.not(res));
					emit(is.cmpgeq(res, lt, rt));
					return res;
				}

			case EQ:
				emit(is.comment("== expression."));
				if (exp.right instanceof Const) {
					emit(is.comment("\t of temp & const."));
					emit(is.cmpeq(res, lt, (Const) exp.right));
					return res;
				} else {
					emit(is.comment("\t of temp & temp."));
					rt = munchExp(exp.right);
					emit(is.cmpeq(res, rt, lt));
					return res;
				}

			case NEQ:
				emit(is.comment("!= expression."));
				if (exp.right instanceof Const) {
					// emit(is.cmpeq(res, lt, (Const) exp.right));
					// emit(is.not(res));
					emit(is.cmpneq(res, lt, (Const) exp.right));
					return res;
				} else {
					rt = munchExp(exp.right);
					// emit(is.cmpeq(res, rt, lt));
					// emit(is.not(res));
					emit(is.cmpneq(res, lt, rt));
					return res;
				}
		}
		Errors.fatal(
			String.format(
				"Unknown type of Binary operator %s. How did you even get here?",
				exp.op));
		return null;
	}

	private Temp munchExp(Memory mem) {
		// TODO Real Implementation.
		emit(is.comment("Memory access/store."));
		Temp res = Temp.create();
		if (mem.value instanceof Const) {
			emit(is.load(res, (Const) mem.value));
		} else if (mem.value instanceof Binary) {
			Binary bin = (Binary) mem.value;
			if (bin.op == BinOp.PLUS && bin.right instanceof Const) {
				Temp base = munchExp(bin.left);
				emit(is.load(res, base, (Const) bin.right));
			}
		} else {
			Temp addr = munchExp(mem.value);
			emit(is.load(res, addr));
		}

		return res;
	}

	private Temp munchExp(Call call) {
		// Load args into correct argument registers.
		// CallInstructionSequence cis = new CallInstructionSequence();

		//Save reference to curr.
		// InstructionSequence temp = curr;
		// curr = cis; //Bait-and-switch

		emit(is.comment("Expression call"));
		/*
			I suppose you all deserve an explanation.
			I am temporarily replacing curr,
			because I wish to package all instructions
			related to the call in one neat package
			that I can control the use and defs of.
		*/

		// frame.saveRegs(curr);

		List<Temp> args = munchArgs(call.args);

		if (call.func instanceof Name) {
			emit(is.call((Name) call.func, args, call.args.size()));
		} else {
			Temp func = munchExp(call.func);
			emit(is.call(func, args, call.args.size()));
		}

		// curr = temp; //It's time to quietly change back.
		// I hope no one noticed...

		// curr.then_(cis);
		return frame.getReturnReg();
	}

	private List<Temp> munchArgs(NodeList args) {
		frame.prepareForArguments(args.size()+3);

		for (int i = 0; i < args.size(); i++) {
			munchStatement(frame.nextArgument(i, args.get(i)));
		}

		return frame.getArgs(args.size());
	}

	public void generateData(List<DataInstruction> in) {
		data = new StringBuilder();
		for (DataInstruction d : in) {
			if (d instanceof DataLabel) {
				munchData((DataLabel) d);
			} else if (d instanceof Data) {
				munchData((Data) d);
			}
		}
	}

	private void munchData(DataLabel label) {
		emitData(is.dataLabel(label.getLabel()), false);
	}

	private void munchData(Data d) {
		if (d.getValue() instanceof Const) {
			Const c = (Const) d.getValue();
			switch (d.getSize()) {
				case B8: emitData(is.bits8(c)); break;
				case B16: emitData(is.bits16(c)); break;
				case B32: emitData(is.bits32(c)); break;
				case B64: emitData(is.bits64(c)); break;
				case WORD: emitData(is.word(c)); break;
			}
		} else if (d.getValue() instanceof Name) {
			emitData(is.word(((Name) d.getValue()).label));
		} else {
			emitData("Some data of type "  + d.getSize() +
				" was supposed to be here.");
			Errors.fatal(
				"Unknown data value type received. " +
				"Please contact your local compiler hacker for assistance.");
		}
	}
}
