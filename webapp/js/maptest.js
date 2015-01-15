// App's main configs

var AQ_WEST_DATA = [
      [0, 800], [0, 700], [0, 600],
      [0, 500], [0, 400], [0, 300],
      [0, 200], [0, 100], [0, 000],
];

var IMAGE_SIZE = [800, 800];

// all MAP icon styles
var IS_UserDot = new ol.style.Style({
    image: new ol.style.Icon({ src: 'images/Icon_UserDot.png' }),
});

var pixelProjection = new ol.proj.Projection({
    code: 'pixel',
    units: 'pixels',
    extent: [0, 0, IMAGE_SIZE[0], IMAGE_SIZE[1]],
});

var map = new ol.Map({
    layers: [
    new ol.layer.Image({
        source: new ol.source.ImageStatic({
            url: 'images/aq.png',
            imageSize: IMAGE_SIZE,
            projection: pixelProjection,
            imageExtent: pixelProjection.getExtent(),
        }),
    }),
    ],

    target: 'map',

    view: new ol.View({
        projection: pixelProjection,
        center: ol.extent.getCenter(pixelProjection.getExtent()),
        zoom: 0.7,
    }),
});

var features=[];

//add AQ_west data into features
for (i=0; i<AQ_WEST_DATA.length; i++)
    features.push(new ol.Feature({ geometry: new ol.geom.Point(AQ_WEST_DATA[i]) }));

//add the user dot to the map
var F_UserDot = new ol.Feature({ geometry: new ol.geom.Point([400,400]), });
F_UserDot.setStyle(IS_UserDot);
features.push(F_UserDot);

// move the user dot - F_UserDot.setGeometry(new ol.geom.Point([x,y]));

map.addLayer(new ol.layer.Vector({
    source: new ol.source.Vector({ features: features }),
}));


// setInterval(function(){
//     pos[1] += 15;
//     F_UserDot.setGeometry(new ol.geom.Point([pos[0],pos[1]]));
// }, 1500);
