package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.List;
import java.util.ArrayList;

import uk.ac.bris.cs.scotlandyard.model.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.TicketMove;
import uk.ac.bris.cs.scotlandyard.model.DoubleMove;
import uk.ac.bris.cs.scotlandyard.model.PassMove;
import uk.ac.bris.cs.scotlandyard.model.MoveVisitor;

class MoveConfig implements MoveVisitor{
  private int destination;
  private List<Ticket> ticketsUsed;

  public MoveConfig(Move move){
    move.visit(this);
  }

  public void visit(TicketMove move){
    this.destination = move.destination();
    this.ticketsUsed = new ArrayList<>();
    ticketsUsed.add(move.ticket());
  }

  public void visit(DoubleMove move){
    this.destination = move.finalDestination();
    this.ticketsUsed = new ArrayList<>();
    ticketsUsed.add(move.firstMove().ticket());
    ticketsUsed.add(move.secondMove().ticket());
  }

  public void visit(PassMove move){
    throw new IllegalStateException("Mr X should never have a PassMove");
  }

  public int destination(){
    return destination;
  }

  public List<Ticket> ticketsUsed(){
    return ticketsUsed;
  }
}
