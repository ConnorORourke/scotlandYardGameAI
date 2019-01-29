package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import uk.ac.bris.cs.scotlandyard.model.Move;

// A class to store the data at the nodes of the tree
// (Not called Node so as to not get confused with Graph Nodes)
class Leaf{
  private Optional<Move> move;
  private Integer score;
  private List<Leaf> children;

  // Overloaded constructors for an empty and initialised leaf
  public Leaf(){
    this.move = Optional.empty();
    this.score = Integer.MAX_VALUE;
    this.children = new ArrayList<>();
  }
  public Leaf(Move move, Integer score){
    this.move = Optional.of(move);
    this.score = score;
    this.children = new ArrayList<>();
  }

  public void addChild(Leaf leaf){
    children.add(leaf);
  }

  public Move getMove(){
    return move.get();
  }

  public void setMove(Move newMove){
    move = Optional.of(newMove);
  }

  public Integer getScore(){
    return score;
  }

  public void setScore(Integer newScore){
    score = newScore;
  }

  public List<Leaf> getChildren(){
    return children;
  }

  public void emptyChildren(){
    children = new ArrayList<>();
  }
}
