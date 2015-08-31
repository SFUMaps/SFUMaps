//
// MARK: Place Class
//

var PlaceKeys = {
  TITLE         : "placeTitle",
  DESCRIPTION   : "placeDescription",
  TYPE          : "placeType",
  POSITION      : "placePosition",
  ZOOM          : "placeZoom",
}

var CustomPlace = function (mapPlace, marker) {
  this.mapPlace = mapPlace;
  this.marker = marker;
}


//
// MARK: (Application / UI) Logic
//

angular.module('mapsApp', [])
.run(function() {
  Parse.initialize("onN8KLiec9xRevRxwcc1ojQfYPYvtnDOf4w22x1R", "DoByqX8VDewBp4TrOguly3k967jLcZdiPevAskvy");
})
.factory('MapPlace', function () {

  var placeClass = Parse.Object.extend('MapPlace', {/* Instance methods */}, {/* Class methods */});


  /**
   * Bind Parse class get/set methods to `ng-model`
   */


  // Property : 'title'
  Object.defineProperty(placeClass.prototype, PlaceKeys.TITLE, {
    get: function() {
      return this.get(PlaceKeys.TITLE);
    },
    set: function(value) {
      this.set(PlaceKeys.TITLE, value);
    }
  })


  // Property : 'description'
  Object.defineProperty(placeClass.prototype, PlaceKeys.DESCRIPTION, {
    get: function() {
      return this.get(PlaceKeys.DESCRIPTION);
    },
    set: function(value) {
      this.set(PlaceKeys.DESCRIPTION, value);
    }
  })

  return placeClass;
})
.factory('SharedData', function($rootScope){

  var sharedData = {};


  // map marker that is being used for creating a new place
  sharedData.tmpMapMarker = null;



  /**
   * Broadcast event updating view to add more zoom values to a map place
   */
  sharedData.setCurrentZoom = function (zoom) {
    if (this.focusedMapPlace) {
      $rootScope.$broadcast('updateCurrentZoom', zoom);
    }
  }



  /**
   * Set `editingMap` boolean value to editing or not and broadcast event to update views
   */
  sharedData.setEditingMap = function (editing) {
    this.editingMap = editing;
    $rootScope.$broadcast('updateEditingMap');
  }



  /**
   * @return - boolean value indicating if map is being edited
   */  
  sharedData.isEditingMap = function () {
    return this.editingMap;
  }



  /**
   * Sets `focusedMapPlace` to given {{place}} and broadcast event to update views
   */
  sharedData.setFocusedMapPlace = function (place) {
    this.focusedMapPlace = place;
    $rootScope.$broadcast('focusedMapPlaceUpdated', this.focusedMapPlace);
  }



  /**
   * Updates focused place location to the given location
   *
   * @param {{markerPos}} - latLng position of place
   */
  sharedData.setFocusedMapPlacePosition = function (markerPos) {
    var mapPoint = MercatorProjection.fromLatLngToPoint(markerPos)
    this.getFocusedMapPlace().mapPlace.set(PlaceKeys.POSITION, {
      x: mapPoint.x,
      y: mapPoint.y,
    })
  }



  /**
   * Updates the views of the HTML form
   */
  sharedData.updateViews = function () {
    $rootScope.$broadcast('focusedMapPlaceUpdated', this.focusedMapPlace);
  }



  /**
   * @return - current focused map place
   */
  sharedData.getFocusedMapPlace = function () {
    return this.focusedMapPlace;
  }



  // saved map places, i.e. map places stored in parse server
  var places = [];



  /**
   * Add new place to the `places` array
   *
   * @param {{place}} - CustomPlace class object
   */
  sharedData.addPlace = function (place) {
    places.push(place);
  }



  /**
   * Remove place from `places` array
   *
   * @param {{placeIndex}} - CustomPlace class object index in `places` array
   */
  sharedData.removePlace = function (placeIndex) {
    if (placeIndex > -1) {
      places.splice(placeIndex, 1);
    }
  }



  /**
   * @return - Returns all map places, no matter their zoom level
   */
  sharedData.getPlaces = function () {
    return places;
  }



  /**
   * Linear search through `places` array for the place at given position
   *
   * @return - CustomPlace class object at given position, IF not found {return current focused place}
   */
  sharedData.getPlaceAtPosition = function (placePosition) {
    var arrayPlace = _.first(
      _.filter(this.getPlaces(), function (cmp_place) {
        var cmp_place_pos = MercatorProjection.fromLatLngToPoint(cmp_place.marker.getPosition())
        return placePosition.x === cmp_place_pos.x && placePosition.y === cmp_place_pos.y
      })
    );
    return arrayPlace || this.focusedMapPlace;
  }



  /**
   * Linear search through `places` array for the place at given position
   *
   * @return - CustomPlace class object index in `places` array
   */
  sharedData.getPlaceIndexAtPosition = function (placePosition) {
    var idx = -1;
    _.find(places, function(place, index){ 
       if(MercatorProjection.fromLatLngToPoint(place.marker.getPosition()) == placePosition)
        { idx = index; return true;}; 
    });

    return idx;
  }

  return sharedData;
})
.controller('PlaceFormController', function ($scope, SharedData) {

  // place types
  $scope.placeTypes = ["Room", "Lecture Hall / Auditorium", "Building", "Walkway", "Road"]



  /**
   * On updates to the current focused map place, update the views
   */
  $scope.$on('focusedMapPlaceUpdated', function(ev, customPlace) {
    _.defer(function(){$scope.$apply();});
    $scope.focusedMapPlace = customPlace == null ? null : customPlace.mapPlace;
  });



  /**
   * When the map zoom is updated, give place form a choice to add new zoom
   */
  $scope.$on('updateCurrentZoom', function (ev, zoom) {
    _.defer(function(){$scope.$apply();});

    // check if in zooms list then don't allow adding again
    if ($scope.focusedMapPlace.get(PlaceKeys.ZOOM).indexOf(zoom) > -1) {
      $scope.currentZoom = null;
    } else {
      $scope.currentZoom = zoom;
    }
  });



  /**
   * `ng-click` bind function to update focused place 'Type'
   */
  $scope.onPlaceTypeSelected = function (place) {
    $scope.focusedMapPlace.set(PlaceKeys.TYPE, place);
  }



  /**
   * `ng-click` bind function to add new zoom to the place zooms array
   */
  $scope.addNewZoom = function (zoom) {
    $scope.focusedMapPlace.add(PlaceKeys.ZOOM, zoom);
    $scope.currentZoom = null
  }


  /**
   * `ng-click` bind function to remove zoom from the place zooms array
   */
  $scope.removeZoom = function (zoom) {
    var placeZoomIndex = $scope.focusedMapPlace.get(PlaceKeys.ZOOM).indexOf(zoom);
    if (placeZoomIndex > -1) {
      $scope.focusedMapPlace.get(PlaceKeys.ZOOM).splice(placeZoomIndex, 1);
    }
  }


  /**
   * `ng-click` bind function to save focused map place and send to Parse.
   */
  $scope.savePlace = function() {
    var placeToSave = SharedData.getFocusedMapPlace();
    if (SharedData.getPlaceAtPosition(
      MercatorProjection.fromLatLngToPoint(placeToSave.marker.getPosition())).length == 0) {
      SharedData.addPlace(placeToSave);
    }

    placeToSave.marker.setDraggable(false);
    placeToSave.mapPlace.save();
    resetForm();
    SharedData.setEditingMap(false);

    document.getElementById("add_place_form_wrapper").style.top = "-100%"; // hide form
  }



  /**
   * `ng-click` bind function remove place from saved places on Parse and local array
   */
  $scope.removePlace = function () {
    var placeToRemove = SharedData.getFocusedMapPlace();
    placeToRemove.mapPlace.destroy();

    var placeIndex = SharedData.getPlaceIndexAtPosition(
      MercatorProjection.fromLatLngToPoint(placeToRemove.marker.getPosition()));
    if (placeIndex != -1) { // remove from array
      SharedData.removePlace(placeIndex);
    }

    placeToRemove.marker.setMap(null);
    resetForm();
    SharedData.setEditingMap(false);

    document.getElementById("add_place_form_wrapper").style.top = "-100%"; // hide form
  }



  /**
   * Helper function to clear the 'Add New Place' <form> fields.
   */
  function resetForm () {
    document.getElementById("place-title").value = "";
    document.getElementById("place-desc").value = "";
    SharedData.setFocusedMapPlace(null);
    SharedData.updateViews();
  }

})
.controller('MapController', function ($scope, SharedData, MapPlace) {


  /**
   * Google Map Class Object
   */
  $scope.map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 0, lng: 0},
    zoom: 3,
    streetViewControl: false,
    mapTypeControlOptions: {
      mapTypeIds: [MAP_ID]
    }
  });



  /**
   * Create & set base map tiles on the Map object
   */
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




  /**
   * Add buildings overlay on the base Google Map tiles
   */
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




  /**
   * Listen for boolean value indicating if the map is being edited
   */
  $scope.$on('updateEditingMap', function() {
    // toggle map UI
    $scope.map.setOptions({
      zoomControl: !SharedData.isEditingMap(),
      disableDoubleClickZoom: SharedData.isEditingMap(),
      disableDefaultUI: SharedData.isEditingMap(),
    });
  });




  /**
   * On Google Map Class Object `click` listener.
   *
   * - Create new `CustomPlace` class object and show form
   */
  $scope.map.addListener('click', function (e) {
    if (!SharedData.isEditingMap()) {
      SharedData.tmpMapMarker = createMarker({
        position  : e.latLng,
      }, markerClick, markerDragEnd);

      var mapPlace = new MapPlace(); // parse object
      mapPlace.set(PlaceKeys.ZOOM, [$scope.map.getZoom()]);

      SharedData.setFocusedMapPlace(
        new CustomPlace(mapPlace, SharedData.tmpMapMarker));

      SharedData.setFocusedMapPlacePosition(SharedData.tmpMapMarker.getPosition())

      SharedData.setEditingMap(true);

      document.getElementById("add_place_form_wrapper").style.top = "3%"; // show form
    }
  });




  /**
   * On Google Map class object `zoom_changed` event, update `SharedData.currentZoom`
   */
  $scope.map.addListener('zoom_changed', function () {
    SharedData.setCurrentZoom($scope.map.getZoom());
  });

  


  /**
   * Initial Parse Query to fetch Map Places saved online
   */
  var placesQuery = new Parse.Query(MapPlace);
  placesQuery.find({
    success: function(results) {
      for (var i = 0; i < results.length; i++) {
        var object = results[i];
        
        var placePosition = object.get(PlaceKeys.POSITION);
        SharedData.addPlace(new CustomPlace(object, createMarker({
                  position : MercatorProjection.fromPointToLatLng(
                    new google.maps.Point(placePosition.x, placePosition.y)),
                }, markerClick, markerDragEnd)));
      }
    },
    error: function(error) {
      console.log(error)
    }
  });




  /**
   * Responds to marker click events, finds clicked marker in local array and sets
   * as focused map place, IF not editing Map at the time clicked
   *
   * @param {{marker}} - marker that was clicked
   */
  function markerClick(marker) {
    if (!SharedData.isEditingMap()) {
      var clickedPlace = SharedData.getPlaceAtPosition(MercatorProjection.fromLatLngToPoint(marker.latLng));
      clickedPlace.marker.setDraggable(true);

      SharedData.setFocusedMapPlace(clickedPlace);
      SharedData.setEditingMap(true);

      // show form
      document.getElementById("add_place_form_wrapper").style.top = "3%";
    }
  }




  /**
   * If a marker is being dragged, and now the drag has ended update the current focused
   * map location coordinates, then call `updateViews()` to update form
   *
   * @param {{marker}} - marker that was dragged
   */
  function markerDragEnd(marker) {
    SharedData.setFocusedMapPlacePosition(marker.latLng)
    SharedData.updateViews();
  }



  /**
   * Helper function to create new marker
   *
   * @param {{markerInfo}}        - JSON object contaning marker info
   * @param {{cb_markerClick}}    - callback function for marker click
   * @param {{cb_markerDragEnd}}  - callback function for marker drag end
   *
   * @return - google.maps.Marker Class object with the given information
   */
  function createMarker (markerInfo, cb_markerClick, cb_markerDragEnd) {
    var newMarker = new google.maps.Marker({
        position : markerInfo.position,
        map : $scope.map,
        icon: markerInfo.icon,
    });

    if (cb_markerClick != null) {
      newMarker.addListener('click', cb_markerClick);
    }

    if (cb_markerDragEnd != null) {
      newMarker.addListener('dragend', cb_markerDragEnd);
    }

    return newMarker;
  }



  // createMarker({
  //   position: MercatorProjection.fromPointToLatLng(new google.maps.Point(120, 130))
  // })

});