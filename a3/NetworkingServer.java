package a3;

import java.io.IOException;
import ray.networking.IGameConnection.ProtocolType;

public class NetworkingServer {
   private GameServerUDP thisUDPServer;
   private NPCcontroller npcCtrl;
   private long startTime;
   private long lastUpdateTime;
   
   public NetworkingServer(int serverPort, String protocol) { 
      try {  
         thisUDPServer = new GameServerUDP(serverPort);
         startTime = System.nanoTime();
         lastUpdateTime = startTime;
         npcCtrl = new NPCcontroller();
         npcCtrl.setupNPCs();
         npcLoop();
      }
      catch (IOException e) { 
         e.printStackTrace();
      } 
   }
   
   public void npcLoop() { 
      while (true) { 
         long frameStartTime = System.nanoTime();
         float elapMilSecs = (frameStartTime-lastUpdateTime)/(1000000.0f);
         if (elapMilSecs >= 50.0f) { 
            lastUpdateTime = frameStartTime;
            npcCtrl.updateNPCs();
            thisUDPServer.sendNPCinfo();
         }
         Thread.yield();
      } 
   }

   public static void main(String[] args) { 
      if(args.length > 1) { 
         NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
         System.out.println("Server is active");
      } 
   } 
}