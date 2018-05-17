public class TestGameClient extends GameConnectionClient { // same as before, plus code to handle additional NPC messages
 
   private Vector<GhostNPC> ghostNPCs;
   
   public TestGameClient() {
   
   }
   
   private void createGhostNPC(int id, Vector3 position) { 
      GhostNPC newNPC = new GhostNPC(id, position);
      ghostNPCs.add(newNPC);
      game.addGhostNPCtoGameWorld(newNPC);
   }
 
   private void updateGhostNPC(int id, Vector3 position) { 
      ghostNPCs.get(id).setPosition(position);
   }
 
   // handle updates to NPC positions
   // format: (mnpc,npcID,x,y,z)
   if(messageTokens[0].compareTo("mnpc") == 0) { 
      int ghostID = Integer.parseInt(messageTokens[1]);
      Vector3 ghostPosition = Vector3f.createFrom(
         Float.parseFloat(messageTokens[2]),
         Float.parseFloat(messageTokens[2]),
         Float.parseFloat(messageTokens[2]));
      updateGhostNPC(ghostID, ghostPosition);
   }
 
   public void askForNPCinfo() { 
      try { 
         sendPacket(new String("needNPC," + id.toString()));
      } catch (IOException e) { 
         e.printStackTrace();
      } 
   }
}