// {{resourceName}} - {{subresourceName}}

$app->get('/{{resourceRoute}}/{id}/{{subresourceRoute}}', '{{resourceClass}}Controller@get{{subresourceClassPlural}}');

$app->post('/{{resourceRoute}}/{id}/{{subresourceRoute}}', '{{resourceClass}}Controller@create{{subresourceClassPlural}}');
{{#if isManyToMany}}
$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}', '{{resourceClass}}Controller@unlink{{subresourceClassPlural}}');
$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}/{subId}', '{{resourceClass}}Controller@unlink{{subresourceClass}}');{{else}}
$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}', '{{resourceClass}}Controller@delete{{subresourceClassPlural}}');
$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}/{subId}', '{{resourceClass}}Controller@delete{{subresourceClass}}');{{/if}}