package mjc.asm.data;

import mjc.ir.IRNode;
import mjc.ir.Name;
import mjc.ir.Const;
import mjc.Errors;

public class Data implements DataInstruction {
	private final IRNode value;
	private final Size size;

	Data(IRNode v, Size s) {
		value = v; size = s;
	}

	public static Data create(Const co, Size s) {
		int c = co.getValue();

		switch (s) {
			case B8:
				if (c < (1L << 8) && c >= -128) {
					return new Data(co, s);
				}
				break;
			case B16:
				if (c < (1L << 16) && c >= -128) {
					return new Data(co, s);
				}
				break;
			case B32:
				if (c < (1L << 32) && c >= -128) {
					return new Data(co, s);
				}
				break;
			default:
		}

		Errors.warn(
			"Cannot create constant " + c + " with size: " + s);
		return null;
	}

	public static Data create(Name n) {
		return new Data(n, Size.WORD);
	}

	public IRNode getValue() {
		return value;
	}

	public Size getSize() {
		return size;
	}
}
