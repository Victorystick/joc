package mjc.ir;

import java.util.ArrayList;

public class NodeList extends ArrayList<IRNode> {
	private static final NodeList EMPTY = new NodeList();

	public static NodeList create(IRNode ...stmts) {
		NodeList seq = new NodeList();

		for (IRNode node : stmts) {
			seq.then(node);
		}

		return seq;
	}

	public static NodeList emptyList() {
		return EMPTY;
	}

	public NodeList() {
		super();
	}

	public NodeList then(NodeList nodes) {
		addAll(nodes);

		return this;
	}

	public NodeList then(IRNode node) {
		add(node);

		return this;
	}
}
