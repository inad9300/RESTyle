angular.module('{{appModule}}').controller('HomeCtrl', ['$scope', '{{userSrv}}', function ($scope, {{userSrv}}) {

    $scope.user = {{userSrv}}.current();

    if ($scope.user.username && $scope.user.password) {
        $scope.user.isLoggedIn = true;
    }

    $scope.login = function () {
        {{userSrv}}.login($scope.user.username, $scope.user.password);
        $scope.user.isLoggedIn = true;
    };

    $scope.logout = function () {
        {{userSrv}}.logout();
        $scope.user.isLoggedIn = false;
    };

}]);