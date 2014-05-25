package mjc.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mjc.ir.Temp;
import underscore._;
import underscore.Mapper;

public class MoveOperation extends Operation {
	public static Instruction create(String a, Temp def, Temp use) {
		return new MoveOperation(a, Arrays.asList(def, use), use, def);
	}

	public static Instruction createInverted(String a, Temp use, Temp def) {
		return new MoveOperation(a, Arrays.asList(use, def), use, def);
	}

	MoveOperation(String a, List<Temp> ts, Temp use, Temp def) {
		super(a, ts);

		defined = Arrays.asList(def);
		used = Arrays.asList(use);
	}

	public String allocate(Mapper<Temp, String> allocator) {
		Object[] regNames = _.map(temps, allocator).toArray();

		// Remove the move if the source and target registers are the same.
		if (regNames.length == 2 && regNames[0].equals(regNames[1])) {
			return null;
		}

		return String.format(assembly, regNames);
	}
}
