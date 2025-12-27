<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="terrain" tilewidth="16" tileheight="16" spacing="16" margin="8" tilecount="192" columns="12">
 <image source="tileset.png" width="384" height="512"/>
 <tile id="1">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="5.45455" width="16" height="8"/>
  </objectgroup>
 </tile>
 <tile id="4">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="5.5" width="16" height="10.5"/>
  </objectgroup>
 </tile>
 <tile id="8">
  <animation>
   <frame tileid="8" duration="200"/>
   <frame tileid="9" duration="200"/>
   <frame tileid="10" duration="200"/>
   <frame tileid="11" duration="200"/>
  </animation>
 </tile>
 <tile id="12">
  <objectgroup draworder="index" id="2">
   <object id="1" x="5.45455" y="0" width="6.54545" height="16"/>
  </objectgroup>
 </tile>
 <tile id="14">
  <objectgroup draworder="index" id="2">
   <object id="1" x="4" y="0" width="6.36364" height="16"/>
  </objectgroup>
 </tile>
 <tile id="15">
  <objectgroup draworder="index" id="2">
   <object id="1" x="5.6875" y="0" width="10.3125" height="16"/>
  </objectgroup>
 </tile>
 <tile id="17">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="10.4375" height="16"/>
  </objectgroup>
 </tile>
 <tile id="25">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="3.63636" width="16" height="6.90909"/>
  </objectgroup>
 </tile>
 <tile id="28">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="10.4375"/>
  </objectgroup>
 </tile>
 <tile id="63">
  <animation>
   <frame tileid="63" duration="200"/>
   <frame tileid="64" duration="200"/>
   <frame tileid="65" duration="200"/>
  </animation>
 </tile>
 <wangsets>
  <wangset name="Grasscliff" type="corner" tile="-1">
   <wangcolor name="grass" color="#ff0000" tile="-1" probability="1"/>
   <wangcolor name="cliff" color="#00ff00" tile="-1" probability="1"/>
   <wangtile tileid="0" wangid="0,1,0,2,0,1,0,1"/>
   <wangtile tileid="1" wangid="0,1,0,2,0,2,0,1"/>
   <wangtile tileid="2" wangid="0,1,0,1,0,2,0,1"/>
   <wangtile tileid="12" wangid="0,2,0,2,0,1,0,1"/>
   <wangtile tileid="13" wangid="0,1,0,1,0,1,0,1"/>
   <wangtile tileid="14" wangid="0,1,0,1,0,2,0,2"/>
   <wangtile tileid="24" wangid="0,2,0,1,0,1,0,1"/>
   <wangtile tileid="25" wangid="0,2,0,1,0,1,0,2"/>
   <wangtile tileid="26" wangid="0,1,0,1,0,1,0,2"/>
   <wangtile tileid="36" wangid="0,2,0,2,0,2,0,1"/>
   <wangtile tileid="37" wangid="0,1,0,2,0,2,0,2"/>
   <wangtile tileid="48" wangid="0,2,0,2,0,1,0,2"/>
   <wangtile tileid="49" wangid="0,2,0,1,0,2,0,2"/>
   <wangtile tileid="181" wangid="0,1,0,1,0,1,0,1"/>
  </wangset>
 </wangsets>
</tileset>
