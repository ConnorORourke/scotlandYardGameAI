package uk.ac.bris.cs.scotlandyard.model;

import java.util.HashMap;
import java.util.Map;

// A class based on PlayerConfiguration to store the state of a player
public class PlayerConfig {

  private final Colour colour;
  private int location;
  private final Map<Ticket, Integer> tickets;

  public PlayerConfig(Colour colour, int location,
      Map<Ticket, Integer> tickets) {
    this.colour = colour;
    this.location = location;
    this.tickets = new HashMap<>(tickets);
  }

  public Colour colour() {
    return colour;
  }

  public boolean isMrX() {
    return colour.isMrX();
  }

  public boolean isDetective() {
    return colour.isDetective();
  }

  public void location(int location) {
    this.location = location;
  }

  public int location() {
    return location;
  }

  public Map<Ticket, Integer> tickets() {
    return tickets;
  }

  public void addTicket(Ticket ticket) {
    adjustTicketCount(ticket, 1);
  }

  public void removeTicket(Ticket ticket) {
    adjustTicketCount(ticket, -1);
  }

  private void adjustTicketCount(Ticket ticket, int by) {
    Integer ticketCount = tickets.get(ticket);
    ticketCount += by;
    tickets.remove(ticket);
    tickets.put(ticket, ticketCount);
  }

  public boolean hasTickets(Ticket ticket) {
    return tickets.get(ticket) != 0;
  }

  public boolean hasTickets(Ticket ticket, int quantityInclusive) {
    return tickets.get(ticket) >= quantityInclusive;
  }
}
