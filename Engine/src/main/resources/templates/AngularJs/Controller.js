angular.module('{{appModule}}').controller('{{controller}}', ['$scope', '$routeParams', 'ngToast', '{{service}}', function ($scope, $routeParams, ngToast, {{service}}) {

    var resourceId = $routeParams.id;

    // Individual view
    if (resourceId) {
        $scope.errors = [];
        $scope.resource = {};
        var originalResource = {};

        {{service}}.findOne(resourceId)
            .success(function (res) {
                $scope.resource = res;
                angular.copy(res, originalResource);
            });

        $scope.update = function () {
            // Calculate modified (and filled) fields only
            var newFields = {};
            for (var prop in $scope.resource) {
                if ($scope.resource.hasOwnProperty(prop)
                    && $scope.resource[prop]
                    && !angular.equals($scope.resource[prop], originalResource[prop])) {
                    newFields[prop] = $scope.resource[prop];
                }
            }

            if (Object.keys(newFields).length > 0) {
                {{service}}.patchOne(resourceId, newFields)
                    .success(function (res) {
                        $scope.errors = [];

                        ngToast.success('Resource updated');
                        $scope.resource = res;
                        angular.copy(res, originalResource);
                    })
                    .error(function (res) {
                        $scope.errors = [];

                        if (res['invalid-fields'])
                            angular.forEach(res['invalid-fields'], function (val, key) {
                                $scope.errors.push({
                                    key: key,
                                    value: val[0]
                                });
                            });
                                console.log($scope.errors)
                    });
            }
        };
    }
    // List view
    else {
        $scope.elements = [];

        {{service}}.find()
            .success(function (res) {
                $scope.elements = res._embedded.{{plural}};
            });

        $scope.delete = function (id) {
            {{service}}.deleteOne(id)
                .success(function () {
                    for (var i = 0, l = $scope.elements.length; i < l; ++i) {
                        if ($scope.elements[i].id === id) {
                            $scope.elements.splice(i, 1);
                            break;
                        }
                    }
                });
        };
    }

}]);