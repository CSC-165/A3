package a3;

import net.java.games.input.Event;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;

public class RotateUpAction implements Action{
	
	private MyGame game;
	private float time;
	private SceneNode node;
	private Angle angle = Degreef.createFrom(-5.0f);
   private ProtocolClient protClient;
	
	public RotateUpAction(MyGame g, SceneNode n, ProtocolClient p) {
		this.game = g;
		this.node = n;
      this.protClient = p;
	}

	@Override
	public void performAction(float arg0, Event arg1) {
		time = (game.getEngine().getElapsedTimeMillis())/1000;
		node.pitch(angle);
		game.detectCollision();
      protClient.sendPitchMessage(angle);
	}

}
