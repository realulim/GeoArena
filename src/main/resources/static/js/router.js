/* global map, L, hereAppId, hereAppCode, herePlacesGeocodeUrl, hereRoutingUrl, Swal  */

var geocoder = new L.Control.Geocoder.HEREPLACES({
    app_id: hereAppId, app_code: hereAppCode,
    basePath: herePlacesGeocodeUrl,
    geocodingQueryParams: {at: '52,13', tf: 'plain'}
});

var router = new L.Routing.Here(hereAppId, hereAppCode, {serviceUrl: hereRoutingUrl});
var routingErrorText = 'No Route exists via {0} Please choose different waypoints.';

var selectedRoute = null;
var routeName = null;
var saveButton = null;

var geocoderUI = function (waypointIndex, totalWaypoints, options) {
    let container = L.DomUtil.create('div', 'leaflet-routing-geocoder');
    let input = L.DomUtil.create('input', 'geocoder-input', container);
    input.disabled = !options.addWaypoints;
    let remove = options.addWaypoints ? L.DomUtil.create('span', 'leaflet-routing-remove-waypoint', container) : undefined;
    if (totalWaypoints === (waypointIndex + 1)) {
	// last Waypoint
	let closeButton = L.DomUtil.create('button', 'leaflet-routing-close', container);
	closeButton.innerHTML = 'Cancel';
	L.DomEvent.on(closeButton, 'click', function (event) {
	    event.preventDefault();
	    removeRouter();
	}, this);

	saveButton = L.DomUtil.create('button', 'leaflet-routing-save', container);
	saveButton.innerHTML = 'Save Route';
	saveButton.disabled = true;
	L.DomEvent.on(saveButton, 'click', function(event) {
	    event.preventDefault();
	    if (selectedRoute) {
		let geometry = routeToLineString(selectedRoute);
		let geoJsonFeature = {type: "feature", properties: {}, geometry: geometry};
		let shape = JSON.stringify(geoJsonFeature.geometry)
		createRouteLayer(shape, routeName);
		saveRoute(shape, routeName);
		removeRouter();
	    }
	}, this);
    }
    return {
	container: container,
	input: input,
	closeButton: remove
    };
};

var plan = new L.Routing.Plan([], {
    geocoder: geocoder,
    createGeocoder: geocoderUI
});

var routingControl = new L.Routing.control({
    waypoints: [],
    router: router,
    plan: plan
}).on('routeselected', function (e) {
    selectedRoute = e.route;
    let waypointCount = selectedRoute.inputWaypoints.length;
    let firstPart = selectedRoute.inputWaypoints[0].name;
    let lastPart = selectedRoute.inputWaypoints[waypointCount - 1].name;
    routeName = firstPart.substr(0, firstPart.indexOf(",")) + " - " + lastPart.substr(0, lastPart.indexOf(","));
    let line = L.Routing.line(selectedRoute);
    map.fitBounds(line.getBounds());
}).on('routesfound', function (e) {
    saveButton.disabled = false;
}).on('routingerror', function (e) {
    let waypoints = '';
    if (e.target.options && e.target.options.plan && e.target.options.plan._waypoints) {
	$.each(e.target.options.plan._waypoints, function (index, value) {
	    waypoints += '<li>' + value.name + "</li>";
	});
	waypoints = '<p><ul>' + waypoints + '</ul></p>';
    }
    Swal.fire({
	type: 'error',
	title: 'Routing Error',
	html: routingErrorText.replace('{0}', waypoints)});
});

function routeToLineString(route) {
    var lineCoordinates = [], i, latLng;

    for (i = 0; i < route.coordinates.length; i++) {
	latLng = L.latLng(route.coordinates[i]);
	lineCoordinates.push([latLng.lng, latLng.lat]);
    }

    return {
	type: 'LineString',
	coordinates: lineCoordinates
    };
}

var updateSearchAtParameter = function (e) {
    let center = this.getCenter();
    geocoder.options.geocodingQueryParams.at = center.lat + "," + center.lng;
};


window.addRouter = function () {
    if (map) {
        removeRouter();
	routingControl.addTo(map);
	plan.setWaypoints([]);
	map.on('zoomend', updateSearchAtParameter);
	map.on('moveend', updateSearchAtParameter);
    }
};

window.removeRouter = function () {
    if (map) {
	map.off('zoomend', updateSearchAtParameter);
	map.off('moveend', updateSearchAtParameter);
	map.removeControl(routingControl);
	selectedRoute = null;
    }
};

function routeToLineString(route) {
    var lineCoordinates = [], i, latLng;

    for (i = 0; i < route.coordinates.length; i++) {
	latLng = L.latLng(route.coordinates[i]);
	lineCoordinates.push([latLng.lng, latLng.lat]);
    }

    return {
	type: 'LineString',
	coordinates: lineCoordinates
    };
}
