package mjc.translate;

import java.util.HashMap;
import java.util.Map;
import mjc.ir.*;
import mjc.translate.SizeOf;

public class New {
	public static IRNode create(SizeOf size, String name) {
		return create(size.sizeOf(), name);
	}

	public static IRNode create(int size, String name) {
		Temp result = Temp.create();

		return
			StmtAndValue.create(
				StmtSequence.create(
					Move.create(result,
						Call.create(
							"_minijavalib_allocbytes",
							NodeList.create(Const.create(size)))),
					Move.create(
						Memory.create(result),
						Label.create("_mthds_" + name).getName())),
				result);
	}
}
