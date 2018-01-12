'use strict';

myApp.controller("bookingController", function ($scope, $timeout, $filter, $rootScope, roomsFactory, bookingFactory, cateringFactory) {

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
                        messageHandler.showErrorMessage('Błąd pobierania listy sal ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
    getRoomsList();

    var getMenu = function () {
        cateringFactory.getList()
            .then(
                function (response) {
                    $scope.menu = response.data;
                    angular.forEach($scope.menu, function (menuElement) {
                        menuElement.dishAmount = 0;
                    })
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania menu z zewnętrznego serwisu ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z zewnętrznym serwisem");
                    }
                });
    };
    getMenu();

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
                                messageHandler.showErrorMessage('Błąd pobierania zajętych dat rezerwacji sali ', error.data.message);
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

        var sendBooking = function(bookingToSave) {
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
                                    messageHandler.showErrorMessage('Błąd pobierania zajętych dat rezerwacji sali ', error.data.message);
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
        };

        var bookingToSave = {
            userLogin: $scope.userLogin,
            roomName: $scope.roomData.name,
            date: $filter('date')($scope.bookingToSave.bookingDate, 'yyyy-MM-dd'),
            description: $scope.bookingToSave.bookingDescription,
            orderId: '-1'
        };

        var orderToSend = {
            date: bookingToSave.date + 'T08:00:00.000Z',
            orderElement:
                [
                    // {'dishAmount':1,'dishId':1},
                ],
            user: null,
            guestUser: {'id':0,'firstname':'RentingRooms','lastname':'GuestUser','email':'orders@renting.com'},
            statusId: 1,
            address: $scope.roomData.address
        };
        var order = false;

        angular.forEach($scope.menu, function (menuItem) {
            if (menuItem.dishAmount > 0) {
                order = true;
                orderToSend.orderElement.push( {
                    dishAmount: menuItem.dishAmount,
                    dishId: menuItem.id || menuItem.Id
                })
            }
        });

        if (order) {
            cateringFactory.create(orderToSend)
                .then(
                    function (response) {
                        bookingToSave.orderId = '' + response.data.id;
                        sendBooking(bookingToSave);
                    },
                    function (error) {
                        angular.forEach($scope.menu, function (menuItem) {
                            menuItem.dishAmount = 0;
                        });
                        if (error.data) {
                            messageHandler.showErrorMessage('Błąd przy tworzeniu zamówienia ', error.data.message);
                        } else {
                            messageHandler.showErrorMessage('Błąd ', "Brak połączenia z zewnętrznym serwisem kateringu");
                        }
                    }
                )

        } else {
            sendBooking(bookingToSave);
        }
    }
});