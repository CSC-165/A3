package a3;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class GameServerUDP extends GameConnectionServer<UUID> {
   public GameServerUDP(int localPort) throws IOException { 
      super(localPort, ProtocolType.UDP); 
   }
   
   @Override
   public void processPacket(Object o, InetAddress senderIP, int senderPort) {
      String message = (String) o;
      String[] msgTokens = message.split(",");
      if (msgTokens.length > 0) {
         // case where server receives a JOIN message
         // format: join,localid
         if (msgTokens[0].compareTo("join") == 0) { 
            System.out.println("join message heard");
            System.out.println("msgTokens: " + message);
            try { 
               IClientInfo ci;
               ci = getServerSocket().createClientInfo(senderIP, senderPort);
               UUID clientID = UUID.fromString(msgTokens[1]);
               addClient(ci, clientID);
               sendJoinedMessage(clientID, true);
            }
            catch (IOException e) { 
               e.printStackTrace();
            } 
         }
      }

      // case where server receives a CREATE message
      // format: create,localid,x,y,z
      if (msgTokens[0].compareTo("create") == 0) { 
         System.out.println("Received a create message");
         UUID clientID = UUID.fromString(msgTokens[1]);
         String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
         sendCreateMessages(clientID, pos);
         sendWantsDetailsMessages(clientID);
      }

      // case where server receives a BYE message
      // format: bye,localid
      if (msgTokens[0].compareTo("bye") == 0) { 
         UUID clientID = UUID.fromString(msgTokens[1]);
         sendByeMessages(clientID);
         removeClient(clientID);
      }
      
      // case where server receives a DETAILS-FOR message
      if (msgTokens[0].compareTo("dsfr") == 0) { 
         System.out.println("heard details for message");
         UUID clientID = UUID.fromString(msgTokens[1]);
         UUID remID = UUID.fromString(msgTokens[2]);
         String[] pos = { msgTokens[3], msgTokens[4], msgTokens[5] };
         sndDetailsMsg(clientID, remID, pos);
      }

      // case where server receives a MOVE message
      if (msgTokens[0].compareTo("move") == 0) { 
         System.out.println("heard move message");
         UUID clientID = UUID.fromString(msgTokens[1]);
         String[] pos = { msgTokens[2], msgTokens[3], msgTokens[4] };
         sendMoveMessages(clientID, pos);
      }
      
      if (msgTokens[0].compareTo("yaw") == 0) { 
         System.out.println("heard yaw message");
         UUID clientID = UUID.fromString(msgTokens[1]);
         Angle angle = Degreef.createFrom(Float.parseFloat(msgTokens[2]));
         sendYawMessages(clientID, angle);
      }
      
      if (msgTokens[0].compareTo("pitch") == 0) { 
         System.out.println("heard pitch message");
         UUID clientID = UUID.fromString(msgTokens[1]);
         Angle angle = Degreef.createFrom(Float.parseFloat(msgTokens[2]));
         sendPitchMessages(clientID, angle);
      }
   }
   
   public void sendYawMessages(UUID clientID, Angle angle) {
      try {
            String message = new String("yaw," + clientID.toString());
            message += "," + angle.valueDegrees();
            System.out.println("Sending yaw messages");
            forwardPacketToAll(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        }
   }
   
   public void sendPitchMessages(UUID clientID, Angle angle) {
      try {
            String message = new String("pitch," + clientID.toString());
            message += "," + angle.valueDegrees();
            System.out.println("Sending pitch messages");
            forwardPacketToAll(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        }
   }
   
   public void sendJoinedMessage(UUID clientID, boolean success) { 
      // format: join, success or join, failure
      try { 
         String message = new String("join,");
         if (success) { 
            message += "success";
             
         }
         
         else {
            message += "failure";
         }
         System.out.println(message);
         System.out.println("sending packet...");
         sendPacket(message, clientID);
      }
      catch (IOException e) { 
         e.printStackTrace(); 
      }
   }
   
   public void sendCreateMessages(UUID clientID, String[] position) { 
      // format: create, remoteId, x, y, z
      try { 
         String message = new String("create," + clientID.toString());
         message += "," + position[0];
         message += "," + position[1];
         message += "," + position[2];
         forwardPacketToAll(message, clientID);
         
         System.out.println("Letting all other clients know that " + clientID.toString() + " is at position ("
                    + position[0] + ", " + position[1] + ", " + position[2] + ")");
      }
      catch (IOException e) { 
         e.printStackTrace();
      } 
   }
   
   public void sndDetailsMsg(UUID clientID, UUID remoteId, String[] position) { 
      try {
            String message = new String("create," + clientID.toString());
            message += "," + position[0];
            message += "," + position[1];
            message += "," + position[2];
            System.out.println("Sending details message");
            sendPacket(message, remoteId);
        } catch (IOException e) {
            e.printStackTrace();
        } 
   }
   
   public void sendWantsDetailsMessages(UUID clientID) { 
      try {
            System.out.println(clientID.toString() + " requesting details from everyone already joined");
            String message = new String("wsds," + clientID.toString());
            System.out.println("Sending wants details message: " + message);
            forwardPacketToAll(message, clientID);
        } catch (IOException e) {
            System.out.println("error in sending packet");
            e.printStackTrace();
        } 
   }
   
   public void sendMoveMessages(UUID clientID, String[] position) { 
      try {
            String message = new String("move," + clientID.toString());
            message += "," + position[0];
            message += "," + position[1];
            message += "," + position[2];
            System.out.println("Sending move messages");
            forwardPacketToAll(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        } 
   }
   
   public void sendByeMessages(UUID clientID) { 
      // etc….. 
   }
 
}