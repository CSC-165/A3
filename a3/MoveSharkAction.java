package a3;

	import ray.input.action.AbstractInputAction;
	import ray.rage.scene.*;
	import ray.rage.game.*;
	import ray.rml.*;
	import net.java.games.input.Event;

	public class MoveSharkAction extends AbstractInputAction {
		private MyGame game;
   
		public MoveSharkAction(MyGame g) {
			game = g;
		}

		@Override
		public void performAction(float arg0, Event arg1) {
			game.moveShark();
		}
   
}
