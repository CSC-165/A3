package myGameEngine;

import ray.input.InputManager;
import ray.input.action.Action;
import net.java.games.input.Event;
import ray.rage.rendersystem.*;
import ray.rage.scene.*;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import java.lang.Math;
import a3.MyGame;

public class OrbitCameraController {
	
	private Camera camera;
	private SceneNode camNode;
	private SceneNode target;
	private float camAzimuth;
	private float camElevation;
	private float radius;
	private Vector3 targetPos;
	private Vector3 upVec;
	private MyGame game;
	
	public OrbitCameraController(Camera cam, SceneNode camN, SceneNode targ,
			String controller, InputManager im, MyGame g) {
		camera = cam;
		camNode = camN;
		target = targ;
		game = g;
		camAzimuth = 225.0f; //behind and above target
		camElevation = 20f; //degrees
		radius = 2.0f;
		upVec = Vector3f.createFrom(0.0f,1.0f,0.0f);
		if (controller != null)
			setupInput(im,controller);
	}
	
	//Computes azimuth, elevation, and distance relative to target
	//in spherical coordinates. Converts to world Cartesian coordinates
	//and sets camera position.
	public void updateCameraPosition() {
		double theta = Math.toRadians(camAzimuth); //rotate around target
		double phi = Math.toRadians(camElevation); //altitude angle
		double x = radius * Math.cos(phi) * Math.sin(theta);
		double y = radius * Math.sin(phi);
		double z = radius * Math.cos(phi) * Math.cos(theta);
		camNode.setLocalPosition(Vector3f.createFrom((float)x, (float)y,
				(float)z).add(target.getWorldPosition()));
		camNode.lookAt(target, upVec);
	}
	
	public void updateTargetPosition() {
		target.setLocalRotation(camNode.getLocalRotation());
	}
	
	private void setupInput(InputManager im, String cn) {
		Action orbitRightAction = new OrbitAroundRightAction();
		Action orbitLefttAction = new OrbitAroundLeftAction();
		Action zoomIn = new ZoomInAction();
		Action zoomOut = new ZoomOutAction();
		Action increaseElev = new IncreaseElevAngle();
		Action decreaseElev = new DecreaseElevAngle();
		Action modifyElev = new ModifyElevAngle();
		
		Action orbitTargRight = new OrbitRightWithTarg();
		Action orbitTargLeft = new OrbitLeftWithTarg();
		Action incTargElev = new IncreaseElevWithTarg();
		Action decTargElev = new DecreaseElevWithTarg();
		
		im.associateAction(cn, net.java.games.input.Component.Identifier.Key.L, 
			orbitRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.J, 
			orbitLefttAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.V, 
			zoomIn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.B, 
			zoomOut, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.I, 
			increaseElev, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.K, 
			decreaseElev, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
		im.associateAction(cn, net.java.games.input.Component.Identifier.Key.H, 
			orbitTargRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.F, 
			orbitTargLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.T, 
			incTargElev, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.G, 
			decTargElev, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		
      im.associateAction(cn, net.java.games.input.Component.Identifier.Key.E, 
			modifyElev, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	}
	
	private class OrbitAroundRightAction implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float rotAmount;
			if (ev.getValue() < -0.75) {
				rotAmount = -0.75f;
			}else {
				if (ev.getValue() > 0.75) {
					rotAmount = 0.75f;
				}else {
					rotAmount = 0.0f;
				}
			}
			camAzimuth += rotAmount;
			camAzimuth = camAzimuth % 360;
			updateCameraPosition();
		}
	}
	
	private class OrbitAroundLeftAction implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float rotAmount;
			if (ev.getValue() < -0.75) {
				rotAmount = -0.75f;
			}else {
				if (ev.getValue() > 0.75) {
					rotAmount = 0.75f;
				}else {
					rotAmount = 0.0f;
				}
			}
			camAzimuth -= rotAmount;
			camAzimuth = camAzimuth % 360;
			updateCameraPosition();
		}
	}
	
	private class ZoomInAction implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			Vector3 dist = game.distDetection(camNode, target);
			float mag = dist.length();
			//float radAmount;
			if (Math.abs(mag) <= 0.75f) {
				radius = 0.75f;
			}else {
				radius -= 0.1f;
			}
			updateCameraPosition();
		}
	}
	
	private class ZoomOutAction implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			Vector3 dist = game.distDetection(camNode, target);
			float mag = dist.length();
			//float radAmount;
			if (Math.abs(mag) >= 3.0f) {
				radius = 3.0f;
			}else {
				radius += 0.1f;
			}
			updateCameraPosition();
		}
	}
	
	private class IncreaseElevAngle implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float factor = 1.0f;
			if (camElevation >= 89.0f) {
				camElevation = 89.0f;}
			else {
				camElevation += factor;
			}
			updateCameraPosition();
		}
	}
	
	private class DecreaseElevAngle implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float factor = 1.0f;
			if (camElevation <= 5.0f) {
				camElevation = 5.0f;
			}else {
				camElevation -= factor;
			}
			updateCameraPosition();
		}
	}
	 
	private class ModifyElevAngle implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float rotAmount;
			if (ev.getValue() < -0.75) {
				rotAmount = -0.75f;
			}else {
				if (ev.getValue() > 0.75) {
					rotAmount = 0.75f;
				}else {
					rotAmount = 0.0f;
				}
			}
			camElevation += rotAmount;
			updateCameraPosition();
		}
	}
	
	private class OrbitRightWithTarg implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float rotAmount;
			if (ev.getValue() < -0.75) {
				rotAmount = -0.75f;
			}else {
				if (ev.getValue() > 0.75) {
					rotAmount = 0.75f;
				}else {
					rotAmount = 0.0f;
				}
			}
			camAzimuth += rotAmount;
			camAzimuth = camAzimuth % 360;
			updateCameraPosition();
			updateTargetPosition();
		}
	}
	
	private class OrbitLeftWithTarg implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float rotAmount;
			if (ev.getValue() < -0.75) {
				rotAmount = -0.75f;
			}else {
				if (ev.getValue() > 0.75) {
					rotAmount = 0.75f;
				}else {
					rotAmount = 0.0f;
				}
			}
			camAzimuth -= rotAmount;
			camAzimuth = camAzimuth % 360;
			updateCameraPosition();
			updateTargetPosition();
		}
	}
	
	private class IncreaseElevWithTarg implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float factor = 1.0f;
			if (camElevation >= 89.0f) {
				camElevation = 89.0f;}
			else {
				camElevation += factor;
			}
			updateCameraPosition();
			updateTargetPosition();
		}
	}
	
	private class DecreaseElevWithTarg implements Action{
		
		public void performAction(float time, net.java.games.input.Event ev) {
			float factor = 1.0f;
			if (camElevation <= 0.0f) {
				camElevation = 0.0f;
			}else {
				camElevation -= factor;
			}
			updateCameraPosition();
			updateTargetPosition();
		}
	}
	
}
