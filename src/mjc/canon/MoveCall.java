package mjc.canon;

import mjc.ir.Call;
import mjc.ir.IRStatement;
import mjc.ir.Move;
import mjc.ir.NodeList;
import mjc.ir.Temp;

// Nice for some reason?
class MoveCall implements IRStatement {
	Temp dest;
	Call call;

	MoveCall(Temp d, Call c) {
		dest = d;
		call = c;
	}

	public NodeList children() {
		return call.children();
	}

	public IRStatement build(NodeList children) {
		return Move.create(dest, call.build(children));
	}

	public void toString(StringBuilder sb) {
		sb.append("\"MoveCall\"");
	}
}
