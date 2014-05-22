package mjc.live;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import mjc.ir.*;

public class Graph<T> {
	Map<T, Set<T>> successorList;
	Map<T, Set<T>> predecessorList;

	public Graph() {
		successorList = new HashMap<T, Set<T>>();
		predecessorList = new HashMap<T, Set<T>>();
	}

	public Set<T> nodes() {
		Set<T> nodes = new HashSet<T>(successorList.keySet());
		//nodes.addAll(predecessorList.keySet()); //Unnessesary?
		return nodes;
	}

	public Set<T> successors(T t) {
		Set<T> set = successorList.get(t);

		if (set != null) {
			return set;
		}

		return Collections.<T>emptySet();
	}

	public Set<T> predecessors(T t) {
		Set<T> set = predecessorList.get(t);

		if (set != null) {
			return set;
		}

		return Collections.<T>emptySet();
	}

	public Set<T> adjacent(T t) {
		Set<T> adj = new HashSet<T>(successorList.get(t));
		adj.retainAll(predecessorList.get(t));
		return adj;
	}

	private void addNode(T t) {
		if (!successorList.containsKey(t)) {
			successorList.put(t, new HashSet<T>());
			predecessorList.put(t, new HashSet<T>());
		}
	}

	public void addSuccessor(T t1, T t2) {
		addNode(t1);
		addNode(t2);
		successorList.get(t1).add(t2);
		predecessorList.get(t2).add(t1);
	}

	public void removeSuccessor(T t1, T t2) {
		successorList.get(t1).remove(t2);
		predecessorList.get(t2).remove(t1);
	}

	public void remove(T t) {
		if (t == null) {
			throw new NullPointerException();
		}

		if (successorList.containsKey(t)) {
			//For each T in t's successors, delete t from predecessorList
			for (T succ : successorList.get(t)) {
				predecessorList.get(succ).remove(t);
			}
			successorList.remove(t);
		}

		if (predecessorList.containsKey(t)) {
			//For each T in t's predecessors, delete t from successorList
			for (T succ : predecessorList.get(t)) {
				successorList.get(succ).remove(t);
			}
			predecessorList.remove(t);
		}
	}

	public int degree() {
		int degMax = 0;

		for (Set<T> set : successorList.values()) {
			if (set.size() > degMax) {
				degMax = set.size();
			}
		}

		return degMax;
	}

	public T getNodeOfLowestDegree() {
		T lowest = null;
		int lowestDeg = Integer.MAX_VALUE;
		for (Entry<T, Set<T>> entry : successorList.entrySet()) {
			if (entry.getValue().size() < lowestDeg || lowest == null) {
				lowest = entry.getKey();
				lowestDeg = entry.getValue().size();
			}
		}
		return lowest;
	}


	public boolean isEmpty() {
		return (successorList.size() == 0);
	}

	public int size() {
		return successorList.size();
	}

	public int degree(T t) {
		return successorList.get(t).size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("degree: " + degree() + "\n");

		for (Entry<T, Set<T>> entry : successorList.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" -> ");
			sb.append(Arrays.asList(entry.getValue().toArray()));
			sb.append("\n");
		}

		return sb.toString();
	}
}
