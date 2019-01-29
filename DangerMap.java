package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.MoveVisitor;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Transport;

//A class which stores a map of associated dangers
public class DangerMap {
  Map<Integer, Integer> dangerMap;
  ScotlandYardView view;
  Graph<Integer, Transport> graph;

  public DangerMap(ScotlandYardView view){
    this.view = view;
    graph = view.getGraph();
    dangerMap = createDangerMap();
  }

  //Returns the danger associated with the argument node
  public Integer getDanger(Node node){
    return dangerMap.get(node.value());
  }

  //Returns a set of all the nodes and their associated dangers based on where
  //the detectives are
  private Map<Integer, Integer> createDangerMap(){
    Map<Integer, Integer> dangerMap = new HashMap<>();
    //Populate the map with the nodes, all with a danger of 0
    List<Node<Integer>> nodes = graph.getNodes();
    for(Node<Integer> node : nodes){
      dangerMap.put((Integer)node.value(), 0);
    }
    //For each detective, make a dangerous area around them
    for(Integer detectiveLocation : getDetectiveLocations()){
      //Start the danger level as 100%, where the detectives are
      Integer dangerLevel = 100;
      dangerMap.put(detectiveLocation, dangerLevel);
      //Decrease the danger level
      dangerLevel = dangerLevel - 10;
      //Call makeDangerous with the node that the detective is on
      dangerMap = makeDangerous(dangerMap, dangerLevel, graph.getNode(detectiveLocation));
    }
    return dangerMap;
  }

  //A helper method to recursively make the nodes dangerous
  private Map<Integer, Integer> makeDangerous(Map<Integer, Integer> dangerMap, Integer dangerLevel, Node<Integer> node){
    //Get the edges from the argument node
    ArrayList<Edge<Integer, Transport>> edgesFrom = new ArrayList<>(graph.getEdgesFrom(node));
    for(Edge<Integer, Transport> edge : edgesFrom){
      dangerMap.put(edge.destination().value(), dangerMap.get(edge.destination().value())+dangerLevel);
      if(dangerLevel > 10){
        makeDangerous(dangerMap, dangerLevel - 10, edge.destination());
      }
    }
    return dangerMap;
  }

  //Get a list of the locations of where the detectives are
  private ArrayList<Integer> getDetectiveLocations(){
    ArrayList<Integer> detectiveLocations = new ArrayList<>();
    for(Colour colour : view.getPlayers()){
      if(colour != Colour.Black){
        detectiveLocations.add(view.getPlayerLocation(colour));
      }
    }
    return detectiveLocations;
  }

}
