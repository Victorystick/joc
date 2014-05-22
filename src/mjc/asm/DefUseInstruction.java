package mjc.asm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mjc.ir.Label;
import mjc.ir.Temp;
import underscore._;
import underscore.Mapper;

public class DefUseInstruction extends Instruction {
	private List<Temp> defines;
	private List<Temp> uses;

	DefUseInstruction(List<Temp> ds, List<Temp> us) {
		super(null);
		defines = ds;
		uses = us;
	}

	public List<Temp> def() {
		if (defines == null) {
			return super.def();
		}

		return defines;
	}

	public List<Temp> use() {
		if (uses == null) {
			return super.use();
		}

		return uses;
	}

	public String allocate(underscore.Mapper<Temp, String> allocator) {
		return null;
	}
}
