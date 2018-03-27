package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveForwardAction extends AbstractInputAction {
   private SceneNode node;
   
   public MoveForwardAction(SceneNode node) {
      this.node = node;
   }

   public void performAction(float time, Event e) { 
      node.moveForward(.015f);
   }
   
}