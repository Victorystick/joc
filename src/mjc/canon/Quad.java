package mjc.canon;

import mjc.ir.Binary;
import mjc.ir.IRStatement;
import mjc.ir.Move;
import mjc.ir.NodeList;
import mjc.ir.Temp;

class Quad implements IRStatement {
	Temp dest;
	Binary bin;

	Quad(Temp d, Binary c) {
		dest = d;
		bin = c;
	}

	public NodeList children() {
		return bin.children();
	}

	public IRStatement build(NodeList children) {
		return Move.create(dest, bin.build(children));
	}

	public void toString(StringBuilder sb) {
		sb.append("\"MoveCall\"");
	}
}
