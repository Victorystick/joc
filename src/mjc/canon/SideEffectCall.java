package mjc.canon;

import mjc.ir.Call;
import mjc.ir.IRStatement;
import mjc.ir.NodeList;
import mjc.ir.SideEffect;

// Won't be used by our compiler.
// Here for compliance with Appel.
class SideEffectCall implements IRStatement {
	Call call;

	SideEffectCall(Call c) {
		call = c;
	}

	public NodeList children() {
		return call.children();
	}

	public IRStatement build(NodeList children) {
		return SideEffect.create(call.build(children));
	}

	public void toString(StringBuilder sb) {
		sb.append("\"SideEffectCall\"");
	}
}
