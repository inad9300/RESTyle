// {{resourceName}}

$app->get('/{{resourceRoute}}', ['middleware' => [{{#unless guestCanReadResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@getMany']);
$app->get('/{{resourceRoute}}/{id}', ['middleware' => [{{#unless guestCanReadResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@getOne']);

$app->post('/{{resourceRoute}}', ['middleware' => [{{#unless guestCanCreateResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@create']);

$app->put('/{{resourceRoute}}', ['middleware' => [{{#unless guestCanUpdateResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@fullUpdateMany']);
$app->put('/{{resourceRoute}}/{id}', ['middleware' => [{{#unless guestCanUpdateResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@fullUpdateOne']);

$app->patch('/{{resourceRoute}}', ['middleware' => [{{#unless guestCanUpdateResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@partialUpdateMany']);
$app->patch('/{{resourceRoute}}/{id}', ['middleware' => [{{#unless guestCanUpdateResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@partialUpdateOne']);

$app->delete('/{{resourceRoute}}', ['middleware' => [{{#unless guestCanDeleteResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@deleteMany']);
$app->delete('/{{resourceRoute}}/{id}', ['middleware' => [{{#unless guestCanDeleteResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@deleteOne']);
{{#each resourceFiles}}
$app->get('/{{resourceRoute}}/{id}/files/{{name}}', ['middleware' => [{{#unless guestCanReadResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@get{{camelName}}File']);
$app->post('/{{resourceRoute}}/{id}/files/{{name}}', ['middleware' => [{{#unless guestCanReadResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@upload{{camelName}}File']);
$app->put('/{{resourceRoute}}/{id}/files/{{name}}', ['middleware' => [{{#unless guestCanReadResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@upload{{camelName}}File']);
$app->patch('/{{resourceRoute}}/{id}/files/{{name}}', ['middleware' => [{{#unless guestCanReadResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@upload{{camelName}}File']);
$app->delete('/{{resourceRoute}}/{id}/files/{{name}}', ['middleware' => [{{#unless guestCanReadResource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@delete{{camelName}}File']);{{/each}}

{{#each relations}}{{> routes-relation}}

{{/each}}