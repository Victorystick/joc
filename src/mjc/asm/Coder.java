package mjc.asm;

import mjc.asm.InstructionSet;
import mjc.asm.InstructionSequence;
import mjc.frame.Frame;
import mjc.ir.IRStatement;
import mjc.asm.data.DataCoder;

public interface Coder extends DataCoder {
	public InstructionSequence generateCode(Frame f, IRStatement stm);
	public InstructionSequence doneCode();
}
