<?php

namespace App\Http\Middleware;

use Closure;
use GrahamCampbell\Throttle\Data;
use GrahamCampbell\Throttle\Throttle;
// use GrahamCampbell\Throttle\Factories\FactoryInterface;
// use Symfony\Component\HttpKernel\Exception\TooManyRequestsHttpException;

class RateMiddleware {
    /**
     * The throttle instance.
     *
     * @var \GrahamCampbell\Throttle\Throttle
     */
    protected $throttle;

    /**
     * Create a new throttle middleware instance.
     *
     * @param \GrahamCampbell\Throttle\Throttle $throttle
     *
     * @return void
     */
    public function __construct(Throttle $throttle) {
        $this->throttle = $throttle;
    }

    /**
     * Handle an incoming request.
     *
     * @param \Illuminate\Http\Request $request
     * @param \Closure                 $next
     * @param int                      $maxRequests
     * @param int                      $minutes
     *
     * @return mixed
     */
    public function handle($request, Closure $next, $maxRequests = 30, $minutes = 1) {
        // dd($this->timeToUnlock($this->throttle->getFactory()));

        if (!$this->throttle->attempt($request, $maxRequests, $minutes)) {
            // throw new TooManyRequestsHttpException($minutes * 60, 'Rate limit exceeded.');
            $response = response('Rate limit exceeded.', 429);
            $response->header('Retry-After', $minutes * 60);
            $this->throttle->clear($request); // TODO: call appropriately
        } else {
            $response = $next($request);
        }

        $remainingHits = $maxRequests - $this->throttle->count($request);
        if ($remainingHits < 0) {
            $remainingHits = 0;
        }

        $response->header('X-Rate-Limit-Limit', $maxRequests);
        $response->header('X-Rate-Limit-Remaining', $remainingHits);

        return $response;
    }

    /**
     * Get time the user needs to wait. Idea from
     * https://github.com/GrahamCampbell/Laravel-Throttle/issues/29
     *
     * @return int
     */
    /* private function timeToUnlock(FactoryInterface $cache) {
        $parts = array_slice(str_split($hash = md5($cache->key), 2), 0, 2);
        $dir = $cache->store->getDirectory();
        $path = $dir.'/'.implode('/', $parts).'/'.$hash;
        $expire = substr($cache->store->getFilesystem()->get($path), 0, 10);
        $timeToUnlock = intval(ceil(($expire - time()) / 60));
        return $timeToUnlock;
    } */
}