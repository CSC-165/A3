package a3;

import ray.rage.scene.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;

public class RotateController extends AbstractController {
   private Angle rotLeft = Degreef.createFrom(3.0f);

   @Override
   protected void updateImpl(float elapsedTimeMillis) { 
      
      for (Node n : super.controlledNodesList) { 
         n.yaw(rotLeft);
      }
   }
}