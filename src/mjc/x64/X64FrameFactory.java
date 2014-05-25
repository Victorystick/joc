package mjc.x64;

import java.util.List;
import mjc.frame.Frame;
import mjc.frame.FrameFactory;
import mjc.ir.Temp;
import mjc.ir.Label;
import mjc.asm.InstructionSet;

public class X64FrameFactory extends FrameFactory {
	public int getWordSize() {
		return X64Frame.WORDSIZE;
	}

	public Frame create(Label l, int narg) {
		return create(l, new boolean[narg]);
	}

	public Frame create(Label l, boolean[] args) {
		return new X64Frame(l, args);
	}

	public List<Temp> getAvailable() {
		return X64Frame.available;
	}

	public List<Temp> getRegisters() {
		return X64Frame.registers;
	}

	public String map(Temp t) {
		return X64Frame.t2s.get(t);
	}

	public InstructionSet getInstructionSet() {
		return X64InstructionSet.getInstance();
	}
}
