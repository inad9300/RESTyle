<?php

namespace App\Http\Middleware;

use Closure;

class ContentLengthHeaderMiddleware {

    /**
     * Handle an incoming request.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \Closure  $next
     * @return mixed
     */
    public function handle($request, Closure $next) {
        $response = $next($request);
        $response->header('Content-Length', mb_strlen($response->getOriginalContent()));
        return $response;
    }
}