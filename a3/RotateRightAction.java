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
   private ProtocolClient protClient;
   
   public RotateRightAction(MyGame game, SceneNode node, ProtocolClient p) {
	  this.game = game;
     this.node = node;
     this.protClient = p;
   }

   public void performAction(float time, Event e) { 
      node.yaw(rotRight);
      game.detectCollision();
      protClient.sendYawMessage(rotRight);
   }
   
}