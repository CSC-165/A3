package a3;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
import ray.networking.IGameConnection.ProtocolType;
import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;

import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import myGameEngine.*;

import com.jogamp.openal.ALFactory;

public class MyGame extends VariableFrameRateGame {
	int count = 0;

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
	
	private OrbitCameraController orbitController;
	
	private SceneNode cube1N, cube2N, cube3N;
	private Entity cube1E, cube2E, cube3E;
	private Entity ghostE;

	private SceneNode dolphinN, fishN, snowmanN, sharkN;
	
	//directional light
	private SceneNode dlightNode;
	private Light dlight;
	private int lightCount = 0;
	
	//external models
	private SkeletalEntity snowmanSE;
	private int sharkCount = 0;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected;
	private Vector<UUID> gameObjectsToRemove;
	private Iterator<UUID> it;
   
	private SceneManager sceneM;

	private Angle angle = Degreef.createFrom(-75.0f);
	private Angle fishPitchAngle = Degreef.createFrom(90.0f);
	private Angle fishYawAngle = Degreef.createFrom(-120.0f);
	
	//physics/collision detection
	private SceneNode mineN, groundN;
	private SkeletalEntity mineSE;
	private PhysicsEngine physEng;
	private PhysicsObject physMine,  groundPlane;
	private boolean running = false;
	
	private Random r = new Random();
	private Random r2 = new Random();

	private Iterator<SceneNode> iter;
	private int health = 0;
   
	//sound
	private IAudioManager audioMgr;
    private Sound oceanSound, tickSound, explosionSound;
   
	private String avatarID;
   
   //private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();
   //int a[]=new int[5];
	//private GhostAvatar ghostAvatars[] = new GhostAvatar[10];
   //private List<GhostAvatar> ghostAvatars = new ArrayList<GhostAvatar>();
   //private Iterator<GhostAvatar> itr = ghostAvatars.iterator();
   
   public MyGame(String serverAddr, int sPort) {
		super();
      
		this.serverAddress = serverAddr;
		this.serverPort = sPort;
		this.serverProtocol = ProtocolType.UDP;
      
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
		
		System.out.println("\nOther Controls:");
		System.out.println("0 to turn directional light on/off");
		System.out.println("1 to start/stop shark animation");
		System.out.println("2 to start/stop snowman animation");
   }

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

