package mjc.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mjc.ir.Temp;
import underscore._;
import underscore.Mapper;

public class MoveOperation extends Operation {
	public static Instruction create(String a, Temp... ts) {
		return new MoveOperation(a, Arrays.asList(ts));
	}

	MoveOperation(String a, List<Temp> ts) {
		super(a, ts);

		defined = ts.subList(0, 1);

		if (ts.size() > 1) {
			used = ts.subList(1, 2);
		} else {
			used = Collections.<Temp>emptyList();
		}
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
