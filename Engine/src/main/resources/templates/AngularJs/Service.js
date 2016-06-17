angular.module('{{appModule}}').factory('{{service}}', ['$http', '$rootScope', function ($http, $rootScope) {

    var srv = {};
    var resourceRoute = '{{path}}/';
    var baseUrl = API_URL + resourceRoute;
    {{#if isUser}}

    // User-related actions
    // --------------------

    var CURR_USER_KEY = '__current_user_credentials__';

    srv.current = function () {
        var userRaw = localStorage.getItem(CURR_USER_KEY);
        if (typeof userRaw !== 'string')
            return {};

        var userArr = atob(userRaw).split(':');
        return {
            username: userArr[0],
            password: userArr[1]
        };
    };

    srv.login = function (username, password) {
        localStorage.setItem(CURR_USER_KEY, btoa(username + ':' + password));
        $rootScope.$broadcast('user_logged_in', { username: username });
    };

    srv.logout = function () {
        var oldUsername = srv.current().username;
        localStorage.removeItem(CURR_USER_KEY);
        $rootScope.$broadcast('user_logged_out', { username: oldUsername });
    };
    {{/if}}

    // Resource-related actions
    // ------------------------

    srv.find = function (filter) {
        return $http.get(baseUrl, { params: filter });
    };

    srv.findOne = function (id) {
        return $http.get(baseUrl + id);
    };

    srv.create = function (data) {
        return $http.post(baseUrl, data);
    };

    srv.update = function (data) {
        return $http.put(baseUrl, data);
    };

    srv.updateOne = function (id, data) {
        return $http.put(baseUrl + id, data);
    };

    srv.patch = function (data) {
        return $http.path(baseUrl, data);
    };

    srv.patchOne = function (id, data) {
        return $http.patch(baseUrl + id, data);
    };

    srv.delete = function (data) {
        return $http.delete(baseUrl, { data: data });
    };

    srv.deleteOne = function (id) {
        return $http.delete(baseUrl + id);
    };

    {{#each resourceFiles}}
    srv.find{{camelName}} = function (id) {
        return $http.get(baseUrl + id + '/files/' + {{name}});
    };

    srv.create{{camelName}} = function (id, data) {
        return $http.post(baseUrl + id + '/files/' + {{name}}, data);
    };

    srv.update{{camelName}} = function (id, data) {
        return $http.put(baseUrl + id + '/files/' + {{name}}, data);
    };

    srv.patch{{camelName}} = function (id, data) {
        return $http.patch(baseUrl + id + '/files/' + {{name}}, data);
    };

    srv.delete{{camelName}} = function (id) {
        return $http.delete(baseUrl + id + '/files/' + {{name}});
    };{{/each}}


    // Relation-related actions
    // ------------------------

    // TODO
    {{#each relations}}
    srv.find{{subresourceClassPlural}} = function (rid, filter) {
        return $http.get(baseUrl + rid + '/{{subresourcePath}}/', { params: filter });
    };

    srv.findOne{{subresourceClass}} = function (rid, sid) {
        return $http.get(baseUrl + rid + '/{{subresourcePath}}/' + sid);
    };

    srv.create{{subresourceClassPlural}} = function (rid, data) {
        return $http.post(baseUrl + rid + '/{{subresourcePath}}/', data);
    };

    srv.delete{{subresourceClassPlural}} = function (rid, data) {
        return $http.delete(baseUrl + rid + '/{{subresourcePath}}/', { data: data });
    };

    srv.delete{{subresourceClassPlural}} = function (rid, sid) {
        return $http.delete(baseUrl + rid + '/{{subresourcePath}}/' + sid);
    };
    {{/each}}


    return srv;
}]);