		System.out.println("\nOther Controls:");
		System.out.println("0 to turn directional light on/off");
		System.out.println("1 to start/stop shark animation");
		System.out.println("2 to start/stop snowman animation");
   }

   public static void main(String[] args) {
	   MyGame game = new MyGame(args[0], Integer.parseInt(args[1]));
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
   
   public void setIsConnected(boolean isConnected) {
      this.isClientConnected = isConnected;   
   }
   
   private void setupNetworking() { 
      gameObjectsToRemove = new Vector<UUID>();
      isClientConnected = false;

      try { 
         protClient = new ProtocolClient(InetAddress.
         getByName(serverAddress), serverPort, serverProtocol, this);
      } catch (UnknownHostException e) { 
         e.printStackTrace();
      } catch (IOException e) { 
         e.printStackTrace();
      }

      if (protClient == null) { 
         System.out.println("missing protocol host"); 
      }
      
      else { // ask client protocol to send initial join message to server, with a unique identifier for this client
         protClient.sendJoinMessage();
         System.out.println("sent join message from protocolclient");
      } 
   }
   
   protected void processNetworking(float elapsTime) { // Process packets received by the client from the server
      
      if (protClient != null) {
         protClient.processPackets();
      }

      // remove ghost avatars for players who have left the game
      it = gameObjectsToRemove.iterator();

      while(it.hasNext()) { 
         sceneM.destroySceneNode(it.next().toString());
      }
      gameObjectsToRemove.clear();
   }
   
   public Vector3 getPlayerPosition() { 
      return dolphinN.getWorldPosition();
   }
   
   public void addGhostAvatarToGameWorld(GhostAvatar avatar) throws IOException { 
      if (avatar != null) {  
         ghostE = this.getEngine().getSceneManager().createEntity("ghost", "dolphinHighPoly.obj");
         ghostE.setPrimitive(Primitive.TRIANGLES);
         SceneNode ghostN = this.getEngine().getSceneManager().getRootSceneNode().
            createChildSceneNode(avatar.getID().toString());
         ghostN.attachObject(ghostE);
         ghostN.setLocalPosition(avatar.getPosition());
         avatar.setNode(ghostN);
         avatar.setEntity(ghostE);
         
         avatar.setPosition(avatar.getPosition());
      } 
   }
   
   public void removeGhostAvatarFromGameWorld(GhostAvatar avatar) { 
      if (avatar != null) gameObjectsToRemove.add(avatar.getID());
   }
   
   private class SendCloseConnectionPacketAction extends AbstractInputAction { 
      // for leaving the game... need to attach to an input device
      @Override
      public void performAction(float time, Event evt) { 
         if (protClient != null && isClientConnected == true) { 
            protClient.sendByeMessage();
         } 
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
		//(comment out rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);)
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
      
      dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
      dolphinN.moveBackward(1.5f);
      dolphinN.moveDown(148f);
      dolphinN.attachObject(dolphinE);
   
      //External objects
      //snowman
      snowmanSE = sm.createSkeletalEntity("snowman", "snowman.rkm", "snowman.rks");
      Texture snowmanTex = sm.getTextureManager().getAssetByPath("coloredgoodsnowman.png");
      TextureState snowmanTState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
      snowmanTState.setTexture(snowmanTex);
      snowmanSE.setRenderState(snowmanTState);
     
      snowmanN = sm.getRootSceneNode().createChildSceneNode("snowmanNode");
      snowmanN.attachObject(snowmanSE);
      snowmanN.scale(.5f, .5f, .5f);
      snowmanN.translate(0, .5f, 0);
      snowmanN.moveDown(149f);
      
      snowmanSE.loadAnimation("waveAnimation", "snowman.rka");
      
      //shark
      SkeletalEntity sharkSE = sm.createSkeletalEntity("sharkAvatar", "sharkAvatar.rkm",  
    		  "sharkAvatar.rks");
      Texture sharkTex = sm.getTextureManager().getAssetByPath("sharkAvatarUV.jpg");
      TextureState sharkTState = (TextureState)sm.getRenderSystem().
    		  createRenderState(RenderState.Type.TEXTURE);
      sharkTState.setTexture(sharkTex);
      sharkSE.setRenderState(sharkTState);
      
      SceneNode sharkNode = sm.getRootSceneNode().createChildSceneNode("sharkNode");
      sharkN = sharkNode.createChildSceneNode("shark1Node");
      sharkN.attachObject(sharkSE);
      sharkN.scale(0.15f,0.15f,0.15f);
      sharkN.translate(-1.75f, -148f, -1.75f);
      sharkN.yaw(angle);
      
      sharkSE.loadAnimation("moveShark", "sharkAvatar.rka");
      
      //fish
      SceneNode fishNode = sm.getRootSceneNode().createChildSceneNode("fishNode");
      fishNode.moveDown(148f);
  	
      for (int i = 0; i < 10; i++){
      	String s = Integer.toString(i);
      	SkeletalEntity fishSE = sm.createSkeletalEntity("fish" + s, "fish.rkm", "fish.rks");
      	Texture fishTex = eng.getTextureManager().getAssetByPath("fishUV.jpg");
        TextureState fishTState = (TextureState)sm.getRenderSystem().
      			createRenderState(RenderState.Type.TEXTURE);
      	fishTState.setTexture(fishTex);
      	fishSE.setRenderState(fishTState);
      	
      	fishN = fishNode.createChildSceneNode("fish"+s+"Node");
      	fishN.attachObject(fishSE);
      	fishN.scale(0.1f,0.1f,0.1f);
      	fishN.translate((float)(r.nextInt(6) - 3), 0, (float)(r.nextInt(6) - 3));
        fishN.pitch(fishPitchAngle);
        fishN.yaw(fishYawAngle);
      } 
      
      //lights
      sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
      
      ScriptEngineManager factory = new ScriptEngineManager();
      java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
      ScriptEngine jsEngine = factory.getEngineByName("js");
      
      File scriptFile2 = new File("CreateLight.js");
      jsEngine.put("sm", sm);
      this.executeScript(jsEngine, "CreateLight.js");
      dolphinN.attachObject((Light)jsEngine.get("plight"));
      
      //physics
      mineSE = sm.createSkeletalEntity("mine","mine.rkm","mine.rks");
	  Texture mineTex = sm.getTextureManager().getAssetByPath("mine.jpg");
	  TextureState mineTState = (TextureState)sm.getRenderSystem().
		   createRenderState(RenderState.Type.TEXTURE);
	  mineTState.setTexture(mineTex);
	  mineSE.setRenderState(mineTState);
      mineN = sm.getRootSceneNode().createChildSceneNode("mineNode");
	  mineN.attachObject(mineSE);
	  mineN.scale(0.25f, 0.25f, 0.25f);
	  
      Entity groundE = sm.createEntity(GROUND_E, "cube.obj");
      groundN = sm.getRootSceneNode().createChildSceneNode(GROUND_N);
      groundN.attachObject(groundE);
      groundN.moveDown(200f);
      
      setupNetworking();
      setupInputs(sm);
      setupOrbitCameras(eng,sm);
      
      initAudio(sm);
   }
   
   protected void setupOrbitCameras(Engine eng, SceneManager sm) {
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
      
      moveForwardAction = new MoveForwardAction(this, dolphinN, protClient);
      moveBackwardAction = new MoveBackwardAction(this, dolphinN, protClient);
      moveLeftAction = new MoveLeftAction(this, dolphinN, protClient);
      moveRightAction = new MoveRightAction(this, dolphinN, protClient);
      rotateLeftAction = new RotateLeftAction(this, dolphinN, protClient);
      rotateRightAction = new RotateRightAction(this, dolphinN, protClient);
      rotateUpAction = new RotateUpAction(this,dolphinN, protClient);
      rotateDownAction = new RotateDownAction(this,dolphinN, protClient);

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
                  
                  //Gamepad associations
      if (im.getFirstGamepadName() != null) {   
                            
         im.associateAction(gamePad1, 
                            net.java.games.input.Component.Identifier.Button._3, 
                            moveForwardAction,
                            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                            
         im.associateAction(gamePad1, 
                            net.java.games.input.Component.Identifier.Button._0, 
                            moveBackwardAction,
                            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                            
         im.associateAction(gamePad1, 
                            net.java.games.input.Component.Identifier.Button._2, 
                            moveLeftAction,
                            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                            
         im.associateAction(gamePad1, 
                            net.java.games.input.Component.Identifier.Button._1, 
                            moveRightAction,
                            InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                         
      }
   }
   
   protected void updateVerticalPos(SceneNode node) {
	   Vector3 newAvatarPos;
	   SceneNode tessN = this.getEngine().
			   getSceneManager().getSceneNode("tessN");
	   Tessellation tessE = ((Tessellation)tessN.getAttachedObject("tessE"));
	   
	   Vector3 worldAvatarPos = node.getWorldPosition();
	   Vector3 localAvatarPos = node.getLocalPosition();
	   float tessY = tessE.getWorldHeight(worldAvatarPos.x(), worldAvatarPos.z()) + 1.0f;
	   float fishY = fishN.getWorldPosition().y();
	   if (fishY > tessY && fishY < (tessY + 5.0f)) 
		   newAvatarPos = Vector3f.createFrom(localAvatarPos.x(), fishY,
				   localAvatarPos.z());
	   else
		   newAvatarPos = Vector3f.createFrom(localAvatarPos.x(), tessY,
				   localAvatarPos.z());
	   
	   
	   node.setLocalPosition(newAvatarPos);
   }

   @Override
   protected void update(Engine engine) {
		// build and set HUD
	   rs = (GL4RenderSystem) engine.getRenderSystem();
	   SceneManager sm = engine.getSceneManager();
	   elapsTime += engine.getElapsedTimeMillis();
	   elapsTimeSec = Math.round(elapsTime/1000.0f);
	   elapsTimeStr = Integer.toString(elapsTimeSec);
	   dispStr = "Assignment #3   " + "Player 1   " + "Time = " + elapsTimeStr + "   Health = " + health;
	   rs.setHUD(dispStr, 15, 15);
	
	   im.update(elapsTime);
	   orbitController.updateCameraPosition();
	   
	   //move fish and shark
	   SceneNode fish = sm.getSceneNode("fishNode");
	   SceneNode shark = sm.getSceneNode("sharkNode");
	   moveFish(engine, fish);
	   moveFish(engine, shark);
      
	   //update animation
	   SkeletalEntity sharkSE = (SkeletalEntity)engine.
			getSceneManager().getEntity("sharkAvatar");
	   sharkSE.update();
	   snowmanSE.update();
      
      processNetworking(elapsTime);
      
      //sound
      tickSound.setLocation(mineN.getWorldPosition());
      explosionSound.setLocation(mineN.getWorldPosition());
      oceanSound.setLocation(dolphinN.getWorldPosition());
      setEarParameters(sm);
      
	  //physics
      if (elapsTimeSec % 10 == 0) 
    	  moveMine(sm);
      
      detectCollision();
          
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

   public void setEarParameters(SceneManager sm) { 
      Vector3 avDir = dolphinN.getWorldForwardAxis();

      // note - should get the camera's forward direction
      // - avatar direction plus azimuth
      audioMgr.getEar().setLocation(dolphinN.getWorldPosition());
      audioMgr.getEar().setOrientation(avDir, Vector3f.createFrom(0,1,0));
   }

   public void initAudio(SceneManager sm) { 
	  AudioResource oceanResource, tickResource, explosionResource;
	  audioMgr = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");
      if (!audioMgr.initialize()) { 
         System.out.println("Audio Manager failed to initialize!");
         return;
      }

      oceanResource = audioMgr.createAudioResource("sounds/ocean.wav", 
    		  AudioResourceType.AUDIO_STREAM);
      tickResource = audioMgr.createAudioResource("sounds/tickingSound.wav",
    		  AudioResourceType.AUDIO_SAMPLE);
      explosionResource = audioMgr.createAudioResource("sounds/explosionSound.wav",
    		  AudioResourceType.AUDIO_SAMPLE);
      
      oceanSound = new Sound(oceanResource,
    		  SoundType.SOUND_MUSIC, 75, true);
      oceanSound.initialize(audioMgr);
      oceanSound.setMaxDistance(10.0f);
      oceanSound.setMinDistance(0.5f);
      oceanSound.setLocation(dolphinN.getWorldPosition());
      
      tickSound = new Sound(tickResource,
    		  SoundType.SOUND_EFFECT, 75, true);
      tickSound.initialize(audioMgr);
      tickSound.setMaxDistance(10.0f);
      tickSound.setMinDistance(0.5f);
      tickSound.setRollOff(5.0f);     
      tickSound.setLocation(mineN.getWorldPosition());
      
      explosionSound = new Sound(explosionResource,
    		  SoundType.SOUND_EFFECT, 200, true);
      explosionSound.initialize(audioMgr);
      explosionSound.setMaxDistance(10.0f);
      explosionSound.setMinDistance(0.5f);
      explosionSound.setRollOff(5.0f); 
      explosionSound.setLocation(mineN.getWorldPosition());
      
      setEarParameters(sm);
      oceanSound.play();
      explosionSound.play();
      tickSound.play();
   }
   
   private void initPhysicsSystem() {
	   String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
	   float[] gravity = {0, -0.75f, 0};
	   
	   physEng = PhysicsEngineFactory.createPhysicsEngine(engine);
	   physEng.initSystem();
	   physEng.setGravity(gravity);
   }
   
   private void createRagePhysicsWorld(SceneManager sm) {
	   float mass = 1.0f;
	   float up[] = {0, 1, 0};
	   double[] temptf;
	   
	   temptf = toDoubleArray(mineN.getLocalTransform().toFloatArray());
	   physMine = physEng.addSphereObject(physEng.nextUID(),
			   mass, temptf, 2.0f);
	   mineN.setPhysicsObject(physMine);
	   
	   temptf = toDoubleArray(groundN.getLocalTransform().toFloatArray());
	   groundPlane = physEng.addStaticPlaneObject(physEng.nextUID(),
			    temptf, up, 0.0f);
	   groundN.setPhysicsObject(groundPlane);
   }
   
   public void moveMine(SceneManager sm) {
	   running = false;
	   mineN.setLocalPosition(dolphinN.getWorldPosition());
	   mineN.moveUp(1f);
       initPhysicsSystem();
 	   createRagePhysicsWorld(sm);
	   running = true;
   }
   
   public void moveFish(Engine eng, SceneNode node) {
	   node.moveForward(eng.getElapsedTimeMillis()/1000);
	   Angle turn = Degreef.createFrom(15.0f);
	   count++;
	   if (count % 200 == 0) {
		   node.yaw(turn);
	   }
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
   
   public Vector3 distDetection(SceneNode temp, SceneNode other) {
	   Vector3 vec1 = temp.getWorldPosition();
	   Vector3 vec2 = other.getWorldPosition();
	   Vector3 dist = vec1.sub(vec2);
	   return dist;
   }
   
   public void detectCollision() {
	   SceneManager sm = getEngine().getSceneManager();
	   iter = sm.getSceneNodes().iterator();
	   Float mineMag, fishMag;
	   
	   mineMag = distDetection(mineN, dolphinN).length();
	   if (Math.abs(mineMag) < 0.2f) {
		   health --;
		   explosionSound.togglePause();
		   sm.getAmbientLight().setIntensity(new Color(1.0f, 0.0f, 0.0f));
	   }else {
		   sm.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));
		   explosionSound.stop();
	   }
	   while (iter.hasNext()) {
		   SceneNode temp = iter.next();
		   String s = temp.getName();
		   if (s.startsWith("fish")){   
			   fishMag = distDetection(temp, dolphinN).length();
			   if (Math.abs(fishMag) < 0.3f) {
				   System.out.println("fish");
				   health ++;
				   float pos = (float)r.nextInt(10) - 5;
				   if (temp.getName().equals("fishNode")) 
					   temp.setLocalPosition(pos, -148f, pos);
				   else
					   temp.setLocalPosition(pos, 0f, pos);
			   }
		   }else if (s.startsWith("shark")){
			   fishMag = distDetection(temp, dolphinN).length();
			   if (Math.abs(fishMag) < 0.3f) {
				   System.out.println("shark");
				   health --;
				   float pos = (float)r.nextInt(10) - 5;
				   temp.setLocalPosition(pos, -148f, pos);
			   }
		   }
	   }
   }
   
   @Override
   public void keyPressed(KeyEvent e) {
	   SceneManager sm = this.getEngine().getSceneManager();
	  switch (e.getKeyCode()) {
         case KeyEvent.VK_2:
            doTheWave();
            break;
         case KeyEvent.VK_ESCAPE:
            System.exit(0);
         
         case KeyEvent.VK_0:
        	 lightCount++;
        	 if (lightCount % 2 != 0) {
        		dlight = sm.createLight("dirLight",  Light.Type.DIRECTIONAL);
       	      	dlight.setAmbient(new Color(.3f, .3f, .3f));
       	      	dlight.setDiffuse(new Color(.7f, .7f, .7f));
       	      	dlight.setSpecular(new Color(1f, 1f, 1f));
       	      	dlight.setRange(10f);
       	      	dlightNode = sm.getRootSceneNode().createChildSceneNode("dlightNode");
       	      	dlightNode.moveDown(140f);
       	      	dlightNode.attachObject(dlight);
        	 }   
        	 else {
        		 sm.destroySceneNode(dlightNode);
        		 sm.destroyLight(dlight);
        	 }
        	 break;
      }
      super.keyPressed(e);
   }
}