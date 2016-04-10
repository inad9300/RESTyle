$app->group(['prefix' => {{prefix}}, 'namespace' => {{namespace}}, 'middleware' => [{{middleware}}]], function () use ($app) {
    {{routes}}
});