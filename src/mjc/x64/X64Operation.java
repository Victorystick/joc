package mjc.x64;

import java.util.*;
import mjc.ir.Temp;
import mjc.asm.Operation;
import mjc.asm.Instruction;
import underscore.*;

class X64Operation extends Operation {
	static final Map<String, String> quad2byte = new HashMap<String, String>() {{
		put("%rax", "%al");
		put("%rbx", "%bl");
		put("%rcx", "%cl");
		put("%rdx", "%dl");
		put("%rsp", "%spl"); // stack
		put("%rbp", "%bpl");
		put("%rsi", "%sil");
		put("%rdi", "%dil");
		put("%r8", "%r8b");
		put("%r9", "%r9b");
		put("%r10", "%r10b");
		put("%r11", "%r11b");
		put("%r12", "%r12b");
		put("%r13", "%r13b");
		put("%r14", "%r14b");
		put("%r15", "%r15b");
	}};

	int registerSize;

	static Instruction createByte(String asm, Temp t) {
		return new X64Operation(asm, 1, t);
	}

	private X64Operation(String asm, int regSize, Temp t) {
		super(asm, Arrays.asList(t), X64InstructionSet._0, X64InstructionSet._);

		registerSize = regSize;
	}

	public String allocate(Mapper<Temp, String> allocator) {
		Map<String, String> map = null;

		switch (registerSize) {
			case 1:
				map = quad2byte;
					break;
		}

		List<String> regList = _.map(temps, allocator);

		if (map != null) {
			regList = _.map(regList, map);
		}

		Object[] regNames = regList.toArray();

		if (Arrays.asList(regNames).contains(null)) {
			return null;
		}

		return String.format(assembly, regNames);
	}
}
