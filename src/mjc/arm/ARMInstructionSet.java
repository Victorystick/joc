package mjc.arm;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mjc.asm.*;
import mjc.Errors;
import mjc.ir.*;

public class ARMInstructionSet extends ReducedInstructionSet {
	private static ARMInstructionSet instance = null;

	private ARMInstructionSet() {}

	public static ARMInstructionSet getInstance() {
		if (instance == null) {
			instance = new ARMInstructionSet();
		}

		return instance;
	}

	// Constants
	public static final int[]
		_ = new int[] {},
		_0 = new int[] { 0 },
		_01 = new int[] { 0, 1 },
		_012 = new int[] { 0, 1, 2 },
		_1 = new int[] { 1 },
		_12 = new int[] { 1, 2 },
		_123456789 = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	// --------- DATA --------
	private static int alignment = 0;
	private void align(int a) {
		alignment = (alignment + a) % 4;
	}

	public String dataLabel(Label l) {
		return l.label + ":";
	}

	public String word(Label l) {
		return ".word   " + l.label;
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

	// Comments in ARM start with `@` and continue until the
	// end of the line. `\n` are replaced with `\n@`
	public CommentInstruction comment(String s) {
		return new CommentInstruction("@ " + s.replace("\n", "\n@ "));
		// return null;
	}

	public Instruction nop() {
		return Operation.create("mov     r0, r0");
	}

	public Instruction ret(List<Temp> sink) {
		return new NonTempInstruction("bx      lr", sink);
	}

	// --------- CALLS ---------

	private Instruction wrapCall(int args, Instruction call) {
		args = Math.max(0, args - 4);

		// return call;

		InstructionSequence is = InstructionSequence.create();
		is.then(comment("Volatile Register store-back"));
		for (int i = 0; i < ARMFrame.volatyle.size(); i++) {
			is.then( Operation.create(String.format("str     %s, [sp, #%d]",
						ARMFrame.t2s.get(ARMFrame.volatyle.get(i)),
						(args + i + 1) * ARMFrame.WORDSIZE)));
		}
		is.then(call);
		is.then(comment("Volatile Register load-back"));
		for (int i = ARMFrame.volatyle.size()-1; i >= 0 ; i--) {
			is.then(Operation.create(String.format("ldr     %s, [sp, #%d]",
				ARMFrame.t2s.get(ARMFrame.volatyle.get(i)),
				(args + i + 1) * ARMFrame.WORDSIZE)));
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
				comment("def: " + ARMFrame.calldefs),
				JumpOperation.create("blx     %s",
					func,
					ARMFrame.calldefs,
					uses)));
	}

	public Instruction call(Name func, List<Temp> args, int narg) {
		Label label = func.label;

		// System.err.println("args: " + args);

		return wrapCall(narg, JumpOperation.create("bl      " + label.label,
			label,
			ARMFrame.calldefs,
			args == null ? Collections.<Temp>emptyList() : args));
	}

	// private String genConst(Const cnst) {
	// 	int tmp, shift = 0, c = cnst.value;

	// 	tmp = c >> shift;
	// 	while (shift < 32 && (tmp & 255) != tmp) {
	// 		shift++;
	// 		tmp = c >> shift;
	// 	}

	// 	if (shift != 32) {
	// 		return "#" + c.value + (shift == 0 ? "" : " lsl #" + shift);
	// 	}

	// 	return null;
	// }

	// --------- DATA ---------

	private Instruction movelsl(Temp target, Temp val, int c) {
		return Operation.create("mov     %s, %s, lsl #" + c,
			_0, _1, target, val);
	}

	public Instruction move(Temp target, Temp source) {
		// if (target == source) {
		// 	System.err.println(target == source);
		// 	return null;
		// }
		return MoveOperation.create("mov     %s, %s", target, source);
	}

	private Instruction move(Temp target, int c) {
		if (c >= 0) {
			if (c <= 255) {
				return Operation.create("mov     %s, #" + c,
					_0, _, target);
			}

			if (c < (1 << 16)) {
				InstructionSequence seq = InstructionSequence.create(
					move(target, c >> 8),
					movelsl(target, target, 8));

				if ((c & 255) != 0) {
					seq.then(add(target, target, c & 255));
				}

				return seq;
			}
		}

		return Operation.create("ldr     %s, =" + c,
			_0, _, target);
	}

