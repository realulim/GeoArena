/* global L, hereAppId, hereAppCode, street, terrain, satellite, defaultLayer */

window.hereAppId = $('meta[name="_hereAppId"]').attr('content');
window.hereAppCode = $('meta[name="_hereAppCode"]').attr('content');
window.herePlacesGeocodeUrl = $('meta[name="_herePlacesGeocodeUrl"]').attr('content');
window.hereRoutingUrl = $('meta[name="_hereRoutingUrl"]').attr('content');

let attrib = '© <a href="https://legal.here.com/terms" target="here">HERE maps</a>';
let street = new L.tileLayer.here({appId: hereAppId, appCode: hereAppCode, scheme: 'normal.day', attribution: attrib});
let terrain = new L.tileLayer.here({appId: hereAppId, appCode: hereAppCode, scheme: 'terrain.day', attribution: attrib});
let satellite = new L.tileLayer.here({appId: hereAppId, appCode: hereAppCode, scheme: 'satellite.day', attribution: attrib});
let topo = new L.tileLayer('http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
    minZoom: 2,
    maxZoom: 17,
    attribution: 'Map data: &copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap</a>-Mitwirkende, SRTM | Kartendarstellung: © <a href="http://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)'
});

let mountainsLayerGroup = new L.featureGroup();
let placesLayerGroup = new L.featureGroup();
let routesLayerGroup = new L.featureGroup();
let masterLayerGroup = new L.layerGroup();
masterLayerGroup.addLayer(placesLayerGroup);
masterLayerGroup.addLayer(routesLayerGroup);

let map = new L.Map('map', {
    center: [20.0, 20.0],
    zoom: 2,
    defaultExtentControl: true,
    scrollWheelZoom: true,
    fullscreenControl: true,
    fullscreenControlOptions: {
	position: 'topleft'
    },
    layers: [terrain, masterLayerGroup]
});

var baseLayers = {
    "Street": street,
    "Terrain": terrain,
    "Topography": topo,
    "Satellite": satellite
};

var overlays = {
    "Mountains": mountainsLayerGroup
};

var targetIcon = L.icon({
    iconUrl: '/img/target.png',
    iconSize: [48, 47], // size of the icon
    iconAnchor: [18, 28], // point of the icon which will correspond to marker's location
    popupAnchor: [0, -18] // point from which the popup should open relative to the iconAnchor
});

var mountainIcon = L.icon({
    iconUrl: '/img/mountain.png',
    iconSize: [36, 48], // size of the icon
    iconAnchor: [17, 47], // point of the icon which will correspond to marker's location
    popupAnchor: [0, -50] // point from which the popup should open relative to the iconAnchor
});

var placeIcon = L.icon({
    iconUrl: '/img/house.png',
    iconSize: [48, 46], // size of the icon
    iconAnchor: [24, 45], // point of the icon which will correspond to marker's location
    popupAnchor: [0, -50] // point from which the popup should open relative to the iconAnchor
});

var noPlaceIcon = L.icon({
    iconUrl: '/img/noplace.png',
    iconSize: [48, 48], // size of the icon
    iconAnchor: [24, 45], // point of the icon which will correspond to marker's location
    popupAnchor: [0, -50] // point from which the popup should open relative to the iconAnchor
});

L.control.layers(baseLayers, overlays).addTo(map);
map.on('overlayadd', function(overlay) {
    if (overlay.name === 'Mountains') {
	$.ajax({
	    type: "POST",
	    url: '/config/showmountains/true'
	});
    }
}).on('overlayremove', function(overlay) {
    if (overlay.name === 'Mountains') {
	$.ajax({
	    type: "POST",
	    url: '/config/showmountains/false'
	});
    }
});

window.createCountryLayer = function (geometry) {
    if (geometry && (geometry.type === 'Polygon' || geometry.type === 'MultiPolygon')) {
	var countryLayer = new L.geoJson(geometry, {
	    style: {color: 'blue', weight: 2}
	});
	masterLayerGroup.addLayer(countryLayer);
	map.fitBounds(countryLayer.getBounds());
	return countryLayer;
    }
};

