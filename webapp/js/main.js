var TILE_SIZE = 256,
    MAP_ID = "SFU",
    SERVER_URL = "http://localhost:8080",
    SSIDS = [ "SFUNET", "SFUNET-SECURE", "eduroam" ];

var AQ_SIZE = 140;

var Map, MapProj;

var self = this;

var customMapOptions = {

  getTileUrl: function(coord, zoom) {
    var normalizedCoord = MapProj.getNormalizedCoord(coord, zoom);
    if (!normalizedCoord) return null;
    return "maptiles/"+zoom+"/"+normalizedCoord.x+ "/"+normalizedCoord.y + ".png";
  },

  tileSize: new google.maps.Size(TILE_SIZE, TILE_SIZE),
  maxZoom: 6,
  minZoom: 1,
  name: MAP_ID // this is displayed on the top right corner button
}

function plotData(data) {
  for (i in data) {
    for (j in data[i]) {
      var dataRow = data[i][j]
      var point = new google.maps.Point(196,60) // start at AQ, that's its top-left corner

      // the magic formule
      point.y += ( parseInt(j) * ( AQ_SIZE / (data[i].length - 1.03) ) )

      dataRow.push(point)
      // console.log(dataRow)

      MapTools.addMarker(MapProj.fromPointToLatLng(point), "images/routerdot.png", dataRow[2])
    }
  }
}

function initialize() {
  MapProj = new MercatorProjection();

  var mapSettings = {
    center: new google.maps.LatLng(0,0),
    zoom: 2,
    streetViewControl: false,
    mapTypeControlOptions: {
      mapTypeIds: [MAP_ID]
    }
  };

  Map = new google.maps.Map(document.getElementById("map-canvas"),
        mapSettings);

  Map.mapTypes.set(MAP_ID, new google.maps.ImageMapType(customMapOptions));
  Map.setMapTypeId(MAP_ID); // can control floor levels with this

  $.post(SERVER_URL, function(r) {
    for (i in r) {
      if (i != "apsdata_AQ_East_M_VR") continue

      // east side
      var seper = MapTools.seperateByKeys(r[i], SSIDS, 1)
      plotData(seper)
      console.log(seper["SFUNET"][5][6].y)
    }
  })
}

google.maps.event.addDomListener(window, 'load', initialize);