	public Instruction move(Temp target, Const c) {
		return move(target, c.value);
	}

	public Instruction mvn(Temp target, Temp source) {
		return Operation.create("mvn     %s, %s",
			_0, _1, target, source);
	}

	public Instruction add(Temp target, Temp val, Temp other) {
		return Operation.create("add     %s, %s, %s",
			_0, _12, target, val, other);
	}

	public Instruction add(Temp target, Temp val, Const c) {
		return add(target, val, c.value);
	}

	private Instruction add(Temp target, Temp val, int c) {
		if (c == 0) {
			return move(target, val);
		} else if (c <= 255 && c > 0) {
			return Operation.create("add     %s, %s, #" + c,
				_0, _1, target, val);
		}

		Temp temp = Temp.create();
		return InstructionSequence.create(
			move(temp, c),
			add(target, val, temp));
	}

	public Instruction sub(Temp target, Temp val, Temp other) {
		return Operation.create("sub     %s, %s, %s",
			_0, _12, target, val, other);
	}

	public Instruction sub(Temp target, Temp val, Const c) {
		if (c.value == 0) {
			return move(target, val);
		} else if (c.value <= 255 && c.value > 0) {
			return Operation.create("sub     %s, %s, #" + c.value,
				_0, _1, target, val);
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			sub(target, val, ARMFrame.$scratch));
	}

	public Instruction subr(Temp target, Temp val, Const c) {
		if (c.value <= 255 && c.value >= 0) {
			return Operation.create("rsb     %s, %s, #" + c.value,
				_0, _1, target, val);
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			sub(target, ARMFrame.$scratch, val));
	}

	public Instruction mul(Temp target, Temp val, Temp other) {
		Temp temp;

		if (target == val) {
			if (target != other) {
				temp = val;
				val = other;
				other = temp;
			} else {
				Errors.warn("ARMInstructionSet: it may not possible for a " +
					"`mul Rd, Rm, Rs` operation to have Rd == Rm");
			}
		}

		return Operation.create("mul     %s, %s, %s",
			_0, _12, target, val, other);
	}

	public Instruction mul(Temp target, Temp val, Const c) {
		int cv = c.getValue();
		int shift = 1;
		while (shift < 32) {
			if (cv == (1 << shift)) {
				movelsl(target, val, c.value);
			}
			shift++;
		}

		Temp temp = Temp.create();
		return InstructionSequence.create(
			move(temp, c),
			mul(target, val, temp));
	}

	public Instruction and(Temp target, Temp val, Temp other) {
		return Operation.create("and     %s, %s, %s",
			_0, _12, target, val, other);
	}

	public Instruction and(Temp target, Temp val, Const c) {
		if (c.value <= 255 && c.value >= 0) {
			return Operation.create("and     %s, %s, #" + c.value,
				_0, _1, target, val);
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			and(target, val, ARMFrame.$scratch));
	}

	public Instruction or(Temp target, Temp val, Temp other) {
		return Operation.create("orr     %s, %s, %s",
			_0, _12, target, val, other);
	}

	public Instruction or(Temp target, Temp val, Const c) {
		if (c.value <= 255 && c.value >= 0) {
			return Operation.create("arr     %s, %s, #" + c.value,
				_0, _1, target, val);
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			or(target, val, ARMFrame.$scratch));
	}

