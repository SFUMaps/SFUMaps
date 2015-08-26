//
// MARK: Place Class
//

var Place = function (marker, zoom) {
  this.marker = marker;
  this.position = marker.getPosition();
  this.zooms = [zoom];
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

Place.prototype.setPosition = function (position) {
  this.position = position;
  return this;
};

Place.prototype.addZoom = function (zoom) {
  this.zooms.push(zoom);
  return this;
};

Place.prototype.removeZoom = function (zoom) {
  this.zooms.splice(this.zooms.indexOf(zoom), 1);
  return this;
};

// getters

Place.prototype.getTitle = function () {
  return this.title;
};

Place.prototype.getDescription = function () {
  return this.description;
};

Place.prototype.getType = function () {
  return this.type;
};

Place.prototype.getPosition = function() {
  return this.position;
};

Place.prototype.getPositionString = function() {
  var pos = this.position;
  return pos.lat().toFixed(5) + ", " + pos.lng().toFixed(5);
};

Place.prototype.getZoom = function () {
  return this.zoom;
};

Place.prototype.getMarker = function () {
  return this.marker;
};


//
// MARK: (Application / UI) Logic
//

angular.module('mapsApp', [])
.factory('SharedData', function($rootScope){

  var sharedData = {};

  var allPlaces = [];

  sharedData.grabbedMarker = null;

  // setters

  sharedData.addPlace = function (place) {
    allPlaces.push(place);
  }

  sharedData.updatePlace = function (index, place) {
    allPlaces[index] = place;
  }

  sharedData.removePlace = function (index) {
    if (index > -1) {
      allPlaces.splice(index, 1);
    }
  }

  sharedData.setTmpPlace = function (place) {
    this.tmpPlace = place;
    $rootScope.$broadcast("tmpPlaceUpdated", this.tmpPlace);
  }

  sharedData.setIsEditingPlace = function (isEditingPlace) {
    this.addingNewPlace = isEditingPlace;
    $rootScope.$broadcast("updateIsEditingPlace");
  }

  sharedData.updatePlaceLocation = function (marker) {
    $rootScope.$broadcast("placeLocationUpdated", marker);
  }

  // getters

  sharedData.isEditingPlace = function () {
    return this.addingNewPlace;
  }

  sharedData.getTmpPlace = function () {
    return this.tmpPlace;
  }

  sharedData.getAllPlaces = function () {
    return allPlaces;
  }

  return sharedData;
})
.controller('PlaceFormController', function ($scope, SharedData) {

  $scope.placeTypes = ["Room", "Lecture Hall / Auditorium", "Building", "Walkway", "Road"]

  $scope.$on('tmpPlaceUpdated', function(ev, place) {
    _.defer(function(){$scope.$apply();});
    
    // update place model
    $scope.tmpPlace = place
  });

  $scope.$on('placeLocationUpdated', function (ev, marker) {
    SharedData.setTmpPlace(
      SharedData.getTmpPlace()
        .setPosition(marker.latLng))
  })

  $scope.savePlace = function () {
    SharedData.setIsEditingPlace(false)
  }

  $scope.removePlace = function () {
  }

  $scope.onPlaceTypeSelected = function (place) {
    SharedData.setTmpPlace(
      SharedData.getTmpPlace()
        .setType(place))
  }

})
.controller('MapController', function ($scope, SharedData) {

  $scope.map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 0, lng: 0},
    zoom: 3,
    streetViewControl: false,
    mapTypeControlOptions: {
      mapTypeIds: [MAP_ID]
    }
  });

  // add base map
  $scope.map.mapTypes.set(MAP_ID, new google.maps.ImageMapType({
    getTileUrl: function (coord, zoom) {
      var normalizedCoord = MercatorProjection.getNormalizedCoord(coord, zoom);
      if (!normalizedCoord) return null;
      return "assets/maptiles/basemap/" + zoom
            + "/" + normalizedCoord.x
            + "/" + normalizedCoord.y
            + ".png";
    },

    tileSize: new google.maps.Size(TILE_SIZE, TILE_SIZE),
    maxZoom: 6,
    minZoom: 1,
    name: MAP_ID,
  }));
  $scope.map.setMapTypeId(MAP_ID);

  // add overlay for the buildings
  $scope.map.overlayMapTypes.push(new google.maps.ImageMapType({
    getTileUrl: function (coord, zoom) {
      var normalizedCoord = MercatorProjection.getNormalizedCoord(coord, zoom);
      if (!normalizedCoord) return null;
      return "assets/maptiles/overlay/" + zoom
            + "/" + normalizedCoord.x
            + "/" + normalizedCoord.y
            + ".png";
    },
    tileSize: new google.maps.Size(TILE_SIZE, TILE_SIZE),
    maxZoom: 6,
    minZoom: 1,
    name: MAP_ID,
  }));

  $scope.$on('updateIsEditingPlace', function() {

    // toggle map UI
    $scope.map.setOptions({
      // draggable: !SharedData.isEditingPlace(),
      zoomControl: !SharedData.isEditingPlace(),
      disableDoubleClickZoom: SharedData.isEditingPlace(),
      disableDefaultUI: SharedData.isEditingPlace(),
    });
  });


  //
  // MARK: helper functions
  //

  function createMarker (markerInfo, cb_markerClick, cb_markerDragEnd) {
    var newMarker = new google.maps.Marker({
        position : markerInfo.position,
        map : $scope.map,
        icon: markerInfo.icon,
        title: markerInfo.title,
        draggable : markerInfo.draggable
    });

    if (cb_markerClick != null) {
      newMarker.addListener('click', cb_markerClick);
    }

    if (cb_markerDragEnd != null) {
      newMarker.addListener('dragend', cb_markerDragEnd);
    }

    return newMarker;
  }

  //
  // MARK: Map Event Listeners
  //

  $scope.map.addListener('click', function (e) {
    if (!SharedData.isEditingPlace()) {

      // set @value{SharedData.grabbedMarker}
      if (SharedData.grabbedMarker !== null) {
        SharedData.grabbedMarker.setPosition(e.latLng)
      } else {
        SharedData.grabbedMarker = createMarker({
          position  : e.latLng,
          title     : "",
          icon      : "",
          draggable : true,
        }, onMarkerClick, onMarkerDragEnd)
      }

      SharedData.setTmpPlace(new Place(SharedData.grabbedMarker, $scope.map.getZoom()))

      SharedData.setIsEditingPlace(true)
    }
  });


  // fetch initial map data
  // $.post('/', JSON.stringify({'fetch_data': ''}), function (data) {
  //   _.each(data['places'], function (placeJson) {
  //     var newPlace = Place.fromJson(placeJson, createMarker({
  //         position  : MercatorProjection.fromPointToLatLng(placeJson.location),
  //         title     : placeJson.title,
  //         icon      : "",
  //         draggable : true,
  //       }, onMarkerClick, onMarkerDragEnd)
  //     )
  //     SharedData.addPlace(newPlace)
  //   })
  // });

  function onMarkerClick(marker) {

    // var placeToEdit = _.first(
    //   _.filter(SharedData.getAllPlaces(), function (place) {
    //     return place.getMarker().getPosition() == marker.latLng
    //   })
    // )

    // SharedData.setIsEditingPlace(true);
    // SharedData.setTmpPlace(placeToEdit.setZoom($scope.map.getZoom()));

    // show form
  }

  function onMarkerDragEnd(marker) {
    SharedData.updatePlaceLocation(marker)
    SharedData.setIsEditingPlace(true);
    
    // show form
  }

});
