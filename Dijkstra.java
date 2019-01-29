package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.Collections;
import static java.util.Objects.requireNonNull;

import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.model.Transport;

//A Dijkstra implementation which uses a breadth first search instead of
//finding the closest node
class Dijkstra {

  Graph<Integer, Transport> graph;
  //List<Node<Integer>> unvisitedNodes;
  //A map of the distances to the source node of every node
  Map<Node<Integer>, Integer> distance;
  //A map of the previous nodes of each node
  Map<Node<Integer>, Node<Integer>> previous;
  Node<Integer> source;

  public Dijkstra(Graph<Integer, Transport> graph, Node<Integer> source){
    this.graph = graph;
    this.source = source;
    distance = new HashMap<>();
    previous = new HashMap<>();
  }

  public void performDijkstra(){
    //Create a queue for the nodes
    LinkedList<Node<Integer>> queue = new LinkedList<>();
    //Add the nodes to the queue
    for(Node<Integer> node : graph.getNodes()){
      //The distance is "infinity" and the previous node is unknown
      distance(node, Integer.MAX_VALUE);
      queue.add(node);
    }
    //The distance from the source to itself is 0
    distance(source, 0);
    //Remove the node at the start of the queue while the queue is not empty
    while (!queue.isEmpty()){
      Node<Integer> source = getMinimum(queue);
      queue.removeFirstOccurrence(source);
      //Get the neighbours of the node
      List<Node<Integer>> neighbours = getNeighbours(source);
      //For each neighbour, check if there is a shorter path from the current node
      for(Node<Integer> neighbour : neighbours){
        Integer alt = distance(source) + 1;
        //If the current node offers a better path, update the distance and previous value
        if(alt < distance(neighbour)){
          distance(neighbour, alt);
          previous(neighbour, source);
        }
      }
    }
  }

  //A method to get the minimum distance in the queue
  private Node<Integer> getMinimum(LinkedList<Node<Integer>> queue){
    //Stream to allow functional programming
    //1. map distance over the queue, which leaves a list of Integers
    //2. map the Integer values in queue to ints
    //3. get the minimum of the values in the queue
    //4. .min() returns an optional so .orElse() to return "infinity" if no min exists
    Integer min = queue.stream()
                       .map(this::distance)
                       .mapToInt(Integer::intValue)
                       .min()
                       .orElse(Integer.MAX_VALUE);
    //1. filter queue so that it only leaves the nodes which have a distance of min
    //2. get the first node with distance of min, as it doesn't matter which one
    //3. .findFirst() returns an optional so .orElse() to return null if no node exists
    return queue.stream()
                .filter(n -> min == distance(n))
                .findFirst()
                .orElse(null);
  }

  //A method to update the distance from the node to the source
  private void distance(Node<Integer> node, Integer value){
    distance.put(node, value);
  }

  //A method to get the distance from the node to the source
  private Integer distance(Node<Integer> node){
    return requireNonNull(distance.get(node));
  }

  //A method to update the previous node from a node
  private void previous(Node<Integer> node, Node<Integer> prev){
    previous.put(node, prev);
  }

  //A method to get the previous node from a node
  private Node<Integer> previous(Node<Integer> node){
    return requireNonNull(previous.get(node));
  }

  //A method to get the neighbours of a node
  private List<Node<Integer>> getNeighbours(Node<Integer> node){
    //Get the edges from the node
    List<Edge<Integer, Transport>> edgesFrom = new ArrayList<>(graph.getEdgesFrom(node));
    List<Node<Integer>> neighbours = new ArrayList<>();
    for(Edge<Integer, Transport> edge : edgesFrom){
      neighbours.add(edge.destination());
    }
    return neighbours;
  }

  //A method to return the path from the source to a node
  public List<Node<Integer>> getPath(Node<Integer> node){
    List<Node<Integer>> path = new ArrayList<>();
    Node<Integer> current = node;
    while (current != source){
      path.add(current);
      current = previous(current);
    }
    return path;
  }

}
