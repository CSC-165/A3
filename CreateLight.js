var JavaPackages = new JavaImporter(
 Packages.ray.rage.scene.SceneManager,
 Packages.ray.rage.scene.Light,
 Packages.ray.rage.scene.Light.Type,
 Packages.ray.rage.scene.Light.Type.POINT,
 Packages.java.awt.Color
 );

// creates a RAGE object - in this case a light
with (JavaPackages) { 
   var plight = sm.createLight("testLamp1", Light.Type.POINT);
   plight.setAmbient(new Color(1.0, 1.0, 1.0));
   plight.setDiffuse(new Color(.0, .7, .0));
   plight.setSpecular(new Color(.4, .8, .2));
   plight.setRange(500);
}