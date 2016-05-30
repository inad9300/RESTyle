angular.module('{{appModule}}').factory('{{resourceClass}}Srv', function ($http) {
    var srv = {};
    var resourceRoute = '{{resourceRoute}}/';
    var baseUrl = API_URL + resourceRoute;


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
    {{#each relations}}
    srv.find{{subresourceClassPlural}} = function (rid, filter) {
        return $http.get(baseUrl + rid + '/{{subresourceRoute}}/', { params: filter });
    };

    srv.findOne{{subresourceClass}} = function (rid, sid) {
        return $http.get(baseUrl + rid + '/{{subresourceRoute}}/' + sid);
    };

    srv.create{{subresourceClassPlural}} = function (rid, data) {
        return $http.post(baseUrl + rid + '/{{subresourceRoute}}/', data);
    };

    srv.delete{{subresourceClassPlural}} = function (rid, data) {
        return $http.delete(baseUrl + rid + '/{{subresourceRoute}}/', { data: data });
    };

    srv.delete{{subresourceClassPlural}} = function (rid, sid) {
        return $http.delete(baseUrl + rid + '/{{subresourceRoute}}/' + sid);
    };
    {{/each}}


    return srv;
});