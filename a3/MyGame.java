package a3;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.*;
import ray.rage.asset.texture.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.states.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import static ray.rage.scene.SkeletalEntity.EndType.*;
import ray.rage.util.*;
import ray.rml.*;
import ray.audio.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import myGameEngine.OrbitCameraController;
import myGameEngine.ShrinkController;

//import com.jogamp.*;

public class MyGame extends VariableFrameRateGame {

	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr;
	int elapsTimeSec, counter = 0;
   
	private InputManager im;
	
	private Action moveForwardAction, moveBackwardAction, moveLeftAction,
		moveRightAction, rotateLeftAction, rotateRightAction, rotateUpAction,
		rotateDownAction, moveSharkAction;
	
	private final static String GROUND_E = "GroundEntity";
	private final static String GROUND_N = "GroundNode";
	private static final String SKYBOX = "SkyBox";
	private boolean skyBoxVisible = true;

	private ShrinkController sc = new ShrinkController(this);
   
    private SkeletalEntity snowmanSE;
   
	private RotateController rotateController;
   
	private OrbitCameraController orbitController;
	
	private int sharkCount = 0;
	private Angle angle = Degreef.createFrom(-90.0f);
	
	private SceneNode groundN;
	private PhysicsEngine physEng;
	private PhysicsObject physSphere1, physSphere2, physSphere3, 
		physSphere4, physSphere5, groundPlane;
	
	private boolean running = false;
	private Random r1 = new Random();
	private Random r2 = new Random();
	private int foodCount = 0;

	private Iterator<SceneNode> iter;
	private int score = 0;
   
   private IAudioManager audioMgr;
   private Sound backgroundMusic;
	
	public MyGame() {
		super();
		System.out.println("Avatar Controls: ");
		System.out.println("W to move forward");
		System.out.println("S to move backwards");
		System.out.println("A to move left");
		System.out.println("D to move right\n");
      
		System.out.println("LEFT arrow to rotate left");
		System.out.println("RIGHT arrow to rotate right");
		System.out.println("UP arrow to rotate up");
		System.out.println("DOWN arrow to rotate down");
      
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
		
		System.out.println("\nBlender object Controls:");
		System.out.println("1 to start/stop shark animation");
		System.out.println("2 to start/stop snowman animation");
		
		System.out.println("\nPress SPACE to drop food");
   }

   public static void main(String[] args) {
      MyGame game = new MyGame();
      try {
         game.startup();
         
         
         ScriptEngineManager factory = new ScriptEngineManager();
         String scriptFileName = "hello.js";
         
         // get a list of the script engines on this platform
         List<ScriptEngineFactory> list = factory.getEngineFactories();
         
         System.out.println("Script Engine Factories found:");
         
         for (ScriptEngineFactory f : list) { 
            System.out.println(" Name = " + f.getEngineName()
               + " language = " + f.getLanguageName()
               + " extensions = " + f.getExtensions());
         }
         
         // get the JavaScript engine
         ScriptEngine jsEngine = factory.getEngineByName("js");
         
         // run the script
         game.executeScript(jsEngine, scriptFileName);
         
         game.run();
         
      } catch (Exception e) {
         e.printStackTrace(System.err);
      } finally {
         game.shutdown();
         game.exit();
      }
   }
   
   public void executeScript(ScriptEngine engine, String scriptFileName) {
      try { 
         FileReader fileReader = new FileReader(scriptFileName);
         engine.eval(fileReader); //execute the script statements in the file
         fileReader.close();
      }
      
      catch (FileNotFoundException e1) { 
         System.out.println(scriptFileName + " not found " + e1); 
      }
      catch (IOException e2) { 
         System.out.println("IO problem with " + scriptFileName + e2); 
      }
      catch (ScriptException e3) { 
         System.out.println("ScriptException in " + scriptFileName + e3); 
      }
      catch (NullPointerException e4) { 
         System.out.println ("Null ptr exception in " + scriptFileName + e4); 
      }
   }

	
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
		
		//full screen mode
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
      
      snowmanSE = sm.createSkeletalEntity("snowman", "snowman.rkm", "snowman.rks");
      
