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

Place.prototype.setLocation = function (location) {
  this.location = location;
  return this;
};

Place.prototype.setZoom = function (zoom) {
  this.zoom = zoom;
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
  return location.x + ", " + location.y + ", " + this.getZoom() + "z";
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

  sharedData.addNewPlace = function (place) {
    this.newPlace = place;
    $rootScope.$broadcast("newPlaceUpdated");
  }

  sharedData.setIsEditingPlace = function (isEditingPlace) {
    this.addingNewPlace = isEditingPlace;
    $rootScope.$broadcast("updateIsEditingPlace");
  }

  sharedData.updatePlaceLocation = function (marker, zoom) {
    $rootScope.$broadcast("placeLocationUpdated", [marker, zoom]);
  }

  // getters

  sharedData.isEditingPlace = function () {
    return this.addingNewPlace;
  }

  sharedData.getNewPlace = function () {
    return this.newPlace;
  }

  sharedData.getAllPlaces = function () {
    return allPlaces;
  }

  return sharedData;
})
.controller('PlaceFormController', function ($scope, SharedData) {

  $scope.$on('newPlaceUpdated', function() {
    _.defer(function(){$scope.$apply();});
    $scope.place = SharedData.getNewPlace();
    $scope.placeTitle = $scope.place.getTitle();
    $scope.placeDescription = $scope.place.getDescription();
    $scope.placeType = $scope.place.getType();
  });

  $scope.$on('placeLocationUpdated', function (ev, location_data) {
    _.defer(function(){$scope.$apply();});
    var marker = location_data[0]
    var editedPlace = _.first(
      _.filter(SharedData.getAllPlaces(), function (place) {
        return place.getMarker().getPosition() == marker.latLng
      })
    )
    $scope.place = ($scope.place === null || $scope.place === undefined) ? editedPlace : $scope.place;
    $scope.place.setZoom(location_data[1]);
    $scope.place.setLocation(
      MercatorProjection.fromLatLngToPoint(marker.latLng)
    )
  })

  $scope.savePlace = function () {
    SharedData.setIsEditingPlace(false);

    var foundIndex = -1;
    _.find(SharedData.getAllPlaces(), function(thisPlace, i){
       if ($scope.place.getMarker().getPosition() == thisPlace.getMarker().getPosition()) {
          foundIndex = i;
          return true;
       }
    });

    // update place values
    $scope.place.setTitle($scope.placeTitle)
    $scope.place.setDescription($scope.placeDescription)

    if (foundIndex == -1)
      SharedData.addPlace($scope.place);
    else
      SharedData.updatePlace(foundIndex, $scope.place);

    // clear marker
    SharedData.grabbedMarker = null

    // hide form
    $(".add_place_form_wrapper").animate({top: "-100%"}, 250);
    $("#place_save_form").trigger('reset');


    // finally send data to server
    $.post('/', JSON.stringify({places: Place.placesToJson(SharedData.getAllPlaces())}), function (r) {
      console.log(r.success);
    })
  }

  $scope.removePlace = function () {
    // clear the new place marker and if exists in SharedData.allPlaces, remove from there too, then send data to server
    var foundIndex = -1;
    _.find(SharedData.getAllPlaces(), function(thisPlace, i){
       if ($scope.place.getMarker().getPosition() == thisPlace.getMarker().getPosition()) {
          foundIndex = i;
          return true;
       }
    });

    SharedData.removePlace(foundIndex)

    $scope.place.getMarker().setMap(null);
    $scope.place = null;
    // hide form
    $(".add_place_form_wrapper").animate({top: "-100%"}, 250);
    $("#place_save_form").trigger('reset');

    // finally send data to server
    $.post('/', JSON.stringify({places: Place.placesToJson(SharedData.getAllPlaces())}), function (r) {
      console.log(r.success);
    })
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

  $scope.placeTypes = ["Room", "Lecture Hall / Auditorium", "Building", "Walkway", "Road"]

  // add base map
  $scope.map.mapTypes.set(MAP_ID, new google.maps.ImageMapType({
    getTileUrl: function(coord, zoom) {
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
    getTileUrl: function(coord, zoom) {
      var normalizedCoord = MercatorProjection.getNormalizedCoord(coord, zoom);
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
  }));

  $scope.$on('updateIsEditingPlace', function() {
    $scope.contextMenuOpen = SharedData.isEditingPlace();
    $scope.toggleMapUI()
  });

  $scope.toggleMapUI = function () {
    $scope.map.setOptions({
      draggable: !SharedData.isEditingPlace(),
      zoomControl: !SharedData.isEditingPlace(),
      scrollwheel: !SharedData.isEditingPlace(),
      disableDoubleClickZoom: SharedData.isEditingPlace(),
      disableDefaultUI: SharedData.isEditingPlace(),
    });
  }

  $scope.onPlaceTypeSelected = function (e) {
    var mapPoint = MercatorProjection.fromLatLngToPoint(
      SharedData.grabbedMarker.getPosition())

    SharedData.addNewPlace(
      SharedData.getNewPlace()
        .setType(this.place)
    )

    $(".add_place_form_wrapper").animate({top: "3%"}, 250);
  }

  function addPlaceToMap (place) {
    // @link{Place} is a JS class
    place.setMarker(createMarker({
      position  : MercatorProjection.fromPointToLatLng(place.getLocation()),
      title     : place.getTitle(),
      icon      : "",
      draggable : true,
    }))

    $scope.allPlaces.push(place);
  }

  function createMarker (markerInfo, markerClick, markerDragEnd) {
    var newMarker = new google.maps.Marker({
        position : markerInfo.position,
        map : $scope.map,
        icon: markerInfo.icon,
        title: markerInfo.title,
        draggable : markerInfo.draggable
    });

    newMarker.addListener('click', markerClick);
    newMarker.addListener('dragend', markerDragEnd);
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

  $scope.map.addListener('click', function (e) {
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
        }, onMarkerClick, onMarkerDragEnd)
      }

      SharedData.addNewPlace(
        new Place(
          MercatorProjection.fromLatLngToPoint(e.latLng),
          $scope.map.getZoom(),
          SharedData.grabbedMarker
        )
      )

      SharedData.setIsEditingPlace(true)
    }
  });


  // fetch initial map data
  $.post('/', JSON.stringify({'fetch_data': ''}), function (data) {
    _.each(data['places'], function (placeJson) {
      var newPlace = Place.fromJson(placeJson, createMarker({
          position  : MercatorProjection.fromPointToLatLng(placeJson.location),
          title     : placeJson.title,
          icon      : "",
          draggable : true,
        }, onMarkerClick, onMarkerDragEnd)
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

    SharedData.setIsEditingPlace(true);
    SharedData.addNewPlace(placeToEdit.setZoom($scope.map.getZoom()));

    // show form
    $(".add_place_form_wrapper").animate({top: "3%"}, 250);
  }

  function onMarkerDragEnd(marker) {
    SharedData.updatePlaceLocation(marker, $scope.map.getZoom())

    SharedData.setIsEditingPlace(true);
    // show form
    $(".add_place_form_wrapper").animate({top: "3%"}, 250);
  }

});
