package mjc.asm;

import mjc.asm.LabelInstruction;
import mjc.ir.*;

public abstract class ReducedInstructionSet implements InstructionSet {

	public Instruction add(Temp target, Temp reg) {
		return add(target, target, reg);
	}

	public Instruction add(Temp target, Const c) {
		return add(target, target, c);
	}

	public Instruction sub(Temp target, Temp reg) {
		return sub(target, target, reg);
	}

	public Instruction sub(Temp target, Const c) {
		return sub(target, target, c);
	}

	public Instruction subr(Temp target, Const c) {
		return subr(target, target, c);
	}

	public Instruction mul(Temp target, Temp reg) {
		return mul(target, target, reg);
	}

	public Instruction mul(Temp target, Const c) {
		return mul(target, target, c);
	}

	public Instruction and(Temp target, Temp reg) {
		return and(target, target, reg);
	}

	public Instruction and(Temp target, Const c) {
		return and(target, target, c);
	}

	public Instruction or(Temp target, Temp reg) {
		return or(target, target, reg);
	}

	public Instruction or(Temp target, Const c) {
		return or(target, target, c);
	}

	public Instruction cmplt(Temp target, Temp reg) {
		return cmplt(target, target, reg);
	}

	public Instruction cmplt(Temp target, Const c) {
		return cmplt(target, target, c);
	}

	public Instruction cmpleq(Temp target, Temp reg) {
		return cmpleq(target, target, reg);
	}

	public Instruction cmpleq(Temp target, Const c) {
		return cmpleq(target, target, c);
	}

	public Instruction cmpgt(Temp target, Temp reg) {
		return cmpgt(target, target, reg);
	}

	public Instruction cmpgt(Temp target, Const c) {
		return cmpgt(target, target, c);
	}

	public Instruction cmpgeq(Temp target, Temp reg) {
		return cmpgeq(target, target, reg);
	}

	public Instruction cmpgeq(Temp target, Const c) {
		return cmpgeq(target, target, c);
	}

	public Instruction cmpeq(Temp target, Temp reg) {
		return cmpeq(target, target, reg);
	}

	public Instruction cmpeq(Temp target, Const c) {
		return cmpeq(target, target, c);
	}

	public Instruction cmpneq(Temp target, Const c) {
		return cmpeq(target, target, c);
	}

	public Instruction not(Temp target) {
		return not(target, target);
	}

	public Instruction label(Label l) {
		return new LabelInstruction(l);
	}

	public Instruction load(Temp target, Const addr) {
		return InstructionSequence.create(
			move(target, Const.ZERO),
			load(target, target, addr));
	}
}
