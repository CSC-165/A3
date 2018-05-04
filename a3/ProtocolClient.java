package a3;

import java.util.UUID;
import java.util.Vector;
import java.net.InetAddress;
import java.io.IOException;

import ray.networking.client.GameConnectionClient;
import ray.networking.IGameConnection.ProtocolType;
import ray.rml.Vector3f;
import ray.rage.scene.*;

public class ProtocolClient extends GameConnectionClient { 
   
   private MyGame game;
   private UUID id;
   private Vector<GhostAvatar> ghostAvatars;
   
   public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType, MyGame game) throws IOException { 
      super(remAddr, remPort, pType);
      this.game = game;
      this.id = UUID.randomUUID();
      this.ghostAvatars = new Vector<GhostAvatar>();
   }
   
   @Override
   protected void processPacket(Object msg) { 
      String strMessage = (String)msg;
      String[] msgTokens = strMessage.split(",");
      if (msgTokens.length > 0) {
         if (msgTokens[0].compareTo("join") == 0) { // receive "join" 
         // format: join, success or join, failure
            if (msgTokens[1].compareTo("success") == 0) { 
               game.setIsConnected(true);
               sendCreateMessage(game.getPlayerPosition());
            }
            
            if (msgTokens[1].compareTo("failure") == 0) { 
               game.setIsConnected(false);
            } 
         }
         if (msgTokens[0].compareTo("bye") == 0) { // receive "bye" 
            // format: bye, remoteId
            UUID ghostID = UUID.fromString(msgTokens[1]);
            removeGhostAvatar(ghostID);
         }                              // receive "dsfr"
         if ((msgTokens[0].compareTo("dsfr") == 0 ) || (msgTokens[0].compareTo("create")==0)) { // format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
            UUID ghostID = UUID.fromString(msgTokens[1]);
            Vector3f ghostPosition = (Vector3f) Vector3f.createFrom(
            Float.parseFloat(msgTokens[2]),
            Float.parseFloat(msgTokens[3]),
            Float.parseFloat(msgTokens[4]));
            
            //try { 
               createGhostAvatar(ghostID, ghostPosition);
            //} catch (IOException e) { 
            //   System.out.println("error creating ghost avatar");
            //} 
         }

         if (msgTokens[0].compareTo("wsds") == 0) { // rec. “create…” 
            // etc….. 
         }
         
         if (msgTokens[0].compareTo("wsds") == 0) { // rec. “wants…” 
            // etc….. 
         }
         if (msgTokens[0].compareTo("move") == 0) { // rec. “move...” 
            // etc….. 
         }
      }
   }
   
   public void removeGhostAvatar(UUID id) {
      System.out.println(id + " removed.");
   }
   
   public void createGhostAvatar(UUID id, Vector3f position) {
      System.out.println("Ghost avatar created");
   }
   
   public void sendJoinMessage() { // format: join, localId 
      try { 
         sendPacket(new String("join," + id.toString()));
      } catch (IOException e) { 
         e.printStackTrace();
      } 
   }
   
   public void sendCreateMessage(Vector3f pos) { // format: (create, localId, x,y,z)
      try { 
         String message = new String("create," + id.toString());
         message += "," + pos.x()+"," + pos.y() + "," + pos.z();
         sendPacket(message);
      }
      catch (IOException e) { 
         e.printStackTrace();
      } 
   }
   
   public void sendByeMessage() { 
      // etc….. 
   }
   
   public void sendDetailsForMessage(UUID remId, Vector3f pos) { 
      // etc….. 
   }

   public void sendMoveMessage(Vector3f pos) { 
      // etc….. 
   }
   
}
   