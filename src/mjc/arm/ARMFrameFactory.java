package mjc.arm;

import java.util.List;
import mjc.frame.Frame;
import mjc.frame.FrameFactory;
import mjc.ir.Temp;
import mjc.ir.Label;

public class ARMFrameFactory extends FrameFactory {
	static final Temp fp = ARMFrame.$frame;
	static final Temp lr = ARMFrame.$link;
	static final Temp pc = ARMFrame.$counter;

	public int getWordSize() {
		return ARMFrame.WORDSIZE;
	}

	public Frame create(Label l, int narg) {
		return create(l, new boolean[narg]);
	}

	public Frame create(Label l, boolean[] args) {
		return new ARMFrame(l, args);
	}

	public List<Temp> getAvailable() {
		return ARMFrame.available;
	}

	public List<Temp> getRegisters() {
		return ARMFrame.registers;
	}

	public String map(Temp t) {
		return ARMFrame.t2s.get(t);
	}
}
