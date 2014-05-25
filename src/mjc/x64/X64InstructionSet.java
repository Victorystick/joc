package mjc.x64;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mjc.asm.*;
import mjc.Errors;
import mjc.ir.*;

public class X64InstructionSet extends ComplexInstructionSet {
	private static X64InstructionSet instance = null;

	private X64InstructionSet() {}

	public static X64InstructionSet getInstance() {
		if (instance == null) {
			instance = new X64InstructionSet();
		}

		return instance;
	}

	// Constants
	public static final int[]
		_ = new int[] {},
		_0 = new int[] { 0 },
		_1 = new int[] { 1 },
		_2 = new int[] { 2 },
		_01 = new int[] { 0, 1 },
		_12 = new int[] { 1, 2 },
		_012 = new int[] { 0, 1, 2 },
		_123456789 = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	// --------- META --------
	public String beginData() {
		return ".section .rodata\n.align 8";
	}

	public String endData() {
		return "";
	}

	public String beginCode() {
		return ".text";
	}

	public String endCode() {
		return "";
	}

	public String export(String name) {
		return ".globl " + name;
	}

	// --------- DATA --------
	private static int alignment = 0;
	private void align(int a) {
		alignment = (alignment + a) % 4;
	}

	public String dataLabel(Label l) {
		return l.label + ":";
	}

	public String word(Label l) {
		return ".long   " + l.label;
	}

	public String word(Const c) {
		return bits32(c);
	}

	public String bits8(Const c) {
		align(1);
		return ".byte   " + c.getValue();
	}

	public String bits16(Const c) {
		String s = ".hword  " + c.getValue();

		if (alignment != alignment % 2) {
			align(3);
			return ".align  1\n" + s;
		}

		align(2);
		return s;
	}

	public String bits32(Const c) {
		String s = ".word   " + c.getValue();

		if (alignment != 0) {
			int fix = 4 - alignment;
			align(fix);
			return ".align  " + fix + "\n" + s;
		}

		return s;
	}

	public String bits64(Const c) {
		Errors.error("Compiler doesn't support 64bit constants.");
		return "";
	}

	// Comments in X64 start with `@` and continue until the
	// end of the line. `\n` are replaced with `\n@`
	public CommentInstruction comment(String s) {
		return new CommentInstruction("/* " + s + " */");
		// return null;
	}

	public Instruction nop() {
		return Operation.create("mov     r0, r0");
	}

	public Instruction ret(List<Temp> sink) {
		//return new NonTempInstruction("bx      lr", sink);
		return null;
	}

	// --------- CALLS ---------

	private Instruction wrapCall(int args, Instruction call) {
		args = Math.max(0, args - 4);

		// return call;

		InstructionSequence is = InstructionSequence.create();
		is.then(comment("Volatile Register store-back"));
		for (int i = 0; i < X64Frame.volatyle.size(); i++) {
			is.then( Operation.create(String.format("movq    %s, %d(%s)",
				X64Frame.t2s.get(X64Frame.volatyle.get(i)),
				(args + i + 1) * X64Frame.WORDSIZE,
				X64Frame.t2s.get(X64Frame.$stack))));
		}
		is.then(call);
		is.then(comment("Volatile Register load-back"));
		for (int i = X64Frame.volatyle.size()-1; i >= 0 ; i--) {
			is.then(Operation.create(String.format("movq    %d(%s), %s",
				(args + i + 1) * X64Frame.WORDSIZE,
				X64Frame.t2s.get(X64Frame.$stack),
				X64Frame.t2s.get(X64Frame.volatyle.get(i)))));
		}
		return is;
	}

	public Instruction call(Temp func, List<Temp> args, int narg) {
		List<Temp> uses = new ArrayList<Temp>();

		if (args != null) {
			uses.addAll(args);
		}

		uses.add(func);

		return wrapCall(narg,
			InstructionSequence.create(
				comment("use: " + uses),
				comment("def: " + X64Frame.calldefs),
				JumpOperation.create("call    *%s",
					func,
					X64Frame.calldefs,
					uses)));
	}

