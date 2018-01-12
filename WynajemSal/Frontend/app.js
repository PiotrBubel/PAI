'use strict';

var myApp = angular.module('myApp', ['ngRoute', 'ngMessages', 'ngCookies', 'ngMaterial'])

    .config(['$routeProvider', '$locationProvider',
        function ($routeProvider, $locationProvider) {
            $routeProvider.when('/login', {
                controller: 'loginController',
                templateUrl: '/app/components/login/loginView.html'
            }).when('/rooms', {
                controller: 'roomsController',
                templateUrl: '/app/components/rooms/roomsView.html'
            }).when('/bookings', {
                controller: 'bookingController',
                templateUrl: '/app/components/booking/bookingCreateView.html'
            }).when('/bookingsManagement', {
                controller: 'bookingManagementController',
                templateUrl: '/app/components/booking/bookingManagementView.html'
            }).when('/users', {
                controller: 'usersController',
                templateUrl: '/app/components/users/usersView.html'
            }).when('/account', {
                controller: 'accountController',
                templateUrl: '/app/components/account/accountView.html'
            }).when('/main', {
                controller: 'navbarController',
                templateUrl: '/app/components/main/mainView.html'
            }).when('/about', {
                controller: 'aboutController',
                templateUrl: '/app/components/about/aboutView.html'
            }).otherwise({
                redirectTo: '/main'
            });
            $locationProvider.html5Mode(false).hashPrefix('!');
        }]);

myApp.controller("navbarController", function ($http, $scope, $cookies, $location, $rootScope, $interval) {
    $rootScope.globalUser = $cookies.getObject('user');
    $rootScope.token = $cookies.getObject('token');
    $http.defaults.headers.post["Content-Type"] = "text/plain";


    $scope.logout = function () {
        $cookies.remove('user');
        $cookies.remove('token');
        delete($rootScope.globalUser);
        delete($rootScope.token);
    };

    $scope.initCarousel = function () {
        var carousel = new Siema({
            selector: '.siema',
            duration: 200,
            easing: 'ease-out',
            perPage: 1,
            startIndex: 0,
            draggable: true,
            multipleDrag: true,
            threshold: 20,
            loop: true
        });

        var moveCarousel = function () {
            carousel.next();
        };

        $interval(moveCarousel, 5000);
    };
});