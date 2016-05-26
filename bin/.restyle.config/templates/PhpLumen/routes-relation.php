// {{resourceName}} - {{subresourceName}}

$app->get('/{{resourceRoute}}/{id}/{{subresourceRoute}}', ['middleware' => [{{#unless guestCanReadSubresource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@get{{#if isBelongTo}}{{subresourceClass}}{{else if isOneToOne}}{{subresourceClass}}{{else}}{{subresourceClassPlural}}{{/if}}']);

{{#unless isBelongTo}}$app->post('/{{resourceRoute}}/{id}/{{subresourceRoute}}', ['middleware' => [{{#unless guestCanCreateSubresource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@create{{subresourceClassPlural}}']);
{{#if isManyToMany}}
$app->get('/{{resourceRoute}}/{id}/{{subresourceRoute}}/{subId}', ['middleware' => [{{#unless guestCanDeleteSubresource}}{{#unless guestCanDeleteResource}}'auth', {{/unless}}{{/unless}}], 'uses' => '{{resourceClass}}Controller@getPivotWith{{subresourceClass}}']);
$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}', ['middleware' => [{{#unless guestCanDeleteSubresource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@unlink{{subresourceClassPlural}}']);
$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}/{subId}', ['middleware' => [{{#unless guestCanDeleteSubresource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@unlink{{subresourceClass}}']);{{else}}
$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}', ['middleware' => [{{#unless guestCanDeleteSubresource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@delete{{subresourceClassPlural}}']);
{{#unless isOneToOne}}$app->delete('/{{resourceRoute}}/{id}/{{subresourceRoute}}/{subId}', ['middleware' => [{{#unless guestCanDeleteSubresource}}'auth', {{/unless}}], 'uses' => '{{resourceClass}}Controller@delete{{subresourceClass}}']);{{/unless}}{{/if}}{{/unless}}