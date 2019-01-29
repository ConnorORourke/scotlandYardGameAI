
package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

// A class to store the state of a ScotlandYardView, based on our ScotlandYardModel
public class GameConfig implements ScotlandYardView, MoveVisitor{

	private final List<Boolean> rounds;
	private final Graph<Integer, Transport> graph;
	private final ArrayList<PlayerConfig> playerList;
	private int currentRound;
	private int currentPlayer;
	private int mrXLastLocation;

  //Constructor
	public GameConfig(ScotlandYardView view){
		this.rounds = view.getRounds();
		this.graph = view.getGraph();
		this.playerList = new ArrayList<>();
		for (Colour colour : view.getPlayers()){
			playerList.add(new PlayerConfig(colour, view.getPlayerLocation(colour), getTickets(view, colour)));
		}
		this.currentRound = view.getCurrentRound();
		this.currentPlayer = view.getPlayers().indexOf(view.getCurrentPlayer());
		this.mrXLastLocation = view.getPlayerLocation(Colour.Black);
	}

  // Gives the new player the correct number of tickets
	private Map<Ticket, Integer> getTickets(ScotlandYardView view, Colour colour){
		Map<Ticket, Integer> ticketMap = new HashMap<>();
		ArrayList<Ticket> allTickets = new ArrayList<Ticket>(asList(Ticket.Bus,Ticket.Taxi,Ticket.Underground,Ticket.Double, Ticket.Secret));
		for (Ticket ticket : allTickets){
			ticketMap.put(ticket, view.getPlayerTickets(colour, ticket));
		}
		return ticketMap;
	}

	public List<Colour> getPlayers(){
		//Make & return a new unmodifiable list of colours corresponding to the players
		List<Colour> colourList = new ArrayList<Colour>();
		for(PlayerConfig player : playerList){
			colourList.add(player.colour());
		}
		return Collections.unmodifiableList(colourList);
	}

	// A method to return MrX
	public PlayerConfig getMrX(){
		return playerList.get(0);
	}

	// A method to return detectives
	public List<PlayerConfig> getDetectives(){
		List<PlayerConfig> detectives = new ArrayList<>();
		for (int i = 1; i < playerList.size(); i++){
			detectives.add(playerList.get(i));
		}
		return detectives;
	}

	public int getPlayerLocation(Colour colour){
		//If the player is MrX and the round is not a reveal round, return 0
		if(findPlayer(colour).isMrX()){
			return mrXLastLocation;
		}
		//Returns the int location of the player
		return findPlayer(colour).location();
	}

	public int getPlayerTickets(Colour colour, Ticket ticket){
		//Returns the number of tickets of the correct type held by the player
		return findPlayer(colour).tickets().get(ticket);
	}

	public PlayerConfig findPlayer(Colour colour){
		return playerList.get(getPlayers().indexOf(colour));
	}

	public boolean isGameOver(){
		return false;
	}

	public Set<Colour> getWinningPlayers(){
		return new HashSet<Colour>();
	}

	//Returns whether it is the end of the game
	private boolean endOfGame(){
		if(currentRound == rounds.size() && currentPlayer == playerList.size()-1){
			return true;
		}
		return false;
	}

  //A helper method to return whether it is the end of the round
	private boolean endOfRound(){
		if(currentPlayer == playerList.size() - 1){
			return true;
		}
		return false;
	}

	public Colour getCurrentPlayer(){
		//Returns the colour of the current player
		return playerList.get(currentPlayer).colour();
	}

	public int getCurrentRound(){
		//Returns the current round
		return currentRound;
	}

	public boolean isRevealRound(){
		//Returns the boolean of the current round as this corresponds to reveal rounds
		return rounds.get(currentRound);
	}

	public List<Boolean> getRounds(){
		//Returns an unmodifiable copy of the list of rounds using the Collections.unmodifiableList() method
		return Collections.unmodifiableList(rounds);
	}

	public Graph<Integer, Transport> getGraph(){
		//Returns an immutable copy of the graph using the ImmutableGraph() method
		return new ImmutableGraph<Integer, Transport>(graph);
	}

	//Method to create a set of valid moves
	public Set<Move> validMoves(PlayerConfig player){
		//Creates an empty set for putting moves in and returning them
		HashSet<Move> set = new HashSet<Move>();
		//Creates an empty array for putting single moves in
		List<TicketMove> singleMoves = new ArrayList<TicketMove>();
		//Gets a list of the edges from the current node
		List<Edge<Integer, Transport>> edgesFrom = new ArrayList<Edge<Integer, Transport>>(graph.getEdgesFrom(graph.getNode(player.location())));

		//--------------------------SINGLE MOVE--------------------------//

		//Gets a list of the possible single moves
		singleMoves = movesFrom(edgesFrom, player);
		//Add the moves from the list to the set to return
		for (TicketMove move : singleMoves){
			set.add(move);
		}

		//--------------------------DOUBLE MOVE--------------------------//

		List<DoubleMove> doubleMoves = new ArrayList<DoubleMove>();
		//For every possible first move, if the player has a double ticket, get the possible double moves
		if(player.hasTickets(Ticket.Double) && currentRound < rounds.size() - 1){
			for (TicketMove firstMove : singleMoves){
				//Get the nodes reached by the first move
				edgesFrom = new ArrayList<Edge<Integer, Transport>>(graph.getEdgesFrom(graph.getNode(firstMove.destination())));
				//Take the ticket used in the firstMove from the player
				player.removeTicket(firstMove.ticket());
				//Get the moves playable as a second move
				List<TicketMove> secondMoves = movesFrom(edgesFrom, player);
				//Add each double move possible
				for (TicketMove secondMove : secondMoves){
					set.add(new DoubleMove(player.colour(), firstMove, secondMove));
				}
				player.addTicket(firstMove.ticket());
			}
		}
		if(set.isEmpty() && player.isDetective()){
			set.add(new PassMove(player.colour()));
		}
		return set;
	}

	private List<TicketMove> movesFrom(List<Edge<Integer, Transport>> edgesFrom, PlayerConfig player){
		List<TicketMove> moves = new ArrayList<TicketMove>();
		//Iterates through the edges and adds the nodes that can be traversed to with a ticket
		for (Edge<Integer, Transport> edge : edgesFrom){
			//The ticket version of the transport
			Ticket currentTicket = Ticket.fromTransport(edge.data());
			//Get the list of occupied locations
			ArrayList<Integer> locationList = occupiedLocations();
			//If the edge can be traversed and is not occupied, add the TicketMove to the set of Moves
			if(player.hasTickets(currentTicket) || player.hasTickets(Ticket.Secret)){
				if(!locationList.contains(edge.destination().value())){
					//If the player has a valid transport ticket to get to the location add it
					if(player.hasTickets(currentTicket)){
						moves.add(new TicketMove(player.colour(), currentTicket, edge.destination().value()));
					}
					//If the player has a secret ticket to get to the location add it
					if(player.hasTickets(Ticket.Secret)){
						moves.add(new TicketMove(player.colour(), Ticket.Secret, edge.destination().value()));
					}
				}
			}
		}
		return moves;
	}

	//A helper method to return an ArrayList of the occupied locations on the board
	private ArrayList<Integer> occupiedLocations(){
		//Create list of occupied locations except the current player and Mr X's location
		ArrayList<Integer> locationList = new ArrayList<Integer>();
		for (PlayerConfig locPlayer : playerList){
			if((locPlayer != findPlayer(getCurrentPlayer()))&&(!locPlayer.isMrX())){
				locationList.add(locPlayer.location());
			}
		}
	return locationList;
	}

}
