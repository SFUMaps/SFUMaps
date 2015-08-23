// globals and constants
var TILE_SIZE = 256,
    MAP_ID = "SFU";

var _Map;
var _MapProj;

// member variables
var allPlaces = [];
var allPlacesMarkers = [];

var current_place_data = {
  location: {},
  map_zoom: -1,
  place_type: ""
};
var currentPlaceMarker = {};

function initMap() {
  _Map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 0, lng: 0},
    zoom: 3,
    streetViewControl: false,
    mapTypeControlOptions: {
      mapTypeIds: [MAP_ID]
    }
  });
  _MapProj = new MercatorProjection();

  // add base map
  var baseMap = new google.maps.ImageMapType({
    getTileUrl: function(coord, zoom) {
      var normalizedCoord = _MapProj.getNormalizedCoord(coord, zoom);
      if (!normalizedCoord) return null;
      return "assets/maptiles/basemap/"
            + zoom
            + "/"
            + normalizedCoord.x
            + "/"
            + normalizedCoord.y
            + ".png";
    },

    tileSize: new google.maps.Size(TILE_SIZE, TILE_SIZE),
    maxZoom: 6,
    minZoom: 1,
    name: MAP_ID,
  });
  _Map.mapTypes.set(MAP_ID, baseMap);
  _Map.setMapTypeId(MAP_ID);

  // add overlay for the buildings
  var overlay = new google.maps.ImageMapType({
    getTileUrl: function(coord, zoom) {
      var normalizedCoord = _MapProj.getNormalizedCoord(coord, zoom);
      if (!normalizedCoord) return null;
      return "assets/maptiles/overlay/"
            + zoom
            + "/"
            + normalizedCoord.x
            + "/"
            + normalizedCoord.y
            + ".png";
    },
    tileSize: new google.maps.Size(TILE_SIZE, TILE_SIZE),
    maxZoom: 6,
    minZoom: 1,
    name: MAP_ID,
  });
  _Map.overlayMapTypes.push(overlay);

  // fetch map data
  $.post('/', JSON.stringify({'fetch_data': ''}), function(r) {
    loadPlaces(r);
  });


  //
  // MARK: add event listeners
  //

  // 1. Map click listener
  _Map.addListener('click', function(e) {
    current_place_data['location'] = _MapProj.fromLatLngToPoint(e.latLng);
    openplace_typeSelectorMenu(e.pixel);
  });

  // 2. Place selector menu item click
  $("#place-selector-places li").on('click', function() {

    /* logic */

    current_place_data['place_type'] = $(this).text();
    current_place_data['map_zoom'] = _Map.getZoom();

    /* UI */

    // show the place save form
    $(".add_place_form_wrapper").animate({
      top: "3%",
    }, 250);

    // update location text
    $(".lat-lng").text(
      current_place_data['location'].x + ", " + current_place_data['location'].y + ", " + current_place_data['map_zoom'] + "z"
    );
    // update place type
    $(".place-type").text(
      current_place_data['place_type']
    );

    // reset form
    $("#place_save_form").trigger('reset');

    if (Object.keys(currentPlaceMarker).length !== 0)
      currentPlaceMarker.setMap(null);

    currentPlaceMarker = MapTools.addMarker(
      _MapProj.fromPointToLatLng(current_place_data['location']),
      $("#place-title").val(),
      ""
    )

    // focus on first form element
    $("#place-title").focus();
  });

  // 3. Add place form submit listener
  $("#save-place").mouseup(function() {
    // hide the place save form
    $(".add_place_form_wrapper").animate({
      top: "-100%",
    }, 250);

    saveNewPlaceForm( $("#place_save_form") );

    // clear `current_place_data`
    current_place_data = {}
  });

  // 4. Cancel place save button listener
  $("#cancel-save-place").mouseup(function() {
    // hide the place save form
    $(".add_place_form_wrapper").animate({
      top: "-100%",
    }, 250);

    // remove current placed marker
    if (Object.keys(currentPlaceMarker).length !== 0)
      currentPlaceMarker.setMap(null);

    // clear `current_place_data`
    current_place_data = {}
  });

}

function openplace_typeSelectorMenu(screenPoint) {
  // position the place selector menu
  $(".place_selector_menu_wrapper").css({
    'top'   : screenPoint.y + 'px',
    'left'  : screenPoint.x + 'px',
  });
  // simulate click on connected button to open the menu
  document.getElementById("place-selector-menu").click();
}

function saveNewPlaceForm(form) {
  var data = {};
  form.serializeArray().map(function(x){data[x.name] = x.value;});

  var formData = $.extend({}, data, current_place_data);
  console.log(allPlaces.length);

  // add / update `allPlaces`
  var placeExists = false;
  for (var i = 0; i < allPlaces.length; i++) {
    if (allPlaces[i].location == formData.location) {
      // update
      allPlaces[i] = formData;
      placeExists = true
    }
  }
  if (!placeExists)
    allPlaces.push(formData);

  console.log(allPlaces);

  $.post('/', JSON.stringify(formData), function(r) {
    console.log("saved: ",r.success);
  });
}

// TODO: display makers according to zoom level
// load the makers onto map
function loadPlaces(data) {
  data['places'].forEach(function(val, i) {
    // add to array
    allPlaces.push(val);

    // display on map
    allPlacesMarkers.push(MapTools.addMarker(
      _MapProj.fromPointToLatLng(new google.maps.Point(val.location.x, val.location.y)),
      val.place_title,
      ""
    ));
  });
}


//
function onMarkerClick(marker) {
  // find this marker in our list and and edit in display form
  for (var i = 0; i < allPlacesMarkers.length; i++) {
    if (allPlacesMarkers[i].position === marker.position) {
      // set form data for maker and show form
      currentPlaceMarker = allPlacesMarkers[i]

      setPlaceSaveForm(allPlaces[i])

      break;
    }
  }
}


function setPlaceSaveForm(formData) {

  current_place_data = {
    location: formData.location,
    map_zoom: formData.map_zoom,
    place_type: formData.place_type
  }

  // add data to textfield and let it know it has data
  $("#place-title").val(formData.place_title);
  $("#place-title").parent(".mdl-textfield").addClass("is-upgraded is-dirty");

  $("#place-desc").val(formData.place_desc);
  $("#place-desc").parent(".mdl-textfield").addClass("is-upgraded is-dirty");

  $(".lat-lng").text(
    formData.location.x + ", " + formData.location.y + ", " + formData.map_zoom + "z"
  );
  $(".place-type").text(formData.place_type);

  // show the place save form
  $(".add_place_form_wrapper").animate({
    top: "3%",
  }, 250);
}
