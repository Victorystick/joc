package mjc.alloc;

import java.util.*;
import mjc.asm.*;
import mjc.arm.ARMFrame;
import mjc.frame.FrameFactory;
import mjc.ir.Temp;
import mjc.live.*;
import mjc.Errors;
import mjc.CompilerOptions;
import underscore.*;

public class ColoringAllocator implements Allocator {
	private static class Move {
		Temp left, right;
		Move(Temp l, Temp r) {left = l; right = r;}
	}

	public ColoringAllocator(CompilerOptions o, FrameFactory ff) {
		factory = ff;
		options = o;
	}

	private final CompilerOptions options;
	private final FrameFactory factory;

	private FlowAnalysis flow;
	private InterferenceGraph live;

	private InstructionSequence instructions;
	private List<Instruction> instrList;

	private List<Temp> precolored;
	private int numRegs; // number of registers

	private List<Temp> available;
	private int k; // number of available registers
	private int totalTemps;

	// registers to allocate
	private Set<Temp> initial;

	// list of low-degree, non-move-related nodes
	private Deque<Temp> simplifyList;
	// low-degree move-related nodes
	private List<Temp> freezeList;
	// high-degree nodes
	private Deque<Temp> spillDeque;

	// nodes marked for spilling
	private List<Temp> spilledNodes;

	// registers that have been coalesced
	// when u <- v, v is added here and u is put in worklist
	private List<Temp> coalescedNodes;

	// successfully colored nodes
	private List<Temp> coloredNodes;

	// temps removed from the graph
	// private Deque<Tuple<Temp, Set<Temp>>> selectStack;
	private Deque<Temp> selectStack;

	// moves that have been coalesced
	private List<MoveOperation> coalescedMoves;

	// moves whose source and target interfere
	private List<MoveOperation> constrainedMoves;

	// moves that will no longer be considered for coalescing
	private List<MoveOperation> frozenMoves;

	// moves enabled for possible coalescing
	private Deque<MoveOperation> workDequeMoves;

	// moves not ready for coalescing
	private List<MoveOperation> activeMoves;

	// adjSet and adjList combined
	private List<Set<Temp>> adjSet;
	private List<Set<MoveOperation>> moveSets;
	private int[] color;
	private int[] alias;
	private int[] degree;

	private Mapper<Temp, Temp> mapToColor = new Mapper<Temp, Temp>() {
		public Temp map(Temp t) {
			return Temp.get(color[t.id]);
		}
	};

	public Mapper<Temp, Temp> getMapper() {
		return mapToColor;
	}

	public InstructionSequence allocate( InstructionSequence is ) {
		// throw new NullPointerException(
		// 	"ColoringAllocator is overriding the colors of precolored registers!!");

		instructions = is;
		instrList = is.getInstructions();

		flow = new FlowAnalysis(is);
		live = new Interference(flow);

		precolored = factory.getRegisters();
		numRegs = precolored.size();

		available = factory.getAvailable();
		k = available.size();

		// initial = Temp.nonregisters;
		// initial = new HashSet<Temp>();
		totalTemps = Temp.numberOfTemps();

		color = new int[totalTemps];
		alias = new int[totalTemps];
		degree = new int[totalTemps];

		for (int i = 0; i < numRegs; i++) {
			color[i] = i;
		}

		checkRegisterColors();

		adjSet = new ArrayList<Set<Temp>>(totalTemps);
		moveSets = new ArrayList<Set<MoveOperation>>(totalTemps);

		for (int i = 0; i < totalTemps; i++) {
			adjSet.add(new HashSet<Temp>());
			moveSets.add(new HashSet<MoveOperation>());
		}

		simplifyList = new ArrayDeque<Temp>();
		freezeList = new ArrayList<Temp>();
		spillDeque = new LinkedList<Temp>();

		spilledNodes = new ArrayList<Temp>();
		coalescedNodes = new ArrayList<Temp>();
		coloredNodes = new ArrayList<Temp>();

		selectStack = new ArrayDeque<Temp>();

		workDequeMoves = new ArrayDeque<MoveOperation>();

		coalescedMoves = new ArrayList<MoveOperation>();
		constrainedMoves = new ArrayList<MoveOperation>();
		frozenMoves = new ArrayList<MoveOperation>();
		activeMoves = new ArrayList<MoveOperation>();

		main();

		return null;
	}

	private void main() {
		build();
		makeWorklists();

		while (true) {
			if (!simplifyList.isEmpty()) {
				simplify();
			} else if (!workDequeMoves.isEmpty()) {
				coalesce();
			} else if (!freezeList.isEmpty()) {
				freeze();
			} else if (!spillDeque.isEmpty()) {
				selectSpill();
			} else {
				break;
			}
		}

		assignColors();

		if (!spilledNodes.isEmpty()) {
			Errors.error(
				String.format(
					"ColoringAllocator: Unable to allocate %d temps to registers:\n%s",
					spilledNodes.size(), spilledNodes.toString()));
		}
	}

