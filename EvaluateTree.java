package uk.ac.bris.cs.scotlandyard.ui.ai;
import java.util.*;

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

class EvaluateTree{

  private Tree tree;

  public EvaluateTree(Tree tree){
    this.tree = tree;
  }

  // Takes the parent leaf and recursively gets the best score from it's children
  // If the parent is the "root" of the tree, get the move of it's best scoring child
  public void evaluateTree(Leaf parent){
    while (!isAboveBottom(parent)){
      for(Leaf newChild : parent.getChildren()){
        evaluateTree(newChild);
      }
    }
    Leaf bestChild = getBestChild(parent.getChildren());
    parent.setScore(bestChild.getScore());
    if (parent == tree.firstLayer){
      parent.setMove(bestChild.getMove());
    }
    parent.emptyChildren();
  }

  // Return the leaf with the best scoring child
  private Leaf getBestChild(List<Leaf> children){
    Leaf bestChild = children.get(0);
    for (Leaf child : children){
      if (child.getScore() < bestChild.getScore()){
        bestChild = child;
      }
    }
    return bestChild;
  }

  // Checks whether the given leaf is one layer above the bottom of the tree
  private Boolean isAboveBottom(Leaf leaf){
    List<Leaf> children = leaf.getChildren();
    if (children.get(children.size()-1).getChildren().isEmpty()){
      return true;
    }
    return false;
  }

}
