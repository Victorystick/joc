package mjc.live;

import java.util.List;
import mjc.ir.Temp;
import mjc.asm.MoveOperation;

abstract public class InterferenceGraph extends Graph<Temp> {
	abstract public FlowAnalysis getFlow();

	abstract public int degree(Temp t);

	abstract public List<MoveOperation> moves();
}
