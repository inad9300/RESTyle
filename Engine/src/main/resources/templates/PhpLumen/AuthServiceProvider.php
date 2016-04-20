<?php

namespace App\Providers;

use Illuminate\Support\Facades\Gate;
use Illuminate\Support\ServiceProvider;

{{#each resources}}use App\Models{{classBack}};
{{/each}}
{{#each resources}}use App\Policies{{classBack}}Policy;
{{/each}}

class AuthServiceProvider extends ServiceProvider {

    /**
     * The policy mappings for the application.
     *
     * @var array
     */
    protected $policies = [
        {{#each resources}}{{class}}::class => {{class}}Policy::class,
        {{/each}}
    ];

    /**
     * Register any application services.
     *
     * @return void
     */
    public function register(Gate $gate) {
        $this->registerPolicies($gate);
    }

    /**
     * Boot the authentication services for the application.
     *
     * @return void
     */
    public function boot() {
        // Here you may define how you wish users to be authenticated for your Lumen
        // application. The callback which receives the incoming request instance
        // should return either a {{userClass}} instance or null. You're free to obtain
        // the User instance via an API token or any other method necessary.

        $this->app['auth']->viaRequest('api', function ($request) {
            // NOTE for the future: user name is forbidden to contain colon (':') in Basic Authentication

            if (!isset($_SERVER['PHP_AUTH_USER'])) {
                // header('WWW-Authenticate: Basic realm="My Realm"');
                // header('HTTP/1.0 401 Unauthorized');
                return null;
            }

            $user = {{userClass}}::where('username', $_SERVER['PHP_AUTH_USER'])->first();

            if (hash_equals($user->password, crypt($_SERVER['PHP_AUTH_PW'], $user->password))) {
                return $user;
            }
            return null;
        });

        // $this->app['auth']->viaRequest('api', function ($request) {
        //     if ($request->input('api_token')) {
        //         return {{userClass}}::where('api_token', $request->input('api_token'))->first();
        //     }
        // });
    }
}