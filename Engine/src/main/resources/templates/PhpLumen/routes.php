<?php
{{unauthRoutes}}

$app->group(['prefix' => '{{prefix}}', 'middleware' => ['auth'], function () use ($app) {

{{{authRoutes}}}
});