	/**
	 * Builds the entire function as a single block.
	 */
	private void build() {
		// for blocks in program??
		// live = liveOut(block)

		// initial = new HashSet<Temp>();
		// Set<Temp> live = new HashSet<Temp>();

		// for (int i = instrList.size() - 1; i >= 0; i--) {
		// 	Instruction inst = instrList.get(i);

		// 	if (inst instanceof MoveOperation) {
		// 		MoveOperation move = (MoveOperation) inst;
		// 		live.removeAll(move.use());

		// 		for (Temp d : move.def()) {
		// 			moveSets.get(d.id).add(move);
		// 		}
		// 		for (Temp u : move.use()) {
		// 			moveSets.get(u.id).add(move);
		// 		}
		// 		workDequeMoves.add(move);
		// 	}

		// 	live.addAll(inst.def());

		// 	for (Temp d : inst.def()) {
		// 		for (Temp l : live) {
		// 			addEdge(l, d);
		// 		}
		// 	}

		// 	live.removeAll(inst.def());
		// 	live.addAll(inst.use());

		// 	initial.addAll(inst.def());
		// 	initial.addAll(inst.use());

		// 	//Errors.debug(inst);
		// 	// Errors.debug("\t\t" + Arrays.asList(live.toArray()));
		// }

		// live.removeAll(precolored);

		// if (!live.isEmpty()) {
		// 	for (Temp unassigned : live) {
		// 		String name = unassigned.getName();

		// 		if (name != null) {
		// 			Errors.warn("Variable '" + name +	"'" +
		// 				(options.debug ? " (" + unassigned + ")" : "") +
		// 				" isn't assigned a value before it is used.");
		// 		} else if (options.debug) {
		// 			Errors.warn("Variable " + unassigned +
		// 				" isn't assigned a value before it is used.");
		// 		}


		// 		adjSet.get(unassigned.id).clear();
		// 		degree[unassigned.id] = 0;
		// 	}
		// 	// These are never assigned to.
		// 	initial.removeAll(live);
		// }

		// initial.removeAll(precolored);

		initial = live.nodes();

		for (Temp a : initial) {
			for (Temp b : live.successors(a)) {
				addEdge(a, b);
			}
		}

		for (MoveOperation move : live.moves()) {
			for (Temp d : move.def()) {
				moveSets.get(d.id).add(move);
			}
			for (Temp u : move.use()) {
				moveSets.get(u.id).add(move);
			}
			workDequeMoves.add(move);
		}

		if (options.debug) {
			Errors.debug("---- Temp degrees: ----");
			for (int i = 0; i < degree.length; i++) {
				if (degree[i] > 0) {
					Errors.debug(i + ": " + degree[i]);
					Errors.debug("\t" + adjSet.get(i));
				}
			}
		}
	}

	private boolean isPrecolored(Temp t) {
		return t.id < numRegs;
	}

	private int degree(Temp t) {
		return degree[t.id];
	}

	/**
	 * Connects u and v with an edge.
	 */
	private void addEdge(Temp u, Temp v) {
		Set<Temp> set;

		if (u != v && !areAdjacent(u, v)) {

			if (!isPrecolored(u)) {
				set = adjSet.get(u.id);

				if (!set.contains(v)) {
					set.add(v);
					degree[u.id]++;
				}
			}

			if (!isPrecolored(v)) {
				set = adjSet.get(v.id);

				if (!set.contains(u)) {
					set.add(u);
					degree[v.id]++;
				}
			}
		}
	}

	private void makeWorklists() {
		//Errors.debug(Arrays.asList(initial.toArray()));

		for (Temp t : initial) {
			if (degree(t) >= k) {
				spillDeque.add(t);
			} else if (isMoveRelated(t)) {
				freezeList.add(t);
			} else {
				simplifyList.add(t);
			}
		}
	}

	/**
	 * Returns the set of all Temps adjacent to `n`.
	 *
	 * According to Appel, the coalescedNodes should not be
	 * included. However, during color selection this information
	 * is required.
	 */
	private Set<Temp> adjacent(Temp n) {
		Set<Temp> set = new HashSet<Temp>(adjSet.get(n.id));
		set.removeAll(selectStack);
		set.removeAll(coalescedNodes);
		return set;
	}

