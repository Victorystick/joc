package mjc.asm;

import mjc.asm.LabelInstruction;
import mjc.ir.*;

public abstract class ComplexInstructionSet implements InstructionSet {

	public Instruction add(Temp target, Temp reg1, Temp reg2) {
		return InstructionSequence.create(
			move(target, reg1),
			add(target, reg2));
	}

	public Instruction add(Temp target, Temp reg, Const c) {
		return InstructionSequence.create(
			move(target, reg),
			add(target, c));
	}

	public Instruction sub(Temp target, Temp reg1, Temp reg2) {
		return InstructionSequence.create(
			move(target, reg1),
			sub(target, reg2));
	}

	public Instruction sub(Temp target, Temp reg, Const c) {
		return InstructionSequence.create(
			move(target, reg),
			sub(target, c));
	}

	public Instruction subr(Temp target, Temp reg, Const c) {
		Temp t = Temp.create();
		return InstructionSequence.create(
			move(t, reg),
			move(target, c),
			sub(target, t));
	}

	public Instruction subr(Temp target, Const c) {
		Temp t = Temp.create();

		return InstructionSequence.create(
			move(t, target),
			move(target, c),
			sub(target, t));
	}

	public Instruction mul(Temp target, Temp reg1, Temp reg2) {
		return InstructionSequence.create(
			move(target, reg1),
			mul(target, reg2));
	}

	public Instruction mul(Temp target, Temp reg, Const c) {
		return InstructionSequence.create(
			move(target, reg),
			mul(target, c));
	}

	public Instruction and(Temp target, Temp reg1, Temp reg2) {
		return InstructionSequence.create(
			move(target, reg1),
			and(target, reg2));
	}

	public Instruction and(Temp target, Temp reg, Const c) {
		return InstructionSequence.create(
			move(target, reg),
			and(target, c));
	}

	public Instruction or(Temp target, Temp reg1, Temp reg2) {
		return InstructionSequence.create(
			move(target, reg1),
			or(target, reg2));
	}

	public Instruction or(Temp target, Temp reg, Const c) {
		return InstructionSequence.create(
			move(target, reg),
			or(target, c));
	}

	public Instruction cmplt(Temp target, Temp reg, Const c) {
		Temp t = Temp.create();

		return InstructionSequence.create(
			move(t, c),
			cmplt(target, reg, t));
	}

	public Instruction cmpleq(Temp target, Temp reg, Const c) {
		Temp t = Temp.create();

		return InstructionSequence.create(
			move(t, c),
			cmpleq(target, reg, t));
	}

	public Instruction cmpgt(Temp target, Temp reg, Const c) {
		Temp t = Temp.create();

		return InstructionSequence.create(
			move(t, c),
			cmpgt(target, reg, t));
	}

	public Instruction cmpgeq(Temp target, Temp reg, Const c) {
		Temp t = Temp.create();

		return InstructionSequence.create(
			move(t, c),
			cmpgeq(target, reg, t));
	}

	public Instruction cmpeq(Temp target, Temp reg, Const c) {
		Temp t = Temp.create();

		return InstructionSequence.create(
			move(t, c),
			cmpeq(target, reg, t));
	}

	public Instruction cmpneq(Temp target, Temp reg, Const c) {
		Temp t = Temp.create();

		return InstructionSequence.create(
			move(t, c),
			cmpneq(target, reg, t));
	}

	public Instruction not(Temp target, Temp reg) {
		return InstructionSequence.create(
			move(target, reg),
			not(target));
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
