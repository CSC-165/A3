package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveBackwardAction extends AbstractInputAction {
   private SceneNode node;
   
   public MoveBackwardAction(SceneNode node) {
      this.node = node;
   }

   public void performAction(float time, Event e) { 
      node.moveBackward(.015f);
   }
   
}