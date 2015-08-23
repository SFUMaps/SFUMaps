//
// MARK: Place Class
//

var Place = function () {};

// setters

Place.prototype.setTitle = function (title) {
  this.title = title;
  return this;
}

Place.prototype.setDescription = function (description) {
  this.description = description;
  return this;
};

Place.prototype.setLocation = function (location) {
  this.location = location;
  return this;
};

Place.prototype.setType = function (type) {
  this.type = type;
  return this;
};

Place.prototype.setZoom = function (zoom) {
  this.zoom = zoom;
  return this;
};

Place.prototype.setMarker = function (marker) {
  this.marker = marker;
  return this;
};

Place.fromJson = function (place) {
  return new Place()
      .setTitle(place.title)
      .setDescription(place.description)
      .setLocation(place.location)
      .setZoom(place.zoom)
      .setType(place.type);
};


// getters

Place.prototype.getTitle = function () {
  return this.title;
};

Place.prototype.getDescription = function () {
  return this.description;
};

Place.prototype.getLocation = function () {
  return this.location;
};

Place.prototype.getType = function () {
  return this.type;
};

Place.prototype.getZoom = function () {
  return this.zoom;
};

Place.prototype.getMarker = function () {
  return this.marker;
};

Place.prototype.toJson = function () {
  return {
    'title' : this.title,
    'description' : this.description,
    'location' : this.location,
    'type' : this.type,
    'zoom' : this.zoom
  };
};

Place.placesToJson = function (places) {
  var jsonPlaces = []
  _.each(places, function (place) {
    jsonPlaces.push(place.toJson());
  })
  return jsonPlaces
}

Place.allPlaces = [];

Place.tmpPlace = new Place();



//
// MARK: (Application / UI) Logic
//


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
  $.post('/', JSON.stringify({'fetch_data': ''}), loadPlaces);


  //
  // MARK: add event listeners
  //

  // 1. Map event listeners
  _Map.addListener('click', function(e) {

    var tmpIsInList = _.first(_.filter(Place.allPlaces, function (place) {
      return place.getMarker() === Place.tmpPlace.getMarker()
    })) === undefined;

    // remove old marker if there was one
    if (Place.tmpPlace.getMarker() !== undefined && tmpIsInList)
      Place.tmpPlace.getMarker().setMap(null);

    // set to new empty place
    Place.tmpPlace = new Place();

    // set place location
    Place.tmpPlace.setLocation(_MapProj.fromLatLngToPoint(e.latLng));

    // set new marker
    Place.tmpPlace.setMarker(
      MapTools.addMarker(
        _MapProj.fromPointToLatLng(Place.tmpPlace.getLocation()),
        Place.tmpPlace.getTitle(),
        ""
      )
    )

    openplace_typeSelectorMenu(e.pixel);
  });
  _Map.addListener('zoom_changed', zoomChanged);

  // 2. Place selector menu item click
  $("#place-selector-places li").click(function() {

    Place.tmpPlace.setType($(this).text());
    Place.tmpPlace.setZoom(_Map.getZoom());

    // show the place save form
    $(".add_place_form_wrapper").animate({top: "3%"}, 250, function () {
      formDisplay(Place.tmpPlace);
    });
  });

  // 3. Add place form submit listener
  $("#save-place").mouseup(function() {
    // hide the place save form
    $(".add_place_form_wrapper").animate({
      top: "-100%",
    }, 250);

    saveNewPlaceForm( $("#place_save_form"), Place.tmpPlace );
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

function zoomChanged() {}

function openplace_typeSelectorMenu(screenPoint) {
  // position the place selector menu
  $(".place_selector_menu_wrapper").css({
    'top'   : screenPoint.y + 'px',
    'left'  : screenPoint.x + 'px',
  });
  // simulate click on connected button to open the menu
  document.getElementById("place-selector-menu").click();
}

function loadPlaces(data) {
  // console.log(places);
  _.each(data['places'], function(placeJson) {

    var newPlace = Place.fromJson(placeJson);

    // add to map
    newPlace.setMarker(
      MapTools.addMarker(
        _MapProj.fromPointToLatLng(newPlace.getLocation()),
        newPlace.getTitle(),
        ""
      )
    )

    // add to list
    Place.allPlaces.push(newPlace);
  });
}

function onMarkerClick(clickedMarker) {

  // show info window
  // clickedMarker.info.open(_Map, clickedMarker);

  // find in list
  var foundPlace = _.first(
    _.filter(Place.allPlaces, function(place) {
      return place.getMarker() === clickedMarker
    })
  );

  var tmpIsInList = _.first(_.filter(Place.allPlaces, function (place) {
    return place.getMarker() === Place.tmpPlace.getMarker()
  })) === undefined

  // remove old marker if there was one
  if (Place.tmpPlace.getMarker() !== undefined && tmpIsInList)
    Place.tmpPlace.getMarker().setMap(null);

  Place.tmpPlace = foundPlace

  // show the place save form
  $(".add_place_form_wrapper").animate({top: "3%"}, 250, function () {
    formDisplay(foundPlace);
  });
}

function onMarkerDrag(draggedMarker) {
  console.log(draggedMarker);
  // find marker in list and update location
}

function formDisplay(place) {

  // set place location
  $(".lat-lng").text(
    place.getLocation().x + ", "
    + place.getLocation().y + ", "
    + place.getZoom() + "z"
  );

  // set place type
  $(".place-type").text(place.getType());

  // reset form
  $("#place_save_form").trigger('reset');

  // focus on first form element
  $("#place-title").focus();

  if (place.getTitle()) {
    $("#place-title").val(place.getTitle());
    $("#place-title").parent(".mdl-textfield").addClass("is-upgraded is-dirty");
  }
  if (place.getDescription()) {
    $("#place-desc").val(place.getDescription());
    $("#place-desc").parent(".mdl-textfield").addClass("is-upgraded is-dirty");
  }
}


function saveNewPlaceForm(form) {
  var data = {};
  form.serializeArray().map(function(x){data[x.name] = x.value;});

  Place.tmpPlace.setTitle(data['title']);
  Place.tmpPlace.setDescription(data['description']);

  // add / update `Place.allPlaces`
  var idx;
  _.find(Place.allPlaces, function(thisPlace, placeIndex){
     if (thisPlace.getMarker() == Place.tmpPlace.getMarker()) {
        idx = placeIndex;
        return true;
     };
  });

  if (idx === undefined)
    Place.allPlaces.push(Place.tmpPlace);
  else
    Place.allPlaces[idx] = Place.tmpPlace

  $.post('/', JSON.stringify({places:Place.placesToJson(Place.allPlaces)}), function(r) {
    console.log("saved: ",r.success);
  });
}
