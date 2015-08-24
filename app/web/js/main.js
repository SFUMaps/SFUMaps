//
// MARK: Place Class
//

var Place = function (location, zoom, marker) {
  this.location = location;
  this.zoom = zoom;
  this.marker = marker;
};

// setters

Place.prototype.setTitle = function (title) {
  this.title = title;
  return this;
}

Place.prototype.setDescription = function (description) {
  this.description = description;
  return this;
};

Place.prototype.setType = function (type) {
  this.type = type;
  return this;
};

Place.fromJson = function (place, marker) {
  return new Place(place.location, place.zoom, marker)
      .setTitle(place.title)
      .setDescription(place.description)
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
    'title'         : this.title,
    'description'   : this.description,
    'location'      : this.location,
    'type'          : this.type,
    'zoom'          : this.zoom
  };
};

Place.prototype.getLocationString = function () {
  var location = this.getLocation()
  return location.x + ", " + location.y + "," + this.getZoom() + "z"
};

Place.placesToJson = function (places) {
  var jsonPlaces = []
  _.each(places, function (place) {
    jsonPlaces.push(place.toJson());
  })
  return jsonPlaces
}


//
// MARK: (Application / UI) Logic
//

angular.module('mapsApp', [])
.factory('SharedData', function($rootScope){
  var sharedData = {};

  var allPlaces = [];

  sharedData.grabbedMarker = null;

  sharedData.addPlace = function (place) {
    allPlaces.push(place);
  }

  sharedData.addNewPlace = function (place) {
    this.newPlace = place;
    $rootScope.$broadcast("newPlaceUpdated");
  }

  sharedData.setIsEditingNewPlace = function (addingPlace) {
    this.addingNewPlace = addingPlace;
    $rootScope.$broadcast("updateAddingPlace");
  }

  sharedData.isEditingNewPlace = function () {
    return this.addingNewPlace;
  }

  sharedData.getNewPlace = function () {
    return this.newPlace;
  }

  sharedData.getAllPlaces = function () {
    return allPlaces
  }

  return sharedData;
})
.controller('PlaceFormController', function ($scope, SharedData) {

  $scope.$on('newPlaceUpdated', function() {
    _.defer(function(){$scope.$apply();});
    $scope.place = SharedData.getNewPlace();
  });

  $scope.saveNewPlace = function () {
    SharedData.setIsEditingNewPlace(false);

    // hide form
    $(".add_place_form_wrapper").animate({top: "-100%"}, 250);

    SharedData.grabbedMarker = null

    $("#place_save_form").trigger('reset');
  }

})
.controller('MapController', function ($scope, SharedData) {

  $scope._Map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 0, lng: 0},
    zoom: 3,
    streetViewControl: false,
    mapTypeControlOptions: {
      mapTypeIds: [MAP_ID]
    }
  });

  //
  $scope._MapProj = new MercatorProjection();
  $scope.placeTypes = ["Room", "Lecture Hall / Auditorium", "Building", "Walkway", "Road"]


  // add base map
  var baseMap = new google.maps.ImageMapType({
    getTileUrl: function(coord, zoom) {
      var normalizedCoord = $scope._MapProj.getNormalizedCoord(coord, zoom);
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
  $scope._Map.mapTypes.set(MAP_ID, baseMap);
  $scope._Map.setMapTypeId(MAP_ID);

  // add overlay for the buildings
  var buildingsOverlay = new google.maps.ImageMapType({
    getTileUrl: function(coord, zoom) {
      var normalizedCoord = $scope._MapProj.getNormalizedCoord(coord, zoom);
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
  $scope._Map.overlayMapTypes.push(buildingsOverlay);

  $scope.toggleMapUI = function () {
    $scope._Map.setOptions({
      draggable: !SharedData.isEditingNewPlace(),
      zoomControl: !SharedData.isEditingNewPlace(),
      scrollwheel: !SharedData.isEditingNewPlace(),
      disableDoubleClickZoom: SharedData.isEditingNewPlace(),
      disableDefaultUI: SharedData.isEditingNewPlace(),
    });
  }

  $scope.onPlaceTypeSelected = function (e) {
    var mapPoint = $scope._MapProj.fromLatLngToPoint(SharedData.grabbedMarker.getPosition())

    SharedData.addNewPlace(
      SharedData.getNewPlace()
        .setType(this.place)
    )

    // show form
    $(".add_place_form_wrapper").animate({top: "3%"}, 250);
  }

  $scope.$on('updateAddingPlace', function() {
    _.defer(function(){$scope.$apply();});
    $scope.contextMenuOpen = SharedData.isEditingNewPlace();
    $scope.toggleMapUI()
  });

  function addPlaceToMap (place) {
    // @link{Place} is a JS class
    place.setMarker(createMarker({
      position  : $scope._MapProj.fromPointToLatLng(place.getLocation()),
      title     : place.getTitle(),
      icon      : "",
      draggable : true,
    }))

    $scope.allPlaces.push(place);
  }

  function createMarker (markerInfo, markerClick) {
    var newMarker = new google.maps.Marker({
        position : markerInfo.position,
        map : $scope._Map,
        icon: markerInfo.icon,
        title: markerInfo.title,
        draggable : markerInfo.draggable
    });

    newMarker.addListener('click', markerClick);
    return newMarker;
  }

  function openContextMenu (e) {
    // position the place selector menu
    $(".place_selector_menu_wrapper").css({
      'top'   : e.pixel.y + 'px',
      'left'  : e.pixel.x + 'px',
    });
    // simulate click on connected button to open the menu
    document.getElementById("place-selector-menu").click();
  }

  //
  // MARK: Map Event Listeners
  //

  $scope._Map.addListener('click', function (e) {
    if (!$scope.contextMenuOpen) {
      openContextMenu(e);

      // set @value{SharedData.grabbedMarker}
      if (SharedData.grabbedMarker !== null) {
        SharedData.grabbedMarker.setPosition(e.latLng)
      } else {
        SharedData.grabbedMarker = createMarker({
          position  : e.latLng,
          title     : "",
          icon      : "",
          draggable : true,
        }, onMarkerClick)
      }

      SharedData.addNewPlace(
        new Place(
          $scope._MapProj.fromLatLngToPoint(e.latLng),
          $scope._Map.getZoom(),
          SharedData.grabbedMarker
        )
      )

      SharedData.setIsEditingNewPlace(true)
    }
  });


  // fetch initial map data
  $.post('/', JSON.stringify({'fetch_data': ''}), function (data) {
    _.each(data['places'], function (placeJson) {
      var newPlace = Place.fromJson(placeJson, createMarker({
          position  : $scope._MapProj.fromPointToLatLng(placeJson.location),
          title     : placeJson.title,
          icon      : "",
          draggable : true,
        }, onMarkerClick)
      )
      SharedData.addPlace(newPlace)
    })
  });

  function onMarkerClick(marker) {
    var placeToEdit = _.first(
      _.filter(SharedData.getAllPlaces(), function (place) {
        return place.getMarker().getPosition() == marker.latLng
      })
    )
    SharedData.setIsEditingNewPlace(true);
    SharedData.addNewPlace(placeToEdit)

    // show form
    $(".add_place_form_wrapper").animate({top: "3%"}, 250);
  }
});
