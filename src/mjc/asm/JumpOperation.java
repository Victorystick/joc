package mjc.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mjc.ir.Label;
import mjc.ir.Temp;
import mjc.Errors;

public class JumpOperation extends Operation {
	public static JumpOperation create(String a, Label l, boolean fallthrough) {
		return new JumpOperation(a, l, fallthrough);
	}

	public static JumpOperation create(String a, Temp t, boolean fallthrough) {
		return new JumpOperation(a, Arrays.asList(t), fallthrough);
	}

	public static JumpOperation create(String a, Label l, List<Temp> def, List<Temp> use) {
		JumpOperation jump = create(a, l, true);
		jump.defined = def;
		jump.used = use;
		return jump;
	}

	public static JumpOperation create(String a, Temp t, List<Temp> def, List<Temp> use) {
		JumpOperation jump = create(a, t, true);
		jump.defined = def;
		jump.used = use;
		return jump;
	}

	public static JumpOperation create(String a) {
		Errors.fatal("Avoid using JumpOperation.create(String). Need label!\n"+a);
		return null;
	}

	private List<Label> ls;
	private boolean fallthrough;

	JumpOperation(String a, Label l, boolean c) {
		super(a, Collections.<Temp>emptyList());
		ls = Arrays.asList(l);
		fallthrough = c;
	}

	JumpOperation(String a, List<Temp> ts, boolean c) {
		super(a, ts);
		ls = Collections.<Label>emptyList();
		fallthrough = c;
	}

	public List<Label> jumps() {
		return ls;
	}

	public boolean canFallThrough() {
		return fallthrough;
	}
}
