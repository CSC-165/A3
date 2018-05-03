package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveForwardAction extends AbstractInputAction {
   private SceneNode node;
   private MyGame game;
   private float time;
   
   public MoveForwardAction(MyGame g, SceneNode node) {
      this.node = node;
      game = g;
   }

   public void performAction(float time, Event e) { 
	  time = (game.getEngine().getElapsedTimeMillis())/1000;
	  node.moveForward(time);
	  game.updateVerticalPos();
	  game.detectCollision();
   }
   
}
