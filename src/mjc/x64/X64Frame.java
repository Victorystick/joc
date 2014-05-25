package mjc.x64;

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

public class X64Frame extends Frame {
	private static final X64InstructionSet is =
		X64InstructionSet.getInstance();
	public static final int WORDSIZE = 8;

	public static final Temp
		r0       = Temp.create(true), // 0
		r1       = Temp.create(true), // 1
		r2       = Temp.create(true), // 2
		r3       = Temp.create(true), // 3
		r4       = Temp.create(true), // 4
		r5       = Temp.create(true), // 5
		r6       = Temp.create(true), // 6
		r7       = Temp.create(true), // 7
		r8       = Temp.create(true), // 8
		r9       = Temp.create(true), // 9
		r10      = Temp.create(true), // 10
		r11      = Temp.create(true), // 11
		r12      = Temp.create(true), // 12
		r13      = Temp.create(true), // 13
		r14      = Temp.create(true), // 14
		r15      = Temp.create(true); // 15

	public static final Temp
		eax = r0,
		ebx = r1,
		ecx = r2,
		edx = r3,

		esp = r4,
		ebp = r5,
		esi = r6,
		edi = r7,

		r8d = r8,
		r9d = r9,
		r10d = r10,
		r11d = r11,

		r12d = r12,
		r13d = r13,
		r14d = r14,
		r15d = r15,

		$stack = esp,
		$frame = ebp;


	public static final List<Temp> registers = Arrays.asList(
			r0, r1, r2, r3, r4, r5, r6, r7,
			r8, r9, r10, r11, r12, r13, r14, r15
		);

	public static final List<Temp> available = Arrays.asList(
			eax, ebx, ecx, edx, ebp, esi, edi,
			r8d, r9d, r10d, r11d, r12d, r13d, r14d, r15d
		);

	public static final List<Temp> volatyle = Arrays.asList(
			r10d, r11d
		);

	static final List<Temp>
		specials = Arrays.asList(
			r13d, r14d, r15d
		),
		arguments = Arrays.asList(
			edi, esi, edx, ecx, r8d, r9d
		),
		callee_saved = Arrays.asList(
			ebx, esp, ebp, r12d, r13d, r14d, r15d
		),
		calldefs = Arrays.asList(
			eax, ecx, edx, esi, edi, r8d, r9d
		),
		returnSink = Arrays.asList(
			// r0, r4, r5, r6, r7, r8, r9, r10, //r11,
			// r13, //r14,
			// r15
		);

	static final Map<Temp, String> t2s = new HashMap<Temp, String>() {{
		put(r0,      "%rax"); // argument / scratch
		put(r1,      "%rbx");
		put(r2,      "%rcx");
		put(r3,      "%rdx");
		put(r4,      "%rsp"); // variable
		put(r5,      "%rbp");
		put(r6,      "%rsi");
		put(r7,      "%rdi"); // stack pointer?
		put(r8,      "%r8");
		put(r9,      "%r9");  // platform specific
		put(r10,     "%r10"); // variable
		put(r11,     "%r11");  // Frame Pointer / variable
		put(r12,     "%r12");  // Intra-Procedure-call scratch
		put(r13,     "%r13");  // Stack Pointer
		put(r14,     "%r14");  // Link Register
		put(r15,     "%r15");  // Program Counter
	}};

	private boolean makesACall = false;
	private int size;
	private int offset;
	private int maximumStackArguments = 0;
	private final int ARGS_IN_REGISTERS = arguments.size();
	private static final int PUSH_REGS = volatyle.size();

	X64Frame(Label label, boolean[] args) {
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
						r13,
						(i - ARGS_IN_REGISTERS) * WORDSIZE / 2)),
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
		return eax;
	}

	public IRStatement viewShift(IRStatement body) {
		// StmtSequence seq = new StmtSequence();

		// for (int i = 0; i < params.size(); i++) {
		// 	if (i < arguments.size()) {
		// 		seq.then(
		// 			Move.create(
		// 				params.get(i).value(r11),
		// 				arguments.get(i)));
		// 	} else {
		// 		seq.then(
		// 			Move.create(
		// 				params.get(i).value(r11),
		// 				Memory.create(
		// 					Binary.create(
		// 						BinOp.PLUS,
		// 						r11,
		// 						(params.size() - i) * WORDSIZE))));
		// 	}
		// }

		// return seq.then(body);
		return body;
	}

	public void appendSink(InstructionSequence seq) {
		seq.then(Operation.createUse(returnSink));
	}

	/**
	 * Opens a function frame
	 */
	public void openFunction(InstructionSequence seq) {
		seq
			.then(Operation.create(".type  " + name.label + ", @function"))
			.then(is.comment("Function " + name.label))
			.then(is.label(name))
			.then(Operation.createDef(registers))
			// .then(Operation.create("enter   " +
			// 	maximumStackArguments * WORDSIZE / 2 + ", 0"))
			// .then(is.sub($stack, $stack,
			// 	Const.create(maximumStackArguments * WORDSIZE / 2)))
			.then(Operation.createUse(callee_saved));

		// if (makesACall) {
		// 	seq.then(is.sub($stack, $stack,
		// 		Const.create((volatyle.size() + maximumStackArguments) * WORDSIZE)));
		// }

		seq.then(Operation.createDef(arguments));

		for (int i = 0; i < params.size(); i++) {
			Temp t = (Temp) params.get(i).value(r11);

			seq.then(is.comment("def: " + t));

			if (i < ARGS_IN_REGISTERS) {
				seq.then(is.move(t, arguments.get(i)));
			} else {
				// seq.then(is.load(t, r11,
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
			// .then(is.add($stack, $stack,
			// 	Const.create(maximumStackArguments * WORDSIZE / 2)))
			// .then(Operation.create("leave"))
			.then(Operation.createDef(callee_saved))
			.then(Operation.createDef(Arrays.asList(r11, r15)))
			.then(Operation.createUse(returnSink))
			.then(Operation.create("ret"))
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
		sb.append("{\"<X64Frame>\":");
		name.toString(sb);
		sb.append("}");
	}
}


