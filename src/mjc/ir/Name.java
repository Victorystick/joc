package mjc.ir;

public class Name extends IRNode {
	public Label label;

	Name(Label l) {
		label = l;
	}

	public void toString(StringBuilder sb) {
		sb.append("{\"name\":");
		label.toString(sb);
		sb.append("}");
	}
}
