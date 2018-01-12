'use strict';

myApp.controller("roomsController", function ($scope, $timeout, roomsFactory, $rootScope, $interval) {

    $scope.canManageRooms = $rootScope.globalUser && $rootScope.globalUser.permissions && $rootScope.globalUser.permissions.canManageRooms;

    $scope.createNew = true;
    $scope.rooms = [];
    $scope.roomData = {
        description: '',
        price: 1,
        type: ''
    };

    var clearData = angular.copy($scope.roomData);
    var rawData = angular.copy($scope.roomData);

    var messageHandler = {};
    messageHandler.showErrorMessage = function (message, description) {
        if (message) {
            $scope.errorMessage = message;
        } else {
            $scope.errorMessage = 'Błąd';
        }
        if (description) {
            $scope.errorDescription = description;
        } else {
            $scope.errorDescription = '';
        }
        $scope.showErrorMessage = true;
        $(".alert-danger").hide().show('medium');
        $timeout(function () {
            $scope.showErrorMessage = false;
        }, 3000);
    };

    messageHandler.showSuccessMessage = function (message, description) {
        if (message) {
            $scope.successMessage = message;
        } else {
            $scope.successMessage = 'Sukces';
        }
        if (description) {
            $scope.successDescription = description;
        } else {
            $scope.successDescription = '';
        }
        $scope.showSuccessMessage = true;
        $(".alert-success").hide().show('medium');
        $timeout(function () {
            $scope.showSuccessMessage = false;
        }, 3000);
    };

    var refreshList = function () {
        roomsFactory.getList()
            .then(
                function (response) {
                    $scope.rooms = response.data.list;
                    if (!$scope.canManageRooms) {
                        $scope.changeSelected($scope.rooms[0]);
                    }
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania listy sal ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
    refreshList();

    $scope.changeSelected = function (name) {
        roomsFactory.getDetails(name)
            .then(
                function (response) {
                    $scope.roomData = response.data;
                    rawData = response.data;
                    $scope.createNew = false;
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania szczegółów sali ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.setNew = function () {
        $scope.createNew = true;
        $scope.roomData = angular.copy(clearData);
        delete($scope.roomData.name);
    };

    $scope.saveNewRoom = function () {
        roomsFactory.create($scope.roomData)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Dodano pomyślnie');
                    $scope.changeSelected($scope.roomData.name);
                    $scope.createNew = false;
                    refreshList();
                },
                function (error) {
                    if (error.data) {
                        if (error.data.message.includes('duplicate')) {
                            error.data.message = ' Pokój o podanej nazwie już istnieje';
                        }
                        messageHandler.showErrorMessage('Błąd przy tworzeniu sali', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.removeRoom = function () {
        roomsFactory.remove($scope.roomData.name)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Usunięto pomyślnie');
                    refreshList();
                    $scope.setNew();
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd przy usuwaniu sali ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.editRoom = function () {
        var newData = angular.copy($scope.roomData);
        delete(newData.name);
        roomsFactory.edit($scope.roomData.name, newData)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Edytowano pomyślnie');
                    refreshList();
                    $scope.changeSelected($scope.roomData.name);
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd podczas edycji sali', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.exists = function (givenObject) {
        return typeof givenObject !== 'undefined';
    };

    var interval = null;
    var carousel = null;

    $scope.initCarousel = function () {
        carousel = new Siema({
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

        interval = $interval(moveCarousel, 5000);
    };

    $scope.$on('$destroy', function () {
        if (interval) {
            $interval.cancel(interval);
            carousel.destroy();
        }
    })
});