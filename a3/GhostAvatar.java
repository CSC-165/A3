package a3;

import java.util.UUID;
import ray.rage.scene.*;
import ray.rml.Vector3f;

public class GhostAvatar { 
      private UUID id;
      private SceneNode node;
      private Entity entity;
      private Vector3f position;
      
      public GhostAvatar(UUID id, Vector3f position) { 
         this.id = id;
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
      
      public void setPosition(Vector3f positionSet) {
         this.position = positionSet;
      }
      
      // accessors and setters for id, node, entity, and position
}