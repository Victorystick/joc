package mjc.alloc;

import java.util.*;
import mjc.asm.InstructionSequence;
import mjc.asm.MoveOperation;
import mjc.CompilerOptions;
import mjc.Errors;
import mjc.frame.FrameFactory;
import mjc.ir.Temp;
import mjc.live.*;
import underscore._;
import underscore.Mapper;

public class OptimisticAllocator implements Allocator {
	private Deque<Triple<Temp, Set<Temp>, Boolean>> stack;
	private FrameFactory factory;
	private CompilerOptions opts;
	private final Temp defaultReg;
	private final int K;
	private final int numRegs;
	private Map<Temp, Temp> color;

	private InterferenceGraph live;

	private final Mapper<Temp, Temp> mapToColor = new Mapper<Temp, Temp>() {
		public Temp map(Temp t) {
			if (!color.containsKey(t)) {
				Errors.debug("Null-allocation of temp " + t);
				return defaultReg;
			}

			return color.get(t);
		}
	};

	public OptimisticAllocator(CompilerOptions o, FrameFactory f) {
		opts = o;
		factory = f;
		defaultReg = f.getRegisters().get(0);
		K = f.getRegisters().size();
		numRegs = f.getRegisters().size();
	}

	/**
	 * Attempts to allocate an instruction sequence
	 * given an InterferenceGraph.
	 */
	public InstructionSequence allocate( InstructionSequence is ) {
		FlowAnalysis flow = new FlowAnalysis(is);

		live = new Interference(flow);

		List<Temp> availableColors;
		List<Temp> uncolored = Temp.nonregisters;

		Map<Temp, Set<Temp>> regsOf = new HashMap<Temp, Set<Temp>>();

		for (Temp node : live.nodes()) {
			regsOf.put(node, new HashSet<Temp>());
		}

		color = new HashMap<Temp, Temp>();

		for (Temp reg : factory.getRegisters()) {
			color.put(reg, reg);

			for (Temp other : live.successors(reg)) {
				regsOf.get(other).add(reg);
			}

			live.remove(reg);
		}

		// for (Temp reg : factory.getRegisters()) {
		// 	if (mapToColor.map(reg) != reg) {
		// 		Errors.fatal("Register doesn't map to itself!");
		// 	}
		// 	System.out.println(factory.map(mapToColor.map(reg)));
		// }

		stack = new ArrayDeque<Triple<Temp, Set<Temp>, Boolean>>();

		if (opts.debug) {
			// System.out.println(is);
			// System.out.println(live.getFlow());
			// System.out.println(live);
		}

		while (!live.isEmpty()) {
			Temp t = live.getNodeOfLowestDegree();

			Set<Temp> set = null;

			if (!color.containsKey(t)) {
				/**
				 * Simple nodes are taken care of.
				 */
				// if (live.degree(t) < K) {
					set = new HashSet<Temp>(live.successors(t));
					stack.addFirst(
						new Triple<Temp, Set<Temp>, Boolean>(t, set, false));
				// } else {
				// 	set = new HashSet<Temp>(live.successors(t));
				// 	stack.addFirst(
				// 		new Triple<Temp, Set<Temp>, Boolean>(t, set, true));
				// }
			}

			// if (set != null) {
			// 	for (Temp m : set) {
			// 		if (live.degree(m) == K) {
			// 			Set<Temp> adj = live.adjacent(m);
			// 			adj.add(m);
			// 			enableMoves(adj);
			// 		}
			// 	}
			// }

			live.remove(t);
		}

		List<Temp> unallocable = new LinkedList<Temp>();

		while (!stack.isEmpty()) {
			availableColors = new LinkedList<Temp>(factory.getAvailable());
			// System.out.println(availableColors);

			Triple<Temp, Set<Temp>, Boolean> tri = stack.removeFirst();

			availableColors.removeAll(regsOf.get(tri.first));
			availableColors.removeAll(_.map(tri.second, mapToColor));

			// System.out.println("colors for: " + tri.first);
			// System.out.println("interferes with: " + _.map(tri.second, mapToColor));
			// System.out.println(availableColors);

			if (availableColors.isEmpty()) {
				unallocable.add(tri.first);
				// Errors.warn(
				// 	String.format("Unable to allocate %d temporaries to registers.",
				// 		stack.size()));
			} else {
				color.put(tri.first, availableColors.get(0));
			}
		}

		if (!unallocable.isEmpty()) {
			Errors.error(
				String.format(
					"Unable to allocate %d temporaries to registers.\n%s",
					unallocable.size(),
					unallocable));
		}

		return is;
	}

	public Mapper<Temp, Temp> getMapper() {
		return mapToColor;
	}
}
