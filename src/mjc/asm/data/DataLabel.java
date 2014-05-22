package mjc.asm.data;

import mjc.ir.Label;

public class DataLabel implements DataInstruction {
	private final Label label;

	DataLabel(Label l) {
		label = l;
	}

	public static DataLabel create(Label l) {
		return new DataLabel(l);
	}

	public Label getLabel() {
		return label;
	}
}