	public Instruction call(Name func, List<Temp> args, int narg) {
		Label label = func.label;

		// System.err.println("args: " + args);

		return wrapCall(narg, JumpOperation.create("call    " + label.label,
			label,
			X64Frame.calldefs,
			args == null ? Collections.<Temp>emptyList() : args));
	}

	// --------- DATA ---------

	public Instruction move(Temp target, Temp source) {
		return MoveOperation.createInverted("movq    %s, %s",
			source, target);
	}

	public Instruction move(Temp target, Const c) {
		return Operation.create("movq    $" + c.value + ", %s",
			_0, _, target);
	}

	public Instruction not(Temp target) {
		return Operation.create("not     %s",	_0, _0, target);
	}

	public Instruction add(Temp target, Temp val) {
		return Operation.create("add     %s, %s",
			_1, _01, val, target);
	}

	public Instruction add(Temp target, Const c) {
		if (c == Const.ZERO) {
			return null;
		} else if (c == Const.ONE) {
			return Operation.create("inc     %s", _0, _0, target);
		}

		return Operation.create("add     $" + c.value + ", %s",
			_0, _0, target);
	}

	public Instruction sub(Temp target, Temp val) {
		return Operation.create("sub     %s, %s",
			_1, _01, val, target);
	}

	public Instruction sub(Temp target, Const c) {
		if (c == Const.ZERO) {
			return null;
		} else if (c == Const.ONE) {
			return Operation.create("dec     %s", _0, _0, target);
		}

		return Operation.create("sub     $" + c.value + ", %s",
			_0, _0, target);
	}

	public Instruction mul(Temp target, Temp val) {
		return Operation.create("imul    %s, %s",
			_1, _01, val, target);
	}

	public Instruction mul(Temp target, Const c) {
		return Operation.create("imul    $" + c.value + ", %s",
			_0, _0, target);
	}

	public Instruction and(Temp target, Temp val) {
		return Operation.create("and     %s, %s",
			_1, _01, val, target);
	}

	public Instruction and(Temp target, Const c) {
		return Operation.create("and     $" + c.value + ", %s",
			_0, _0, target);
	}

	public Instruction or(Temp target, Temp val) {
		return Operation.create("or      %s, %s",
			_1, _01, val, target);
	}

	public Instruction or(Temp target, Const c) {
		return Operation.create("or      $" + c.value + ", %s",
			_0, _0, target);
	}

	private Instruction cmp(Temp a, Temp b) {
		return Operation.create("cmp     %s, %s",	_, _01, b, a);
	}

	private Instruction cmp(Temp a, Const c) {
		return Operation.create("cmp     $" + c.value + ", %s", _, _0, a);
	}

