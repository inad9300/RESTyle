<?php

{{> routes-root}}

$app->group(['prefix' => '{{prefix}}', 'namespace' => 'App\Http\Controllers', 'middleware' => []], function () use ($app) {

{{{routes}}}
});