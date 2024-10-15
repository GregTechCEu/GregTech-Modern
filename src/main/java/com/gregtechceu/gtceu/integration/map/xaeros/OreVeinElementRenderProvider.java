package com.gregtechceu.gtceu.integration.map.xaeros;

import xaero.map.WorldMap;
import xaero.map.element.MapElementRenderProvider;

import java.util.Iterator;

public class OreVeinElementRenderProvider extends MapElementRenderProvider<OreVeinElement, OreVeinElementContext> {
   private Iterator<OreVeinElement> iterator;

   public OreVeinElementRenderProvider() {}

   public void begin(int location, OreVeinElementContext context) {
      if (WorldMap.settings.waypoints) {
         this.iterator = XaerosRenderer.elements.values().iterator();
         context.worldmapWaypointsScale = WorldMap.settings.worldmapWaypointsScale;
      } else {
         this.iterator = null;
      }
   }

   public boolean hasNext(int location, OreVeinElementContext context) {
      return this.iterator != null && this.iterator.hasNext();
   }

   public OreVeinElement getNext(int location, OreVeinElementContext context) {
      return this.iterator.next();
   }

   public void end(int location, OreVeinElementContext context) {
   }
}
