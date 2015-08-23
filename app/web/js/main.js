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
    'title'         : this.title,
    'description'   : this.description,
    'location'      : this.location,
    'type'          : this.type,
    'zoom'          : this.zoom
  };
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
.factory('DataService', function($rootScope){
  var service = {};
  service.location = "unknown";
  service.placeType = "unknown";

  service.updateLocation = function (location) {
    this.location = location;
    $rootScope.$broadcast("valuesUpdated");
  }

  service.updatePlaceType = function (placeType) {
    this.placeType = placeType;
    $rootScope.$broadcast("valuesUpdated");
  }

  return service;
})
.controller('PlaceFormController', function ($scope, DataService) {
  $scope.$on('valuesUpdated', function() {
    $scope.location = DataService.location;
    $scope.placeType = DataService.placeType;
  });
})
.controller('MapController', function ($scope, DataService) {

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
  // all map places holding array
  $scope.allPlaces = []
  // temporary marker placed when new place is created
  $scope.grabbedMarker;
  // if context menu is showing or not
  $scope.contextMenuOpen = false;


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
      draggable: $scope.contextMenuOpen,
      zoomControl: $scope.contextMenuOpen,
      scrollwheel: $scope.contextMenuOpen,
      disableDoubleClickZoom: !$scope.contextMenuOpen,
      disableDefaultUI: !$scope.contextMenuOpen
    });
  }

  $scope.onPlaceTypeSelected = function (e) {
    var mapPoint = $scope._MapProj.fromLatLngToPoint($scope.grabbedMarker.getPosition())
    DataService.updateLocation(mapPoint.x + ", "+mapPoint.y +", "+ $scope._Map.getZoom()+"z")
    DataService.updatePlaceType(this.place);

    // show form
    $(".add_place_form_wrapper").animate({top: "3%"}, 250);

    $scope.toggleMapUI()
    $scope.contextMenuOpen = false;
  }

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

  function createMarker (markerInfo) {
    return new google.maps.Marker({
        position : markerInfo.position,
        map : $scope._Map,
        icon: markerInfo.icon,
        title: markerInfo.title,
        draggable : markerInfo.draggable
    });
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

  function onMapClick(e) {
    if (!$scope.contextMenuOpen) {
      openContextMenu(e);

      // set @value{$scope.grabbedMarker}
      if ($scope.grabbedMarker !== undefined) {
        $scope.grabbedMarker.setPosition(e.latLng)
      } else {
        $scope.grabbedMarker = createMarker({
          position  : e.latLng,
          title     : "",
          icon      : "",
          draggable : true,
        })
      }

      // menu is showing now, disable map zoom & drag
      $scope.toggleMapUI()

      $scope.contextMenuOpen = true;
    }
  }

  //
  // MARK: Map Event Listeners
  //

  $scope._Map.addListener('click', onMapClick);

  $scope._Map.addListener('zoom_changed', function () {
    console.log("zoom: " + $scope._Map.getZoom());
  });



  // fetch initial map data
  $.post('/', JSON.stringify({'fetch_data': ''}), function (data) {
    _.each(data['places'], function (placeJson) {
      addPlaceToMap(Place.fromJson(placeJson))
    })
  });
});
