var TILE_SIZE = 256,
    MAP_ID = "SFU",
    SERVER_URL = "http://localhost:8080",
    SSIDS = [ "SFUNET", "SFUNET-SECURE", "eduroam" ];

var AQ_SIZE = 140;

var Map, MapProj;

var userMarker;

var singleAP;

var customMapOptions = {

  getTileUrl: function(coord, zoom) {
    var normalizedCoord = MapProj.getNormalizedCoord(coord, zoom);
    if (!normalizedCoord) return null;
    return "maptiles/"+zoom+"/"+normalizedCoord.x+ "/"+normalizedCoord.y + ".png";
  },

  tileSize: new google.maps.Size(TILE_SIZE, TILE_SIZE),
  maxZoom: 16,
  minZoom: 1,
  name: MAP_ID // this is displayed on the top right corner button
}

function plotData(set){
  var data = set['data']
  var startT = set['startT']
  var endT = set['endT']
  var totalSeconds = (endT - startT) / 1000
  var scaleFactor = AQ_SIZE/totalSeconds
  var revrsd = set['reversed']

  for (i in data) {
    for (j in data[i]){
      console.log(j)
      if(j!=singleAP) continue;
      var apArr = data[i][j]
      var point = new google.maps.Point(190,60) // AQ top right
      var thisLength = ((endT - apArr[0][4])/1000 * scaleFactor)

      if (revrsd) {
        thisLength = AQ_SIZE - thisLength
        point.x = 195
      }

      point.y += thisLength
      MapTools.addMarker(MapProj.fromPointToLatLng(point), "images/routerdot.png", apArr[0][1])

      // draw it's shadows
      apArr.forEach(function(el,i) {
        point.y = 60
        var thisLength = (((endT-el[4])/1000)*scaleFactor)

        if(revrsd) {
          thisLength = AQ_SIZE - thisLength
        }

        point.y += thisLength
        MapTools.addMarker(MapProj.fromPointToLatLng(point), "images/Red-Circle.png", String(el[3]))
      })
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

  userMarker = MapTools.addMarker(new google.maps.LatLng(0,0))

  // raw_data = test_data
  $.post(SERVER_URL, {'tables': ['_1', '_R_1'], 'raw_data':['_3']}, function(r) {

    singleAP = "00:1f:45:64:71:d0"

    plotData(r[0])
    plotData(r[1])


  })
}

google.maps.event.addDomListener(window, 'load', initialize);
