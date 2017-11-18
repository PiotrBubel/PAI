'use strict';

myApp.factory("roomsFactory", function ($http, $rootScope, appConfig) {

    var headers = function () {
        return {headers: {'Auth-Token': $rootScope.token}};
    };

    return {
        getList: function () {
            return $http.get(appConfig.apiAddress + '/rooms', headers());
        },
        getDetails: function (name) {
            return $http.get(appConfig.apiAddress + '/rooms/' + name, headers());
        },
        edit: function (name, roomData) {
            return $http.put(appConfig.apiAddress + '/rooms/' + name, {room: roomData}, headers());
        },
        create: function (roomData) {
            return $http.post(appConfig.apiAddress + '/rooms', {room: roomData}, headers());
        },
        remove: function (name) {
            return $http.delete(appConfig.apiAddress + '/rooms/' + name, headers());
        }
    };
});