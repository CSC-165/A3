package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveLeftAction extends AbstractInputAction {
   private SceneNode node;
   
   public MoveLeftAction(SceneNode node) {
      this.node = node;
   }

   public void performAction(float time, Event e) { 
      node.moveRight(.015f);
   }
   
}