	private Set<Temp> adjacentDuringColoring(Temp n) {
		Set<Temp> set = new HashSet<Temp>(adjSet.get(n.id));
		set.removeAll(selectStack);
		// set.removeAll(coalescedNodes);
		return set;
	}

	private Set<MoveOperation> nodeMoves(Temp n) {
		Set<MoveOperation> set = new HashSet<MoveOperation>(activeMoves);
		set.addAll(workDequeMoves);
		set.retainAll(moveSets.get(n.id));
		// 	new HashSet<MoveOperation>(moveSets.get(n.id));
		// Set<MoveOperation> set2 =
		// 	new HashSet<MoveOperation>(moveSets.get(n.id));

		// set1.retainAll(activeMoves);
		// set2.retainAll(workDequeMoves);

		// set1.addAll(set2);
		return set;
	}

	private boolean isMoveRelated(Temp t) {
		return !nodeMoves(t).isEmpty();
	}

	private void simplify() {
		Temp t = simplifyList.removeFirst();

		if (!isPrecolored(t)) {
			selectStack.addFirst(t);
		}

		for (Temp m : adjacent(t)) {
			decrementDegree(m);
		}
	}

	/**
	 * Decrements the degree of Temp m
	 */
	private void decrementDegree(Temp m) {
		int d = degree(m);

		degree[m.id]--;

		if (d == k) {
			Set<Temp> set = adjacent(m);
			set.add(m);
			enableMoves(set);

			if (isMoveRelated(m)) {
				freezeList.add(m);
			} else {
				simplifyList.add(m);
			}
		}
	}

	/**
	 * Enables moves on a set of nodes.
	 */
	private void enableMoves(Set<Temp> nodes) {
		for (Temp n : nodes) {
			enableMoves(n);
		}
	}

	/**
	 * TODO: Unsure if this should be here.
	 *
	 * Appel defined enableMoves as taking `nodes` in plural.
	 * However, on page 238, referenced enableMoves of a single node.
	 * I assume this is what he meant.
	 */
	private void enableMoves(Temp n) {
		for (MoveOperation m : nodeMoves(n)) {
			if (activeMoves.contains(m)) {
				activeMoves.remove(m);
				workDequeMoves.add(m);
			}
		}
	}

	private void addWorkList(Temp m) {
		if (!isPrecolored(m) && !isMoveRelated(m) && degree(m) < k) {
			freezeList.remove(m);
			simplifyList.add(m);
		}
	}

	private boolean isOK(Temp t, Temp r) {
		return degree(t) < k || isPrecolored(t) || areAdjacent(t, r);
	}

	private boolean areAdjacent(Temp t, Temp r) {
		return adjSet.get(t.id).contains(r) ||
			adjSet.get(r.id).contains(t);
	}

	private boolean conservative(Set<Temp> nodes) {
		int nodesOfDegreeGeqK = 0;

		for (Temp n : nodes) {
			if (degree(n) >= k) {
				nodesOfDegreeGeqK++;
			}
		}

		return nodesOfDegreeGeqK < k;
	}

	/**
	 * Alias for conservative(adjacent(u) U adjacent(v))
	 */
	private boolean conservative(Temp u, Temp v) {
		Set<Temp> set = adjacent(u);
		set.addAll(adjacent(v));
		return conservative(set);
	}

	/**
	 * Attempts to coalesce two temporaries
	 */
	private void coalesce() {
		Temp x, y, u, v;

		MoveOperation m = workDequeMoves.removeFirst();

		x = getAlias(m.def().get(0));
		y = getAlias(m.use().get(0));

		if (isPrecolored(y)) {
			u = y;
			v = x;
		} else {
			u = x;
			v = y;
		}

		if (u == v) {
			coalescedMoves.add(m);
			addWorkList(u);
			return;
		}

		if (isPrecolored(v) || areAdjacent(u, v)) {
			constrainedMoves.add(m);
			addWorkList(u);
			addWorkList(v);
			return;
		}

		if (isPrecolored(u) && allAdjacentOk(v, u) ||
				!isPrecolored(u) && conservative(u, v)) {

			combine(u, v);
			addWorkList(u);
			return;
		}

		activeMoves.add(m);
	}

