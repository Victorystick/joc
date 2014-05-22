package mjc.arm;

import java.util.HashMap;
import java.util.Map;
import mjc.frame.Access;
import mjc.ir.*;

class InFrame extends Access {
	public static Map<Integer, InFrame> map = new HashMap<Integer, InFrame>();
	int offset;

	public static InFrame create(int offset) {
		if (map.containsKey(offset)) {
			return map.get(offset);
		}

		InFrame in = new InFrame(offset);

		map.put(offset, in);

		return in;
	}

	public InFrame(int o) {
		offset = o;
	}

	public IRNode value(IRNode framePointer) {
		return Memory.create(
			Binary.create(
				BinOp.PLUS,
				framePointer,
				Const.create(offset)));
	}
}
