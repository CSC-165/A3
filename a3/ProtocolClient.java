package a3;

import java.util.UUID;
import java.util.Vector;
import java.net.InetAddress;
import java.io.IOException;

import ray.networking.client.GameConnectionClient;
import ray.networking.IGameConnection.ProtocolType;
import ray.rml.Vector3f;
import ray.rml.Vector3;
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
      System.out.println("protocolclient instantiated successfully");
   }
   
   @Override
   protected void processPacket(Object msg) { 
      String strMessage = (String)msg;
      System.out.println("packet received: " + strMessage);
      String[] msgTokens = strMessage.split(",");
      if (msgTokens.length > 0) {
         if (msgTokens[0].compareTo("join") == 0) { // receive "join" 
         // format: join, success or join, failure
            if (msgTokens[1].compareTo("success") == 0) { 
               game.setIsConnected(true);
               System.out.println("Now sending a create message...");
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
            
            System.out.println("Client " + ghostID.toString() + " has joined at position " + ghostPosition.toString());
            try { 
               System.out.println("about to try to create a ghost avatar with ghostID: " + ghostID + " and ghostposition: " + ghostPosition);
               createGhostAvatar(ghostID, ghostPosition);
            } catch (IOException e) { 
               System.out.println("error creating ghost avatar");
               System.out.println(e);
            } 
         }

         if (msgTokens[0].compareTo("wsds") == 0) { // rec. “create…” 
            System.out.println("Sending details for message");
            sendDetailsForMessage(UUID.fromString(msgTokens[1]), game.getPlayerPosition());
              
         }
         
         //if (msgTokens[0].compareTo("wsds") == 0) { // rec. “wants…” 
            // etc….. 
         //}
         if (msgTokens[0].compareTo("move") == 0) { // rec. “move...” 
            UUID ghostID = UUID.fromString(msgTokens[1]);
            Vector3f ghostPosition = (Vector3f) Vector3f.createFrom(
            Float.parseFloat(msgTokens[2]),
            Float.parseFloat(msgTokens[3]),
            Float.parseFloat(msgTokens[4]));
            
            GhostAvatar g = null;
            
            for (int i = 0; i < ghostAvatars.size(); i++) {
                 if ((ghostAvatars.get(i).getID().compareTo(ghostID)) == 0) {
                     // System.out.println("Found the ghost to move");
                     g = ghostAvatars.get(i);
                 }
                 if (g != null) {
                          g.getNode().setLocalPosition(ghostPosition);
                          System.out.println("Position should be updated");
                          System.out.println(ghostPosition);
                          System.out.println("node: " + g.getNode());
                          System.out.println("Position: " + g.getPosition());
                      }
             }
                            //game.moveGhostAvatarInGameWorld(ghostID, ghostPosition); 
         }
      }
   }
   
   public void removeGhostAvatar(UUID id) {
      System.out.println(id + " removed.");
   }
   
   public void createGhostAvatar(UUID id, Vector3 position) throws IOException {
      GhostAvatar ghostAvatar = new GhostAvatar(id, position);
      game.addGhostAvatarToGameWorld(ghostAvatar);
      ghostAvatars.add(ghostAvatar);
   }
   
   public void sendJoinMessage() { // format: join, localId 
      try { 
         sendPacket(new String("join," + id.toString()));
      } catch (IOException e) { 
         e.printStackTrace();
      } 
      
   }
   
   public void sendCreateMessage(Vector3 pos) { // format: (create, localId, x,y,z)
      try { 
         String message = new String("create," + id.toString());
         message += "," + pos.x()+"," + pos.y() + "," + pos.z();
         System.out.println("Sending this create: " + message);
         sendPacket(message);
      }
      catch (IOException e) { 
         e.printStackTrace();
      } 
   }
   
   public void sendByeMessage() { 
      // etc….. 
   }
   
   public void sendDetailsForMessage(UUID remId, Vector3 pos) { 
      try {
            System.out.println("Send my position (" + pos.x() + "," + pos.y() + "," + pos.z() + ") to client " + remId.toString());
            String message = new String("dsfr," + id.toString());
            message += "," + remId.toString() + "," + pos.x() + "," + pos.y() + "," + pos.z();
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        } 
   }

   public void sendMoveMessage(Vector3 pos) { 
      try {
         String message = new String("move," + id.toString());
         message += "," + pos.x() + "," + pos.y() + "," + pos.z();
         sendPacket(message);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   
}
   