	public Instruction cmplt(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			Operation.create("cmp     %s, %s",
				_, _01, val, other),
			Operation.create("movlt   %s, #1",
				_0, _, target),
			Operation.create("movge   %s, #0",
				_0, _, target));
	}

	public Instruction cmplt(Temp target, Temp val, Const c) {
		if (c.value <= 255 && c.value >= 0) {
			return InstructionSequence.create(
				Operation.create("cmp     %s, #" + c.value,
					_, _0, val),
				Operation.create("movlt   %s, #1",
					_0, _, target),
				Operation.create("movge   %s, #0",
					_0, _, target));
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			cmplt(target, val, ARMFrame.$scratch));
	}

	public Instruction cmpleq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			Operation.create("cmp     %s, %s",
				_, _01, val, other),
			Operation.create("movle   %s, #1",
				_0, _, target),
			Operation.create("movgt   %s, #0",
				_0, _, target));
	}

	public Instruction cmpleq(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			cmpleq(target, val, ARMFrame.$scratch));
	}

	public Instruction cmpgt(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			Operation.create("cmp     %s, %s",
				_, _01, val, other),
			Operation.create("movgt   %s, #1",
				_0, _, target),
			Operation.create("movle   %s, #0",
				_0, _, target));
	}

	public Instruction cmpgt(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			cmpgt(target, val, ARMFrame.$scratch));
	}

	public Instruction cmpgeq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			Operation.create("cmp     %s, %s",
				_, _01, val, other),
			Operation.create("movge   %s, #1",
				_0, _, target),
			Operation.create("movlt   %s, #0",
				_0, _, target));
	}

	public Instruction cmpgeq(Temp target, Temp val, Const c) {
		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			cmpgeq(target, val, ARMFrame.$scratch));
	}

	public Instruction cmpeq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			Operation.create("cmp     %s, %s",
				_, _01, val, other),
			Operation.create("moveq   %s, #1",
				_0, _, target),
			Operation.create("movne   %s, #0",
				_0, _, target));
	}

	public Instruction cmpeq(Temp target, Temp val, Const c) {
		if (c.value <= 255 && c.value >= 0) {
			return InstructionSequence.create(
				Operation.create("cmp     %s, #" + c.value,
					_, _0, val),
				Operation.create("moveq   %s, #1",
					_0, _, target),
				Operation.create("movne   %s, #0",
					_0, _, target));
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			cmpeq(target, val, ARMFrame.$scratch));
	}

	public Instruction not(Temp target, Temp val) {
		return InstructionSequence.create(
			mvn(target, val),
			add(target, Const.TWO));
	}

	public Instruction cmpneq(Temp target, Temp val, Temp other) {
		return InstructionSequence.create(
			Operation.create("cmp     %s, %s",
				_, _01, val, other),
			Operation.create("movne   %s, #1",
				_0, _, target),
			Operation.create("moveq   %s, #0",
				_0, _, target));
	}

	public Instruction cmpneq(Temp target, Temp val, Const c) {
		if (c.value <= 255 && c.value >= 0) {
			return InstructionSequence.create(
				Operation.create("cmp     %s, #" + c.value,
					_, _0, val),
				Operation.create("movne   %s, #1",
					_0, _, target),
				Operation.create("moveq   %s, #0",
					_0, _, target));
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			cmpneq(target, val, ARMFrame.$scratch));
	}

	// --------- MEMORY ---------

	public Instruction load(Temp target, Temp source) {
		return Operation.create("ldr     %s, [%s]",
			_0, _1, target, source);
	}

	public Instruction load(Temp target, Temp source, Const c) {
		Temp temp;

		if (c.value == 0) {
			return load(target, source);
		} else if (c.value <= 255 && c.value > 0) {
			return Operation.create(
				"ldr     %s, [%s, #" + c.value + "]",
					_0, _1, target, source);
		} else if (c.value > 0 && (c.value & 3) == 0 && (c.value >> 2) < 255) {
			temp = Temp.create();
			return InstructionSequence.create(
				move(temp, Const.create(c.value >> 2)),
				Operation.create(
					"ldr     %s, [%s, %s, lsl #2]",
					_0, _12, target, source, temp));
		}

		temp = Temp.create();
		return InstructionSequence.create(
			move(temp, c),
			load(target, source, temp));
	}

	public Instruction load(Temp target, Temp source, Temp reg) {
		return Operation.create("ldr     %s, [%s, %s]",
				_0, _12, target, source, reg);
	}

	public Instruction laddr(Temp target, Label label) {
		return Operation.create("ldr     %s, =" + label.label,
			_0, _, target);
	}

	public Instruction store(Temp target, Temp source) {
		return Operation.create("str     %s, [%s]",
			_, _01, source, target);
	}

	public Instruction store(Temp target, Temp offset, Temp source) {
		return Operation.create("str     %s, [%s, %s]",
			_, _012, source, target, offset);
	}

	public Instruction store(Temp target, Const c, Temp source) {
		if (c.value == 0) {
			return store(target, source);
		} else if (c.value <= 255 && c.value > 0) {
			return Operation.create(
				"str     %s, [%s, #" + c.value + "]",
					_, _01, source, target);
		}

		Temp temp = Temp.create();

		if (c.value > 0 && (c.value & 3) == 0 && (c.value >> 2) <= 255) {
			return InstructionSequence.create(
				move(temp, Const.create(c.value >> 2)),
				Operation.create(
					"str     %s, [%s, %s, lsl #2]",
					_, _012, source, target, temp));
		}

		return InstructionSequence.create(
			move(temp, c),
			store(target, temp, source));

		// if (c.value <= 255 && c.value >= 0) {
		// 	return Operation.create("str     %s, [%s, #" + c.value + "]",
		// 		_, _01, source, target);
		// }

		// return InstructionSequence.create(
		// 	move(ARMFrame.$scratch, c),
		// 	store(target, ARMFrame.$scratch, source));
	}

	public Instruction mmove(Temp target, Temp source) {
		Temp temp = Temp.create();
		return InstructionSequence.create(
			load(temp, source),
			store(target, temp));
	}

	// --------- JUMPS ---------

	public Instruction jump(Label label) {
		return JumpOperation.create("b       " + label.label, label, false);
	}

	public Instruction jump(Temp reg) {
		return JumpOperation.create("bx      %s", reg, false);
	}

	private Instruction jumpWithCmpString(Label label, Temp left, Temp right, String cmp) {
		return InstructionSequence.create(
			Operation.create("cmp     %s, %s",
				_, _01, left, right),
			JumpOperation.create("b" + cmp + "     " + label.label, label, true));
	}

	public Instruction jumpeq(Label label, Temp left, Temp right) {
		return jumpWithCmpString(label, left, right, "eq");
	}

	public Instruction jumpneq(Label label, Temp left, Temp right) {
		return jumpWithCmpString(label, left, right, "ne");
	}

	public Instruction jumplt(Label label, Temp left, Temp right) {
		return jumpWithCmpString(label, left, right, "lt");
	}

	public Instruction jumpleq(Label label, Temp left, Temp right) {
		return jumpWithCmpString(label, left, right, "le");
	}

	public Instruction jumpgt(Label label, Temp left, Temp right) {
		return jumpWithCmpString(label, left, right, "gt");
	}

	public Instruction jumpgeq(Label label, Temp left, Temp right) {
		return jumpWithCmpString(label, left, right, "ge");
	}

	private Instruction jumpWithCmpString(Label label, Temp reg, Const c, String cmp) {
		if (c.value <= 255 && c.value >= 0) {
			return InstructionSequence.create(
				Operation.create("cmp     %s, #" + c.value,
					_, _0, reg),
				JumpOperation.create("b" + cmp + "     " + label.label, label, true));
		}

		return InstructionSequence.create(
			move(ARMFrame.$scratch, c),
			jumpWithCmpString(label, reg, ARMFrame.$scratch, cmp));
	}

	public Instruction jumpeq(Label label, Temp reg, Const c) {
		return jumpWithCmpString(label, reg, c, "eq");
	}

	public Instruction jumpneq(Label label, Temp reg, Const c) {
		return jumpWithCmpString(label, reg, c, "ne");
	}

	public Instruction jumplt(Label label, Temp reg, Const c) {
		return jumpWithCmpString(label, reg, c, "lt");
	}

	public Instruction jumpleq(Label label, Temp reg, Const c) {
		return jumpWithCmpString(label, reg, c, "le");
	}

	public Instruction jumpgt(Label label, Temp reg, Const c) {
		return jumpWithCmpString(label, reg, c, "gt");
	}

	public Instruction jumpgeq(Label label, Temp reg, Const c) {
		return jumpWithCmpString(label, reg, c, "ge");
	}
}
