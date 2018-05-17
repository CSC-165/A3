package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveLeftAction extends AbstractInputAction {
   private SceneNode node;
   private MyGame game;
   private float time;
   private ProtocolClient protClient;
   
   public MoveLeftAction(MyGame g, SceneNode node, ProtocolClient p) {
      this.node = node;
      game = g;
      protClient = p;
   }

   public void performAction(float time, Event e) { 
	  time = (game.getEngine().getElapsedTimeMillis())/100;
	  node.moveRight(time);
	  game.updateVerticalPos(node);
	  game.detectCollision();
     protClient.sendMoveMessage(node.getWorldPosition());
   }
   
}