	public Instruction cmplt(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			cmp(val, other),
			move(target, Const.ZERO),
			X64Operation.createByte("setl    %s", target));
	}

	public Instruction cmplt(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			cmp(val, c),
			move(target, Const.ZERO),
			X64Operation.createByte("setl    %s", target));
	}

	public Instruction cmpleq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			cmp(val, other),
			move(target, Const.ZERO),
			X64Operation.createByte("setle   %s", target));
	}

	public Instruction cmpleq(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			cmp(val, c),
			move(target, Const.ZERO),
			X64Operation.createByte("setle   %s", target));
	}

	public Instruction cmpgt(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			cmp(val, other),
			move(target, Const.ZERO),
			X64Operation.createByte("setg    %s", target));
	}

	public Instruction cmpgt(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			cmp(val, c),
			move(target, Const.ZERO),
			X64Operation.createByte("setg    %s", target));
	}

	public Instruction cmpgeq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			cmp(val, other),
			move(target, Const.ZERO),
			X64Operation.createByte("setge   %s", target));
	}

	public Instruction cmpgeq(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			cmp(val, c),
			move(target, Const.ZERO),
			X64Operation.createByte("setge   %s", target));
	}

	public Instruction cmpeq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			cmp(val, other),
			move(target, Const.ZERO),
			X64Operation.createByte("sete    %s", target));
	}

	public Instruction cmpeq(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			cmp(val, c),
			move(target, Const.ZERO),
			X64Operation.createByte("sete    %s", target));
	}

	public Instruction cmpneq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			cmp(val, other),
			move(target, Const.ZERO),
			X64Operation.createByte("setne   %s", target));
	}

	public Instruction cmpneq(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			cmp(val, c),
			move(target, Const.ZERO),
			X64Operation.createByte("setne   %s", target));
	}

	// --------- MEMORY ---------

	public Instruction load(Temp target, Temp source) {
		return Operation.create("movq    (%s), %s",
			_1, _0, source, target);
	}

	public Instruction load(Temp target, Temp source, Const c) {
		return Operation.create("movq    " + c.value + "(%s), %s",
			_1, _0, source, target);
	}

	public Instruction load(Temp target, Temp source, Temp offset) {
		return Operation.create("movq    (%s, %s), %s",
			_2, _01, source, offset, target);
	}

	public Instruction laddr(Temp target, Label label) {
		return Operation.create("leaq    " + label.label + ", %s",
			_0, _, target);
	}

	public Instruction store(Temp target, Temp source) {
		return Operation.create("movq    %s, (%s)",
			_, _01, source, target);
	}

	public Instruction store(Temp target, Temp source, Temp offset) {
		return Operation.create("movq    %s, (%s, %s)",
			_, _012, source, target, offset);
	}

	public Instruction store(Temp target, Const c, Temp source) {
		return Operation.create("movq    %s, " + c.value + "(%s)",
			_, _01, source, target);
	}

	public Instruction mmove(Temp target, Temp source) {
		Temp temp = Temp.create();

		return InstructionSequence.create(
			load(temp, source),
			store(target, temp));
	}

	// --------- JUMPS ---------

	public Instruction jump(Label label) {
		return JumpOperation.create("jmp     " + label.label, label, false);
	}

	public Instruction jump(Temp reg) {
		return JumpOperation.create("jmp     %s", reg, false);
	}

	private Instruction jumpWithX(Label label,
		                            Instruction cmpInstr,
		                            String cmp) {
		return InstructionSequence.create(
			cmpInstr,
			JumpOperation.create("j" + cmp + "     " + label.label, label, true));
	}

	private Instruction jumpWithTemp(Label label, Temp left,
		                               Temp right, String cmp) {
		return jumpWithX(label, cmp(left, right), cmp);
	}

	private Instruction jumpWithConst(Label label, Temp reg,
		                                Const c, String cmp) {
		return jumpWithX(label, cmp(reg, c), cmp);
	}

	public Instruction jumpeq(Label label, Temp left, Temp right) {
		return jumpWithTemp(label, left, right, "e ");
	}

	public Instruction jumpneq(Label label, Temp left, Temp right) {
		return jumpWithTemp(label, left, right, "ne");
	}

	public Instruction jumplt(Label label, Temp left, Temp right) {
		return jumpWithTemp(label, left, right, "l ");
	}

	public Instruction jumpleq(Label label, Temp left, Temp right) {
		return jumpWithTemp(label, left, right, "le");
	}

	public Instruction jumpgt(Label label, Temp left, Temp right) {
		return jumpWithTemp(label, left, right, "g ");
	}

	public Instruction jumpgeq(Label label, Temp left, Temp right) {
		return jumpWithTemp(label, left, right, "ge");
	}

	public Instruction jumpeq(Label label, Temp reg, Const c) {
		return jumpWithConst(label, reg, c, "e ");
	}

	public Instruction jumpneq(Label label, Temp reg, Const c) {
		return jumpWithConst(label, reg, c, "ne");
	}

	public Instruction jumplt(Label label, Temp reg, Const c) {
		return jumpWithConst(label, reg, c, "l ");
	}

	public Instruction jumpleq(Label label, Temp reg, Const c) {
		return jumpWithConst(label, reg, c, "le");
	}

	public Instruction jumpgt(Label label, Temp reg, Const c) {
		return jumpWithConst(label, reg, c, "g ");
	}

	public Instruction jumpgeq(Label label, Temp reg, Const c) {
		return jumpWithConst(label, reg, c, "ge");
	}
}
