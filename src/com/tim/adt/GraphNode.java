package com.tim.adt;

import java.util.*;

public interface GraphNode {
    
    Vector getEdges();
    void setFlagged(boolean flagged);
    boolean isFlagged();
    
}
