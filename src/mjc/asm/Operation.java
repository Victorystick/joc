package mjc.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mjc.ir.Temp;

public class Operation extends Instruction {
	protected List<Temp> used = null, defined = null;

	public static Instruction create(String a) {
		return new NonTempInstruction(a);
	}

	public static Instruction createUse(List<Temp> temps) {
		return new DefUseInstruction(null, temps);
	}

	public static Instruction createDef(List<Temp> temps) {
		return new DefUseInstruction(temps, null);
	}

	public static Instruction create(String a, Temp... ts) {
		return new Operation(a, Arrays.asList(ts));
	}

	public static Instruction create(String a, int[] def, int[] use, Temp... ts) {
		return new Operation(a, Arrays.asList(ts), def, use);
	}

	Operation(String a, List<Temp> ts) {
		super(a);
		temps = ts;
	}

	public Operation(String a, List<Temp> ts, int[] def, int[] use) {
		super(a);
		temps = ts;

		defined = new ArrayList<Temp>(def.length);
		for (int i : def) {
			if (ts.get(i) == null) {
				throw new NullPointerException("Found a null definition: " + i +
					"\n" + assembly + ":::" + temps);
			}
			defined.add(ts.get(i));
		}

		used = new ArrayList<Temp>(def.length);
		for (int i : use) {
			if (ts.get(i) == null) {
				throw new NullPointerException("Found a null usage: " + i +
					"\n" + assembly + ":::" + temps);
			}
			used.add(ts.get(i));
		}
	}

	public List<Temp> use() {
		if (used == null)
			return super.use();
		return used;
	}

	public List<Temp> def() {
		if (defined == null)
			return super.def();
		return defined;
	}
}
