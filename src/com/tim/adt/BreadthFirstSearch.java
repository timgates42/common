package com.tim.adt;

import java.util.*;

public class BreadthFirstSearch {
    
    public static Vector run(Graph graph, GraphNode start, Condition condition) {
        graph.setAllFlags(false);
        return run(start, condition, new Vector(), new LinkedList());
    }
    
    public static Vector run(GraphNode node, Condition condition, Vector path, LinkedList queue) {
        if(condition.matches(node)) {
            return path;
        }
        node.setFlagged(true);
        Vector edges = node.getEdges();
        for(int i = 0; i < edges.size(); i++) {
            DirectedEdge edge = (DirectedEdge) edges.elementAt(i);
            GraphNode elem = edge.getDestination();
            if(!elem.isFlagged()) {
                Vector new_path = (Vector) path.clone();
                new_path.addElement(edge);
                queue.addLast(new_path);
            }
        }
        if(queue.isEmpty()) {
            return null;
        }
        Vector next_path = (Vector) queue.removeFirst();
        DirectedEdge next = (DirectedEdge) next_path.lastElement();
        return run(next.getDestination(), condition, next_path, queue);
    }
    
}