      Texture tex2 = sm.getTextureManager().getAssetByPath("coloredgoodsnowman.png");
      TextureState tstate2 = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
      tstate2.setTexture(tex2);
      snowmanSE.setRenderState(tstate2);
      // attach the entity to a scene node
      SceneNode snowmanN = sm.getRootSceneNode().createChildSceneNode("snowmanNode");
      snowmanN.attachObject(snowmanSE);
      snowmanN.scale(.5f, .5f, .5f);
      snowmanN.translate(0, .5f, 0);
      snowmanN.moveDown(149f);
      
      snowmanSE.loadAnimation("waveAnimation", "snowman.rka");
      
      //skybox
      //http://www.custommapmakers.org/skyboxes.php
      Configuration config = eng.getConfiguration();
      TextureManager tm = eng.getTextureManager();
      tm.setBaseDirectoryPath(config.valueOf("assets.skyboxes.path"));
      Texture front = tm.getAssetByPath("front.jpg");
      Texture back = tm.getAssetByPath("back.jpg");
      Texture left = tm.getAssetByPath("left.jpg");
      Texture right = tm.getAssetByPath("right.jpg");
      Texture top = tm.getAssetByPath("top.jpg");
      Texture bottom = tm.getAssetByPath("bottom.jpg");
      tm.setBaseDirectoryPath(config.valueOf("assets.textures.path"));
      
      AffineTransform xform = new AffineTransform();
      xform.translate(0,front.getImage().getHeight());
      xform.scale(1d, -1d);
      
      front.transform(xform);
      back.transform(xform);
      left.transform(xform);
      right.transform(xform);
      top.transform(xform);
      bottom.transform(xform);
      
      SkyBox sb = sm.createSkyBox(SKYBOX);
      sb.setTexture(front,  SkyBox.Face.FRONT);
      sb.setTexture(back,  SkyBox.Face.BACK);
      sb.setTexture(left,  SkyBox.Face.LEFT);
      sb.setTexture(right,  SkyBox.Face.RIGHT);
      sb.setTexture(top,  SkyBox.Face.TOP);
      sb.setTexture(bottom,  SkyBox.Face.BOTTOM);
      sm.setActiveSkyBox(sb);
      
      //terrain
      Tessellation tessE = sm.createTessellation("tessE", 6);
      tessE.setSubdivisions(8f);
      SceneNode tessN = sm.getRootSceneNode().createChildSceneNode("tessN");
      tessN.attachObject(tessE);
      tessN.moveDown(150f);
      tessN.scale(200, 150, 200);
      tessE.setHeightMap(eng, "height_map.jpg");
      tessE.setTexture(eng, "bottom.jpg");
      
      //second instance of terrain (to fill gaps)
      Tessellation tessE2 = sm.createTessellation("tessE2", 6);
      tessE2.setSubdivisions(8f);
      SceneNode tessN2 = sm.getRootSceneNode().createChildSceneNode("tessN2");
      tessN2.attachObject(tessE2);
      tessN2.moveDown(150.1f);
      tessN2.scale(200, 150, 200);
      tessE2.setHeightMap(eng, "height_map.jpg");
      tessE2.setTexture(eng, "bottom.jpg");    
      
      //dolphin
	  Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
      dolphinE.setPrimitive(Primitive.TRIANGLES);
      
      SceneNode dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
      Angle faceFront = Degreef.createFrom(45.0f);
        
      dolphinN.moveBackward(0.5f);
      dolphinN.moveDown(148f);
      dolphinN.yaw(faceFront);
      dolphinN.attachObject(dolphinE);
      
      //add shark and animations
      SkeletalEntity sharkSE = sm.createSkeletalEntity("sharkAvatar", "sharkAvatar.rkm",  
    		  "sharkAvatar.rks");
      Texture tex = sm.getTextureManager().getAssetByPath("sharkAvatarUV.jpg");
      TextureState tstate = (TextureState)sm.getRenderSystem().
    		  createRenderState(RenderState.Type.TEXTURE);
      tstate.setTexture(tex);
      sharkSE.setRenderState(tstate);
      
      SceneNode sharkN = dolphinN.createChildSceneNode("sharkNode");
      sharkN.attachObject(sharkSE);
      sharkN.scale(0.15f,0.15f,0.15f);
      sharkN.translate(-0.75f, 0f, -0.75f);
      sharkN.yaw(angle);
      
      sharkSE.loadAnimation("moveShark", "sharkAvatar.rka");
      
      //lights
      sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		
      /*Light plight = sm.createLight("testLamp1", Light.Type.POINT);
      plight.setAmbient(new Color(.3f, .3f, .3f));
      plight.setDiffuse(new Color(.7f, .7f, .7f));
      plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
      plight.setRange(5f);*/
      
