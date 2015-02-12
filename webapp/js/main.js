var TILE_SIZE = 256;

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
    map.setMapTypeId('aq');

    var projection = new MercatorProjection();

    //image points range from (0,0) to (256, 256)
    var pos = projection.fromPointToLatLng( new google.maps.Point(TILE_SIZE/2,
      TILE_SIZE/2) );

    var marker = new google.maps.Marker({
        position: pos,
        map: map
    });

}

google.maps.event.addDomListener(window, 'load', initialize);
