package mjc.ir;

import java.util.List;

public class Call extends IRNode {
	public IRNode func;
	public NodeList args;

	public static IRNode create(String name, NodeList args) {
		return create(Label.create(name), args);
	}

	public static IRNode create(Label l, NodeList args) {
		return new Call(l.getName(), args);
	}

	public static IRNode create(IRNode n, NodeList args) {
		return new Call(n, args);
	}

	Call(IRNode f, NodeList ns) {
		func = f;
		args = ns;
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"call\":");
		func.toString(sb);
		sb.append(",\"args\":");
		sb.append(args.toString());
		sb.append("}");
	}

	public NodeList children() {
		if (func instanceof Name) {
			return args;
		} else {
			return NodeList.create().then(func).then(args);
		}
	}

	public IRNode build(NodeList list) {
		if (!(func instanceof Name)) {
			func = list.remove(0);
		}
		args = list;
		return this;
	}
}
