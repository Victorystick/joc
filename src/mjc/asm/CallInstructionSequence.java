package mjc.asm;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import underscore.Mapper;

import mjc.ir.Temp;
import mjc.ir.Label;

public class CallInstructionSequence extends InstructionSequence {

	private List<Temp> uses = new LinkedList<Temp>();
	private List<Temp> defs = new ArrayList<Temp>();

	public void setUse(List<Temp> used) {
		uses = used;
	}

	public void setDefines(List<Temp> defines) {
		defs = defines;
	}

	public InstructionSequence then(Instruction i) {
		if (i instanceof InstructionSequence) {
			InstructionSequence is = ((InstructionSequence) i);
			for (Instruction ins : is.list) {
				uses.addAll(ins.use()); //We need to "use" all that the internal stuff uses.
			}
			list.addAll(is.list);
		} else if (i != null) {
			list.add(i);
			uses.addAll(i.use());
		}

		return this;
	}

	public InstructionSequence then_(Instruction i) {
		list.add(i);
		uses.addAll(i.use()); //We need to "use" all that the internal stuff uses.
		return this;
	}

	public InstructionSequence before(Instruction i) {
		if (i instanceof InstructionSequence) {
			list.addAll(0, ((InstructionSequence) i).list);
		} else if (i != null) {
			list.add(0, i);
		}

		return this;
	}

	public List<Temp> use() {
		if (uses == null) {
			return Collections.<Temp>emptyList();
		} else {
			return uses;
		}
	}

	public List<Temp> def() {
		if (defs == null) {
			return Collections.<Temp>emptyList();
		} else {
			return defs;
		}
	}

	public List<Label> jumps() {
		return Collections.<Label>emptyList();
	}

	public String allocate(Mapper<Temp, String> allocator) {
		return allocateAll(allocator);
	}
}
