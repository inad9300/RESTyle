<?php

namespace App\Http\Middleware;

use Closure;

class CorsMiddleware {
    public function handle($request, Closure $next) {
        $response = $next($request);

        $response->header('Access-Control-Allow-Origin', '*');
        // $response->header('Access-Control-Allow-Methods', 'DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT');
        // $response->header('Access-Control-Allow-Headers', 'Authorization, X-Requested-With, X-Auth-Token, Content-Type');
        // $response->header('Access-Control-Allow-Credentials', 'true');

        return $response;
    }
}