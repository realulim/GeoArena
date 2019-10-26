(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
(function (global){
"use strict";

var _leaflet = _interopRequireDefault((typeof window !== "undefined" ? window['L'] : typeof global !== "undefined" ? global['L'] : null));

var _util = require("../util");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

module.exports = {
  "class": _leaflet["default"].Class.extend({
    options: {
      basePath: 'https://places.cit.api.here.com/places/v1/',
      suggestEndpoint: 'autosuggest',
      searchEndpoint: 'discover/search',
      geocodingQueryParams: {},
      reverseQueryParams: {}
    },
    initialize: function initialize(options) {
      _leaflet["default"].Util.setOptions(this, options);
    },
    geocode: function geocode(query, cb, context) {
      var params = {
        q: query,
        app_id: this.options.app_id,
        app_code: this.options.app_code
      };
      params = _leaflet["default"].Util.extend(params, this.options.geocodingQueryParams);
      callGetJson(params, cb, context, this.options);
    },
    reverse: function reverse(location, scale, cb, context) {
      var query = "".concat(encodeURIComponent(location.lat), ",").concat(encodeURIComponent(location.lng));
      var params = {
        q: query,
        at: query,
        tf: 'html',
        app_id: this.options.app_id,
        app_code: this.options.app_code
      };
      params = _leaflet["default"].Util.extend(params, this.options.reverseQueryParams);
      callGetJson(params, cb, context, this.options);
    },
    suggest: function suggest(query, cb, context) {
      var params = {
        q: query,
        app_id: this.options.app_id,
        app_code: this.options.app_code,
        result_types: 'place, address'
      };
      params = _leaflet["default"].Util.extend(params, this.options.geocodingQueryParams);
      (0, _util.getJSON)(this.options.basePath + this.options.suggestEndpoint, params, function (data) {
        cb.call(context, data.results.map(function (result) {
          return {
            name: "".concat(result.title, ", ").concat(result.vicinity || ''),
            html: "".concat(result.highlightedTitle, ", ").concat(result.highlightedVicinity),
            bbox: convertHereBoundingBoxToLatLngBounds(result),
            center: _leaflet["default"].latLng(result.position),
            href: result.href
          };
        }));
      });
    }
  }),
  factory: function factory(options) {
    return new _leaflet["default"].Control.Geocoder.HEREPLACES(options);
  }
};

var convertHereBoundingBoxToLatLngBounds = function convertHereBoundingBoxToLatLngBounds(result) {
  if (result.hasOwnProperty('bbox')) {
    return _leaflet["default"].latLngBounds(_leaflet["default"].latLng(result.bbox[1], result.bbox[0]), _leaflet["default"].latLng(result.bbox[3], result.bbox[2]));
  } else {
    return _leaflet["default"].latLngBounds(_leaflet["default"].latLng(result.position), _leaflet["default"].latLng(result.position));
  }
};

var callGetJson = function callGetJson(params, cb, context, options) {
  (0, _util.getJSON)(options.basePath + options.searchEndpoint, params, function (data) {
    cb.call(context, data.results.items.map(function (item) {
      return {
        name: "".concat(item.title, ", ").concat(item.vicinity || ''),
        bbox: convertHereBoundingBoxToLatLngBounds(item),
        center: _leaflet["default"].latLng(item.position),
        href: item.href
      };
    }));
  });
};

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"../util":3}],2:[function(require,module,exports){
(function (global){
"use strict";

var _leaflet = _interopRequireDefault((typeof window !== "undefined" ? window['L'] : typeof global !== "undefined" ? global['L'] : null));

var _places = _interopRequireDefault(require("./geocoders/places"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

_leaflet["default"].Util.extend(_leaflet["default"].Control.Geocoder, {
  HEREPLACES: _places["default"]["class"],
  hereplaces: _places["default"].factory
});

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"./geocoders/places":1}],3:[function(require,module,exports){
(function (global){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.htmlEscape = htmlEscape;
exports.jsonp = jsonp;
exports.getJSON = getJSON;
exports.template = template;
exports.getParamString = getParamString;

var _leaflet = _interopRequireDefault((typeof window !== "undefined" ? window['L'] : typeof global !== "undefined" ? global['L'] : null));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var lastCallbackId = 0; // Adapted from handlebars.js
// https://github.com/wycats/handlebars.js/

var badChars = /[&<>"'`]/g;
var possible = /[&<>"'`]/;
var escape = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#x27;',
  '`': '&#x60;'
};

function escapeChar(chr) {
  return escape[chr];
}

function htmlEscape(string) {
  if (string == null) {
    return '';
  } else if (!string) {
    return string + '';
  } // Force a string conversion as this will be done by the append regardless and
  // the regex test will do this transparently behind the scenes, causing issues if
  // an object's to string has escaped characters in it.


  string = '' + string;

  if (!possible.test(string)) {
    return string;
  }

  return string.replace(badChars, escapeChar);
}

function jsonp(url, params, callback, context, jsonpParam) {
  var callbackId = '_l_geocoder_' + lastCallbackId++;
  params[jsonpParam || 'callback'] = callbackId;
  window[callbackId] = _leaflet["default"].Util.bind(callback, context);
  var script = document.createElement('script');
  script.type = 'text/javascript';
  script.src = url + getParamString(params);
  script.id = callbackId;
  document.getElementsByTagName('head')[0].appendChild(script);
}

function getJSON(url, params, callback) {
  var xmlHttp = new XMLHttpRequest();

  xmlHttp.onreadystatechange = function () {
    if (xmlHttp.readyState !== 4) {
      return;
    }

    if (xmlHttp.status !== 200 && xmlHttp.status !== 304) {
      callback('');
      return;
    }

    callback(JSON.parse(xmlHttp.response));
  };

  xmlHttp.open('GET', url + getParamString(params), true);
  xmlHttp.setRequestHeader('Accept', 'application/json');
  xmlHttp.send(null);
}

function template(str, data) {
  return str.replace(/\{ *([\w_]+) *\}/g, function (str, key) {
    var value = data[key];

    if (value === undefined) {
      value = '';
    } else if (typeof value === 'function') {
      value = value(data);
    }

    return htmlEscape(value);
  });
}

function getParamString(obj, existingUrl, uppercase) {
  var params = [];

  for (var i in obj) {
    var key = encodeURIComponent(uppercase ? i.toUpperCase() : i);
    var value = obj[i];

    if (!_leaflet["default"].Util.isArray(value)) {
      params.push(key + '=' + encodeURIComponent(value));
    } else {
      for (var j = 0; j < value.length; j++) {
        params.push(key + '=' + encodeURIComponent(value[j]));
      }
    }
  }

  return (!existingUrl || existingUrl.indexOf('?') === -1 ? '?' : '&') + params.join('&');
}

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{}]},{},[2]);
