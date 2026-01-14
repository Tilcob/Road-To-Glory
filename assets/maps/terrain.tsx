<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="terrain" tilewidth="16" tileheight="16" spacing="16" margin="8" tilecount="192" columns="12">
 <image source="tileset.png" width="384" height="512"/>
 <tile id="0">
  <objectgroup draworder="index" id="2">
   <object id="1" x="15.9497" y="5.99264" rotation="359.922">
    <polygon points="0,0 -7.91304,3 -10.9565,9.95652 -3.95652,10 0.0869565,7"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="1">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="5.45455" width="16" height="8"/>
  </objectgroup>
 </tile>
 <tile id="2">
  <objectgroup draworder="index" id="2">
   <object id="1" x="-0.0434783" y="7">
    <polygon points="0,0 8,2.04348 11.0435,8.95652 4,8.95652 0,5.91304"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="3">
  <objectgroup draworder="index" id="2">
   <object id="1" x="15.913" y="5.95652">
    <polygon points="0,0 -7.95652,3 -10.913,10 0.0869565,10.0435"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="4">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="5.5" width="16" height="10.5"/>
  </objectgroup>
 </tile>
 <tile id="5">
  <objectgroup draworder="index" id="2">
   <object id="1" x="-0.0434783" y="6.91304">
    <polygon points="0,0 8.13043,2.13043 11.0435,9 4.04348,9.04348 0.0434783,6"/>
   </object>
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
 <tile id="24">
  <objectgroup draworder="index" id="2">
   <object id="1" x="5" y="0">
    <polygon points="0,0 2.95652,6.91304 10.9565,10 11,3.95652 6.95652,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="25">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="3.63636" width="16" height="6.90909"/>
  </objectgroup>
 </tile>
 <tile id="26">
  <objectgroup draworder="index" id="2">
   <object id="1" x="10.913" y="-0.0434783">
    <polygon points="0,0 -2.91304,7 -10.8696,9 -10.913,4.04348 -6.95652,0.0434783"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="27">
  <objectgroup draworder="index" id="2">
   <object id="1" x="5" y="0">
    <polygon points="0,0 2.91304,7 10.9565,10 11,5.95652 6.91304,-0.0434783"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="28">
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="10.4375"/>
  </objectgroup>
 </tile>
 <tile id="29">
  <objectgroup draworder="index" id="2">
   <object id="1" x="10.9565" y="0">
    <polygon points="0,0 -2.95652,6.91304 -10.8696,9 -10.913,5.95652 -6.91304,-0.0434783"/>
   </object>
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