	/**
	 * Returns true if all adjacent nodes of `v`
	 * are OK in respect to `u`.
	 */
	private boolean allAdjacentOk(Temp v, Temp u) {
		for (Temp t : adjacent(v)) {
			if (!isOK(t, u)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Combines the Temps `v` and `u`.
	 */
	private void combine(Temp u, Temp v) {
		Errors.debug("Combining " + u + " and " + v);

		if (freezeList.contains(v)) {
			freezeList.remove(v);
		} else {
			spillDeque.remove(v);
		}

		coalescedNodes.add(v);
		setAlias(v, u);
		moveSets.get(u.id).addAll(moveSets.get(v.id));
		enableMoves(v);

		for (Temp t : adjacent(v)) {
			addEdge(t, u);
			decrementDegree(t);
		}

		if (degree(u) >= k && freezeList.contains(u)) {
			freezeList.remove(u);
			spillDeque.add(u);
		}
	}

	private Temp getAlias(Temp t) {
		if (coalescedNodes.contains(t)) {
			return getAlias(Temp.get(alias[t.id]));
		}
		return t;
	}

	private void setAlias(Temp v, Temp u) {
		if (v.id < numRegs) {
			Errors.error("Attempting to set alias of register!");
		}

		alias[v.id] = u.id;
	}

	private void freeze() {
		Temp u = freezeList.remove(freezeList.size() - 1);
		simplifyList.add(u);
		freezeMoves(u);
	}

	private void freezeMoves(Temp u) {
		Temp x, y, v;

		u = getAlias(u);

		for (MoveOperation m : nodeMoves(u)) {
			x = m.def().get(0);
			y = m.use().get(0);

			y = getAlias(y);

			if (y == u) {
				v = getAlias(x);
			} else {
				v = y;
			}

			activeMoves.remove(m);
			frozenMoves.add(m);

			if (freezeList.contains(v) && nodeMoves(v).isEmpty()) {
				freezeList.remove(v);
				simplifyList.add(u);
			}
		}
	}

	private void selectSpill() {
		Temp m = spillDeque.removeFirst();
		// SEE Appels book. Something about heuristics.
		simplifyList.add(m);
		freezeMoves(m);
	}

	private void checkRegisterColors() {
		for (int col, i = 0; i < numRegs; i++) {
			col = color[i];

			if (i < numRegs && col != i) {
				new Exception(
					String.format("Reg %d was assigned color %d.",
						i, col)).printStackTrace();
				Errors.crash();
			}
		}
	}

	private void setColor(Temp t, Temp o) {
		color[t.id] = color[o.id];
	}

	private void assignColors() {
		LinkedList<Temp> okColors;

		checkRegisterColors();

		while (!selectStack.isEmpty()) {
			okColors = new LinkedList<Temp>();

			okColors.addAll(available);

			Temp node = selectStack.removeFirst();

			if (node.id < numRegs) {
				if (options.debug) {
					Errors.warn("ColoringAllocator.assignColors()\n" +
						"\tRegister " + node.id + " on selectStack");
				}
				continue;
			}

			for (Temp other : adjacentDuringColoring(node)) {
				Temp alias = getAlias(other);
					// Errors.debug(node + ": adj " + other +
					// 	" with color " + color[alias.id] );
				if (isPrecolored(alias) || coloredNodes.contains(alias)) {
					// Errors.debug(node + ": Removing adj " + other);
					okColors.remove(Temp.get(color[alias.id]));
				}
			}

			if (okColors.isEmpty()) {
				spilledNodes.add(node);
				if (options.debug) {
					Errors.debug("Spilling " + node);
				}
			} else {
				coloredNodes.add(node);
				if (options.debug) {
					Errors.debug("Assigning " + okColors.get(0) + " to " + node);
				}
				setColor(node, getAlias(okColors.get(0)));
				// color[node.id] = color[getAlias(okColors.get(0)).id];
			}
		}

		for (int i = 0; i < numRegs; i++) {
			if (coalescedNodes.contains(Temp.get(i))) {
				// Errors.error("Register " + i + " coalesced!!");
			}
		}

		for (Temp node : coalescedNodes) {
			setColor(node, getAlias(node));
			// color[node.id] = color[getAlias(node).id];
		}

		Set<Temp> set = new HashSet<Temp>(coloredNodes);
		set.retainAll(coalescedNodes);
		if (options.debug && !set.isEmpty()) {
			System.out.println("coalesced and colored overlap!!!");
			System.out.println(Arrays.asList(set.toArray()));
		}

		// if (initial.size() != coloredNodes.size() + coalescedNodes.size()) {
		// 	Errors.error(
		// 		String.format(
		// 			"Only allocated %d/%d temps",
		// 			coloredNodes.size() + coalescedNodes.size(),
		// 			initial.size()));
		// }

		checkRegisterColors();

		if (options.debug)
			Errors.debug("---- Non-zero colors: ----");

		for (int col, i = numRegs; i < color.length; i++) {
			col = color[i];

			if (options.debug && col != 0) {
				Errors.debug(i + ": " + col);
			}

			if (col >= numRegs) {
				Errors.error(
					String.format(
						"Temp %s was given color %s, which has no register.",
						Temp.get(i),
						Temp.get(col)));
			}
		}
	}
}
