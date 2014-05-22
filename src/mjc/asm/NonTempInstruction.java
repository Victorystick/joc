package mjc.asm;

import mjc.ir.Temp;
import java.util.List;

public class NonTempInstruction extends Instruction {
	NonTempInstruction(String a) {
		super(a);
	}

	public NonTempInstruction(String a, List<Temp> ts) {
		super(a);
		temps = ts;
	}

	public String allocate(underscore.Mapper<Temp, String> allocator) {
		return assembly;
	}
}
