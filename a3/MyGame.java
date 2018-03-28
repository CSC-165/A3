package a3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
   
   private OrbitCameraController orbitController;

   public MyGame() {
      super();
		System.out.println("press t to render triangles");
		System.out.println("press L to render lines");
		System.out.println("press P to render points");
		System.out.println("press C to increment counter");
      
      System.out.println("W to move forward");
      System.out.println("S to move backwards");
      System.out.println("A to move left");
      System.out.println("D to move right\n");
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

      SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
      Angle faceFront = Degreef.createFrom(180.0f);
        
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

   @Override
   protected void update(Engine engine) {
		// build and set HUD
	   rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		dispStr = "Time = " + elapsTimeStr + "   Keyboard hits = " + counterStr;
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
         case KeyEvent.VK_L:
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
				break;
      }
      super.keyPressed(e);
   }
}