package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class RotateRightAction extends AbstractInputAction {
   private MyGame game;
   private SceneNode node;
   private Angle rotRight = Degreef.createFrom(-5.0f);
   
   public RotateRightAction(MyGame game, SceneNode node) {
	  this.game = game;
      this.node = node;
   }

   public void performAction(float time, Event e) { 
      node.yaw(rotRight);
      game.detectCollision();
   }
   
}