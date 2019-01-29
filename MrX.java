package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Collection;
import java.util.function.Consumer;
import static java.util.Arrays.asList;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.Visualiser;
import uk.ac.bris.cs.scotlandyard.ai.ResourceProvider;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Transport;
import uk.ac.bris.cs.scotlandyard.model.GameConfig;
import uk.ac.bris.cs.scotlandyard.model.PlayerConfig;
import uk.ac.bris.cs.scotlandyard.model.MoveVisitor;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;

@ManagedAI("Mr X AI")
public class MrX implements PlayerFactory {

	// A map to store the paths between every source and destination on the board
	public static Map<Node<Integer>, Map<Node<Integer>, List<Node<Integer>>>> dijkstraMap = new HashMap<>();
	public static Graph<Integer, Transport> superGraph;
	// Set the number of Mr X's goes to look ahead
	public static Integer lookAhead = 2;

	@Override
	public Player createPlayer(Colour colour) {
		return new MyPlayer();
	}

	@Override
	public void ready(Visualiser visualiser, ResourceProvider provider){
		superGraph = provider.getGraph();
		// Get the graph from the provider
		Graph<Integer, Transport> graph = superGraph;
		List<Node<Integer>> nodes = graph.getNodes();
		// For each possible source node, make a new dijkstra and perform the algorithm
		for (Node<Integer> source : nodes){
			Dijkstra dijkstra = new Dijkstra(graph, source);
			dijkstra.performDijkstra();
			Map<Node<Integer>, List<Node<Integer>>> destinationMap = new HashMap<>();
			// For each possible destination from the source, add the path to the map
			for (Node<Integer> destination : nodes){
				// No path to and from the same place
				if (source != destination){
					// The value of the map is the path from the source to the destination
					List<Node<Integer>> value = dijkstra.getPath(destination);
					// Make the inner map from the destination to the path
					destinationMap.put(destination, value);
				}
				else{
					destinationMap.put(destination, new ArrayList<>(asList(destination)));
				}
			}
			dijkstraMap.put(source, destinationMap);
		}
	}

	private static class MyPlayer implements Player, MoveVisitor {

		@Override
		public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
				Consumer<Move> callback) {
			// Makes a move
			callback.accept(choose(moves, view));
		}

		private ArrayList<Integer> getDetectiveLocations(ScotlandYardView view){
		//Funtion to return a list of the current detective locations
			ArrayList<Integer> detectiveLocations = new ArrayList<>();
			for (Colour colour : view.getPlayers()){
				if (colour != Colour.Black){
					detectiveLocations.add(view.getPlayerLocation(colour));
				}
			}
			return detectiveLocations;
		}

		// A method to choose from a set of valid moves
				private Move choose(Set<Move> moves, ScotlandYardView view){
					Random random = new Random();
					Tree gameTree = makeGameTree(moves, view);
					EvaluateTree evaluateTree = new EvaluateTree(gameTree);
					evaluateTree.evaluateTree(gameTree.firstLayer);
					//return gameTree.firstLayer.getMove();
					//return (new ArrayList<>(moves)).get(random.nextInt(moves.size()-1));
					return gameTree.firstLayer.getMove();
				}

				// A method to create the game tree
				private Tree makeGameTree(Set<Move> validMoves, ScotlandYardView view){
					Tree tree = new Tree();
					populateChildren(tree.firstLayer, shrinkValidMoves(validMoves), view, (Integer)1);
					return tree;
				}

				private void populateChildren(Leaf parent, Set<Move> validMoves, ScotlandYardView view, Integer layer){
					if (layer <= lookAhead){
						DangerMap dangerMap = new DangerMap(view);
						for (Move move : validMoves){
							Leaf leaf = new Leaf(move, new Score(view, move, dangerMap).getScore());
							parent.addChild(leaf);
							if (layer + 1 <= lookAhead){
								GameConfig updatedView = newView(view, move);
								Set<Move> newValidMoves = updatedView.validMoves(updatedView.getMrX());
								newValidMoves = shrinkValidMoves(newValidMoves);
								populateChildren(leaf, newValidMoves, updatedView, layer+1);
						  }
					  }
					}
				}

				private GameConfig newView(ScotlandYardView oldView, Move move){
					GameConfig view = new GameConfig(oldView);
					MoveConfig moveConfig = new MoveConfig(move);
					// Move the detectives
					view = moveDetectives(view);
					// Move Mr X depending on the move he just took
					PlayerConfig mrXPlayer = view.getMrX();
					mrXPlayer.location(moveConfig.destination());
					// Take Mr X's ticket
					for (Ticket ticket : moveConfig.ticketsUsed()){
						mrXPlayer.removeTicket(ticket);
					}
					return view;
				}

				private GameConfig moveDetectives(GameConfig view){
					Random random = new Random();
					Graph<Integer, Transport> graph = view.getGraph();
				 	for (PlayerConfig detective : view.getDetectives()){
				 		Node<Integer> location = superGraph.getNode(detective.location());
				 		Node<Integer> destination = superGraph.getNode(view.getPlayerLocation(Colour.Black));
						if (destination == null){
							destination = superGraph.getNode(random.nextInt(graph.size()-1) + 1);
						}
						Node<Integer> nextLocation = dijkstraMap.get(destination).get(location).get(0);
						detective.location(nextLocation.value());
				 	}
					return view;
				 }

				 // Returns a set of validMoves but with no duplicate destinations
				 private Set<Move> shrinkValidMoves(Set<Move> moves){
					 Set<Move> shrunkMoves = new HashSet<>();
					 Set<Node<Integer>> moveDestinations = new HashSet<>();
					 for (Move move : moves){
						 MoveConfig validMove = new MoveConfig(move);
						 Node<Integer> moveDestination = superGraph.getNode(validMove.destination());
						 if (!moveDestinations.contains(moveDestination)){
							 shrunkMoves.add(move);
							 moveDestinations.add(moveDestination);
						 }
					 }
					 return shrunkMoves;
				 }
	}

}
