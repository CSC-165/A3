package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveBackwardAction extends AbstractInputAction {
   private SceneNode node;
   private MyGame game;
   private float time;
   
   public MoveBackwardAction(MyGame g, SceneNode node) {
      this.node = node;
      game = g;
   }

   public void performAction(float time, Event e) { 
	  time = (game.getEngine().getElapsedTimeMillis())/1000;
	  node.moveBackward(time);
	  game.updateVerticalPos();
   }
   
}
