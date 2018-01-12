'use strict';

myApp.factory("cateringFactory", function ($http, $rootScope, appConfig) {

    return {
        getList: function () {
            return $http.get(appConfig.cateringApiAddress + '/menu');
        },
        getStatus: function (id) {
            return $http.get(appConfig.cateringApiAddress + '/orders/' + id);
        },
        create: function (orderData) {
            return $http.post(appConfig.cateringApiAddress + '/orders', orderData);
        },
        getAllStatuses: function () {
            return $http.get(appConfig.cateringApiAddress + '/status');
        }
    };
});