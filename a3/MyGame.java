package a3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;

import myGameEngine.OrbitCameraController;

public class MyGame extends VariableFrameRateGame {

	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec, counter = 0;
   
	private InputManager im;
	
   private MoveForwardAction moveForwardAction;
   private MoveBackwardAction moveBackwardAction;
   private MoveLeftAction moveLeftAction;
   private MoveRightAction moveRightAction;
   private RotateLeftAction rotateLeftAction;
   private RotateRightAction rotateRightAction;
   private RotateUpAction rotateUpAction;
   private RotateDownAction rotateDownAction;
   
   private SceneNode cube1N;
   private SceneNode cube2N;
   private SceneNode cube3N;
   
   private Entity cube1E;
   private Entity cube2E;
   private Entity cube3E;
   
   private Random cube1Rand = new Random();
   private Random cube2Rand = new Random();
   private Random cube3Rand = new Random();
   
   private RotateController rotateController;
   
   private OrbitCameraController orbitController;

   public MyGame() {
      super();
      System.out.println("Avatar Controls: ");
      System.out.println("W to move forward");
      System.out.println("S to move backwards");
      System.out.println("A to move left");
      System.out.println("D to move right\n");
      
      System.out.println("Q to rotate left");
      System.out.println("Z to rotate right");
      System.out.println("E to rotate up");
      System.out.println("X to rotate down");
      
      System.out.println("\nCamera Controls: ");
      System.out.println("V to zoom in");
      System.out.println("B to zoom out");
      System.out.println("L to orbit right");
      System.out.println("J to orbit left");
      System.out.println("I to orbit up");
      System.out.println("K to orbit down");
      
      System.out.println("\nAvatar with Camera Controls: ");
      System.out.println("F to rotate camera/avatar right");
      System.out.println("H to rotate camera/avatar left");
      System.out.println("T to rotate camera/avatar up");
      System.out.println("G to rotate camera/avatar down");
   }

   public static void main(String[] args) {
      Game game = new MyGame();
      try {
         game.startup();
         game.run();
      } catch (Exception e) {
         e.printStackTrace(System.err);
      } finally {
         game.shutdown();
         game.exit();
      }
   }
	
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		/* Makes game windowed mode */
      //rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
      
