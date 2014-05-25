package mjc.asm;

import java.util.List;
import mjc.ir.*;

public interface InstructionSet {
	/**
	 * Ensures that the given string is included in the generated
	 * assembly code.
	 */
	CommentInstruction comment(String s);

	/**
	 * Meta data
	 */
	String beginData();
	String endData();
	String beginCode();
	String endCode();
	String export(String str);

	/**
	 * Data fields
	 */
	String dataLabel(Label l);
	String word(Label l);
	String word(Const c);
	String bits8(Const c);
	String bits16(Const c);
	String bits32(Const c);
	String bits64(Const c);

	/**
	 * Common
	 */
	Instruction label(Label l);

	/**
	 * C equivalent: target = a + b
	 */
	Instruction add(Temp target, Temp a, Temp b);
	Instruction add(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target += a
	 */
	Instruction add(Temp target, Temp a);
	Instruction add(Temp target, Const a);

	/**
	 * C equivalent: target = a - b
	 */
	Instruction sub(Temp target, Temp a, Temp b);
	Instruction sub(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target -= a
	 */
	Instruction sub(Temp target, Temp a);
	Instruction sub(Temp target, Const a);

	/**
	 * C equivalent: target = b - a
	 */
	Instruction subr(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target = a - target
	 */
	Instruction subr(Temp target, Const a);

	/**
	 * C equivalent: target = a * b
	 */
	Instruction mul(Temp target, Temp a, Temp b);
	Instruction mul(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target *= a
	 */
	Instruction mul(Temp target, Temp a);
	Instruction mul(Temp target, Const a);

	/**
	 * C equivalent: target = a &amp; b
	 */
	Instruction and(Temp target, Temp a, Temp b);
	Instruction and(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target &amp;= a
	 */
	Instruction and(Temp target, Temp a);
	Instruction and(Temp target, Const a);

	/**
	 * C equivalent: target = a | b
	 */
	Instruction or(Temp target, Temp a, Temp b);
	Instruction or(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target |= a
	 */
	Instruction or(Temp target, Temp a);
	Instruction or(Temp target, Const a);

	/**
	 * C equivalent: target != a
	 */
	Instruction not(Temp target);
	Instruction not(Temp target, Temp a);

	/*
	 * Comparator instructions
	 */
	 Instruction cmplt(Temp target, Temp a, Temp b);
	 Instruction cmplt(Temp target, Temp a, Const c);
	 // Instruction cmplt(Temp target, Const b);

	 Instruction cmpleq(Temp target, Temp a, Temp b);
	 Instruction cmpleq(Temp target, Temp a, Const c);
	 // Instruction cmpleq(Temp target, Const b);

	 Instruction cmpgt(Temp target, Temp a, Temp b);
	 Instruction cmpgt(Temp target, Temp a, Const c);
	 // Instruction cmpgt(Temp target, Const b);

	 Instruction cmpgeq(Temp target, Temp a, Temp b);
	 Instruction cmpgeq(Temp target, Temp a, Const c);
	 // Instruction cmpgeq(Temp target, Const b);

	 Instruction cmpeq(Temp target, Temp a, Temp b);
	 Instruction cmpeq(Temp target, Temp a, Const c);
	 // Instruction cmpeq(Temp target, Const b);

	 Instruction cmpneq(Temp target, Temp a, Temp b);
	 Instruction cmpneq(Temp target, Temp a, Const c);
	 // Instruction cmpneq(Temp target, Const b);


	/**
	 * C equivalent: target = source
	 */
	Instruction move(Temp target, Temp source);
	Instruction move(Temp target, Const source);

	Instruction mmove(Temp target, Temp source);

	/**
	 * C equivalent: MEMORY[target] = source
	 */
	Instruction store(Temp target, Temp source);

	/**
	 * C equivalent: MEMORY[target + c] = source
	 */
	Instruction store(Temp target, Const c, Temp source);

	/**
	 * C equivalent: target = MEMORY[source]
	 */
	Instruction load(Temp target, Temp source);
	Instruction load(Temp target, Const source);

	/**
	 * C equivalent: target = MEMORY[source + c]
	 */
	Instruction load(Temp target, Temp source, Const c);
	Instruction load(Temp target, Temp source, Temp c);

	/**
	 * C equivalent: none
	 * Loads the address of a Label.
	 */
	Instruction laddr(Temp target, Label label);

	//Jumps
	Instruction jump(Label target);
	Instruction jumplt(Label target, Temp left, Temp right);
	Instruction jumpgt(Label target, Temp left, Temp right);
	Instruction jumpleq(Label target, Temp left, Temp right);
	Instruction jumpgeq(Label target, Temp left, Temp right);
	Instruction jumpeq(Label target, Temp left, Temp right);
	Instruction jumpneq(Label target, Temp left, Temp right);

	//Jumps with const
	Instruction jumplt(Label target, Temp left, Const right);
	Instruction jumpgt(Label target, Temp left, Const right);
	Instruction jumpleq(Label target, Temp left, Const right);
	Instruction jumpgeq(Label target, Temp left, Const right);
	Instruction jumpeq(Label target, Temp left, Const right);
	Instruction jumpneq(Label target, Temp left, Const right);

	// Call of function
	// Apparently needs the arguments to do liveness analysis.
	Instruction call(Temp func, List<Temp> args, int narg);
	Instruction call(Name func, List<Temp> args, int narg);
}
