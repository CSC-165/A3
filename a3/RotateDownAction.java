package a3;

import net.java.games.input.Event;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;

public class RotateDownAction implements Action{
	
	private MyGame game;
	private float time;
	private SceneNode node;
	private Angle angle = Degreef.createFrom(5.0f);
	
	public RotateDownAction(MyGame g, SceneNode n) {
		game = g;
		node = n;
	}

	@Override
	public void performAction(float arg0, Event arg1) {
		time = (game.getEngine().getElapsedTimeMillis())/1000;
		node.pitch(angle);
		game.detectCollision();
	}

}
