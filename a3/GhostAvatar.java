package a3;

import java.util.UUID;
import ray.rage.scene.*;
import ray.rml.Vector3f;
import ray.rml.Vector3;

public class GhostAvatar { 
      private UUID id;
      private SceneNode node;
      private Entity entity;
      private Vector3 position;
      
      public GhostAvatar(UUID id, Vector3 position) { 
         this.id = id;
         this.position = position;
      }
      
      public UUID getID() {
         return this.id;
      }
      
      public void setNode(SceneNode nodeSet) {
         this.node = nodeSet;
      }
      
      public void setEntity(Entity entitySet) {
         this.entity = entitySet;
      }
      
      public void setPosition(Vector3 positionSet) {
         this.position = positionSet;
      }
      
      public Vector3 getPosition() {
         return this.position;
      }
      
      public SceneNode getNode()  {
         return this.node;
      }
      
      // accessors and setters for id, node, entity, and position
}