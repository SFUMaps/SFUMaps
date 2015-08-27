//
// MARK: Place Class
//

var PlaceKeys = {
  TITLE : "placeTitle",
  DESCRIPTION : "placeDescription",
  TYPE : "placeType",
  POSITION : "placePosition",
  ZOOM : "placeZoom",
}

var CustomPlace = function (mapPlace, marker) {
  this.mapPlace = mapPlace;
  this.marker = marker;
}

CustomPlace.prototype.getPositionString = function() {
  var location = this.mapPlace.get(PlaceKeys.POSITION);
  return location.lat.toFixed(6) + ", " + location.lng.toFixed(6);
};

CustomPlace.prototype.getZooms = function() {
  return this.mapPlace.get(PlaceKeys.ZOOM);
};

CustomPlace.prototype.getType = function() {
  return this.mapPlace.get(PlaceKeys.TYPE);
};

CustomPlace.prototype.getTitle = function() {
  return this.mapPlace.get(PlaceKeys.TITLE);
};

CustomPlace.prototype.getDescription = function() {
  return this.mapPlace.get(PlaceKeys.DESCRIPTION);
};


//
// MARK: (Application / UI) Logic
//

angular.module('mapsApp', [])
.factory('SharedData', function($rootScope){

  Parse.initialize("onN8KLiec9xRevRxwcc1ojQfYPYvtnDOf4w22x1R", "DoByqX8VDewBp4TrOguly3k967jLcZdiPevAskvy");

  var sharedData = {}; // SharedData object

  var allPlaces = []; // privvate `allPlaces` Array()

  sharedData.grabbedMarker = null; // current marker held in user's hand

  sharedData.mapPlace = Parse.Object.extend("MapPlace"); // parse object

  // setters

  sharedData.addPlace = function (place) {
    allPlaces.push(place);
  }

  sharedData.removePlace = function (index) {
    if (index > -1) {
      allPlaces.splice(index, 1);
    }
  }

  sharedData.updateViews = function () {
    $rootScope.$broadcast('tmpPlaceUpdated', this.tmpPlace);
  }

  sharedData.setIsEditingPlace = function (isEditingPlace) {
    this.addingNewPlace = isEditingPlace;
    $rootScope.$broadcast('updateIsEditingPlace');
  }

  sharedData.updatePlaceLocation = function (marker) {
    $rootScope.$broadcast('placeLocationUpdated', marker);
  }

  sharedData.updateCurrentMapZoom = function (zoom) {
    // check if it's already in list, if yes {return null} else {return mapzoom}
    var returnZoom = null;
    if (this.tmpPlace !== undefined) {
      if (!(this.tmpPlace.getZooms().indexOf(zoom) > -1)) {
        returnZoom = zoom;
      }
    }
    this.currentZoom = returnZoom;
    $rootScope.$broadcast('mapZoomChanged', this.currentZoom);
  }

  // getters

  sharedData.isEditingPlace = function () {
    return this.addingNewPlace;
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
    $scope.tmpPlace = place;
    $scope.placeTitle = place.getTitle();
    $scope.placeDescription = place.getDescription();
  });

  $scope.$on('placeLocationUpdated', function (ev, marker) {
    SharedData.tmpPlace.mapPlace.set(PlaceKeys.POSITION, {
      lat: marker.latLng.lat(),
      lng: marker.latLng.lng()
    });
    SharedData.updateViews();
  });

  $scope.$on('mapZoomChanged', function (ev, zoom) {
    _.defer(function(){$scope.$apply();});
    $scope.currentZoom = zoom;
  });

  $scope.onPlaceTypeSelected = function (placeType) {
    SharedData.tmpPlace.mapPlace.set(PlaceKeys.TYPE, placeType);
    SharedData.updateViews();
  };

  $scope.addNewZoom = function (zoom) {
    $scope.currentZoom = null;
    SharedData.tmpPlace.mapPlace.add(PlaceKeys.ZOOM, zoom);
    SharedData.updateViews();
  };

  $scope.savePlace = function () {
    // create new place or update an older one

    var placeObj = SharedData.tmpPlace.mapPlace;

    placeObj.set(PlaceKeys.TITLE, $scope.placeTitle);
    placeObj.set(PlaceKeys.DESCRIPTION, $scope.placeDescription);

    placeObj.save().then(function(object) {
      console.log("saved place" + object);
    });

    SharedData.grabbedMarker = null;
    SharedData.setIsEditingPlace(false)
  }

  $scope.removePlace = function () {
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

      SharedData.grabbedMarker = createMarker({
          position  : e.latLng,
          title     : "",
          icon      : "",
          draggable : true,
        }, onMarkerClick, onMarkerDragEnd)


      var parsePlace = new SharedData.mapPlace()

      var markerPos = SharedData.grabbedMarker.getPosition();
      parsePlace.set(PlaceKeys.POSITION, {
        lat: markerPos.lat(),
        lng: markerPos.lng()
      })

      parsePlace.set(PlaceKeys.ZOOM, [$scope.map.getZoom()])
      
      SharedData.tmpPlace = new CustomPlace(parsePlace, SharedData.grabbedMarker);
      SharedData.updateViews();

      SharedData.setIsEditingPlace(true);
    }
  });

  $scope.map.addListener('zoom_changed', function () {
    SharedData.updateCurrentMapZoom($scope.map.getZoom())
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

  var placesQuery = new Parse.Query(SharedData.mapPlace);
  placesQuery.containedIn(PlaceKeys.ZOOM, [$scope.map.getZoom()]) // get current zoom markers
  placesQuery.find({
    success: function(results) {
      for (var i = 0; i < results.length; i++) {
        var object = results[i];
        
        var placePosition = object.get(PlaceKeys.POSITION);
        SharedData.addPlace(new CustomPlace(object, createMarker({
                  position : new google.maps.LatLng(placePosition.lat, placePosition.lng),
                  title : object.get(PlaceKeys.TITLE),
                  draggable : true,
                }, onMarkerClick, onMarkerDragEnd)));
      }
    },
    error: function(error) {
      console.log(error)
    }
  });

  function onMarkerClick(marker) {

    

    // SharedData.setIsEditingPlace(true);
    // SharedData.setTmpPlace(placeToEdit.setZoom($scope.map.getZoom()));

    // show form
  }

  function onMarkerDragEnd(marker) {
    // find place with this marker and set to tmpPlace
    if (SharedData.tmpPlace == null) {
      SharedData.tmpPlace = _.first(
        _.filter(SharedData.getAllPlaces(), function (place) {
          return place.marker.getPosition() == marker.latLng
        })
      );
    };

    SharedData.updatePlaceLocation(marker)
    SharedData.setIsEditingPlace(true);
    
    // TODO: show form
  }

});