      /* Makes game fullscreen */
      rs.createRenderWindow(true);
	}

   @Override
   protected void setupCameras(SceneManager sm, RenderWindow rw) {
      SceneNode rootNode = sm.getRootSceneNode();
      Camera camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
      rw.getViewport(0).setCamera(camera);
		
		camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		
		camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));

      SceneNode cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
      camera.setMode('n');
      camera.getFrustum().setFarClipDistance(1000.0f);
      cameraNode.attachObject(camera);
   }
	
   @Override
   protected void setupScene(Engine eng, SceneManager sm) throws IOException {
      im = new GenericInputManager();
      
      //temp object for perspective
      Entity sphere1 = sm.createEntity("sphere1", "sphere.obj");
      sphere1.setPrimitive(Primitive.TRIANGLES);
      SceneNode sphere1Node = sm.getRootSceneNode().createChildSceneNode(sphere1.getName() + "Node");
      sphere1Node.scale(0.1f,0.1f,0.1f);
      sphere1Node.attachObject(sphere1);
      sphere1Node.moveForward(2.0f);
      
	   Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
      dolphinE.setPrimitive(Primitive.TRIANGLES);
      
      cube1E = sm.createEntity("cube1", "cube.obj");
      cube2E = sm.createEntity("cube2", "cube.obj");
      cube3E = sm.createEntity("cube3", "cube.obj");
      
      cube1E.setPrimitive(Primitive.TRIANGLES);
      cube2E.setPrimitive(Primitive.TRIANGLES);
      cube3E.setPrimitive(Primitive.TRIANGLES);
      
      SceneNode controlStuff = sm.getRootSceneNode().createChildSceneNode("myControlStuffNode");
        
      cube1N = controlStuff.createChildSceneNode(cube1E.getName() + "Node");
      cube2N = controlStuff.createChildSceneNode(cube2E.getName() + "Node");
      cube3N = controlStuff.createChildSceneNode(cube3E.getName() + "Node");
      
      float cube1Pos1 = 2.0f + cube1Rand.nextFloat() * (10.0f - 2.0f);
      float cube1Pos2 = 2.0f + cube1Rand.nextFloat() * (10.0f - 2.0f);

      float cube2Pos1 = 2.0f + cube2Rand.nextFloat() * (10.0f - 2.0f);
      float cube2Pos2 = 2.0f + cube2Rand.nextFloat() * (10.0f - 2.0f);

      float cube3Pos1 = 2.0f + cube3Rand.nextFloat() * (10.0f - 2.0f);
      float cube3Pos2 = 2.0f + cube3Rand.nextFloat() * (10.0f - 2.0f);
      
      cube1N.moveBackward(cube1Pos1);
      cube1N.moveLeft(cube1Pos1);
      cube1N.moveRight(cube1Pos2);
      
      cube2N.moveBackward(cube2Pos1);
      cube2N.moveLeft(cube2Pos1);
      cube2N.moveRight(cube2Pos2);
      
      cube3N.moveBackward(cube3Pos1);
      cube3N.moveLeft(cube3Pos1);
      cube3N.moveRight(cube3Pos2);
      
      cube1N.attachObject(cube1E);
      cube1N.scale(.25f, .25f, .25f);
        
      cube2N.attachObject(cube2E);
      cube2N.scale(.25f, .25f, .25f);
        
      cube3N.attachObject(cube3E);
      cube3N.scale(.25f, .25f, .25f);
      
      StretchController sc = new StretchController();

      sc.addNode(controlStuff);
      sm.addController(sc);
      
      rotateController = new RotateController();
      rotateController.addNode(cube1N);
      rotateController.addNode(cube2N);
      rotateController.addNode(cube3N);
      sm.addController(rotateController);

      SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
      Angle faceFront = Degreef.createFrom(45.0f);
        
      dolphinN.moveBackward(2.0f);
      dolphinN.yaw(faceFront);
      dolphinN.attachObject(dolphinE);

      sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		
      Light plight = sm.createLight("testLamp1", Light.Type.POINT);
      plight.setAmbient(new Color(.3f, .3f, .3f));
      plight.setDiffuse(new Color(.7f, .7f, .7f));
      plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
      plight.setRange(5f);
		
      SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
      plightNode.attachObject(plight);
      
      setupInputs(sm);
      setupOrbitCameras(eng,sm);

   }
   
   protected void setupOrbitCameras(Engine eng, SceneManager sm) {
	   SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
	   SceneNode cameraN = sm.getSceneNode("MainCameraNode");
	   Camera camera = sm.getCamera("MainCamera");
	   String kbName = im.getKeyboardName();
	   
	   orbitController = new OrbitCameraController(camera, cameraN,
			   dolphinN, kbName, im, this);
   }
   
   protected void setupInputs(SceneManager sm) {
      im = new GenericInputManager();
      
      String keyboard1 = im.getKeyboardName();
      String gamePad1 = im.getFirstGamepadName();
      
      SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("myDolphinNode");
      
      moveForwardAction = new MoveForwardAction(dolphinN);
      moveBackwardAction = new MoveBackwardAction(dolphinN);
      moveLeftAction = new MoveLeftAction(dolphinN);
      moveRightAction = new MoveRightAction(dolphinN);
      rotateLeftAction = new RotateLeftAction(dolphinN);
      rotateRightAction = new RotateRightAction(dolphinN);
      rotateUpAction = new RotateUpAction(this,dolphinN);
      rotateDownAction = new RotateDownAction(this,dolphinN);
      
      System.out.println("Keyboard: " + keyboard1);
      System.out.println("Gamepad: " + gamePad1);
      
      //Keyboard associations
      im.associateAction(keyboard1, 
                         net.java.games.input.Component.Identifier.Key.W, 
                         moveForwardAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                         
      im.associateAction(keyboard1, 
                         net.java.games.input.Component.Identifier.Key.S, 
                         moveBackwardAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                         
      im.associateAction(keyboard1, 
                         net.java.games.input.Component.Identifier.Key.A, 
                         moveLeftAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                         
      im.associateAction(keyboard1, 
                         net.java.games.input.Component.Identifier.Key.D, 
                         moveRightAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                         
      im.associateAction(keyboard1, 
                         net.java.games.input.Component.Identifier.Key.Q, 
                         rotateLeftAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                         
      im.associateAction(keyboard1, 
                         net.java.games.input.Component.Identifier.Key.Z, 
                         rotateRightAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
      im.associateAction(keyboard1, 
              net.java.games.input.Component.Identifier.Key.E, 
              rotateUpAction,
              InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
      im.associateAction(keyboard1, 
              net.java.games.input.Component.Identifier.Key.X, 
              rotateDownAction,
              InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);


   }

   @Override
   protected void update(Engine engine) {
		// build and set HUD
	   rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		dispStr = "Assignment #3   " + "Player 1   " + "Time = " + elapsTimeStr + "   Keyboard hits = " + counterStr;
		rs.setHUD(dispStr, 15, 15);
		
		im.update(elapsTime);
		orbitController.updateCameraPosition();
	}
   
   //distDetection takes two SceneNodes as parameters and
   //returns the distance between the two as a vector.
   public Vector3 distDetection(SceneNode temp, SceneNode other) {
   	Vector3 vec1 = temp.getWorldPosition();
   	Vector3 vec2 = other.getWorldPosition();
   	Vector3 dist = vec1.sub(vec2);
   	return dist;
   }

   @Override
   public void keyPressed(KeyEvent e) {
      Entity dolphin = getEngine().getSceneManager().getEntity("myDolphin");
      switch (e.getKeyCode()) {
        /* case KeyEvent.VK_L:
            dolphin.setPrimitive(Primitive.LINES);
            break;
         case KeyEvent.VK_T:
            dolphin.setPrimitive(Primitive.TRIANGLES);
            break;
         case KeyEvent.VK_P:
            dolphin.setPrimitive(Primitive.POINTS);
            break;
			case KeyEvent.VK_C:
				counter++;
				break;*/
      }
      super.keyPressed(e);
   }
}