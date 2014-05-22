package mjc.frame;

import mjc.ir.*;
import mjc.asm.InstructionSequence;
import java.util.List;

public abstract class Frame {
	public Label name;
	public List<Access> params;

	public Frame(Label n) {
		name = n;
	}

	abstract public void toString(StringBuilder sb);

	abstract public int wordSize();

	abstract public Access allocLocal(boolean escape);
	abstract public IRNode externalCall(String func, NodeList args);

	abstract public void prepareForArguments(int num);
	abstract public IRStatement nextArgument(int i, IRNode value);
	abstract public List<Temp> getArgs(int num);

	// Registers
	abstract public Temp getFramePointer();
	abstract public Temp getReturnReg();

	// Confusing exits.
	// procEntryExit1
	abstract public IRStatement viewShift(IRStatement body);
	// procEntryExit2
	abstract public void appendSink(InstructionSequence seq);
	// procEntryExit3
	abstract public InstructionSequence scaffold(InstructionSequence seq);
	abstract public void openFunction(InstructionSequence seq);
	abstract public void closeFunction(InstructionSequence seq);
}
