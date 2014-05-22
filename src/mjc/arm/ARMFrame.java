package mjc.arm;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mjc.asm.InstructionSequence;
import mjc.asm.Operation;
import mjc.frame.Access;
import mjc.frame.Frame;
import mjc.ir.*;

public class ARMFrame extends Frame {
	private static final ARMInstructionSet is =
		ARMInstructionSet.getInstance();
	public static final int WORDSIZE = 4;

	public static final Temp
		a0       = Temp.create(true), // 0
		a1       = Temp.create(true), // 1
		a2       = Temp.create(true), // 2
		a3       = Temp.create(true), // 3
		r4       = Temp.create(true), // 4
		r5       = Temp.create(true), // 5
		r6       = Temp.create(true), // 6
		r7       = Temp.create(true), // 7
		r8       = Temp.create(true), // 8
		r9       = Temp.create(true), // 9
		r10      = Temp.create(true), // 10
		$frame   = Temp.create(true), // 11
		$scratch = Temp.create(true), // 12
		$stack   = Temp.create(true), // 13
		$link    = Temp.create(true), // 14
		$counter = Temp.create(true); // 15

	public static final List<Temp> registers = Arrays.asList(
			a0, a1, a2, a3, r4, r5, r6, r7, r8, r9, r10,
			$frame, $scratch, $stack, $link, $counter
		);

	public static final List<Temp> available = Arrays.asList(
			a0, a1, a2, a3, r4, r5, r6, r7, r8, r9, r10, $frame
		);

	public static final List<Temp> volatyle = Arrays.asList(
			// a1, a2, a3,
			// $scratch, $link
		);

	static final List<Temp>
		specials = Arrays.asList(
			$stack, $link, $counter
		),
		arguments = Arrays.asList(
			a0, a1, a2, a3
		),
		callee_saved = Arrays.asList(
			r4, r5, r6, r7, r8, r9, r10, $frame
		),
		caller_saved = Arrays.asList(
			$scratch
		),
		calldefs = Arrays.asList(
			// a0
			a0, a1, a2, a3
			// $scratch, $link
		),
		returnSink = Arrays.asList(
			a0, r4, r5, r6, r7, r8, r9, r10, //$frame,
			$stack, //$link,
			$counter
		);

	static final Map<Temp, String> t2s = new HashMap<Temp, String>() {{
		put(a0,      "r0");  // argument / scratch
		put(a1,      "r1");
		put(a2,      "r2");
		put(a3,      "r3");
		put(r4,      "r4");  // variable
		put(r5,      "r5");
		put(r6,      "r6");
		put(r7,      "r7");
		put(r8,      "r8");
		put(r9,      "r9");  // platform specific
		put(r10,     "r10"); // variable
		put($frame,  "fp");  // Frame Pointer / variable
		put($scratch,"ip");  // Intra-Procedure-call scratch
		put($stack,  "sp");  // Stack Pointer
		put($link,   "lr");  // Link Register
		put($counter,"pc");  // Program Counter
	}};

	private boolean makesACall = false;
	private int size;
	private int offset;
	private int maximumStackArguments = 0;
	private final int ARGS_IN_REGISTERS = arguments.size();
	private static final int PUSH_REGS = volatyle.size();

	ARMFrame(Label label, boolean[] args) {
		super(label);

		params = new ArrayList<Access>(args.length);

		offset = WORDSIZE;
		int usedRegisters = 0;
		for (int i = 0; i < args.length; i++) {
			// boolean escapes = args[i];

			// Escapes aren't implemented since no Front-end supports it.
			// if (i < arguments.size()) {
				params.add(InRegister.create());
			// } else {
			// 	params.add(InFrame.create(nextOffset()));
			// }
		}
	}

	public int wordSize() {
		return WORDSIZE;
	}

	private int nextOffset() {
		int curr = offset;
		offset += WORDSIZE;
		return curr;
	}

	public Access allocLocal(boolean escapes) {
		if (escapes) {
			return InFrame.create(nextOffset());
		}
		return InRegister.create();
	}

	public IRNode externalCall(String func, NodeList args) {
		return Call.create(func, args);
	}

	private int maxArg;
	public void prepareForArguments(int num) {
		makesACall = true;
		maxArg = num;
		maximumStackArguments =
			Math.max(maximumStackArguments, num - ARGS_IN_REGISTERS);
	}

