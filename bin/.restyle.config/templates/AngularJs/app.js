// Global, convenient variables
var API_URL = '{{apiUrl}}';

// Main module and its dependencies
angular.module('{{appModule}}', [
    'ngRoute',
    'ngSanitize',
    'ngToast'
])

// Application-wide configuration, specially to define the application routes
.config(['$routeProvider', 'ngToastProvider', '$httpProvider', function ($routeProvider, ngToastProvider, $httpProvider) {
    var pagesDir = 'pages/';

    $routeProvider
        .when('/', {
            templateUrl: pagesDir + 'home/home.html',
            controller: 'HomeCtrl'
        })
        {{#each resources}}.when('/{{path}}', {
            templateUrl: pagesDir + '{{folder}}/{{listView}}',
            controller: '{{controller}}'
        })
        .when('/{{path}}/:id', {
            templateUrl: pagesDir + '{{folder}}/{{singleView}}',
            controller: '{{controller}}'
        })
        {{/each}}.otherwise({ redirectTo: '/' });

    $httpProvider.interceptors.push('HttpInterceptor');

    ngToastProvider.configure({
    	timeout: 4000,
    	dismissButton: true,
    	combineDuplications: true,
    	animation: 'slide' // or 'fade'
    });
}]);