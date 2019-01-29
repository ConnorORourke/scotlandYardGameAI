package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

public class DijkstraInfo {
  private final Integer node;
  private Integer prev;
  private int dist;

  public DijkstraInfo(Integer node){
    this.node = node;
    // Previous node is undefined
    this.prev = 0;
    // Distance is "infinity"
    dist = Integer.MAX_VALUE;
  }

  // Gets the integer value of the node
  public Integer node(){
    return node;
  }

  // Gets the integer value of the previous node
  public Integer prev(){
    return prev;
  }

  // Sets the inteter value of the previous node
  public void prev(Integer prev){
    this.prev = prev;
  }

  // Gets the distance of the node from the graph
  public int dist(){
    return dist;
  }

  // Sets the distance of the node from the graph
  public void dist(int dist){
    this.dist = dist;
  }
}
