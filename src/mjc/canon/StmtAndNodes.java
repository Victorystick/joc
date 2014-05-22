package mjc.canon;

import mjc.ir.IRStatement;
import mjc.ir.NodeList;

class StmtAndNodes {
	IRStatement stmt;
	NodeList nodes;

	StmtAndNodes(IRStatement s, NodeList l) {
		stmt = s;
		nodes = l;
	}
}
