// MARK: Mercator Projection (class object)

var TILE_SIZE = 256;
var MAP_ID = "SFU";

var MercatorProjection = (function() {
  var pixelOrigin_ = new google.maps.Point(TILE_SIZE / 2,
      TILE_SIZE / 2);
  var pixelsPerLonDegree_ = TILE_SIZE / 360;
  var pixelsPerLonRadian_ = TILE_SIZE / (2 * Math.PI);

  var bound = function(value, opt_min, opt_max) {
    if (opt_min != null) value = Math.max(value, opt_min);
    if (opt_max != null) value = Math.min(value, opt_max);
    return value;
  }

  var degreesToRadians = function(deg) {
    return deg * (Math.PI / 180);
  }

  var radiansToDegrees = function(rad) {
    return rad / (Math.PI / 180);
  }

  var getNormalizedCoord = function(coord, zoom) {
      var y = coord.y;
      var x = coord.x;

      // tile range in one direction range is dependent on zoom level
      // 0 = 1 tile, 1 = 2 tiles, 2 = 4 tiles, 3 = 8 tiles, etc
      var tileRange = 1 << zoom;

      /**
      * If we don't want to repeat in any direction
      * simply return null.
      */
      // don't repeat across y-axis (vertically)
      if (y < 0 || y >= tileRange) return null
      // repeat across x-axis (horizontally)
      if (x < 0 || x >= tileRange) {
        x = (x % tileRange + tileRange) % tileRange
      }

      return {
        x: x,
        y: y
      };
  }

  var fromLatLngToPoint = function (latLng) {
    var point = new google.maps.Point(0, 0);
    var origin = pixelOrigin_;
    point.x = origin.x + latLng.lng() * pixelsPerLonDegree_;
    // Truncating to 0.9999 effectively limits latitude to 89.189. This is
    // about a third of a tile past the edge of the world tile.
    var siny = bound(Math.sin(degreesToRadians(latLng.lat())), -0.9999,
        0.9999);
    point.y = origin.y + 0.5 * Math.log((1 + siny) / (1 - siny)) * -pixelsPerLonRadian_;
    return point;
  }

  var fromPointToLatLng = function (point) {
    var origin = pixelOrigin_;
    var lng = (point.x - origin.x) / pixelsPerLonDegree_;
    var latRadians = (point.y - origin.y) / -pixelsPerLonRadian_;
    var lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) -
        Math.PI / 2);
    return new google.maps.LatLng(lat, lng);
  }
  return {
      fromLatLngToPoint   : fromLatLngToPoint,
      fromPointToLatLng   : fromPointToLatLng,
      getNormalizedCoord  : getNormalizedCoord,
  }
})();


// MARK: Custom Map Tools (module)

var MapTools = (function() {

  return {
    addMarker: function(map, pos, title, iconPath){
      iconPath = iconPath || ""
      title = title || ""
      var marker = new google.maps.Marker({
          position: pos,
          map: map,
          icon: iconPath,
          title: title,
          draggable:true
      });

      marker.info = new google.maps.InfoWindow({
        content: title
      });

      marker.addListener('drag', function () {
        onMarkerDrag(this);
      });
      marker.addListener('click', function () {
        onMarkerClick(this);
      });

      return marker;
    },

    seperateByKeys: function(data, keys, keyIndex){
      // seperate the data under each key by that key @ keyIndex
      var seperated={}
      for (i in keys) {
        seperated[keys[i]]=[]
      }
      for (i in data) {
        seperated[data[i][keyIndex]].push(data[i])
      }

      return seperated
    },
  }
}());
