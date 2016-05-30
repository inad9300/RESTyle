// Global, convenient variables
var API_URL = 'http://localhost:5555/api/';

// Main module and its dependencies
angular.module('', [
    'ngRoute',
    'ngSanitize',
    'ngToast'
])

// App routes
.config(['$routeProvider', 'ngToastProvider', '$httpProvider', function ($routeProvider, ngToastProvider, $httpProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'home/home.html',
            controller: 'HomeCtrl',
        })
        .otherwise({ redirectTo: '/' });

    $httpProvider.interceptors.push('HttpErrorsInterceptor');

    ngToastProvider.configure({
    	timeout: 8000,
    	dismissButton: true,
    	combineDuplications: true,
    	animation: 'slide' // or 'fade'
    });
}]);