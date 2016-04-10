// {{resourceName}}

$app->get('/{{resourceRoute}}', '{{resourceClass}}Controller@getMany');
$app->get('/{{resourceRoute}}/{id}', '{{resourceClass}}Controller@getOne');

$app->post('/{{resourceRoute}}', '{{resourceClass}}Controller@create');

$app->put('/{{resourceRoute}}', '{{resourceClass}}Controller@fullUpdateMany');
$app->put('/{{resourceRoute}}/{id}', '{{resourceClass}}Controller@fullUpdateOne');

$app->patch('/{{resourceRoute}}', '{{resourceClass}}Controller@partialUpdateMany');
$app->patch('/{{resourceRoute}}/{id}', '{{resourceClass}}Controller@partialUpdateOne');

$app->delete('/{{resourceRoute}}', '{{resourceClass}}Controller@deleteMany');
$app->delete('/{{resourceRoute}}/{id}', '{{resourceClass}}Controller@deleteOne');

{{#each relations}}{{> routes-relation}}

{{/each}}