window.addPlaceMarker = function(location) {
    let placeMarker = null;
    if (location && location.shape) {
	let geometry = JSON.parse(location.shape);
	if (geometry && geometry.type && geometry.type === 'Point') {
	    placeMarker = createMarker(geometry, location.title ? placeIcon : noPlaceIcon);
	    placeMarker.on('click', function () {
		map.flyToBounds(placeMarker.getBounds(), {duration: 0.5});
	    });
	    if (location.title && location.title.length) {
		placeMarker.bindPopup("<div><b>" + location.title + "</b></div><div>" + location.text + "</div>");
	    }
	    else {
		placeMarker.bindPopup("<b>Nothing to see here</b><br>" + location.text);
	    }
	    placesLayerGroup.addLayer(placeMarker);
	    placeMarker.openPopup();
	}
    }
    return placeMarker;
};

window.addLocationMarkerToLayer = function (layer, location) {
    let locationMarker = null;
    if (layer && location && location.shape) {
	let geometry = JSON.parse(location.shape);
	if (geometry && geometry.type && geometry.type === 'Point') {
	    locationMarker = createMarker(geometry, targetIcon);
	    if (location.title && location.title.length) {
		locationMarker.bindPopup("<div><b>" + location.title + "</b></div><div>" + location.text + "</div>");
	    }
	    locationMarker.on('click', function () {
		map.flyToBounds(locationMarker.getBounds(), {duration: 0.5, maxZoom: 11});
	    });
	    layer.addLayer(locationMarker);
	    locationMarker.openPopup();
	}
    }
    return locationMarker;
};

window.addLocationMarkerToRoutesLayerGroupAndZoom = function (location) {
    addLocationMarkerToLayer(routesLayerGroup, location);
    map.flyToBounds(routesLayerGroup.getBounds(), {duration: 0.5, maxZoom: 11});
};

function createMarker(geometry, icon) {
    return L.geoJSON(geometry, {
	pointToLayer: function (feature, latlng) {
	    return L.marker(latlng, {
		icon: icon
	    });
	}
    });
}

window.createMountainLayer = function (title, byLine, shape) {
    let mountainLayer = null;
    if (shape) {
	let geometry = JSON.parse(shape);
	if (geometry) {
	    mountainLayer = createMountainMarker(geometry);
	    mountainLayer.bindPopup("<div><b>" + title + "</b></div><div>" + byLine + " m</div>");
	    mountainsLayerGroup.addLayer(mountainLayer);
	}
	return mountainLayer;
    }
};

window.showMountainLayer = function () {
    masterLayerGroup.addLayer(mountainsLayerGroup);
};

function createMountainMarker(geometry) {
    return L.geoJSON(geometry, {
	pointToLayer: function (feature, latlng) {
	    return L.marker(latlng, {
		icon: mountainIcon
	    });
	}
    });
}

var defaultLineStyle = { color: 'crimson', weight: 3, dashArray: null, bubblingMouseEvents: false };

window.createRouteLayer = function(shape, routeName) {
    let geometry = JSON.parse(shape);
    if (geometry && geometry.type && geometry.type === 'LineString') {
	let routeLayer = L.geoJson(geometry, {
	    style: defaultLineStyle
	});
	routeLayer.bindPopup("<div><b>" + routeName + "</b></div>");
	createRouteLayerClickHandlers(routeLayer);
	routesLayerGroup.addLayer(routeLayer);
    }
};

function createRouteLayerClickHandlers(layer) {
    layer.on('mouseover', function (event) {
	layer.setStyle({
	    color: 'dodgerblue',
	    weight: 6
	});
	var popup = event.target.getPopup();
	popup.setLatLng(event.latlng).openOn(map);
    });
    layer.on('mouseout', function () {
	layer.closePopup();
	layer.setStyle(defaultLineStyle);
    });
};

window.saveRoute = function(shape, routeName) {
    $.ajax({
	type: "POST",
	url: '/route/save',
	data: {shape: shape, name: routeName},
	success: function (response) {
	    if (!response) console.log('route not saved...');
	},
	error: function(data) {
	    console.log(data);
	}
    });
};
