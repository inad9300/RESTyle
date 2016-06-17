angular.module('{{appModule}}').factory('HttpInterceptor', ['$q', '$injector', function ($q, $injector) {
    function cut(str, max) {
        if (max <= 3) {
            throw 'Please, do not cut strings so short';
        }
        if (str.length > max) {
            return str.substr(0, max - 3) + '...';
        }
        return str;
    }

    return {
        request: function (config) {
            // Add authentication (needed for the API routes)
            var user = $injector.get('{{userSrv}}').current();
            if (user) {
                config.headers['Authorization'] = 'Basic ' + btoa(user.username + ':' + user.password);
            }
            return config;
        },

        responseError: function (rejection) {
            var ngToast = $injector.get('ngToast');
            ngToast.danger(rejection.statusText + (rejection.data.detail ? ': ' + cut(rejection.data.detail, 60) : ''));
            return $q.reject(rejection);
        }
    };
}]);