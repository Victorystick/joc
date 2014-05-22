package mjc.asm;

import java.util.List;
import mjc.ir.*;

public interface InstructionSet {
	/**
	 * Ensures that the given string is included in the generated
	 * assembly code.
	 */
	public CommentInstruction comment(String s);

	/**
	 * Data fields
	 */
	public String dataLabel(Label l);
	public String word(Label l);
	public String word(Const c);
	public String bits8(Const c);
	public String bits16(Const c);
	public String bits32(Const c);
	public String bits64(Const c);

	/**
	 * Common
	 */
	public Instruction label(Label l);

	/**
	 * C equivalent: target = a + b
	 */
	public Instruction add(Temp target, Temp a, Temp b);
	public Instruction add(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target += a
	 */
	public Instruction add(Temp target, Temp a);
	public Instruction add(Temp target, Const a);

	/**
	 * C equivalent: target = a - b
	 */
	public Instruction sub(Temp target, Temp a, Temp b);
	public Instruction sub(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target -= a
	 */
	public Instruction sub(Temp target, Temp a);
	public Instruction sub(Temp target, Const a);

	/**
	 * C equivalent: target = b - a
	 */
	public Instruction subr(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target = a - target
	 */
	public Instruction subr(Temp target, Const a);

	/**
	 * C equivalent: target = a * b
	 */
	public Instruction mul(Temp target, Temp a, Temp b);
	public Instruction mul(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target *= a
	 */
	public Instruction mul(Temp target, Temp a);
	public Instruction mul(Temp target, Const a);

	/**
	 * C equivalent: target = a &amp; b
	 */
	public Instruction and(Temp target, Temp a, Temp b);
	public Instruction and(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target &amp;= a
	 */
	public Instruction and(Temp target, Temp a);
	public Instruction and(Temp target, Const a);

	/**
	 * C equivalent: target = a | b
	 */
	public Instruction or(Temp target, Temp a, Temp b);
	public Instruction or(Temp target, Temp a, Const b);

	/**
	 * C equivalent: target |= a
	 */
	public Instruction or(Temp target, Temp a);
	public Instruction or(Temp target, Const a);

	/**
	 * C equivalent: target != a
	 */
	public Instruction not(Temp target);
	public Instruction not(Temp target, Temp a);

	/*
	 * Comparator instructions
	 */
	 public Instruction cmplt(Temp target, Temp a, Temp b);
	 public Instruction cmplt(Temp target, Temp a, Const c);
	 public Instruction cmplt(Temp target, Const b);

	 public Instruction cmpleq(Temp target, Temp a, Temp b);
	 public Instruction cmpleq(Temp target, Temp a, Const c);
	 public Instruction cmpleq(Temp target, Const b);

	 public Instruction cmpgt(Temp target, Temp a, Temp b);
	 public Instruction cmpgt(Temp target, Temp a, Const c);
	 public Instruction cmpgt(Temp target, Const b);

	 public Instruction cmpgeq(Temp target, Temp a, Temp b);
	 public Instruction cmpgeq(Temp target, Temp a, Const c);
	 public Instruction cmpgeq(Temp target, Const b);

	 public Instruction cmpeq(Temp target, Temp a, Temp b);
	 public Instruction cmpeq(Temp target, Temp a, Const c);
	 public Instruction cmpeq(Temp target, Const b);

	 public Instruction cmpneq(Temp target, Temp a, Temp b);
	 public Instruction cmpneq(Temp target, Temp a, Const c);
	 public Instruction cmpneq(Temp target, Const b);


	/**
	 * C equivalent: target = source
	 */
	public Instruction move(Temp target, Temp source);
	public Instruction move(Temp target, Const source);

	public Instruction mmove(Temp target, Temp source);

	/**
	 * C equivalent: MEMORY[target] = source
	 */
	public Instruction store(Temp target, Temp source);

	/**
	 * C equivalent: MEMORY[target + c] = source
	 */
	public Instruction store(Temp target, Const c, Temp source);

	/**
	 * C equivalent: target = MEMORY[source]
	 */
	public Instruction load(Temp target, Temp source);
	public Instruction load(Temp target, Const source);

	/**
	 * C equivalent: target = MEMORY[source + c]
	 */
	public Instruction load(Temp target, Temp source, Const c);
	public Instruction load(Temp target, Temp source, Temp c);

	/**
	 * C equivalent: none
	 * Loads the address of a Label.
	 */
	public Instruction laddr(Temp target, Label label);

	//Jumps
	public Instruction jump(Label target);
	public Instruction jumplt(Label target, Temp left, Temp right);
	public Instruction jumpgt(Label target, Temp left, Temp right);
	public Instruction jumpleq(Label target, Temp left, Temp right);
	public Instruction jumpgeq(Label target, Temp left, Temp right);
	public Instruction jumpeq(Label target, Temp left, Temp right);
	public Instruction jumpneq(Label target, Temp left, Temp right);

	//Jumps with const
	public Instruction jumplt(Label target, Temp left, Const right);
	public Instruction jumpgt(Label target, Temp left, Const right);
	public Instruction jumpleq(Label target, Temp left, Const right);
	public Instruction jumpgeq(Label target, Temp left, Const right);
	public Instruction jumpeq(Label target, Temp left, Const right);
	public Instruction jumpneq(Label target, Temp left, Const right);

	// Call of function
	// Apparently needs the arguments to do liveness analysis.
	public Instruction call(Temp func, List<Temp> args, int narg);
	public Instruction call(Name func, List<Temp> args, int narg);
}