	public IRStatement nextArgument(int i, IRNode val) {
		if (i < ARGS_IN_REGISTERS) {
			return Move.create(
				arguments.get(i),
				val);
		} else {
			return Move.create(
				Memory.create(
					Binary.create(
						BinOp.PLUS,
						$stack,
						(i - ARGS_IN_REGISTERS) * WORDSIZE)),
				val);
		}
	}

	public List<Temp> getArgs(int num) {
		return arguments.subList(0, Math.min(num, ARGS_IN_REGISTERS));
	}

	public Temp getFramePointer() {
		return $frame;
	}

	public Temp getReturnReg() {
		return a0;
	}

	public IRStatement viewShift(IRStatement body) {
		// StmtSequence seq = new StmtSequence();

		// for (int i = 0; i < params.size(); i++) {
		// 	if (i < arguments.size()) {
		// 		seq.then(
		// 			Move.create(
		// 				params.get(i).value($frame),
		// 				arguments.get(i)));
		// 	} else {
		// 		seq.then(
		// 			Move.create(
		// 				params.get(i).value($frame),
		// 				Memory.create(
		// 					Binary.create(
		// 						BinOp.PLUS,
		// 						$frame,
		// 						(params.size() - i) * WORDSIZE))));
		// 	}
		// }

		// return seq.then(body);
		return body;
	}

	public void appendSink(InstructionSequence seq) {
		seq.then(Operation.createUse(returnSink));
	}

	private static Temp[]
		stmdb = new Temp[]
			{ $stack, r4, r5, r6, r7, r8, r9, r10, $frame, $link },
		ldmia = new Temp[]
			{ $stack, r4, r5, r6, r7, r8, r9, r10, $frame, $counter };

	/**
	 * Opens a function frame
	 */
	public void openFunction(InstructionSequence seq) {
		seq
			.then(is.comment("Function " + name.label))
			.then(is.label(name))
			.then(Operation.createDef(registers));
			// .then(Operation.create("stmfd   sp!, {fp, lr}"))
			// .then(is.add($frame, $stack, Const.create(4)))
			// .then(Operation.create("stmfd   sp!, {r4-r10}"))
			// .then(Operation.createUse(callee_saved));

		seq
			.then(Operation.create("push    {r4-r11, lr}"))
			.then(is.sub($stack, $stack,
				Const.create(maximumStackArguments * WORDSIZE)))
			.then(Operation.createUse(callee_saved));

		// if (makesACall) {
		// 	seq.then(is.sub($stack, $stack,
		// 		Const.create((volatyle.size() + maximumStackArguments) * WORDSIZE)));
		// }

		seq.then(Operation.createDef(arguments));

		for (int i = 0; i < params.size(); i++) {
			Temp t = (Temp) params.get(i).value($frame);

			seq.then(is.comment("def: " + t));

			if (i < ARGS_IN_REGISTERS) {
				seq.then(is.move(t, arguments.get(i)));
			} else {
				// seq.then(is.load(t, $frame,
				// 	Const.create((i - ARGS_IN_REGISTERS + 1) * WORDSIZE)));

				seq.then(is.load(t, $stack,
					Const.create((i - ARGS_IN_REGISTERS + 9 + maximumStackArguments) * WORDSIZE)));
			}
		}
	}

	/**
	 * Closes a function frame
	 */
	public void closeFunction(InstructionSequence seq) {
		// appendSink(seq);

		// if (makesACall) {
		// 	seq.then(is.add($stack, $stack,
		// 		Const.create((volatyle.size() + maximumStackArguments) * WORDSIZE)));
		// }

		seq
			.then(is.add($stack, $stack,
				Const.create(maximumStackArguments * WORDSIZE)))
			.then(Operation.create("pop     {r4-r11, pc}"))
			// .then(Operation.create("ldmfd   sp!, {r4-r10}"))
			.then(Operation.createDef(callee_saved))
			// .then(is.sub($stack, $frame, Const.create(4)))
			// .then(Operation.create("ldmfd   sp!, {fp, pc}"))
			.then(Operation.createDef(Arrays.asList($frame, $counter)))
			.then(Operation.createUse(returnSink))
			.then(is.comment("End " + name.label));
	}

	public InstructionSequence scaffold(InstructionSequence body) {
		InstructionSequence seq = new InstructionSequence();
		openFunction(seq);
		seq.then(body);
		closeFunction(seq);
		return seq;
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"<ARMFrame>\":");
		name.toString(sb);
		sb.append("}");
	}
}