      ScriptEngineManager factory = new ScriptEngineManager();
      java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
      ScriptEngine jsEngine = factory.getEngineByName("js");
      
      File scriptFile2 = new File("CreateLight.js");
      jsEngine.put("sm", sm);
      this.executeScript(jsEngine, "CreateLight.js");
		
      SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
      dolphinN.attachObject((Light)jsEngine.get("plight"));
      
      //plightNode.attachObject(plight);
      
      //physics
      Entity groundE = sm.createEntity(GROUND_E, "cube.obj");
      groundN = sm.getRootSceneNode().createChildSceneNode(GROUND_N);
      groundN.attachObject(groundE);
      //groundN.moveDown(200f);
      groundN.setLocalPosition(0,-200,-2);
      groundN.scale(3f,0.5f,3f);
      
      setupInputs(sm);
      setupOrbitCameras(eng,sm);
      
      //initAudio(sm);

   }
   
   public void setEarParameters(SceneManager sm) { 
      SceneNode dolphinNode = sm.getSceneNode("myDolphinNode");
      Vector3 avDir = dolphinNode.getWorldForwardAxis();

      // note - should get the camera's forward direction
      // - avatar direction plus azimuth
      audioMgr.getEar().setLocation(dolphinNode.getWorldPosition());
      audioMgr.getEar().setOrientation(avDir, Vector3f.createFrom(0,1,0));
   }
   
   protected void updateVerticalPos() {
	   SceneNode dolphin = this.getEngine().
			   getSceneManager().getSceneNode("myDolphinNode");
	   SceneNode tessN = this.getEngine().
			   getSceneManager().getSceneNode("tessN");
	   Tessellation tessE = ((Tessellation)tessN.getAttachedObject("tessE"));
	   
	   Vector3 worldAvatarPos = dolphin.getWorldPosition();
	   Vector3 localAvatarPos = dolphin.getLocalPosition();
	   
	   Vector3 newAvatarPos = Vector3f.createFrom(localAvatarPos.x(),
			   tessE.getWorldHeight(worldAvatarPos.x(), worldAvatarPos.z()) + 0.75f,
			   localAvatarPos.z());
	   
	   dolphin.setLocalPosition(newAvatarPos);
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
      
      moveForwardAction = new MoveForwardAction(this, dolphinN);
      moveBackwardAction = new MoveBackwardAction(this, dolphinN);
      moveLeftAction = new MoveLeftAction(this, dolphinN);
      moveRightAction = new MoveRightAction(this, dolphinN);
      rotateLeftAction = new RotateLeftAction(this, dolphinN);
      rotateRightAction = new RotateRightAction(this, dolphinN);
      rotateUpAction = new RotateUpAction(this,dolphinN);
      rotateDownAction = new RotateDownAction(this,dolphinN);
      moveSharkAction = new MoveSharkAction(this);
      
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
                         net.java.games.input.Component.Identifier.Key.LEFT, 
                         rotateLeftAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                         
      im.associateAction(keyboard1, 
                         net.java.games.input.Component.Identifier.Key.RIGHT, 
                         rotateRightAction,
                         InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
      im.associateAction(keyboard1, 
              net.java.games.input.Component.Identifier.Key.UP, 
              rotateUpAction,
              InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      
      im.associateAction(keyboard1, 
              net.java.games.input.Component.Identifier.Key.DOWN, 
              rotateDownAction,
              InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
      

      im.associateAction(keyboard1, 
              net.java.games.input.Component.Identifier.Key._1, 
              moveSharkAction,
              InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
   }

   @Override
   protected void update(Engine engine) {
		// build and set HUD
	   rs = (GL4RenderSystem) engine.getRenderSystem();
	   elapsTime += engine.getElapsedTimeMillis();
	   elapsTimeSec = Math.round(elapsTime/1000.0f);
	   elapsTimeStr = Integer.toString(elapsTimeSec);
	   dispStr = "Assignment #3   " + "Player 1   " + "Time = " + elapsTimeStr + "   Score = " + score;
	   rs.setHUD(dispStr, 15, 15);
	
	   im.update(elapsTime);
	   orbitController.updateCameraPosition();

	   snowmanSE.update();
      
	   //update shark animation
	   SkeletalEntity sharkSE = (SkeletalEntity)engine.
			getSceneManager().getEntity("sharkAvatar");
	   sharkSE.update();
		
      SceneManager sm = engine.getSceneManager();
      
      SceneNode dolphinN = getEngine().getSceneManager().getSceneNode("myDolphinNode");
      backgroundMusic.setLocation(dolphinN.getWorldPosition());
      setEarParameters(sm);
      
	   //physics
	   if (running) {
		Matrix4 mat;
		physEng.update(elapsTime);
		for (SceneNode s: engine.getSceneManager().getSceneNodes()) {
			if (s.getPhysicsObject() != null) {
				mat = Matrix4f.createFrom(toFloatArray(
						s.getPhysicsObject().getTransform()));
				s.setLocalPosition(mat.value(0, 3),mat.value(1, 3), mat.value(2, 3));
			}
		}
	   }
   }
   
   public void initAudio(SceneManager sm) { 
      AudioResource oceanTrack;
      audioMgr = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");
      if (!audioMgr.initialize()) { 
         System.out.println("Audio Manager failed to initialize!");
         return;
      }

      oceanTrack = audioMgr.createAudioResource("oceanMusic.mp3",
      AudioResourceType.AUDIO_SAMPLE);

      backgroundMusic = new Sound(oceanTrack,
      SoundType.SOUND_EFFECT, 100, true);
   
      backgroundMusic.initialize(audioMgr);

      backgroundMusic.setMaxDistance(10.0f);
      backgroundMusic.setMinDistance(0.5f);
      backgroundMusic.setRollOff(5.0f);
      SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
      backgroundMusic.setLocation(dolphinN.getWorldPosition());
      setEarParameters(sm);
      backgroundMusic.play();
   }
   
   //physics
   private void graphicsWorldObjects() throws IOException {
	   Engine eng = this.getEngine();
	   SceneManager sm = eng.getSceneManager();
	   
	   if (foodCount == 1 && sm.hasEntity("sphere5")) {
		   for (int i = 1; i < 6; i++) {
			   String s = Integer.toString(i);
			   if (sm.hasSceneNode("sphere"+s+"Node")) {
				   sm.destroySceneNode("sphere"+s+"Node");
			   }
			   sm.destroyEntity("sphere"+s);
		   }
	   }
	   
	   Texture sphereTex = eng.getTextureManager().getAssetByPath("red.jpeg");
	   TextureState sphereTexState = (TextureState)sm.getRenderSystem().
			   createRenderState(RenderState.Type.TEXTURE);
	   sphereTexState.setTexture(sphereTex);
	   
	   Float translate1 = (float)(r1.nextInt(8) - 4);
	   Float translate2 = (float)(r2.nextInt(8) - 4);
	   String s = Integer.toString(foodCount);
	   Entity sphere = sm.createEntity("sphere"+ s, "sphere.obj");
	   sphere.setRenderState(sphereTexState);
	   SceneNode sphereNode = sm.getRootSceneNode().createChildSceneNode("sphere"+s+"Node");
	   sphereNode.attachObject(sphere);
	   sphereNode.scale(0.2f,0.2f,0.2f);
	   sphereNode.translate(translate1, 0, translate2);
	   sphereNode.moveDown(142f);
	      
	   initPhysicsSystem();
	   createRagePhysicsWorld(sm);  
   }
   
   private void initPhysicsSystem() {
	   String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
	   float[] gravity = {0, -0.5f, 0};
	   
	   physEng = PhysicsEngineFactory.createPhysicsEngine(engine);
	   physEng.initSystem();
	   physEng.setGravity(gravity);
   }
   
   private void createRagePhysicsWorld(SceneManager sm) {
	   float mass = 1.0f;
	   float up[] = {0, 1, 0};
	   double[] temptf;
	
	   if (foodCount == 1) {
		   SceneNode node1 = sm.getSceneNode("sphere1Node");
		   temptf = toDoubleArray(node1.getLocalTransform().toFloatArray());
		   physSphere1 = physEng.addSphereObject(physEng.nextUID(),
				   mass, temptf, 2.0f);
		   node1.setPhysicsObject(physSphere1);
	   } else if (foodCount == 2) {
		   SceneNode node2 = sm.getSceneNode("sphere2Node");
		   temptf = toDoubleArray(node2.getLocalTransform().toFloatArray());
		   physSphere2 = physEng.addSphereObject(physEng.nextUID(),
				   mass, temptf, 2.0f);
		   node2.setPhysicsObject(physSphere2);
	   } else if (foodCount == 3) {
		   SceneNode node3 = sm.getSceneNode("sphere3Node");
		   temptf = toDoubleArray(node3.getLocalTransform().toFloatArray());
		   physSphere3 = physEng.addSphereObject(physEng.nextUID(),
				   mass, temptf, 2.0f);
		   node3.setPhysicsObject(physSphere3);
	   } else if (foodCount == 4) {
		   SceneNode node4 = sm.getSceneNode("sphere4Node");
		   temptf = toDoubleArray(node4.getLocalTransform().toFloatArray());
		   physSphere4 = physEng.addSphereObject(physEng.nextUID(),
				   mass, temptf, 2.0f);
		   node4.setPhysicsObject(physSphere4);
	   } else if (foodCount == 5) {
		   SceneNode node5 = sm.getSceneNode("sphere5Node");
		   temptf = toDoubleArray(node5.getLocalTransform().toFloatArray());
		   physSphere5 = physEng.addSphereObject(physEng.nextUID(),
				   mass, temptf, 2.0f);
		   node5.setPhysicsObject(physSphere5);
	   }
	   
	   temptf = toDoubleArray(groundN.getLocalTransform().toFloatArray());
	   groundPlane = physEng.addStaticPlaneObject(physEng.nextUID(),
			    temptf, up, 0.0f);
	   groundN.setPhysicsObject(groundPlane);
   }
   
   private void doTheWave() { 
      SkeletalEntity snowmanSE = (SkeletalEntity)getEngine().getSceneManager().getEntity("snowman");
      snowmanSE.stopAnimation();
      snowmanSE.playAnimation("waveAnimation", 0.5f, LOOP, 0);
   }
   
   public void moveShark() {
	   sharkCount++;
	   SkeletalEntity sharkSE = (SkeletalEntity)getEngine().
			   getSceneManager().getEntity("sharkAvatar");
	   if ((sharkCount % 2) == 0)
		   sharkSE.pauseAnimation();
	   else
		   sharkSE.playAnimation("moveShark", 1.0f, LOOP, 0);
   }
   
   private float[] toFloatArray(double[] arr) {
	   if (arr == null)
		   return null;
	   int n = arr.length;
	   float[] ret = new float[n];
	   for (int i = 0; i < n; i++) {
		   ret[i] = (float)arr[i];
	   }
	   return ret;
   }
   
   private double[] toDoubleArray(float[] arr) {
	   if (arr == null)
		   return null;
	   int n = arr.length;
	   double[] ret = new double[n];
	   for (int i = 0; i < n; i++) {
		   ret[i] = (double)arr[i];
	   }
	   return ret;
   }
   
   //distDetection takes two SceneNodes as parameters and
   //returns the distance between the two as a vector.
   public Vector3 distDetection(SceneNode temp, SceneNode other) {
   	Vector3 vec1 = temp.getWorldPosition();
   	Vector3 vec2 = other.getWorldPosition();
   	Vector3 dist = vec1.sub(vec2);
   	return dist;
   }
   
   public void detectCollision() {
	   SceneManager sm = getEngine().getSceneManager();
	   iter = sm.getSceneNodes().iterator();
	   SceneNode dolphin = sm.getSceneNode("myDolphinNode");
	   SceneNode temp;
	   Vector3 dist;	
	   Float mag;
   	
	   while (iter.hasNext()) {
		   temp = iter.next();
		   String s = temp.getName();
		   if (s.startsWith("sphere")) {
			   dist = distDetection(temp,dolphin);
			   mag = dist.length();
			   if (Math.abs(mag) < 0.5f) {
				   temp.setLocalScale(0.01f,0.01f,0.01f);
				   //temp.translate(0,-10f,0);
				   score++;
   				}
		   }
	   }
   }
   
   @Override
   public void keyPressed(KeyEvent e) {
	  switch (e.getKeyCode()) {
         case KeyEvent.VK_2:
            doTheWave();
            break;
         case KeyEvent.VK_SPACE:
        	 if (foodCount < 5)
        		 foodCount++;
        	 else 
        		 foodCount = 1;
        	 try {
        		 graphicsWorldObjects();
        	 } catch (IOException e1) {
        		 e1.printStackTrace();
        	 }
        	 running = true;
        	 break;
      }
      super.keyPressed(e);
   }
}