var TILE_SIZE = 256;

var AQ_SIZE = 140;

var SSIDS = ["SFUNET-SECURE", "SFUNET", "eduroam"];

var mMap, mMapProj;

var mapTypeOptions = {

  getTileUrl: function(coord, zoom) {

    var normalizedCoord = getNormalizedCoord(coord, zoom);
    if (!normalizedCoord) return null;
    return "maptiles/"+zoom+"/"+normalizedCoord.x+ "/"+normalizedCoord.y + ".png";

  },

  tileSize: new google.maps.Size(TILE_SIZE, TILE_SIZE),

  maxZoom: 6,
  minZoom: 1,

  name: "AQ" // this is displayed on the top right corner button
};

function _addMarker(pos, ssid_name){
  var marker = new google.maps.Marker({
      position: pos,
      map: mMap,
      icon: 'images/'+ssid_name.toLowerCase()+'_dot.png'
  });
}

function _seperateSSID(data){

  /**
  * seperate the whole data into three arrays for each ssid
  */

  var splittedData={};
  for(i in SSIDS){
    var tmpSSIDData=[];
    for(j in data){
      if(data[j][1] == SSIDS[i]){
        tmpSSIDData.push(data[j])
      }
    }
    splittedData[SSIDS[i]] = tmpSSIDData;
  }

  return splittedData
}

function _manageData(data, initPoint){
  for(key in data) {

    var aps = data[key]; // access points array of each table

    if(aps.length > 0){

      var key_S = key.split("_").slice(1); // remove the `apsdata` prefix
      var direction = key_S[3]; //vertical or horizontal
      var floorLevel = key_S[2]; //floor level, worry about this later

      aps = _seperateSSID(aps)

      var mvDiff=0.0;

      for(ssid in aps) {
        var ssid_aps = aps[ssid];

        for(i=0; i<ssid_aps.length; i++) {
          // console.log(ssid_aps[i])
          var point = new google.maps.Point(0,0);
          if(direction=="VR") {
            if (key_S[1] == "East") point.x = TILE_SIZE-initPoint.x+5
            else point.x = initPoint.x;

            point.y = initPoint.y+( i * ( AQ_SIZE/(ssid_aps.length - 1) ) );

          } else {
            point.x = initPoint.x+( i * ( AQ_SIZE/(ssid_aps.length - 1) ) );

            if (key_S[1] == "South") point.y = TILE_SIZE-initPoint.y+5;
            else point.y = initPoint.y;
          }

          point.x += mvDiff;
          point.y += mvDiff;

          _addMarker(mMapProj.fromPointToLatLng(point), ssid)
        }

        mvDiff -= 2;
      }

    }

  }
}

function initialize() {
    var myLatlng = new google.maps.LatLng(0, 0);
    var mapOptions = {
        center: myLatlng,
        zoom: 1,
        streetViewControl: false,
        mapTypeControlOptions: {
            mapTypeIds: ["aq"]
        }
    };

    var map = new google.maps.Map(document.getElementById("map-canvas"),
        mapOptions);

    map.mapTypes.set('aq', new google.maps.ImageMapType(mapTypeOptions));
    map.setMapTypeId('aq'); // controls floor levels with this

    mMap = map; // make map publicly accessible

    mMapProj = new MercatorProjection();

    //image points range from (0,0) to (256, 256)
    var initPoint = new google.maps.Point( 60, 60 );

    $.ajax({
      url:'http://localhost:8080',
      method:'POST',
      // data : JSON.stringify(data),
      success:function(data){
        var data = JSON.parse(data)
        _manageData(data, initPoint);
      }
    });



}

google.maps.event.addDomListener(window, 'load', initialize);
