package myGameEngine;

import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

import java.util.Iterator;
import java.util.Random;
import a3.MyGame;


public class ShrinkController extends AbstractController {
	private float scaleRate = 0.002f;
	private Random r;
	private MyGame game;
	private Iterator<Node> itr;
	
	public ShrinkController(MyGame g) {
		game = g;
	}
	
	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		itr = super.controlledNodesList.iterator();
		while (itr.hasNext()) {
			Node node = itr.next();
			float scaleAmount = 1.0f + -1.0f * scaleRate;
			Vector3 curScale = node.getLocalScale();
			curScale = Vector3f.createFrom(curScale.x()*scaleAmount, 
					curScale.y()*scaleAmount, curScale.z()*scaleAmount);
			node.setLocalScale(curScale);
			if (curScale.x() < 0.01f) {
				if (node.getName().startsWith("sphere")) {
					itr.remove();
				}
			}
		}
	}
}

