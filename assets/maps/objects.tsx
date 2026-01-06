<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="objects" tilewidth="80" tileheight="112" tilecount="6" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="GameObject">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="hasInventory" type="bool" value="true"/>
   <property name="loot" value=""/>
  </properties>
  <image source="objects/chest.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="1" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
  <image source="objects/house.png" width="80" height="112"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="9.33333" y="33.6667" width="62" height="71.6667"/>
  </objectgroup>
 </tile>
 <tile id="2" type="Prop">
  <properties>
   <property name="atlasAsset" value="OBJECTS"/>
  </properties>
  <image source="objects/oak_tree.png" width="41" height="63"/>
  <objectgroup draworder="index" id="2">
   <object id="3" x="26.375" y="42.25">
    <polygon points="0,0 -10.375,0.125 -11,5.625 -14,11 -6.25,13.375 3.125,10.625 -0.125,5.75"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="3" type="GameObject">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="attackSound" value="SWING"/>
   <property name="bodyType" value="DynamicBody"/>
   <property name="cameraFollow" type="bool" value="true"/>
   <property name="controller" type="bool" value="true"/>
   <property name="damage" type="float" value="7"/>
   <property name="damageDelay" type="float" value="0.2"/>
   <property name="hasInventory" type="bool" value="true"/>
   <property name="life" type="int" value="12"/>
   <property name="lifeRegeneration" type="float" value="0.25"/>
   <property name="speed" type="float" value="4"/>
  </properties>
  <image source="objects/player.png" width="32" height="32"/>
  <objectgroup draworder="index" id="5">
   <object id="6" x="9.54545" y="18" width="11.9091" height="4.90909">
    <ellipse/>
   </object>
   <object id="7" name="attack_sensor_down" x="0" y="18" width="32" height="14">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="9" name="attack_sensor_left" x="0" y="0" width="14" height="31.8182">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="10" name="attack_sensor_right" x="18" y="0" width="14" height="31.8182">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="11" name="attack_sensor_up" x="0" y="0" width="32" height="14">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="4" type="GameObject">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="bodyType" value="StaticBody"/>
   <property name="life" type="int" value="9999"/>
   <property name="lifeRegeneration" type="float" value="99"/>
  </properties>
  <image source="objects/training_dummy.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="3" y="5" width="26" height="23"/>
  </objectgroup>
 </tile>
 <tile id="5" type="GameObject">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="z" type="int" value="0"/>
  </properties>
  <image source="objects/trap.png" width="16" height="16"/>
 </tile>
</tileset>
