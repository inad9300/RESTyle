<?php

namespace App\Models;

class {{resourceClass}} extends RestModel {
    protected $table = '{{resourceTable}}';

    protected $fillable = [
        {{#each fillableAttributes}}'{{this}}', {{/each}}
    ];

    protected $hidden = [
        {{#each hiddenAttributes}}'{{this}}', {{/each}}
    ];

    protected static $filterable = [
        {{#each filterableAttributes}}'{{this}}', {{/each}}
    ];

    protected static $sortable = [
        {{#each sortableAttributes}}'{{this}}', {{/each}}
    ];

    protected $casts = [
        {{#each casts}}'{{prop}}' => '{{type}}',{{#unless @last}}{{! remove last new line }}
        {{/unless}}{{/each}}
    ];

    private static $validationRules = [
        {{#each validationRules}}'{{prop}}' => '{{rule}}',{{#unless @last}}
        {{/unless}}{{/each}}
    ];

    {{#each hasOneRelations}}public function {{fn}}() {
        return $this->hasOne('App\Models{{classBack}}', '{{fk}}', '{{id}}');
    }
    {{/each}}
    {{#each hasManyRelations}}public function {{fn}}() {
        return $this->hasMany('App\Models{{classBack}}', '{{fk}}', '{{id}}');
    }
    {{/each}}
    {{#each belongsToRelations}}public function {{fn}}() {
        return $this->belongsTo('App\Models{{classBack}}', '{{fk}}', '{{id}}');
    }
    {{/each}}
    {{#each belongsToManyRelations}}public function {{fn}}() {
        return $this->belongsToMany('App\Models{{classBack}}', '{{middleTable}}', '{{fk}}', '{{id}}'){{#if pivotCols}}->withPivot({{#each pivotCols}}'{{this}}'{{#unless @last}}, {{/unless}}{{/each}}){{/if}};
    }
    {{/each}}
}