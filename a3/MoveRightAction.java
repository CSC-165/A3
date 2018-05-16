package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveRightAction extends AbstractInputAction {
   private SceneNode node;
   private MyGame game;
   private float time;
   private ProtocolClient protClient;
   
   public MoveRightAction(MyGame g, SceneNode node, ProtocolClient p) {
      this.node = node;
      game = g;
      protClient = p;
   }

   public void performAction(float time, Event e) { 
	  time = (game.getEngine().getElapsedTimeMillis())/100;
	  node.moveLeft(time);
	  game.updateVerticalPos();
	  game.detectCollision();
     protClient.sendMoveMessage(node.getWorldPosition());
   }
   
}
