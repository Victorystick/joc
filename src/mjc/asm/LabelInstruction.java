package mjc.asm;

import mjc.ir.Label;

public class LabelInstruction extends NonTempInstruction {
	private Label label;

	public LabelInstruction(Label l) {
		super(l.label + ":");
		label = l;
	}

	public boolean is(Label l) {
		return label == l;
	}
}
