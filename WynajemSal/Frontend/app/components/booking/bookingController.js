'use strict';

myApp.controller("bookingController", function ($scope, $timeout, $filter, $rootScope, roomsFactory, bookingFactory) {


    $scope.userLoggedIn = $rootScope.globalUser && $rootScope.globalUser.login;
    if ($scope.userLoggedIn) {
        $scope.userLogin = $rootScope.globalUser.login;
    }

    $scope.createNew = true;
    $scope.roomData = {
        name: '',
        description: '',
        price: 1
    };
    $scope.rooms = [];
    $scope.bookingToSave = {
        bookingDate: new Date(),
        bookingDescription: ''
    };
    $scope.minDate = new Date();
    var notAvailableDates = [];

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

    var getRoomsList = function () {
        roomsFactory.getList()
            .then(
                function (response) {
                    $scope.rooms = response.data.list;
                    $scope.changeSelected($scope.rooms[0]);
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania listy pokojów ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
    getRoomsList();

    $scope.changeSelected = function (name) {
        roomsFactory.getDetails(name)
            .then(
                function (response) {
                    $scope.roomData = response.data;
                    bookingFactory.getDates($scope.roomData.name, $filter('date')(new Date(), 'yyyy')).then(
                        function (response) {
                            notAvailableDates = response.data.list;
                        },
                        function (error) {
                            if (error.data) {
                                messageHandler.showErrorMessage('Błąd pobierania zajętych dat rezerwacji pokoju ', error.data.message);
                            } else {
                                messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                            }
                        });
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania szczegółów rezerwacji ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.dateFilter = function (date) {
        var stringDate = $filter('date')(date, 'yyyy-MM-dd');
        for (var i = 0; i < notAvailableDates.length; i++) {
            if (stringDate === notAvailableDates[i]) {
                return false;
            }
        }
        return true;
    };

    $scope.saveBooking = function () {
        var bookingToSave = {
            userLogin: $scope.userLogin,
            roomName: $scope.roomData.name,
            date: $filter('date')($scope.bookingToSave.bookingDate, 'yyyy-MM-dd'),
            description: $scope.bookingToSave.bookingDescription
        };
        bookingFactory.create(bookingToSave)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Dodano pomyślnie');
                    $scope.bookingToSave.bookingDescription = "";
                    $scope.bookingToSave.bookingDate = new Date();
                    bookingFactory.getDates($scope.roomData.name, $filter('date')(new Date(), 'yyyy')).then(
                        function (response) {
                            notAvailableDates = response.data.list;
                        },
                        function (error) {
                            if (error.data) {
                                messageHandler.showErrorMessage('Błąd pobierania zajętych dat rezerwacji pokoju ', error.data.message);
                            } else {
                                messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                            }
                        });
                },
                function (error) {
                    if (error.data) {
                        if (error.data.message.includes('duplicate')) {
                            error.data.message = ' Pokój jest już zarezerwowana w podanym dniu.';
                        }
                        messageHandler.showErrorMessage('Błąd przy tworzeniu rezerwacji ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    }
});