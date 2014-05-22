package mjc.translate;

import java.util.HashMap;
import java.util.Map;
import mjc.*;
import mjc.minijava.*;
import mjc.ir.Binary;
import mjc.ir.BinOp;
import mjc.ir.Const;
import mjc.ir.IRNode;
import mjc.ir.IRStatement;
import mjc.ir.Temp;

public class ClassData {
	private static final Map<ClassSymbol, ClassData> map =
		new HashMap<ClassSymbol, ClassData>();

	private final ClassData supr;
	private final int maxOffset;
	private final Map<String, Integer> offsets =
		new HashMap<String, Integer>();
	public final String name;
	private final MethodIndex methodIndex;

	public static ClassData create(ClassSymbol cls) {
		if (map.containsKey(cls)) {
			return map.get(cls);
		}

		ClassData data = new ClassData(cls);

		map.put(cls, data);

		return data;
	}

	private ClassData(ClassSymbol cls) {
		int offset = 4; // WORDSIZE;
		int methodOffset;

		if (cls.getSuper() != null) {
			supr = create(cls.getSuper());

			offset = supr.getMaxOffset();
			methodIndex = MethodIndex.create(supr.getMethodIndex());
		} else {
			supr = null;
			methodIndex = MethodIndex.create();
		}

		for (VarSymbol field : cls.getFields()) {
			offsets.put(field.getIdentifier(), offset);
			offset += 4; // WORDSIZE;
		}

		for (MethodSymbol method : cls.getMethods()) {
			String methodName = method.getIdentifier();
			methodIndex.put(methodName, method.getCompleteIdentifier());
			methodOffset = methodIndex.get(methodName);
			method.setOffset(methodOffset);
		}

		name = cls.getIdentifier();
		maxOffset = offset;
	}

	public String toString() {
		String extnd = "";

		if (supr != null) {
			extnd = " extends " + supr.name;
		}

		return "{"+
				"\"name\":\"" + name + extnd +
			"\"}";
	}

	private int getMaxOffset() {
		return maxOffset;
	}

	public MethodIndex getMethodIndex() {
		return methodIndex;
	}

	public int getOffset(String name) {
		if (offsets.containsKey(name)) {
			return offsets.get(name);
		}

		if (supr != null) {
			return supr.getOffset(name);
		}

		Errors.error("Couldn't find offset of var: " + name);
		return 0;
	}

	public IRStatement writeField(Temp tis, String name, IRNode value) {
		return null;
	}
}
