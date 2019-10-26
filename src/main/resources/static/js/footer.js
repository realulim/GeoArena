/* global addRouter */

$(document).ready(function () {
    $("#randomPlace").on('click', getLandmark);
    $("#router").on('click', addRouter);
});

function getLandmark(event) {
    $.ajax({
	type: 'get',
	url: '/random/place',
	success: function (response) {
	    addPlaceMarker(response);
	}
    });
}
