package a3;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class RotateLeftAction extends AbstractInputAction {
   private MyGame game;
   private SceneNode node;
   private Angle rotLeft = Degreef.createFrom(5.0f);
   
   public RotateLeftAction(MyGame game, SceneNode node) {
      this.node = node;
      this.game = game;
   }

   public void performAction(float time, Event e) { 
      node.yaw(rotLeft);
      game.detectCollision();
   }
   
}