package mjc.asm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mjc.ir.Label;
import mjc.ir.Temp;
import underscore._;
import underscore.Mapper;

public class Instruction {
	public final String assembly;
	public List<Temp> temps;

	Instruction(String a) {
		assembly = a;
	}

	public String toString() {
		// return "{" + this.getClass().getName() + " def: " + def() + " use: " + use() + "}";
		return assembly;
		// return String.format(assembly, temps);
	}

	public List<Temp> use() {
		return Collections.<Temp>emptyList();
	}

	public List<Temp> def() {
		return Collections.<Temp>emptyList();
	}

	public List<Label> jumps() {
		return Collections.<Label>emptyList();
	}

	public String allocate(Mapper<Temp, String> allocator) {
		Object[] regNames = _.map(temps, allocator).toArray();

		if (Arrays.asList(regNames).contains(null)) {
			return null;
		}

		return String.format(assembly, regNames);
	}
}
