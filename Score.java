package uk.ac.bris.cs.scotlandyard.ui.ai;

//import java.util.*;
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


public class Score implements MoveVisitor{
	private int finalScore;
  private final ScotlandYardView view;
  private Move move;
  private final Graph<Integer, Transport> graph;
	DangerMap dangerMap;

  public Score(ScotlandYardView view, Move move, DangerMap dangerMap){
    this.finalScore = 0;
    this.view = view;
    this.move = move;
    this.graph = view.getGraph();
		this.dangerMap = dangerMap;
  }

	// Get the score of the game configuration after the given move
  public int getScore(){
		move.visit(this);
		// Returns a score, the lower the better
		return finalScore;
  }

  // The visit method for TicketMoves
	@Override
	public void visit(TicketMove move){
		Node<Integer> moveNode = graph.getNode(move.destination());
		transportLinkUpdate(moveNode);
		addDanger(moveNode);
	}

	// The visit method for DoubleMoves
	@Override
	public void visit(DoubleMove move){
		Node<Integer> moveNode = graph.getNode(move.finalDestination());
		transportLinkUpdate(moveNode);
		addDanger(moveNode);
	}

	// The visit method for PassMoves
	@Override
	public void visit(PassMove move){
		throw new IllegalStateException("Mr X should never have a PassMove");
	}

	// Add the danger of the node to the score
	private void addDanger(Node node){
		finalScore += dangerMap.getDanger(node);
	}

  // Get a list of the locations of where the detectives are
  public ArrayList<Integer> getDetectiveLocations(){
    ArrayList<Integer> detectiveLocations = new ArrayList<>();
    for (Colour colour : view.getPlayers()){
      if (colour != Colour.Black){
        detectiveLocations.add(view.getPlayerLocation(colour));
      }
    }
		return detectiveLocations;
  }

	// Returns the value associated with number of transport links from arg node
	private Integer getTransportLinks(Node<Integer> node){
		// Initialise the value to 100% i.e. there are no transport links
		Integer transportLinks = 100;
		ArrayList<Edge<Integer, Transport>> edgesFrom = new ArrayList<>(graph.getEdgesFrom(node));
		for (Edge<Integer, Transport> edge : edgesFrom){
			// Take 10 from the value if there exists a tranport link
			transportLinks -= 10;
			// Subtract a further 25 weighting for an underground station
			if(edge.data() == Transport.Underground){
				transportLinks -= 25;
			}
		}
		// If the value is < 0, make it equal to 0
		if (transportLinks < 0){
			transportLinks = 0;
		}
		return transportLinks;
	}

	private void transportLinkUpdate(Node<Integer> moveNode){
		finalScore +=  getTransportLinks(moveNode);
	}

}
